package jvn;

import irc.Irc;
import irc.Sentence;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.rmi.ServerError;

public class JvnProxy implements InvocationHandler {
    // Il faut d'abord créer un serveur local Javanaise,
    static JvnServerImpl js = JvnServerImpl.jvnGetServer();

    private JvnObject object;

    private JvnProxy(JvnObject obj){
        this.object= obj;
    }

    public static Object newInstance(Serializable obj, String name) throws IllegalArgumentException, JvnException {
        // faire un lookup et créer l'objet Javanaise a partir d'un object serializable
        JvnObject jo = null;
        try {
            jo = js.jvnLookupObject(name);
            if (jo == null) {
                jo = js.jvnCreateObject(obj);
                jo.jvnUnLock();
                js.jvnRegisterObject(name, jo);
            }
        } catch (Exception e) {
            System.err.println("Proxy newInstance error : "+e.getMessage());
            e.printStackTrace();
        }

        return java.lang.reflect.Proxy.newProxyInstance(
                obj.getClass().getClassLoader(),
                obj.getClass().getInterfaces(),
                new JvnProxy(jo)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        Object newObject = new Object();
        try {
            // On verifie les annotations et on prend le verrou si necessaire avant l'invocation de la methode
            if(method.isAnnotationPresent(JvnAnnotation.class)) {
                // Cas d'une lecture
                if(method.getAnnotation(JvnAnnotation.class).operation().equals("read") ||
                        method.getAnnotation(JvnAnnotation.class).operation().equals("debug")) {
                    this.object.jvnLockRead();
                    // On invoke la methode puis on relache le verrou
                    newObject = method.invoke(this.object.jvnGetSharedObject(), args);
                }
                // Cas d'une ecriture
                else if(method.getAnnotation(JvnAnnotation.class).operation().equals("write")) {
                    this.object.jvnLockWrite();
                    // On invoke la methode puis on relache le verrou
                    method.invoke(this.object.jvnGetSharedObject(), args);
                }
                else {
                    throw new JvnException("method invocation error");
                }
            }
            this.object.jvnUnLock();
        }
        catch (Exception e) {
            System.err.println("JvnProxy invoke error : " + e.toString());
            e.printStackTrace();
        }
        return newObject;
    }
}
