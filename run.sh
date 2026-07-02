#!/bin/bash
echo "Compiling Kasir Retail..."

CP="."
for jar in lib/*.jar; do
    CP="$CP:$jar"
done

mapfile -t FILES < <(find src -name "*.java")

javac -encoding UTF-8 -cp "$CP" -d bin "${FILES[@]}"
if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

echo "Running Kasir Retail..."
java -cp "bin:$CP" com.kasir.retail.Main
