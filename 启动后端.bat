@echo off
chcp 65001 >nul
@echo %cd%
call mvn -v
echo ========================================
echo     AIGC Backend Startup
echo ========================================
echo.
echo Starting backend server...
echo.

cd /d "%~dp0aigc-backend"

echo [Starting] Backend server (please wait...)
echo.
echo NOTE: Make sure Maven and Java are installed!
echo.

call mvn spring-boot:run

pause
