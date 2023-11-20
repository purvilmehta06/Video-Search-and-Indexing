#Generate Noise Videos
for i in {0..5}
do
  for j in {10..20}
  do
    python3 generate_test_dataset.py 0 $i $j
  done
done

# installing all the dependencies
echo "Installing all the dependencies..."
mvn clean install > mvn_install.log
echo "Dependencies installed successfully...\n"

# going through all queries and running the program
for file in ./Dataset/NoiseQuery/*.mp4
do
   mvn exec:java -Dexec.args="$file ./Dataset/Queries/audios/video1_1.wav 0 10 1" | grep -v '\[INFO\]'
   echo "\n"
done

echo "All queries executed successfully...\n"