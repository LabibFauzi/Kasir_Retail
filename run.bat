@echo off
REM Compile and run Kasir Retail
if not exist "bin" mkdir bin
echo Compiling...

set CP=bin
set CP=%CP%;lib\sqlite-jdbc-3.45.3.0.jar
set CP=%CP%;lib\slf4j-api-2.0.13.jar
set CP=%CP%;lib\slf4j-simple-2.0.13.jar

dir /s /b src\*.java > sources.txt
javac -cp "%CP%" -d bin @sources.txt
del sources.txt

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)

echo Compilation successful!
echo.
echo Running Kasir Retail...
java -cp "%CP%" com.kasir.retail.Main
pause
