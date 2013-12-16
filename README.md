ActorTripletsInIMDB
===================

Java implementations of the Misra-Gries algorithm and a modified and generalized FrequentPairsMiner algorithm

Compile from src folder: 
javac -cp . naive/*.java
javac -cp . misragreis/*.java
javac -cp . frequentminer/*.java

Run from source folder:
java naive/ActorTriplet_Naive {inputfile}
java misragreis/ActorTriplet_MisraGries {Size of storage} {inputfile}
java frequentminer/FrequentMiner {itemset-size} {Size of storage} {inputfile}
	e.g. java frequentminer/FrequentMiner 3 2000000 imdb_stream.txt
	
Due to high memory-load, apply -Xmx{}m command: 
	e.g. java -Xmx6144m frequentminer/FrequentMiner 3 20000000 imdb_stream.txt
	
Avaliable input files;
imdb_stream.txt - main dataset, used in the paper.
imdb_stream_small.txt - smaller set, limited to the first 7000 movies.