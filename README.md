#Instalação
- Instalar e configurar [ZooKeeper](https://zookeeper.apache.org/doc/r3.5.8/zookeeperStarted.html)
- Substituir logback.xml na pasta config onde foi instalado o ZooKeeper
- Alterar o local de instalação em "<code style="color : magenta">set</code> ZK" do .bat ou .sh (linux não testado).
- 


Pendências:
- ~~Lógica da barreira não esta funcionando~~ 
- Decidir se implementa o envio único por rodada(?) ou se só fica com os locks. (implementado, mas pode ser alterado)
- ~~Implementar moderador para envio do texto final ou deixar o primeiro grupo que conectou (mesma lógica, mas com uma interface diferente para o moderador)~~
- ~~Leader aparentemente está mudando cada vez que é enviado o texto~~
- ~~Quando um segundo cliente entra e tenta enviar enquanto o lock está ativo, o terminal fecha após o primeiro cliente liberar (não era pra acontecer)~~
- ~~Impedir que sejam enviadas mais de uma palavra (input com espaço)~~
- ~~Ocultar logs do Client~~
