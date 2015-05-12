HIStream
========


Building from source
---------------------
Build requirements: JDK 8, Maven
Run `mvn clean install`


Running HIStream from JAR
-------------------------
`
java -Djava.util.logging.config.file=target\classes\logging.properties -cp target\histream-0.0.1-SNAPSHOT.jar;target\dependency\postgresql-9.4-1201-jdbc41.jar de.sekmi.histream.impl.RunConfiguration
`

