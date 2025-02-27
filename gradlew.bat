@ECHO OFF
SET JAVA_EXE=java
IF NOT "%JAVA_HOME%" == "" SET JAVA_EXE=%JAVA_HOME%in\java
"%JAVA_EXE%" -jar "%~dp0\gradle\wrapper\gradle-wrapper.jar" %*
