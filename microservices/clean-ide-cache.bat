@echo off

REM 清理IDE缓存并重新导入项目

echo 清理IDE缓存...

REM 删除IDE缓存目录
if exist ".idea" rd /s /q ".idea"
if exist "**/.idea" for /d %%i in (**/.idea) do rd /s /q "%%i"

REM 删除构建产物
if exist "build" rd /s /q "build"
if exist "**/build" for /d %%i in (**/build) do rd /s /q "%%i"

REM 删除Gradle缓存
if exist ".gradle" rd /s /q ".gradle"

REM 清理Gradle wrapper缓存
if exist "gradle/wrapper" rd /s /q "gradle/wrapper"

REM 重新下载Gradle wrapper
call gradlew.bat wrapper --gradle-version=9.4.0

echo IDE缓存清理完成，请重新导入项目到IDE中。
echo 重新导入步骤：
echo 1. 在IDE中选择 File -> Open...
echo 2. 选择 microservices 目录
echo 3. 等待IDE完成项目导入和同步

echo.
echo 按任意键退出...
pause > nul
