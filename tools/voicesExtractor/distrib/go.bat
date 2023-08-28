
@echo off

set CLASSPATH=delta-common-1.10.jar
set CLASSPATH=%CLASSPATH%;delta-json-1.1.1.jar
set CLASSPATH=%CLASSPATH%;delta-lotro-dat-utils-10.2.jar
set CLASSPATH=%CLASSPATH%;delta-voicesExtractor-1.0.jar
set CLASSPATH=%CLASSPATH%;log4j-1.2.17.jar

set ROOT_DIR=d:\tmp\voix lotro
set LOTRO_CLIENT_PATH=C:\ProgramData\Turbine\The Lord of the Rings Online

java delta.games.lotro.tools.voicesExtractor.MainVoicesExtractor --rootDir="%ROOT_DIR%" --language=fr
