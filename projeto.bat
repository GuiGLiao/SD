@echo off
set ZK="C:\temp\apache-zookeeper-3.9.3-bin"
set CP_ZK=.;%ZK%\lib\zookeeper-3.9.3.jar;%ZK%\lib\zookeeper-jute-3.9.3.jar;%ZK%\lib\slf4j-api-1.7.30.jar;%ZK%\lib\logback-core-1.2.13.jar;%ZK%\lib\logback-classic-1.2.13.jar;%ZK%\lib\netty-handler-4.1.113.Final.jar

REM Compila todos os arquivos Java
javac -cp %CP_ZK% codigos\*.java


set ZK_ADDR=localhost
set BARRIER_SIZE=3
set MIN_WORDS=10


@echo ***** Formador de Textos *****
@echo Tamanho da barreira = %BARRIER_SIZE%
@echo Quantidade de palavras no texto = %MIN_WORDS%
@echo.


java -Dlogback.configurationFile=file:%ZK%\conf\logback.xml -cp %CP_ZK% codigos.Main %ZK_ADDR% %BARRIER_SIZE% %MIN_WORDS%