package codigos;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
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
import codigos.SyncPrimitive;
import codigos.Queue;
import org.w3c.dom.events.Event;

public class Lock extends SyncPrimitive {
    long wait;
    String pathName;
    String myNodeName;

    /**
     * Constructor of lock
     *
     * @param address
     * @param name Name of the lock node
     */
    Lock(String address, String name, long waitTime) {
        super(address);
        this.root = name;
        this.wait = waitTime;
        // Create ZK node name
        if (zk != null) {
            try {
                Stat s = zk.exists(root, false);
                if (s == null) {
                    zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
            } catch (KeeperException e) {
                System.out.println("Keeper exception when instantiating queue: " + e.toString());
            } catch (InterruptedException e) {
                System.out.println("Interrupted exception");
            }
        }
    }

    boolean lock() throws KeeperException, InterruptedException {
        // Passo 1: Cria um nó efêmero e sequencial
        pathName = zk.create(root + "/lock-", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        myNodeName = pathName.substring(root.length() + 1);
        //System.out.println("Meu nó de lock é: " + myNodeName);

        // Loop de espera pelo lock
        while (true) {
            synchronized (mutex) {
                // Passo 2: Pega a lista de filhos (outros locks)
                List<String> children = zk.getChildren(root, false);
                Collections.sort(children);

                // Verifica se meu nó é o menor (primeiro da lista)
                if (myNodeName.equals(children.get(0))) {
                    System.out.println("Lock adquirido!");
                    return true;
                }

                // Passo 3 e 4: Encontra o nó anterior e coloca um Watcher nele
                int myIndex = children.indexOf(myNodeName);
                String nodeToWatch = children.get(myIndex - 1);
                
                Stat stat = zk.exists(root + "/" + nodeToWatch, this); // 'this' é o Watcher

                // Se o nó anterior existe, espera. Se não, o loop recomeça.
                if (stat != null) {
                    System.out.println("Aguardando envio por outro grupo...");
                    mutex.wait(); // Pausa a thread até ser notificado
                }
            }
        }
    }

    public void unlock() throws KeeperException, InterruptedException {
        if (pathName != null) {
            zk.delete(pathName, -1); // Deleta o nó do lock
            pathName = null;
            System.out.println("Lock liberado!");
        }
    }

    @Override
    synchronized public void process(WatchedEvent event) {
        synchronized (mutex) {
            // Se o nó que estávamos observando foi deletado, notifica a thread em espera.
            if (event.getType() == Watcher.Event.EventType.NodeDeleted) {
                mutex.notify();
            }
        }
    }
}