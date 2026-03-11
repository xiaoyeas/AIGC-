@echo off
chcp 65001 >nul

set "PROJECT_DIR=%~dp0aigc-frontend"

if not exist "%PROJECT_DIR%" (
    echo [Error] Project directory not found: %PROJECT_DIR%
    pause
    exit /b 1
)

echo.
echo ========================================
echo      AIGC Frontend Startup
echo ========================================
echo.
echo [Info] Frontend Access URLs:
echo      - Local:   http://localhost:3000
echo      - Network: http://127.0.0.1:3000
echo.

rem 打开新窗口并启动前端服务
start "AIGC Frontend" "cmd" /k "cd /d %PROJECT_DIR% && npm run dev"

echo [Success] Frontend server started in new window!
echo [Info] You can close this window now.
echo.
timeout /t 2 >nul
