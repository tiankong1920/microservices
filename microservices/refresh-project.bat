@echo off
REM ============================================
REM IntelliJ IDEA 项目刷新脚本
REM 使用方法: 双击运行此脚本
REM ============================================

echo ========================================
echo 清理 IDE 缓存...
echo ========================================

REM 检查并删除 .idea 目录
if exist "E:\101\microservices\.idea" (
    echo 删除 .idea 目录...
    rmdir /s /q "E:\101\microservices\.idea"
)

REM 检查并删除 .gradle 目录
if exist "E:\101\microservices\.gradle" (
    echo 删除 .gradle 目录...
    rmdir /s /q "E:\101\microservices\.gradle"
)

REM 删除所有模块的 build 目录
echo 删除所有 build 目录...
for /d %%d in (E:\101\microservices\*) do (
    if exist "%%d\build" rmdir /s /q "%%d\build"
)
for /d %%d in (E:\101\microservices\*) do (
    for /d %%s in (%%d\*) do (
        if exist "%%s\build" rmdir /s /q "%%s\build"
    )
)

echo.
echo ========================================
echo 缓存清理完成!
echo ========================================
echo.
echo 请执行以下步骤:
echo 1. 关闭 IntelliJ IDEA
echo 2. 重新打开项目: File -^> Open -^> E:\101\microservices
echo 3. 等待 Gradle 索引完成
echo.
pause
