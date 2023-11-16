## Video Search and Indexing

### Metadata
* Dataset: https://drive.google.com/drive/u/4/folders/1PQQKvenltAdSlcNU77kvd9gRdw2D13_0
* Video Frame Rate: 30 FPS
* Video Resolution: 352 x 288
* Query Video Length: 20-30 seconds
* Query Video Resolution: 352 x 288

### How to run
* Clone this repository. 
* Create a folder named `Dataset` in the root directory of the project. 
  * Download the dataset from the link given above and extract it in the `Dataset` folder.
* Install Maven and Java
* Run the following command to build the project
```
mvn clean install
```
* Run the following command to run the project
```
mvn exec:java -Dexec.args="<QUERY_VIDEO_FILE.mp4>"
```

### Contributors
* [Purvil Mehta](https://github.com/purvilmehta06)
* [Meet Raval](https://github.com/meet-2719)
* [Shrey Shah](https://github.com/shreyshah97)
* [Nishi Doshi](https://github.com/nishi1612)