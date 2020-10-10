/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Implementation of a Jvn server
 * Contact:
 *
 * Authors:
 *      Chouaib Mounaime
 *      Emeziem Uwalaka
 */


package jvn;

import irc.Sentence;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import java.io.*;
import java.util.Scanner;

public class JvnServerImpl extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer {

	private static final long serialVersionUID = 1L;

	// A JVN server is managed as a singleton
	private static JvnServerImpl js = null;

	private JvnRemoteCoord coordinator;
	private HashMap<Integer, JvnObject> objects = new HashMap<Integer, JvnObject>();

	//Local cache structures
	private HashMap<String, Integer> jvnObjectIdMap;
	private HashMap<Integer, JvnObject> jvnObjectMap;


	/**
	 * Default constructor
	 *
	 * @throws JvnException
	 **/
	private JvnServerImpl() throws Exception {
		super();
		jvnObjectMap = new HashMap<>();
		jvnObjectIdMap = new HashMap<>();
		if (coordinator == null) {
			try {
				Registry registry = LocateRegistry.getRegistry("localhost", 3000);
				coordinator = (JvnRemoteCoord) registry.lookup("JvnCoord");
			} catch (Exception e) {
				System.err.println("JvnCoord exception : " + e.toString());
			}
		}
	}

	/**
	 * Static method allowing an application to get a reference to a JVN server
	 * instance
	 *
	 * @throws JvnException
	 **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null) {
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				System.err.println(e.getMessage());
				return null;
			}
		}
		return js;
	}

	/**
	 * The JVN service is not used anymore
	 * @throws JvnException
	 **/
	public void jvnTerminate() throws JvnException {
		try {
			coordinator.jvnTerminate(this);
		} catch (RemoteException e) {
			System.err.println("Terminate error : " + e.getMessage());
		}
	}

	/**
	 * creation of a JVN object
	 * @param o : the JVN object state
	 * @throws JvnException
	 **/
	public JvnObject jvnCreateObject(Serializable o) throws JvnException {
		JvnObjectImpl object = null;
		try {
			int objId = coordinator.jvnGetObjectId();
			object = new JvnObjectImpl(objId, o);
			System.out.println("Object created successfully");
			return object;
		} catch (RemoteException e) {
			System.err.println("Object creation error : " + e.toString());
			return null;
		}
	}

	/**
	 * Associate a symbolic name with a JVN object
	 * @param jon : the JVN object name
	 * @param jo : the JVN object
	 * @throws JvnException
	 **/
	public void jvnRegisterObject(String jon, JvnObject jo) throws JvnException {
		try {
			//Registrate object in the remote store
			coordinator.jvnRegisterObject(jon, jo, this);

			//Putting the object in the local cache
			jvnObjectIdMap.put(jon, jo.jvnGetObjectId());
			jvnObjectMap.put(jo.jvnGetObjectId(), jo);
			System.out.println("Object \""+jon+"\" registered successfully");

		} catch (RemoteException e) {
			System.err.println("Object registration error : " + e.toString());
		}
	}

	/**
	 * Provide the reference of a JVN object beeing given its symbolic name
	 * @param jon : the JVN object name
	 * @return the JVN object
	 * @throws JvnException
	 **/
	public JvnObject jvnLookupObject(String jon) throws JvnException {
		//Looking up for the object locally first
		if( jvnObjectIdMap.containsKey(jon)){
			int id = jvnObjectIdMap.get(jon);
			return jvnObjectMap.get(id);
		}

		//Otherwise we lookup in the remote store
		//System.out.println("Object \""+jon+"\" not found locally, looking up on remote store ...");
		try {
			JvnObject obj =  coordinator.jvnLookupObject(jon, this);
			if (obj != null) {
				//Putting the object in the local cache
				this.jvnObjectMap.put(obj.jvnGetObjectId(), obj);
				this.jvnObjectIdMap.put(jon, obj.jvnGetObjectId());
			}
			return obj;
		} catch (RemoteException e) {
			System.err.println("Object lookup error : " + e.toString());
			return null;
		}
	}

	/**
	 * Get a Read lock on a JVN object
	 * @param joi : the JVN object identification
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public Serializable jvnLockRead(int joi) throws JvnException {
		try {
			return coordinator.jvnLockRead(joi, this);
		} catch (RemoteException e) {
			System.err.println("lock read error : " + e.toString());
			return null;
		}
	}

	/**
	 * Get a Write lock on a JVN object
	 * @param joi : the JVN object identification
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public Serializable jvnLockWrite(int joi) throws JvnException {
		try {
			return coordinator.jvnLockWrite(joi, this);
		} catch (RemoteException e) {
			System.err.println("lock write error : " + e.toString());
			return null;		}
	}

	/**
	 * Invalidate the Read lock of the JVN object identified by id
	 * called by the JvnCoord
	 * @param joi : the JVN object id
	 * @return void
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public void jvnInvalidateReader(int joi) throws java.rmi.RemoteException, JvnException {
		JvnObject obj = jvnObjectMap.get(joi);
		if ( obj != null)
			obj.jvnInvalidateReader();
		else
			throw new JvnException("Object with id "+joi+" not found");	};

	/**
	 * Invalidate the Write lock of the JVN object identified by id
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public Serializable jvnInvalidateWriter(int joi) throws java.rmi.RemoteException, JvnException {
		JvnObject obj = jvnObjectMap.get(joi);
		if ( obj != null)
			return obj.jvnInvalidateWriter();
		else
			throw new JvnException("Object with id "+joi+" not found");	};

	/**
	 * Reduce the Write lock of the JVN object identified by id
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public Serializable jvnInvalidateWriterForReader(int joi) throws java.rmi.RemoteException, JvnException {
		JvnObject obj = jvnObjectMap.get(joi);
		if ( obj != null)
			return obj.jvnInvalidateWriterForReader();
		else
			throw new JvnException("Object with id "+joi+" not found");
	}


	public static void main(String argv[]) {
		Scanner sc = new Scanner(System.in);
		try {
			JvnServerImpl js = JvnServerImpl.jvnGetServer();

			for( String arg : argv){
				JvnObject jo = js.jvnLookupObject(arg);
				if (jo == null) {
					jo = js.jvnCreateObject(new Sentence(arg));
					js.jvnRegisterObject(arg, jo);
					Thread.sleep(5000);
				}
			}
			String str = "";
			while (str != "exit"){
				str = sc.nextLine();
				String[] params = str.split(" ");
				JvnObject jo = js.jvnLookupObject(params[1]);
				if (jo == null) {
					jo = js.jvnCreateObject(new Sentence(params[1]));
					js.jvnRegisterObject(params[1], jo);
				}
				switch(params[0]){
					case "read" :
						jo.jvnLockRead();
						String readStr = ((Sentence)jo.jvnGetSharedObject()).read();
						System.out.println(str);
						jo.jvnUnLock();
						break;
					case "write":
						jo.jvnLockWrite();
						((Sentence)jo.jvnGetSharedObject()).write(params[2]);
						jo.jvnUnLock();
						break;
				}
			}

		} catch (Exception e) {
			System.out.println("JvnServer problem : " + e.toString());
			e.printStackTrace();
		}
	}

}
