param([string]$OllamaPath, [string]$Model = 'qwen2.5:7b', [switch]$Lan)
$ErrorActionPreference = 'Stop'
if ($Model -match '(?i)cloud') { throw 'Use a downloaded local model, not a cloud model.' }
if (-not $OllamaPath) {
    $installedOllama = Get-Command ollama -ErrorAction SilentlyContinue
    if ($installedOllama) { $OllamaPath = $installedOllama.Source }
    elseif (Test-Path "$env:LOCALAPPDATA\Programs\Ollama\ollama.exe") { $OllamaPath = "$env:LOCALAPPDATA\Programs\Ollama\ollama.exe" }
    elseif (Test-Path "$env:USERPROFILE\.codex\tools\mbmoney-ollama\runtime\ollama.exe") {
        $OllamaPath = "$env:USERPROFILE\.codex\tools\mbmoney-ollama\runtime\ollama.exe"
        $env:OLLAMA_MODELS = "$env:USERPROFILE\.codex\tools\mbmoney-ollama\models"
    } else { throw 'Install Ollama from https://ollama.com/download/windows, then run this script again.' }
}
$env:OLLAMA_HOST = if ($Lan) { '0.0.0.0:11435' } else { '127.0.0.1:11435' }
$env:OLLAMA_NO_CLOUD = 'true'
$localState = Join-Path $PSScriptRoot '..\.ai-local'
New-Item -ItemType Directory -Path $localState -Force | Out-Null
$ready = $false
try { Invoke-RestMethod 'http://127.0.0.1:11435/api/version' -TimeoutSec 2 | Out-Null; $ready = $true }
catch {
    Start-Process -FilePath $OllamaPath -ArgumentList 'serve' -WindowStyle Hidden `
        -RedirectStandardOutput (Join-Path $localState 'ollama-out.log') `
        -RedirectStandardError (Join-Path $localState 'ollama-error.log')
}
for ($attempt = 0; -not $ready -and $attempt -lt 40; $attempt++) {
    Start-Sleep -Milliseconds 500
    try { Invoke-RestMethod 'http://127.0.0.1:11435/api/version' -TimeoutSec 1 | Out-Null; $ready = $true } catch { }
}
if (-not $ready) { throw 'Ollama did not start. Check .ai-local/ollama-error.log.' }
if ($Lan) {
    $listener = Get-NetTCPConnection -LocalPort 11435 -State Listen -ErrorAction SilentlyContinue
    if (-not ($listener | Where-Object { $_.LocalAddress -eq '0.0.0.0' -or $_.LocalAddress -eq '::' })) {
        throw 'An existing MbMoney Ollama server only accepts localhost. Close that instance and rerun with -Lan.'
    }
}
$models = Invoke-RestMethod 'http://127.0.0.1:11435/api/tags' -TimeoutSec 5
if (-not ($models.models | Where-Object { $_.name -eq $Model })) {
    & $OllamaPath pull $Model
    if ($LASTEXITCODE -ne 0) { throw 'Model download failed.' }
}
$adb = Get-Command adb -ErrorAction SilentlyContinue
$adbPath = if ($adb) { $adb.Source } else { "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" }
if (Test-Path $adbPath) {
    foreach ($line in (& $adbPath devices)) {
        if ($line -match '^([^\s]+)\s+device$') {
            & $adbPath -s $Matches[1] reverse tcp:11435 tcp:11435
            if ($LASTEXITCODE -ne 0) { throw 'Could not connect the Android device to Ollama.' }
        }
    }
}
Write-Output "MbMoney free local AI is ready. Model: $Model"
Write-Output 'USB/emulator app address: http://127.0.0.1:11435'
if ($Lan) {
    Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.IPAddress -match '^(192\.168\.|10\.|172\.(1[6-9]|2[0-9]|3[01])\.)' } |
        ForEach-Object { Write-Output "Wi-Fi app address: http://$($_.IPAddress):11435" }
}
