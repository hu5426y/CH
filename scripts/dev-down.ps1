param()

. (Join-Path $PSScriptRoot 'windows-common.ps1')

$backendPidFile = Join-Path $RunDir 'backend-win.pid'
$frontendPidFile = Join-Path $RunDir 'frontend-win.pid'

function Stop-ServiceProcess {
    param(
        [string]$Name,
        [string]$PidFile
    )

    $pid = Get-ServicePid -Path $PidFile
    if (-not $pid) {
        Remove-Item -Path $PidFile -Force -ErrorAction SilentlyContinue
        Write-Log "$Name 未运行"
        return
    }

    Write-Log "停止 $Name，PID=$pid"
    Stop-ProcessTree -Pid $pid

    if (-not (Wait-ProcessExit -Pid $pid -TimeoutSeconds 20)) {
        Fail "$Name 进程未能正常退出"
    }

    Remove-Item -Path $PidFile -Force -ErrorAction SilentlyContinue
}

function Stop-Infrastructure {
    $docker = Get-Command docker -ErrorAction SilentlyContinue
    if (-not $docker) {
        Write-Log '未检测到 Docker，跳过容器停止'
        return
    }

    Write-Log '停止 MySQL 和 Redis 容器'
    try {
        Invoke-DockerCompose stop mysql redis *> $null
    } catch {
    }
}

Stop-ServiceProcess -Name '前端' -PidFile $frontendPidFile
Stop-ServiceProcess -Name '后端' -PidFile $backendPidFile
Stop-Infrastructure

Write-Log '全部服务已停止'
