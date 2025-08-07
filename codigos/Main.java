package codigos;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;


public class Main {
    public static void main(String[] args) throws Exception {
        String zkAddress = args.length > 0 ? args[0] : "localhost";
        int barrierSize = args.length > 1 ? Integer.parseInt(args[1]) : 3;
        int minWords = args.length > 2 ? Integer.parseInt(args[2]) : 10;

        Barrier barrier = new Barrier(zkAddress, "/barrier", barrierSize);
        Queue queue = new Queue(zkAddress, "/queue");
        Leader leader = new Leader(zkAddress, "/election", "/leader", (int)(Math.random()*1000000));


        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\nDigite:");
            System.out.println("0 - Enviar palavra");
            System.out.println("1 - Ver palavras enviadas");
            System.out.println("2 - Enviar texto final");
            System.out.println("3 - Palavras restantes para atingir o mínimo");
            System.out.println("4 - Limpar fila");
            System.out.println("9 - Sair");
            System.out.print("Opção: ");
            int op = sc.nextInt();
            sc.nextLine(); // consume newline

            int restantes = minWords - queue.size();
            if (restantes < 0) restantes = 0; // Não pode ser negativo


            switch (op) {
                case 0:
                    // Lock para garantir exclusão mútua
                    Lock lock = new Lock(zkAddress, "/lock", 1000);
                    if (lock.lock()) {
                        // Verifica se já enviou uma palavra (pode ser por variável local ou lógica de negócio)
                        // Exemplo simples: verifica se já existe uma palavra igual no queue (ajuste conforme sua regra)
                        List<String> palavrasEnviadas = queue.getAllStrings();
                        boolean jaEnviou = false;
                        for (String p : palavrasEnviadas) {
                            if (p.equalsIgnoreCase("palavra do grupo")) { // Troque por identificação do grupo se necessário
                                jaEnviou = true;
                                break;
                            }
                        }
                        if (jaEnviou) {
                            System.out.println("Warning: Você já enviou uma palavra! Só é permitido uma palavra por rodada.");
                        } else {
                            System.out.print("Digite a palavra: ");
                            String palavra = sc.nextLine();
                            if (palavra.trim().contains(" ")) {
                                System.out.println("Warning: Não é permitido enviar palavras com espaço!");
                            } else if (palavra.trim().isEmpty()) {
                                System.out.println("Warning: Palavra vazia não é permitida!");
                            } else {
                                queue.produce(palavra.trim());
                                System.out.println("Palavra enviada!");
                            }
                        }
                        lock.unlock();
                    } else {
                        System.out.println("Não foi possível adquirir o lock.");
                    }
                    break;
                case 1:
                    List<String> palavras = queue.getAllStrings();
                    System.out.println("Palavras enviadas: " + palavras);
                    break;
                case 2:
                    boolean isLeader = leader.elect();
                    if (isLeader) {
                        if (restantes == 0) {
                            List<String> texto = queue.consumeAllStrings();
                            System.out.println("Texto final: " + String.join(" ", texto));
                        } else {
                            System.out.println("Ainda não atingido o mínimo de palavras.");
                        }
                        // Aqui você pode adicionar lógica para enviar/salvar o texto
                    } else {
                        System.out.println("Warning: Apenas o Líder pode enviar o texto final!");
                    }
                    break;
                case 3:
                    if (restantes == 0) {
                        restantes = 0; // Não pode ser negativo
                        System.out.println("Já atingido o mínimo de palavras.");
                        break;
                    }
                    System.out.println("Palavras restantes para atingir o mínimo: " + restantes);
                    break;
                case 4:
                    //Limpa a fila
                    queue.clear();
                    System.out.println("Fila limpa.");
                    break;
                case 9:
                    System.out.println("Saindo...");
                    return;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }
}