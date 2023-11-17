# installing all the dependencies
echo "Installing all the dependencies..."
mvn clean install > mvn_install.log
echo "Dependencies installed successfully...\n"

# going through all queries and running the program
for file in ./Dataset/Queries/*.mp4
do
   mvn exec:java -Dexec.args="$file ./Dataset/Queries/audios/video1_1.wav 1 5" | grep -v '\[INFO\]'
   echo "\n"
done

echo "All queries executed successfully...\n"