@ECHO OFF
REM ${name}
SET mydir="%~dp0"
java -Djava.util.logging.config.file=logging.properties -jar ${project.build.finalName}.jar %*
