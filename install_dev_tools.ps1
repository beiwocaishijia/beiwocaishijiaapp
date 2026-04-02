# 被窝健身 - 开发环境自动安装脚本
# 该脚本将自动下载 JDK 17 和 Android Studio 并解压到指定目录

$jdkPath = "D:\jdk17"
$asPath = "D:\andraw studio"
$tempDir = "D:\temp_download_fitness"

# 1. 检查并创建目录
Write-Host "--- 步骤 1: 检查目录 ---" -ForegroundColor Cyan
if (-not (Test-Path $jdkPath)) { New-Item -ItemType Directory -Path $jdkPath -Force }
if (-not (Test-Path $asPath)) { New-Item -ItemType Directory -Path $asPath -Force }
if (-not (Test-Path $tempDir)) { New-Item -ItemType Directory -Path $tempDir -Force }

# 清空目标目录（确保纯净安装）
Write-Host "清空旧文件..."
Remove-Item -Path "$jdkPath\*" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$asPath\*" -Recurse -Force -ErrorAction SilentlyContinue

# 2. 下载 JDK 17
Write-Host "--- 步骤 2: 下载 JDK 17 (Eclipse Temurin) ---" -ForegroundColor Cyan
$jdkUrl = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.10%2B7/OpenJDK17U-jdk_x64_windows_hotspot_17.0.10_7.zip"
$jdkZip = "$tempDir\jdk17.zip"
Invoke-WebRequest -Uri $jdkUrl -OutFile $jdkZip
Write-Host "解压 JDK..."
Expand-Archive -Path $jdkZip -DestinationPath "$tempDir\jdk_extracted" -Force
$jdkInner = Get-ChildItem -Path "$tempDir\jdk_extracted" -Directory | Select-Object -First 1
Move-Item -Path "$($jdkInner.FullName)\*" -Destination $jdkPath -Force

# 3. 下载 Android Studio
Write-Host "--- 步骤 3: 下载 Android Studio (Ladybug) ---" -ForegroundColor Cyan
$asUrl = "https://redirector.gvt1.com/edgedl/android/studio/ide-zips/2024.2.1.11/android-studio-2024.2.1.11-windows.zip"
$asZip = "$tempDir\as.zip"
Invoke-WebRequest -Uri $asUrl -OutFile $asZip
Write-Host "解压 Android Studio (这可能需要几分钟)..."
Expand-Archive -Path $asZip -DestinationPath "$tempDir\as_extracted" -Force
$asInner = Get-ChildItem -Path "$tempDir\as_extracted" -Directory | Select-Object -First 1
Move-Item -Path "$($asInner.FullName)\*" -Destination $asPath -Force

# 4. 配置环境变量 (JAVA_HOME)
Write-Host "--- 步骤 4: 配置环境变量 ---" -ForegroundColor Cyan
[Environment]::SetEnvironmentVariable("JAVA_HOME", $jdkPath, "User")
$oldPath = [Environment]::GetEnvironmentVariable("Path", "User")
if ($oldPath -notlike "*$jdkPath\bin*") {
    $newPath = "$oldPath;$jdkPath\bin"
    [Environment]::SetEnvironmentVariable("Path", $newPath, "User")
    Write-Host "已将 JDK bin 添加到用户 Path 中。"
} else {
    Write-Host "Path 中已存在 JDK 路径。"
}

# 5. 清理临时文件
Write-Host "--- 步骤 5: 清理临时文件 ---" -ForegroundColor Cyan
Remove-Item -Path $tempDir -Recurse -Force

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "安装完成！" -ForegroundColor Green
Write-Host "JDK 目录: $jdkPath"
Write-Host "Android Studio 目录: $asPath"
Write-Host "请重新启动 Trae 或命令行以使环境变量生效。"
Write-Host "========================================`n"
pause
