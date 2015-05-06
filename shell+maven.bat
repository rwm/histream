SET PATH=%PATH%;%USERPROFILE%\Tools\apache-maven-3.2.3\bin

SET JAVA_HOME=C:\Program Files\Java\jdk1.7.0_17
IF EXIST "%JAVA_HOME%" GOTO OK

SET JAVA_HOME=c:\Program Files\Java\jdk1.8.0_25
IF EXIST "%JAVA_HOME%" GOTO OK

SET JAVA_HOME=C:\Program Files\Java\jdk1.6.0_35
IF EXIST "%JAVA_HOME%" GOTO OK

ECHO JAVA_HOME not found!
PAUSE 
EXIT

:OK
REM cd psd
START CMD /K

