@echo off
REM ==================== C++ TRADING ENGINE BUILD SCRIPT (WINDOWS) ====================
REM This script builds the C++ trading engine and integrates it with the Java service

echo 🔨 Building C++ Trading Engine for Trading Service

REM Set GCC path
set "GCC_PATH=C:\msys64\mingw64\bin"
set "PATH=%GCC_PATH%;%PATH%"

REM Check if GCC is available
g++ --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ GCC compiler not found at %GCC_PATH%
    echo Please ensure MinGW-w64 is installed and GCC is in PATH
    exit /b 1
)

echo Using GCC from: %GCC_PATH%

REM Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java is not installed or not in PATH
    exit /b 1
)

REM Get Java home
if "%JAVA_HOME%"=="" (
    for /f "tokens=*" %%i in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr "java.home"') do set JAVA_HOME=%%i
    set JAVA_HOME=%JAVA_HOME:java.home =%
    set JAVA_HOME=%JAVA_HOME: =%
)

REM Try common Java installation paths if JAVA_HOME is still empty
if "%JAVA_HOME%"=="" (
    if exist "C:\Program Files\Java\jdk-21" set JAVA_HOME=C:\Program Files\Java\jdk-21
    if exist "C:\Program Files\Java\jdk-21.0.8" set JAVA_HOME=C:\Program Files\Java\jdk-21.0.8
    if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot" set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot
)

if "%JAVA_HOME%"=="" (
    echo ❌ JAVA_HOME is not set and could not be determined
    echo Please set JAVA_HOME to your JDK installation directory
    exit /b 1
)

echo Using JAVA_HOME: %JAVA_HOME%

REM Set JNI include paths
set JNI_INCLUDE_PATH=%JAVA_HOME%\include
set JNI_INCLUDE_WIN32=%JAVA_HOME%\include\win32

echo Using JNI platform include: %JNI_INCLUDE_WIN32%

REM Create build directory
if not exist "src\main\cpp\build" mkdir "src\main\cpp\build"
cd src\main\cpp\build

echo 📦 Compiling C++ source files...

REM Compile C++ source files with Windows-specific flags
REM Use C++20 for compatibility - build without external dependencies first
g++ -c -std=c++20 -O3 -Wall -Wextra -Wpedantic ^
    -march=native -mtune=native ^
    -flto -fexceptions -frtti ^
    -I"%JNI_INCLUDE_PATH%" ^
    -I"%JNI_INCLUDE_WIN32%" ^
    -I.. ^
    ..\MarketDataStore.cpp ^
    ..\OrderBook.cpp ^
    ..\TradingEngineJNI.cpp ^
    ..\TradingEngineJNIWrapper.cpp

if %errorlevel% neq 0 (
    echo ❌ C++ compilation failed
    exit /b 1
)

echo ✅ C++ compilation successful

echo 🔗 Creating shared library...

REM Create shared library (Windows uses .dll extension)
REM Build without external dependencies for now
g++ -shared -o tradingenginejni.dll ^
    MarketDataStore.o OrderBook.o TradingEngineJNI.o TradingEngineJNIWrapper.o

if %errorlevel% neq 0 (
    echo ❌ Shared library creation failed
    exit /b 1
)

echo ✅ Shared library created: tradingenginejni.dll

REM Copy library to resources directory for Java to find
cd ..\..\..
if not exist "src\main\resources\lib" mkdir "src\main\resources\lib"
copy "src\main\cpp\build\tradingenginejni.dll" "src\main\resources\lib\"

echo ✅ Library copied to resources directory

echo 🎉 C++ Trading Engine build completed successfully!
echo.
echo 📚 Next steps:
echo 1. The shared library is ready for use in your Java application
echo 2. The library is copied to src\main\resources\lib\
echo 3. Run 'mvn clean package' to build the complete trading service
echo 4. The trading engine now uses high-performance C++ order book matching

pause
