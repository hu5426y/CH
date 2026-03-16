param(
    [string]$DbHost = $(if ($env:DB_HOST) { $env:DB_HOST } else { '127.0.0.1' }),
    [int]$DbPort = $(if ($env:DB_PORT) { [int]$env:DB_PORT } else { 3306 }),
    [string]$DbName = $(if ($env:DB_NAME) { $env:DB_NAME } else { 'study_room_db' }),
    [string]$DbUser = $(if ($env:DB_USER) { $env:DB_USER } else { 'root' }),
    [string]$DbPassword = $(if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { 'root' }),
    [string]$RedisHost = $(if ($env:REDIS_HOST) { $env:REDIS_HOST } else { '127.0.0.1' }),
    [int]$RedisPort = $(if ($env:REDIS_PORT) { [int]$env:REDIS_PORT } else { 6379 }),
    [string]$JwtSecret = $(if ($env:JWT_SECRET) { $env:JWT_SECRET } else { 'ChangeMeToAtLeast32CharsForJwtSecretKey!' })
)

. (Join-Path $PSScriptRoot 'windows-common.ps1')

$backendPidFile = Join-Path $RunDir 'backend-win.pid'
$frontendPidFile = Join-Path $RunDir 'frontend-win.pid'
$backendLogFile = Join-Path $LogDir 'backend-win.log'
$frontendLogFile = Join-Path $LogDir 'frontend-win.log'
$backendDir = Join-Path $RootDir 'backend'
$frontendDir = Join-Path $RootDir 'frontend'

function ConvertTo-PsLiteral {
    param([string]$Value)
    return "'" + $Value.Replace("'", "''") + "'"
}

function Ensure-Docker {
    Require-Command docker
}

function Start-MySql {
    if (Test-PortOpen -Host $DbHost -Port $DbPort) {
        Write-Log "检测到 MySQL 已在运行，复用 $DbHost`:$DbPort"
        return
    }

    Ensure-Docker
    Write-Log 'MySQL 未运行，启动容器 mysql'
    Invoke-DockerCompose up -d mysql | Out-Null
    Wait-Port -Host $DbHost -Port $DbPort -Name 'MySQL 端口' -TimeoutSeconds 60
    Wait-MySqlContainer -TimeoutSeconds 120
}

function Start-Redis {
    if (Test-PortOpen -Host $RedisHost -Port $RedisPort) {
        Write-Log "检测到 Redis 已在运行，复用 $RedisHost`:$RedisPort"
        return
    }

    Ensure-Docker
    Write-Log 'Redis 未运行，启动容器 redis'
    Invoke-DockerCompose up -d redis | Out-Null
    Wait-Port -Host $RedisHost -Port $RedisPort -Name 'Redis 端口' -TimeoutSeconds 30
    Wait-RedisContainer -TimeoutSeconds 60
}

function Validate-MySqlAccess {
    $mysql = Get-Command mysql -ErrorAction SilentlyContinue
    if (-not $mysql) {
        Write-Log '未检测到 mysql 客户端，跳过 MySQL 鉴权预检查'
        return
    }

    $env:MYSQL_PWD = $DbPassword
    try {
        & mysql -h $DbHost -P $DbPort -u $DbUser -D $DbName -e 'SELECT 1' *> $null
        if ($LASTEXITCODE -ne 0) {
            Fail "无法使用当前配置连接 MySQL $DbHost`:$DbPort/$DbName，请设置 DB_USER/DB_PASSWORD/DB_NAME，或停止本机 MySQL 后重试"
        }
    } finally {
        Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
    }
}

function Ensure-FrontendDependencies {
    $nodeModules = Join-Path $frontendDir 'node_modules'
    if (Test-Path $nodeModules) {
        return
    }

    Write-Log '安装前端依赖'
    Push-Location $frontendDir
    try {
        & npm install
        if ($LASTEXITCODE -ne 0) {
            Fail 'npm install 失败'
        }
    } finally {
        Pop-Location
    }
}

function Start-Backend {
    $existing = Get-ServicePid -Path $backendPidFile
    if ($existing) {
        Write-Log "后端已在运行，PID=$existing"
        return
    }

    if (Test-PortOpen -Host '127.0.0.1' -Port 8080) {
        Fail '8080 端口已被占用，无法自动启动后端'
    }

    Write-Log '启动后端服务'

    $escapedBackendLog = $backendLogFile.Replace("'", "''")
    $command = '$env:DB_HOST=' + (ConvertTo-PsLiteral -Value $DbHost) + ';' +
        '$env:DB_PORT=' + (ConvertTo-PsLiteral -Value ([string]$DbPort)) + ';' +
        '$env:DB_NAME=' + (ConvertTo-PsLiteral -Value $DbName) + ';' +
        '$env:DB_USER=' + (ConvertTo-PsLiteral -Value $DbUser) + ';' +
        '$env:DB_PASSWORD=' + (ConvertTo-PsLiteral -Value $DbPassword) + ';' +
        '$env:REDIS_HOST=' + (ConvertTo-PsLiteral -Value $RedisHost) + ';' +
        '$env:REDIS_PORT=' + (ConvertTo-PsLiteral -Value ([string]$RedisPort)) + ';' +
        '$env:JWT_SECRET=' + (ConvertTo-PsLiteral -Value $JwtSecret) + ';' +
        '& mvn spring-boot:run *>> ''' + $escapedBackendLog + ''''

    $process = Start-Process -FilePath 'powershell.exe' `
        -ArgumentList @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-Command', $command) `
        -WorkingDirectory $backendDir `
        -PassThru `
        -WindowStyle Hidden

    Set-Content -Path $backendPidFile -Value $process.Id
    try {
        Wait-ProcessPort -Pid $process.Id -Host '127.0.0.1' -Port 8080 -Name '后端' -TimeoutSeconds 180
    } catch {
        if (Test-Path $backendLogFile) {
            Get-Content -Path $backendLogFile -Tail 40 | Write-Host
        }
        throw
    }
}

function Start-Frontend {
    $existing = Get-ServicePid -Path $frontendPidFile
    if ($existing) {
        Write-Log "前端已在运行，PID=$existing"
        return
    }

    if (Test-PortOpen -Host '127.0.0.1' -Port 5173) {
        Fail '5173 端口已被占用，无法自动启动前端'
    }

    Ensure-FrontendDependencies

    Write-Log '启动前端服务'
    $escapedFrontendLog = $frontendLogFile.Replace("'", "''")
    $process = Start-Process -FilePath 'powershell.exe' `
        -ArgumentList @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-Command', '& npm run dev -- --host 0.0.0.0 *>> ''' + $escapedFrontendLog + '''') `
        -WorkingDirectory $frontendDir `
        -PassThru `
        -WindowStyle Hidden

    Set-Content -Path $frontendPidFile -Value $process.Id
    try {
        Wait-ProcessPort -Pid $process.Id -Host '127.0.0.1' -Port 5173 -Name '前端' -TimeoutSeconds 120
    } catch {
        if (Test-Path $frontendLogFile) {
            Get-Content -Path $frontendLogFile -Tail 40 | Write-Host
        }
        throw
    }
}

Require-Command mvn
Require-Command npm

Start-MySql
Start-Redis
Validate-MySqlAccess
Start-Backend
Start-Frontend

Write-Log '全部服务已启动'
Write-Host ''
Write-Host '前端: http://127.0.0.1:5173'
Write-Host '后端: http://127.0.0.1:8080'
Write-Host "后端日志: $backendLogFile"
Write-Host "前端日志: $frontendLogFile"
