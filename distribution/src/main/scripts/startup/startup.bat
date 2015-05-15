@ECHO OFF
REM ${name}
SET mydir="%~dp0"
java -Djava.util.logging.config.file=logging.properties -cp lib\* de.sekmi.histream.impl.RunConfiguration %*
