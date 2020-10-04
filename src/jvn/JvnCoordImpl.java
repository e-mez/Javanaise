/***
 * JAVANAISE Implementation
 * JvnCoordImpl class
 * This class implements the Javanaise central coordinator
 * Contact:
 *
 * Authors:
 *      Chouaib Mounaime
 *      Emeziem Uwalaka
 */

package jvn;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.*;

import java.io.Serializable;

public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {

    private static final long serialVersionUID = 1L;
    private static JvnCoordImpl coord = null;

    private  int jvnObjectId;

    private HashMap<String, JvnObject> jvnObjectsMap;
    private HashMap<Integer, String> jvnObjectIdMap;

    //Readers of a JVN object
    private HashMap<Integer, List<JvnRemoteServer>> jvnObjectReaders;

    //Writer of a JVN object
    private HashMap<Integer, JvnRemoteServer> jvnObjectWriter;

    //JVN objects in each remote server
    private Map<JvnRemoteServer, ArrayList<JvnObject>> serverJvnObjects;

    /**
     * Default constructor
     * @throws JvnException
     **/
    public JvnCoordImpl() throws Exception {
        this.jvnObjectId = 0;
        this.jvnObjectsMap = new HashMap<>();
        this.jvnObjectIdMap = new HashMap<>();
        this.jvnObjectReaders = new HashMap<>();
        this.jvnObjectWriter = new HashMap<>();
        this.serverJvnObjects = new HashMap<>();
    }

    /**
     * Method allowing an application to get a reference to
     * a JVN coordinator instance
     * @throws JvnException
     **/
    public static JvnCoordImpl jvnGetCoordinator() {
        if (coord == null) {
            try {
                coord = new JvnCoordImpl();
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        return coord;
    }

    /**
     *  Allocate a NEW JVN object id (usually allocated to a
     *  newly created JVN object)
     * @throws java.rmi.RemoteException,JvnException
     **/
    synchronized public int jvnGetObjectId() throws java.rmi.RemoteException, JvnException {
        return ++this.jvnObjectId;
    }

    /**
     * Associate a symbolic name with a JVN object
     * @param jon : the JVN object name
     * @param jo  : the JVN object
     * @param js  : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     **/
    synchronized public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
        if (this.jvnObjectsMap.containsKey(jon)) {
            throw new jvn.JvnException("Object with name \""+jon+"\" already registered");
        }
        jvnObjectsMap.put(jon, jo);
        jvnObjectIdMap.put(jo.jvnGetObjectId(), jon);

        jvnObjectWriter.put(jo.jvnGetObjectId(), js);
        jvnObjectReaders.put(jo.jvnGetObjectId(), new ArrayList<JvnRemoteServer>());

        serverJvnObjects.computeIfAbsent(js, k -> new ArrayList<JvnObject>());
        serverJvnObjects.get(js).add(jo);
    }

    /**
     * Get the reference of a JVN object managed by a given JVN server
     * @param jon : the JVN object name
     * @param js : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     **/
    synchronized public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
        JvnObject object = jvnObjectsMap.get(jon);
        serverJvnObjects.computeIfAbsent(js, k -> new ArrayList<JvnObject>());
        if(object == null) {
            return null;
        }
        serverJvnObjects.get(js).add(object);
        object.setLockState(LockState.NL);
        return object;
    }

    /**
     * Get a Read lock on a JVN object managed by a given JVN server
     * @param joi : the JVN object identification
     * @param js  : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     **/
    synchronized public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
        if(!jvnObjectIdMap.containsKey(joi) ) {
            System.out.println("Object with id \"" + joi + "\" not found");
            return null;
        }
        JvnRemoteServer writer = jvnObjectWriter.get(joi);
        Serializable object = null;
        String name = jvnObjectIdMap.get(joi);

        if(writer != null) {
            if(!writer.equals(js)) {
                object = writer.jvnInvalidateWriterForReader(joi);
                jvnObjectWriter.put(joi, null);
                jvnObjectReaders.get(joi).add(writer);
            }
            jvnObjectsMap.get(name).setSerializableObject(object);
        } else {
            object = jvnObjectsMap.get(name).jvnGetSharedObject();
        }
        jvnObjectReaders.get(joi).add(js);

        return object;
    }

    /**
     * A JVN server terminates
     * @param js  : the remote reference of the server
     * @throws java.rmi.RemoteException, JvnException
     **/
    synchronized public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {

        JvnRemoteServer writer = jvnObjectWriter.get(joi);
        Serializable object = null;
        String name = jvnObjectIdMap.get(joi);

        if(writer != null && !writer.equals(js)) {
            object = writer.jvnInvalidateWriter(joi);
            jvnObjectWriter.put(joi, null);
            jvnObjectsMap.get(name).setSerializableObject(object);
        }
        else {
            object = jvnObjectsMap.get(name).jvnGetSharedObject();
        }

        for(JvnRemoteServer server : jvnObjectReaders.get(joi)) {
            if(!server.equals(js)) {
                server.jvnInvalidateReader(joi);
            }
        }
        jvnObjectReaders.put(joi, new ArrayList<JvnRemoteServer>());
        jvnObjectWriter.put(joi, js);

        return object;
    }

    /**
     * A JVN server terminates
     *
     * @param js : the remote reference of the server
     * @throws java.rmi.RemoteException, JvnException
     **/
    synchronized public void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
        for(JvnObject jo : serverJvnObjects.get(js)) {
            List<JvnRemoteServer> readers = jvnObjectReaders.get(jo.jvnGetObjectId());
            readers.remove(js);
            JvnRemoteServer writer = jvnObjectWriter.get(jo.jvnGetObjectId());
            if(writer == js) {
                jvnObjectWriter.put(jo.jvnGetObjectId(),null);
            }
        }

        serverJvnObjects.remove(js);
    }


    public static void main(String[] args) {
        try {
            JvnRemoteCoord jvnCoord = new JvnCoordImpl();
            Registry registry = LocateRegistry.createRegistry(3000);
            registry.bind("JvnCoord", jvnCoord);

            System.err.println("Coordinator is running ...");
            Thread thread = new Thread(){
                @Override
                public void run() {
                    while(true) {
                        try {
                            if( ! ((JvnCoordImpl) jvnCoord).jvnObjectsMap.isEmpty()) {
                                for (JvnObject value : ((JvnCoordImpl) jvnCoord).jvnObjectsMap.values()) {
                                    String time = " [" + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + "]  ";
                                    System.out.println(time + value);
                                }
                                System.out.println();
                            }
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            thread.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
