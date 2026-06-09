#!/bin/bash
CP="lib/sqlite-jdbc-3.45.3.0.jar:lib/slf4j-api-2.0.13.jar:lib/slf4j-simple-2.0.13.jar"
echo "Compiling..."
find src -name "*.java" > sources.txt
javac -cp "$CP:." -d bin @sources.txt
rm sources.txt
if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi
echo "Compilation successful!"
echo ""
echo "Running Kasir Retail..."
java -cp "bin:$CP" com.kasir.retail.Main
