#TDMiner - A temporal data mining tool. 
Implementation of Frequent serial and parallel episode mining algorithms with temporal constraints.


##Build
- Clone/Fork from Github
- Build using maven
```
# this way of build project will meet no error
mvn clean compile assembly:single
```
Jar file will be placed in target folder: TDMiner-1.0-jar-with-dependencies.jar 

##Run
```
cd target
java -Xmx2g -jar TDMiner-1.0-jar-with-dependencies.jar 
```
