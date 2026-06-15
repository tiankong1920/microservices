@echo off
cd /d E:\101\microservices

echo ========================================
echo 清理缓存...
echo ========================================

REM 删除 IDE 缓存
if exist ".idea" (
    echo - 删除 .idea
    rmdir /s /q ".idea"
)

REM 删除 Gradle 缓存
if exist ".gradle" (
    echo - 删除 .gradle
    rmdir /s /q ".gradle"
)

echo.
echo ========================================
echo 完成! 请重新打开项目
echo ========================================
pause
