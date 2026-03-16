param(
    [string]$DbHost = $(if ($env:DB_HOST) { $env:DB_HOST } else { '127.0.0.1' }),
    [int]$DbPort = $(if ($env:DB_PORT) { [int]$env:DB_PORT } else { 3306 }),
    [string]$DbName = $(if ($env:DB_NAME) { $env:DB_NAME } else { 'study_room_db' }),
    [string]$DbUser = $(if ($env:DB_USER) { $env:DB_USER } else { 'root' }),
    [string]$DbPassword = $(if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { 'root' }),
    [string]$RedisHost = $(if ($env:REDIS_HOST) { $env:REDIS_HOST } else { '127.0.0.1' }),
    [int]$RedisPort = $(if ($env:REDIS_PORT) { [int]$env:REDIS_PORT } else { 6379 }),
    [switch]$SkipNpmInstall
)

. (Join-Path $PSScriptRoot 'windows-common.ps1')

$schemaPath = Join-Path $RootDir 'backend\src\main\resources\db\schema.sql'
$seedPath = Join-Path $RootDir 'backend\src\main\resources\db\seed.sql'
$frontendDir = Join-Path $RootDir 'frontend'

function Test-MySqlAccess {
    $env:MYSQL_PWD = $DbPassword
    try {
        & mysql -h $DbHost -P $DbPort -u $DbUser -e 'SELECT 1' *> $null
        return ($LASTEXITCODE -eq 0)
    } finally {
        Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
    }
}

function Initialize-Database {
    Write-Log "初始化数据库 $DbName"
    $env:MYSQL_PWD = $DbPassword
    try {
        & mysql -h $DbHost -P $DbPort -u $DbUser -e "CREATE DATABASE IF NOT EXISTS $DbName CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 
        if ($LASTEXITCODE -ne 0) {
            Fail '创建数据库失败'
        }

        Get-Content -Path $schemaPath -Raw | & mysql -h $DbHost -P $DbPort -u $DbUser $DbName
        if ($LASTEXITCODE -ne 0) {
            Fail '执行 schema.sql 失败'
        }

        Get-Content -Path $seedPath -Raw | & mysql -h $DbHost -P $DbPort -u $DbUser $DbName
        if ($LASTEXITCODE -ne 0) {
            Fail '执行 seed.sql 失败'
        }
    } finally {
        Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
    }
}

function Install-FrontendDependencies {
    if ($SkipNpmInstall) {
        Write-Log '跳过 npm install'
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

Require-Command java
Require-Command mvn
Require-Command node
Require-Command npm
Require-Command mysql

if (-not (Test-PortOpen -Host $DbHost -Port $DbPort)) {
    Fail "MySQL 未监听在 $DbHost`:$DbPort，请先启动 MySQL"
}

if (-not (Test-PortOpen -Host $RedisHost -Port $RedisPort)) {
    Write-Log "警告: Redis 未监听在 $RedisHost`:$RedisPort，后续启动前请先启动 Redis 或 Memurai"
} else {
    Write-Log "检测到 Redis: $RedisHost`:$RedisPort"
}

if (-not (Test-MySqlAccess)) {
    Fail "无法使用当前配置连接 MySQL $DbHost`:$DbPort/$DbName，请检查 DB_USER/DB_PASSWORD"
}

Initialize-Database
Install-FrontendDependencies

Write-Log 'Windows 初始化完成'
Write-Host ''
Write-Host '下一步:'
Write-Host 'powershell -ExecutionPolicy Bypass -File .\scripts\dev-up.ps1'
