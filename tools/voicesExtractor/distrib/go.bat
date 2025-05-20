
@echo off

set CLASSPATH=delta-voicesExtractor-2.0.jar
set CLASSPATH=%CLASSPATH%;delta-lotro-dat-utils-11.6.jar
set CLASSPATH=%CLASSPATH%;delta-json-1.1.2.jar
set CLASSPATH=%CLASSPATH%;delta-common-1.15.jar
set CLASSPATH=%CLASSPATH%;slf4j-api-2.0.16.jar
set CLASSPATH=%CLASSPATH%;logback-core-1.3.14.jar
set CLASSPATH=%CLASSPATH%;logback-classic-1.3.14.jar

set ROOT_DIR=d:\tmp\voix lotro2
set LOTRO_CLIENT_PATH=C:\ProgramData\Turbine\The Lord of the Rings Online

java delta.games.lotro.tools.voicesExtractor.MainVoicesExtractor --rootDir="%ROOT_DIR%" --language=fr
