# installing all the dependencies
echo "Installing all the dependencies..."
cd ..
mvn clean install > mvn_install.log
cd Test
echo "Dependencies installed successfully...\n"

folder="../Dataset/New_Queries"
batch_size=5
all_files=("$folder"/*)
java_program="java -cp ../target/classes MainTest"

for ((i = 0; i < ${#all_files[@]}; i++)); do
    echo "Processing file: ${all_files[$i]}"
    java_output="$($java_program "${all_files[$i]}")"
    read -r value1 value2 value3 <<< "$java_output"
    python3 write_to_csv.py "$value1" "$value2" "$value3"
done