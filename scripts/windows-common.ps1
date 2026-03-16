$ErrorActionPreference = 'Stop'

$script:RootDir = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$script:LogDir = Join-Path $script:RootDir 'logs'
$script:RunDir = Join-Path $script:RootDir 'run'
$script:ComposeFile = Join-Path $script:RootDir 'docker-compose.yml'

New-Item -ItemType Directory -Force -Path $script:LogDir | Out-Null
New-Item -ItemType Directory -Force -Path $script:RunDir | Out-Null

function Write-Log {
    param([string]$Message)
    Write-Host ("[{0}] {1}" -f (Get-Date -Format 'HH:mm:ss'), $Message)
}

function Fail {
    param([string]$Message)
    throw ("ERROR: {0}" -f $Message)
}

function Require-Command {
    param([string]$Name)
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        Fail "缺少命令: $Name"
    }
}

function Invoke-DockerCompose {
    param([Parameter(ValueFromRemainingArguments = $true)] [string[]]$Args)
    & docker compose -f $script:ComposeFile @Args
}

function Get-PidFromFile {
    param([string]$Path)
    if (-not (Test-Path $Path)) {
        return $null
    }

    $content = (Get-Content -Path $Path -Raw).Trim()
    if ([string]::IsNullOrWhiteSpace($content)) {
        return $null
    }

    return [int]$content
}

function Test-ProcessExists {
    param([int]$Pid)
    return [bool](Get-Process -Id $Pid -ErrorAction SilentlyContinue)
}

function Remove-StalePidFile {
    param([string]$Path)
    $pid = Get-PidFromFile -Path $Path
    if ($null -ne $pid -and -not (Test-ProcessExists -Pid $pid)) {
        Remove-Item -Path $Path -Force -ErrorAction SilentlyContinue
    }
}

function Get-ServicePid {
    param([string]$Path)
    Remove-StalePidFile -Path $Path
    $pid = Get-PidFromFile -Path $Path
    if ($null -ne $pid -and (Test-ProcessExists -Pid $pid)) {
        return $pid
    }
    return $null
}

function Test-PortOpen {
    param(
        [string]$Host = '127.0.0.1',
        [int]$Port
    )

    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $async = $client.BeginConnect($Host, $Port, $null, $null)
        if (-not $async.AsyncWaitHandle.WaitOne(1000, $false)) {
            return $false
        }
        $client.EndConnect($async)
        return $true
    } catch {
        return $false
    } finally {
        $client.Dispose()
    }
}

function Wait-Port {
    param(
        [string]$Host,
        [int]$Port,
        [string]$Name,
        [int]$TimeoutSeconds = 60
    )

    for ($i = 0; $i -lt $TimeoutSeconds; $i++) {
        if (Test-PortOpen -Host $Host -Port $Port) {
            Write-Log "$Name 已就绪: $Host`:$Port"
            return
        }
        Start-Sleep -Seconds 1
    }

    Fail "$Name 未在预期时间内就绪: $Host`:$Port"
}

function Wait-ProcessExit {
    param(
        [int]$Pid,
        [int]$TimeoutSeconds = 20
    )

    for ($i = 0; $i -lt $TimeoutSeconds; $i++) {
        if (-not (Test-ProcessExists -Pid $Pid)) {
            return $true
        }
        Start-Sleep -Seconds 1
    }

    return (-not (Test-ProcessExists -Pid $Pid))
}

function Wait-ProcessPort {
    param(
        [int]$Pid,
        [string]$Host,
        [int]$Port,
        [string]$Name,
        [int]$TimeoutSeconds = 120
    )

    for ($i = 0; $i -lt $TimeoutSeconds; $i++) {
        if (Test-PortOpen -Host $Host -Port $Port) {
            Write-Log "$Name 已启动: $Host`:$Port"
            return
        }

        if (-not (Test-ProcessExists -Pid $Pid)) {
            Fail "$Name 进程已退出"
        }

        Start-Sleep -Seconds 1
    }

    Fail "$Name 未在预期时间内启动: $Host`:$Port"
}

function Wait-MySqlContainer {
    param([int]$TimeoutSeconds = 120)

    for ($i = 0; $i -lt $TimeoutSeconds; $i++) {
        try {
            & docker compose -f $script:ComposeFile exec -T mysql mysqladmin ping -uroot -proot --silent *> $null
            if ($LASTEXITCODE -eq 0) {
                Write-Log 'MySQL 已就绪'
                return
            }
        } catch {
        }
        Start-Sleep -Seconds 1
    }

    Fail 'MySQL 容器未在预期时间内完成初始化'
}

function Wait-RedisContainer {
    param([int]$TimeoutSeconds = 60)

    for ($i = 0; $i -lt $TimeoutSeconds; $i++) {
        try {
            $result = & docker compose -f $script:ComposeFile exec -T redis redis-cli ping 2>$null
            if (($result | Out-String).Trim() -eq 'PONG') {
                Write-Log 'Redis 已就绪'
                return
            }
        } catch {
        }
        Start-Sleep -Seconds 1
    }

    Fail 'Redis 容器未在预期时间内完成初始化'
}

function Get-ChildProcessIds {
    param([int]$ParentPid)

    $children = Get-CimInstance Win32_Process -Filter "ParentProcessId = $ParentPid" -ErrorAction SilentlyContinue
    foreach ($child in $children) {
        Get-ChildProcessIds -ParentPid $child.ProcessId
        $child.ProcessId
    }
}

function Stop-ProcessTree {
    param([int]$Pid)

    if (-not (Test-ProcessExists -Pid $Pid)) {
        return
    }

    $descendants = @(Get-ChildProcessIds -ParentPid $Pid | Sort-Object -Descending -Unique)
    foreach ($childPid in $descendants) {
        Stop-Process -Id $childPid -Force -ErrorAction SilentlyContinue
    }
    Stop-Process -Id $Pid -Force -ErrorAction SilentlyContinue
}
