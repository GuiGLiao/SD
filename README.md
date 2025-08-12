# Formador de textos

## Instalação
- Instalar e configurar [ZooKeeper](https://zookeeper.apache.org/doc/r3.5.8/zookeeperStarted.html)
- Mudar o %LOG% para o diretório do projeto para remover as mensagens de log do terminal.
- Alterar o local de instalação em "**set ZK**" do .bat ou .sh (linux não testado).
  

## Uso
- Executar o zkServer
- Executar zkCli para monitorar os nós criados (opcional)
- Executar projeto.bat de acordo com o tamanho da barreira
- O primeiro cliente a se conectar será o moderador e eleito líder, o restante será eleito cliente
- Controle os terminais de acordo com o envio das palavras pelos clientes e necessidade de envio por parte do moderador.

