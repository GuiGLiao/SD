package codigos;

import java.util.List;
import java.util.Scanner;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

public class Main {
    // Define um caminho constante para o nó de submissões
    private static final String SUBMISSIONS_PATH = "/submissions";

    public static void main(String[] args) throws Exception {
        String zkAddress = args.length > 0 ? args[0] : "localhost";
        int barrierSize = args.length > 1 ? Integer.parseInt(args[1]) : 3;
        int minWords = args.length > 2 ? Integer.parseInt(args[2]) : 10;

        // Todos os processos entram na barreira e aguardam o quórum mínimo
        System.out.println("Procurando grupo... Aguardando " + barrierSize + " participantes na barreira.");
        Barrier barrier = new Barrier(zkAddress, "/barrier", barrierSize);
        barrier.enter();

        // Após a barreira ser superada, cada processo entra na eleição para definir seu papel
        Leader leader = new Leader(zkAddress, "/election", "/leader", (int)(Math.random()*1000000));
        boolean isModerator = leader.elect();
        
        Queue queue = new Queue(zkAddress, "/queue");
        Scanner sc = new Scanner(System.in);

        if (isModerator) {
            System.out.println("\n--- Eleição concluída. Você é o MODERADOR ---");
            Stat submissionStat = leader.zk.exists(SUBMISSIONS_PATH, false);
            if (submissionStat == null) {
                leader.zk.create(SUBMISSIONS_PATH, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

            // --- INÍCIO DA FUNÇÃO AUTOMATIZADA EM SEGUNDO PLANO ---
            Thread submissionCleanerThread = new Thread(() -> {
                while (true) {
                    try {
                        List<String> barrierNodes = leader.zk.getChildren("/barrier", false);
                        int barrierCurrentSize = barrierNodes.size();

                        List<String> submissionNodes = leader.zk.getChildren(SUBMISSIONS_PATH, false);
                        int submissionsCurrentSize = submissionNodes.size();

                        // Condição: Limpa se todos os clientes (total na barreira - 1) já enviaram uma palavra
                        if (barrierCurrentSize > 1 && submissionsCurrentSize > 0 && submissionsCurrentSize >= barrierCurrentSize - 1) {
                            //System.out.println("\n[AUTO] Todos os clientes enviaram. Limpando o registro de submissões para permitir novos envios...");
                            
                            for (String submission : submissionNodes) {
                                leader.zk.delete(SUBMISSIONS_PATH + "/" + submission, -1);
                            }
                            //System.out.println("[AUTO] Registro de submissões limpo. Clientes podem enviar novamente.");
                        }

                        // Pausa a verificação por 5 segundos
                        Thread.sleep(5000);

                    } catch (KeeperException | InterruptedException e) {
                        System.out.println("[AUTO] Thread de limpeza interrompida.");
                        break;
                    }
                }
            });
            
            submissionCleanerThread.setDaemon(true);
            submissionCleanerThread.start();

        } else {
            System.out.println("\n--- Eleição concluída. Você é um CLIENTE ---");
        }

        int restantes = minWords - queue.size();

        // 3. Loop principal com menus diferentes baseados no papel (Moderador manual e Cliente)
        while (true) {
            if (isModerator) {
                // --- MENU MANUAL DO MODERADOR ---
                System.out.println("\nOpções de Moderador:");
                System.out.println("1 - Verificar palavras restantes");
                System.out.println("2 - Verificar se o texto pode ser enviado");
                System.out.println("3 - Enviar texto final e reiniciar rodada");
                System.out.println("9 - Sair");
                System.out.print("Sua escolha: ");
                int op = sc.nextInt();
                sc.nextLine();

                switch (op) {
                    case 1:
                        if (restantes < 0) restantes = 0;
                        System.out.println("Palavras restantes para atingir o mínimo: " + restantes);
                        break;
                    case 2:
                        if (queue.size() >= minWords) {
                            System.out.println("SIM. O número mínimo de palavras foi atingido. O texto pode ser enviado.");
                        } else {
                            System.out.println("NÃO. Ainda faltam palavras.");
                        }
                        break;
                    case 3:
                        if (queue.size() >= minWords) {
                            List<String> texto = queue.consumeAllStrings();
                            System.out.println("Texto final enviado: " + String.join(" ", texto));

                            // Limpeza final de submissões
                            System.out.println("Limpando o registro de submissões...");
                            List<String> submissions = leader.zk.getChildren(SUBMISSIONS_PATH, false);
                            for (String submission : submissions) {
                                leader.zk.delete(SUBMISSIONS_PATH + "/" + submission, -1);
                            }
                            
                            System.out.println("Iniciando reinicialização da barreira...");
                            barrier.leave();
                            System.out.println("Barreira reiniciada. O programa será encerrado.");
                            return;
                        } else {
                            System.out.println("Ainda não é possível enviar, o mínimo de palavras não foi atingido.");
                        }
                        break;
                    case 9:
                        System.out.println("Saindo e deixando a barreira...");
                        barrier.leave();
                        return;
                    default:
                        System.out.println("Opção inválida.");
                }
            } else {
                // --- MENU DO CLIENTE ---
                System.out.println("\nPalavras restantes para atingir o mínimo: " + (minWords - queue.size()));
                System.out.println("\nOpções de Cliente:");
                System.out.println("0 - Enviar palavra");
                System.out.println("9 - Sair");
                System.out.print("Sua escolha: ");
                int op = sc.nextInt();
                sc.nextLine();
                switch (op) {
                    case 0:
                        if(restantes <= 0) {
                            System.out.println("O número mínimo de palavras já foi atingido. Não é possível enviar mais palavras.");
                            break;
                        }
                        String myId = leader.pathName.replace("/", "_");
                        Stat mySubmission = leader.zk.exists(SUBMISSIONS_PATH + "/" + myId, false);

                        if (mySubmission != null) {
                            System.out.println("Você já enviou uma palavra nesta rodada. Aguarde o fim da rodada.");
                            break;
                        }
                        
                        Lock lock = new Lock(zkAddress, "/lock", 1000);
                        if (lock.lock()) {
                            try {
                                System.out.print("Digite a palavra: ");
                                String palavra = sc.nextLine();
                                if (palavra.trim().contains(" ") || palavra.trim().isEmpty()) {
                                    System.out.println("Warning: Palavra inválida.");
                                } else {
                                    queue.produce(palavra.trim());
                                    System.out.println("Palavra enviada com sucesso!");
                                    leader.zk.create(SUBMISSIONS_PATH + "/" + myId, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                                }
                            } finally {
                                lock.unlock();
                            }
                        }
                        break;
                    case 9:
                        System.out.println("Saindo e deixando a barreira...");
                        barrier.leave();
                        return;
                    default:
                        System.out.println("Opção inválida.");
                }
            }
        }
    }
}