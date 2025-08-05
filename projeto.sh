export ZK=~C:/temp/apache-zookeeper-3.9.3-bin
echo "ZK=$ZK"
export CP_ZK=.:$ZK'/lib/zookeeper-3.9.3.jar':$ZK'/lib/zookeeper-jute-3.9.3.jar':$ZK'/lib/slf4j-api-1.7.30.jar':$ZK'/lib/logback-core-1.2.13.jar':$ZK'/lib/logback-classic-1.2.13.jar':$ZK'/lib/netty-handler-4.1.113.Final.jar'
echo "CP=$CP_ZK"
javac -cp %CP_ZK% codigos\*.java

export bSIZE=10
export qSIZE=10
export WAIT=1000

echo "***** Projeto Principal *****"
echo "Quantidade de palavras no texto =  %bSIZE"

java -Dlogback.configurationFile=file:%ZK%\conf\logback.xml -cp %CP_ZK% codigos.Main %1 %2 %3 %4 %5