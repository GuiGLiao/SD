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
import org.w3c.dom.events.Event;
import codigos.SyncPrimitive;

public class Barrier extends SyncPrimitive {
    int size;
    String nodeName;

    /**
     * Barrier constructor
     *
     * @param address
     * @param root
     * @param size
     */
    Barrier(String address, String root, int size) { //
        super(address);
        this.root = root;
        this.size = size;

        // Create barrier node
        if (zk != null) { //
            try {
                Stat s = zk.exists(root, false); //
                if (s == null) { //
                    zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT); //
                }
            } catch (KeeperException e) {
                System.out
                        .println("Keeper exception when instantiating queue: "
                                + e.toString());
            } catch (InterruptedException e) {
                System.out.println("Interrupted exception");
            }
        }
    }

    /**
     * Join barrier
     *
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    boolean enter() throws KeeperException, InterruptedException{ //
        this.nodeName = zk.create(root + "/node-", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Conectado à barreira com o nó: " + nodeName);
        
        while (true) {
            synchronized (mutex) {
                List<String> list = zk.getChildren(root, true); //

                if (list.size() < size) { //
                    System.out.println("Aguardando mais participantes... (" + list.size() + "/" + size + ")");
                    mutex.wait(); //
                } else {
                    System.out.println("Barreira superada! Iniciando eleição...");
                    return true;
                }
            }
        }
    }

    /**
     * Wait until all reach barrier
     *
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    boolean leave() throws KeeperException, InterruptedException{ //
        zk.delete(this.nodeName, -1);
        System.out.println("Nó " + nodeName + " removido da barreira.");

        while (true) {
            synchronized (mutex) {
                List<String> list = zk.getChildren(root, true); //
                    if (list.size() > 0) { //
                        System.out.println("Aguardando outros participantes saírem...");
                        mutex.wait(); //
                    } else {
                        System.out.println("Barreira vazia. Pronta para a próxima rodada.");
                        return true;
                    }
                }
            }
    }
}