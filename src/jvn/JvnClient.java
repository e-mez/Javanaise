package jvn;

import irc.Sentence;
import irc.Sentence_itf;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class JvnClient {
    private AbstractMap<String, Sentence_itf> objects;
    private JvnRemoteCoord coordinator;

    public JvnClient () {
        objects = new ConcurrentHashMap<>();

        if (coordinator == null) {
            try {
                Registry registry = LocateRegistry.getRegistry("localhost", 3000);
                coordinator = (JvnRemoteCoord) registry.lookup("JvnCoord");
            } catch (Exception e) {
                System.err.println("JvnCoord exception : " + e.toString());
            }
        }

    }

     public void createObject(String name) throws JvnException, RemoteException {
        if (!hasObject(name)) {
            JvnObject jo = coordinator.jvnLookupObject(name, null);
            if (jo == null) {
                System.out.println("Object not found in the remote store");
                objects.put(name, (Sentence_itf) JvnProxy.newInstance(new Sentence(), name));
            }
            else {
                System.out.println("Object found in the remote store");
                objects.put(name, (Sentence_itf) jo.jvnGetSharedObject());
            }
        }

    }

    public String readObject(String name) throws RemoteException, JvnException {
        String val = "";
        Sentence_itf s = objects.get(name);
        if (s == null) {
            JvnObject jo = coordinator.jvnLookupObject(name, null);

            if (jo == null) {
                System.out.println("Object to read not found in the remote store");
            }
            else {
                System.out.println("Object to read found in the remote store");
                val = jo.jvnGetSharedObject().toString();
                objects.put(name, (Sentence_itf) jo.jvnGetSharedObject());
            }
        }
        else {
            val = s.read();
        }

        return val;

    }

    public void writeObject(String name, String val) throws RemoteException, JvnException {
        Sentence_itf s = objects.get(name);
        if (s == null) {
            JvnObject jo = coordinator.jvnLookupObject(name, null);

            if (jo == null) {
                System.out.println("Object to write to was not found in the remote store");
            }
            else {
                System.out.println("Object to write to found in the remote store");
                s = (Sentence_itf) jo.jvnGetSharedObject();
                s.write(val);
                objects.put(name, s);
            }
        }
        else {
            s.write(val);
        }

/*
        Sentence_itf s = objects.get(name);
        if (s != null)
            s.write(val);
        else
            System.out.println("Object '" + name + "' does not exist");
        */
    }

    public int nbLocalObjects() { return objects.size(); }

    public void flushObjects() {
        objects = new ConcurrentHashMap<>();
        System.out.println("Cache flushed\n");
    }

    public boolean hasObject(String name) {
        for (Map.Entry<String, Sentence_itf> m : objects.entrySet()) {
            if (m.getKey().equals(name))
                return true;
        }
        return false;
    }

    public void showLocalObjects() throws RemoteException, JvnException {
        for (Map.Entry<String, Sentence_itf> m : objects.entrySet()) {
            System.out.println(m.getKey() + " => " + this.readObject(m.getKey()) /*m.getValue().*/);
        }
        System.out.println();
    }

    public void showLocalCacheSize() {
        System.out.println("Local cache size = " + nbLocalObjects() + "\n");
    }

    public static void showMenu() {
        System.out.println("'c' to create an object");
        System.out.println("'r' to display local cache content");
        System.out.println("'w' to write to an object");
        System.out.println("'f' to flush the cache");
        System.out.println("'lcs' to display the local cache size");
        System.out.println("'h' to display this menu");
        System.out.println("'q' to quit");
    }

    public static void main(String[] argv) throws JvnException, RemoteException {
        JvnClient c = new JvnClient();
        JvnClient.showMenu();

        Scanner sc = new Scanner(System.in);
        String cmd, oName;
        do {
            cmd = sc.next();
            switch (cmd) {
                case "c":
                    System.out.print("Enter object name: ");
                    oName = sc.next();
                    c.createObject(oName);
                    break;

                case "r":
                    c.showLocalObjects();
                    break;

                case "w":
                    System.out.println("Choose object from cache: ");
                    c.showLocalObjects();
                    oName = sc.next();
                    System.out.println("Enter new value: ");
                    c.writeObject(oName, sc.next());
                    break;

                case "f":
                    System.out.println("Clear cache? y/n ");
                    String ans = sc.next();
                    while (!ans.toLowerCase().equals("y") && !ans.toLowerCase().equals("n")) { ans = sc.next(); }
                    if (ans.toLowerCase().equals("y")) c.flushObjects();
                    break;

                case "lcs":
                    c.showLocalCacheSize();
                    break;

                case "h":
                default:
                    JvnClient.showMenu();
                    break;

            }

        } while (!cmd.equals("q"));

        System.exit(0);


    }
}
