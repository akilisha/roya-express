@echo off
REM AI Showcase Runner Script (Windows)
REM Usage: run-showcase.bat [your-openai-api-key]

setlocal

if "%~1"=="" (
    if "%OPENAI_API_KEY%"=="" (
        echo ❌ Error: OpenAI API key required
        echo.
        echo Usage:
        echo   run-showcase.bat YOUR_API_KEY
        echo   OR
        echo   set OPENAI_API_KEY=your-key
        echo   run-showcase.bat
        echo.
        exit /b 1
    )
    set API_KEY=%OPENAI_API_KEY%
) else (
    set API_KEY=%~1
)

echo 🚀 Starting AI Showcase...
echo 📝 API Key: %API_KEY:~0,8%...
echo.

gradlew.bat :roya-examples:run ^
  -Dai.openai.apiKey="%API_KEY%" ^
  -Dexec.mainClass="com.akilisha.oss.roya.examples.AIShowcase" ^
  --no-daemon

endlocal

