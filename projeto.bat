@echo off
set ZK="C:\temp\apache-zookeeper-3.9.3-bin"
set CP_ZK=.;%ZK%\lib\zookeeper-3.9.3.jar;%ZK%\lib\zookeeper-jute-3.9.3.jar;%ZK%\lib\slf4j-api-1.7.30.jar;%ZK%\lib\logback-core-1.2.13.jar;%ZK%\lib\logback-classic-1.2.13.jar;%ZK%\lib\netty-handler-4.1.113.Final.jar

REM Compila todos os arquivos Java
javac -cp %CP_ZK% codigos\*.java

REM Parâmetros do projeto
set bSIZE=10
set qSIZE=10
set WAIT=1000

@echo ***** Projeto Principal *****
@echo Quantidade de palavras no texto = %bSIZE%

REM Executa o Main centralizando o fluxo do projeto
REM Passe os parâmetros necessários: endereco_zookeeper bSIZE qSIZE WAIT palavra
REM Exemplo de uso: projeto.bat localhost 10 10 1000 palavra

java -Dlogback.configurationFile=file:%ZK%\conf\logback.xml -cp %CP_ZK% codigos.Main %1 %2 %3 %4 %5