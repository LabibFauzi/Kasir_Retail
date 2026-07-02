@echo off
chcp 65001 >nul
echo Kompilasi Kasir Retail...
setlocal enabledelayedexpansion

if exist sources.txt del sources.txt

set CP=.
for %%j in (lib\*.jar) do set CP=!CP!;%%j

set FILES=
for /r src %%f in (*.java) do set FILES=!FILES! "%%f"

javac -encoding UTF-8 -cp "%CP%" -d bin !FILES!
if %ERRORLEVEL% neq 0 (
    echo Kompilasi gagal!
    pause
    exit /b %ERRORLEVEL%
)

echo Menjalankan Kasir Retail...
java -cp "bin;%CP%" com.kasir.retail.Main
pause
