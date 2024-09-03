
@echo off

set CLASSPATH=delta-common-1.13.jar
set CLASSPATH=%CLASSPATH%;delta-json-1.1.1.jar
set CLASSPATH=%CLASSPATH%;delta-lotro-dat-utils-11.4.jar
set CLASSPATH=%CLASSPATH%;delta-datDisplay-1.0.jar
set CLASSPATH=%CLASSPATH%;log4j-1.2.17.jar

set LOTRO_CLIENT_PATH=C:\ProgramData\Turbine\The Lord of the Rings Online

java delta.games.lotro.tools.datDisplay.MainDatDisplay --id=1879048831 --language=en
