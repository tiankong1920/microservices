@echo off
REM ==============================================
REM  Web Frontend Docker Test Runner
REM  在 Docker 容器中运行前端 Vitest 测试
REM ==============================================
setlocal enabledelayedexpansion

cd /d "%~dp0"

set "IMAGE_NAME=web-frontend-test"
set "VERBOSE=0"

:parse_args
if "%~1"=="" goto :run
if /i "%~1"=="-v" (
  set "VERBOSE=1"
  shift
  goto :parse_args
)
if /i "%~1"=="--verbose" (
  set "VERBOSE=1"
  shift
  goto :parse_args
)
if /i "%~1"=="--coverage" (
  set "COVERAGE=1"
  shift
  goto :parse_args
)
if /i "%~1"=="--rebuild" (
  set "REBUILD=1"
  shift
  goto :parse_args
)
if /i "%~1"=="--no-cache" (
  set "NO_CACHE=1"
  shift
  goto :parse_args
)
if /i "%~1"=="--file" (
  set "TEST_FILE=%~2"
  shift
  shift
  goto :parse_args
)
shift
goto :parse_args

:run
echo ============================================
echo  Web Frontend Docker Test Runner
echo  Image: %IMAGE_NAME%
echo ============================================

REM Step 1: 检查镜像是否存在
docker images --format "{{.Repository}}:{{.Tag}}" | findstr /R "^%IMAGE_NAME%:latest$" >nul 2>&1
if errorlevel 1 set "REBUILD=1"

REM Step 2: 构建（如果需要）
if defined REBUILD (
  echo.
  echo [1/3] Building Docker image...
  set "BUILD_ARGS="
  if defined NO_CACHE set "BUILD_ARGS=--no-cache"
  docker build %BUILD_ARGS% -f Dockerfile.test -t %IMAGE_NAME%:latest .
  if errorlevel 1 (
    echo [ERROR] Docker build failed
    exit /b 1
  )
) else (
  echo.
  echo [1/3] Using cached image: %IMAGE_NAME%:latest
)

REM Step 3: 构造 vitest 命令
set "VITEST_CMD=npx vitest run"
if "%VERBOSE%"=="1" set "VITEST_CMD=%VITEST_CMD% --reporter=verbose"
if defined COVERAGE set "VITEST_CMD=%VITEST_CMD% --coverage"
if defined TEST_FILE set "VITEST_CMD=%VITEST_CMD% %TEST_FILE%"

REM Step 4: 在容器中运行测试
echo.
echo [2/3] Running tests in container...
echo        Command: %VITEST_CMD%
echo.
docker run --rm ^
  -v "%cd%:/app" ^
  -v /app/node_modules ^
  -e CI=true ^
  -e NODE_ENV=test ^
  %IMAGE_NAME%:latest ^
  %VITEST_CMD%
set "TEST_EXIT=%ERRORLEVEL%"

REM Step 5: 报告
echo.
echo ============================================
if %TEST_EXIT% EQU 0 (
  echo  [3/3] PASSED - All tests passed in Docker
) else (
  echo  [3/3] FAILED - Tests failed with exit code %TEST_EXIT%
)
echo ============================================

endlocal & exit /b %TEST_EXIT%
