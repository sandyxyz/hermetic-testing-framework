@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-22"
set "PATH=%JAVA_HOME%\bin;%PATH%"
"%~dp0.tools\apache-maven-3.9.14\bin\mvn.cmd" %*

