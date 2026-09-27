@echo off
setlocal

set "GRADLE_VERSION=8.14.3"
set "GRADLE_HOME=%USERPROFILE%\.gradle\wrapper\dists\gradle-%GRADLE_VERSION%"
set "GRADLE_ZIP=%TEMP%\gradle-%GRADLE_VERSION%-bin.zip"
set "GRADLE_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip"

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
  echo Gradle %GRADLE_VERSION% is not installed yet.
  echo Downloading Gradle...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Invoke-WebRequest -Uri '%GRADLE_URL%' -OutFile '%GRADLE_ZIP%'"
  if errorlevel 1 (
    echo Failed to download Gradle.
    exit /b 1
  )

  echo Extracting Gradle...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; New-Item -ItemType Directory -Force -Path '%GRADLE_HOME%' | Out-Null; Expand-Archive -Path '%GRADLE_ZIP%' -DestinationPath '%TEMP%\gradle-extract' -Force; Move-Item -Path '%TEMP%\gradle-extract\gradle-%GRADLE_VERSION%\*' -Destination '%GRADLE_HOME%' -Force; Remove-Item '%TEMP%\gradle-extract' -Recurse -Force; Remove-Item '%GRADLE_ZIP%' -Force"
  if errorlevel 1 (
    echo Failed to extract Gradle.
    exit /b 1
  )
)

call "%GRADLE_HOME%\bin\gradle.bat" %*
endlocal
