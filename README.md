HIStream
========


Building from source
---------------------
Build requirements: JDK 8, Maven

Run `mvn clean install` to download dependencies and build all sub-modules.
The binary distribution zip files can then be found in distribution/target.


Running HIStream
================

Import Data into the i2b2 Data Warehouse
----------------------------------------

Easy way: 
1. copy/unzip the binary distribution ..-i2b2.zip (or ..-full.zip) from distribution/target.
2. adjust the database settings in histream.xml
3. To import data into i2b2 run `startup examples/dwh-eav.xml` or `startup examples/dwh-flat.txt`

Import SKOS ontology into i2b2
------------------------------
1. copy/unzip the binary distribution ..-full.zip from distribution/target. 
The plugins histream-skos and histream-i2b2 are needed.
2. adjust examples/ontology-import.properties
3. adjust paths to RDF files in example/skos-ontology.properties
4. Run `java -cp "lib\*" de.sekmi.histream.i2b2.ont.Import examples/skos-ontology.properties examples/i2b2-ont-import.properties`
