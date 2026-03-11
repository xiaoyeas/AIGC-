@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

title Stop AIGC Frontend

echo.
echo ========================================
echo      Stop AIGC Frontend Server
echo ========================================
echo.

echo [Info] Searching for Node.js processes...
echo.

tasklist /fi "imagename eq node.exe" 2>nul | find /i "node.exe" >nul

if %errorlevel% equ 0 (
    echo [Info] Found running Node.js processes
    echo.
    echo [Info] Process list:
    tasklist /fi "imagename eq node.exe" /fo table
    echo.
    echo [Info] Stopping frontend server...
    taskkill /F /IM node.exe
    
    if %errorlevel% equ 0 (
        echo [Success] Frontend server stopped successfully!
    ) else (
        echo [Warning] Some processes may not have been stopped
    )
) else (
    echo [Info] No Node.js processes found running
)

echo.
echo Done.
timeout /t 2 >nul
