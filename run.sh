#!/bin/bash
echo "Compiling Kasir Retail..."

CP="."
for jar in lib/*.jar; do
    CP="$CP:$jar"
done


find src -name "*.java" > sources.txt

javac -encoding UTF-8 -cp "$CP" -d bin @sources.txt
if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

rm sources.txt

echo "Running Kasir Retail..."
java -cp "bin:$CP" com.kasir.retail.Main
