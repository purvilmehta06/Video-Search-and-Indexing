# installing all the dependencies
echo "Installing all the dependencies..."
mvn clean install > mvn_install.log
echo "Dependencies installed successfully...\n"

echo "Starting python server..."
python3 video_player.py &
echo "Python server started successfully...\n"

echo "Starting the application..."
mvn exec:java | grep -v '\[INFO\]'