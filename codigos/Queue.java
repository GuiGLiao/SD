package codigos;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
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
import codigos.Leader;
import codigos.Barrier;


public class Queue extends SyncPrimitive{

        /**
         * Constructor of producer-consumer queue
         *
         * @param address
         * @param name
         */
        Queue(String address, String name) {
            super(address);
            this.root = name;
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

        /**
         * Add element to the queue.
         *
         * @param i
         * @return
         */

        boolean produce(int i) throws KeeperException, InterruptedException{
            ByteBuffer b = ByteBuffer.allocate(4);
            byte[] value;

            // Add child with value i
            b.putInt(i);
            value = b.array();
            zk.create(root + "/element", value, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);

            return true;
        }

        /**
         * Add element to the queue (String).
         */
        boolean produce(String palavra) throws KeeperException, InterruptedException {
            byte[] value = palavra.getBytes();
            zk.create(root + "/element", value, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
            return true;
        }

        /**
         * Remove first element from the queue.
         *
         * @return
         * @throws KeeperException
         * @throws InterruptedException
         */
        int consume() throws KeeperException, InterruptedException{
            int retvalue = -1;
            Stat stat = null;

            // Get the first element available
            while (true) {
                synchronized (mutex) {
                    List<String> list = zk.getChildren(root, true);
                    if (list.size() == 0) {
                        System.out.println("Going to wait");
                        mutex.wait();
                    } else {
                        Integer min = Integer.valueOf(list.get(0).substring(7));
                        System.out.println("List: "+list.toString());
                        String minString = list.get(0);
                        for(String s : list){
                            Integer tempValue = Integer.valueOf(s.substring(7));
                            //System.out.println("Temp value: " + tempValue);
                            if(tempValue < min) { 
                            	min = tempValue;
                            	minString = s;
                            }
                        }
                       System.out.println("Temporary value: " + root +"/"+ minString);
                        byte[] b = zk.getData(root +"/"+ minString,false, stat);
                        //System.out.println("b: " + Arrays.toString(b)); 	
                        zk.delete(root +"/"+ minString, 0);
                        ByteBuffer buffer = ByteBuffer.wrap(b);
                        retvalue = buffer.getInt();
                        return retvalue;
                    }
                }
            }
        }
        
        public List<String> getAllStrings() throws KeeperException, InterruptedException {
            List<String> palavras = new ArrayList<>();
            List<String> list = zk.getChildren(root, false);
            Collections.sort(list);
            for (String node : list) {
                byte[] b = zk.getData(root + "/" + node, false, null);
                palavras.add(new String(b));
            }
            return palavras;
        }

        public int size() throws KeeperException, InterruptedException {
            return zk.getChildren(root, false).size();
        }

        /**
         * Remove first element from the queue (String).
         */
        String consumeString() throws KeeperException, InterruptedException {
            Stat stat = null;
            while (true) {
                synchronized (mutex) {
                    List<String> list = zk.getChildren(root, true);
                    if (list.size() == 0) {
                        mutex.wait();
                    } else {
                        Integer min = Integer.valueOf(list.get(0).substring(7));
                        String minString = list.get(0);
                        for (String s : list) {
                            Integer tempValue = Integer.valueOf(s.substring(7));
                            if (tempValue < min) {
                                min = tempValue;
                                minString = s;
                            }
                        }
                        byte[] b = zk.getData(root + "/" + minString, false, stat);
                        zk.delete(root + "/" + minString, 0);
                        return new String(b);
                    }
                }
            }
        }

        /**
         * Remove all elements from the queue (String), in order.
         */
        java.util.List<String> consumeAllStrings() throws KeeperException, InterruptedException {
            java.util.List<String> palavras = new java.util.ArrayList<>();
            while (true) {
                synchronized (mutex) {
                    List<String> list = zk.getChildren(root, false);
                    if (list.size() == 0) break;
                    Integer min = Integer.valueOf(list.get(0).substring(7));
                    String minString = list.get(0);
                    for (String s : list) {
                        Integer tempValue = Integer.valueOf(s.substring(7));
                        if (tempValue < min) {
                            min = tempValue;
                            minString = s;
                        }
                    }
                    byte[] b = zk.getData(root + "/" + minString, false, null);
                    zk.delete(root + "/" + minString, 0);
                    palavras.add(new String(b));
                }
            }
            return palavras;
        }
        
        public void clear() throws KeeperException, InterruptedException {
            List<String> list = zk.getChildren(root, false);
            for (String node : list) {
                zk.delete(root + "/" + node, 0);
            }
        }
}
