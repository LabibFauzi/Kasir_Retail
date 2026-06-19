@echo off
chcp 65001 >nul
echo Kompilasi Kasir Retail...
setlocal enabledelayedexpansion

set CP=.
for %%j in (lib\*.jar) do set CP=!CP!;%%j

dir /s /B src\*.java > sources.txt

javac -encoding UTF-8 -cp "%CP%" -d bin @sources.txt
if %ERRORLEVEL% neq 0 (
    echo Kompilasi gagal!
    pause
    exit /b %ERRORLEVEL%
)

del sources.txt

echo Menjalankan Kasir Retail...
java -cp "bin;%CP%" com.kasir.retail.Main
pause
