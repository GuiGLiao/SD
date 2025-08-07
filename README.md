#Formador de textos

##Instalação
- Instalar e configurar [ZooKeeper](https://zookeeper.apache.org/doc/r3.5.8/zookeeperStarted.html)
- Substituir logback.xml na pasta config onde foi instalado o ZooKeeper
- Alterar o local de instalação em "**set ZK**" do .bat ou .sh (linux não testado).

##Uso
- Executar o zkServer
- Executar zkCli para monitorar os nós criados (opcional)
- Executar projeto.bat de acordo com o tamanho da barreira
- O primeiro cliente a se conectar será o moderador e eleito líder, o restante será eleito cliente
- Controle os terminais de acordo com o envio das palavras pelos clientes e necessidade de envio por parte do moderador.


Pendências:
- ~~Lógica da barreira não esta funcionando~~ 
- Decidir se implementa o envio único por rodada(?) ou se só fica com os locks. (implementado, mas pode ser alterado)
- ~~Implementar moderador para envio do texto final ou deixar o primeiro grupo que conectou (mesma lógica, mas com uma interface diferente para o moderador)~~
- ~~Leader aparentemente está mudando cada vez que é enviado o texto~~
- ~~Quando um segundo cliente entra e tenta enviar enquanto o lock está ativo, o terminal fecha após o primeiro cliente liberar (não era pra acontecer)~~
- ~~Impedir que sejam enviadas mais de uma palavra (input com espaço)~~
- ~~Ocultar logs do Client~~
