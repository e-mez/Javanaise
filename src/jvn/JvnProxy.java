package jvn;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.rmi.ServerError;

public class JvnProxy implements InvocationHandler {

    private  JvnObject object;

    private JvnProxy(JvnObject obj){
        this.object= obj;
    }

    public static Object newInstance(JvnObject jvno) throws IllegalArgumentException, JvnException {
        return java.lang.reflect.Proxy.newProxyInstance(
                jvno.jvnGetSharedObject().getClass().getClassLoader(),
                jvno.jvnGetSharedObject().getClass().getInterfaces(),
                new JvnProxy(jvno)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object newObject = new Object();
        try {
            // On verifie les annotations et on prend le verrou si necessaire avant l'invocation de la methode
            if(method.isAnnotationPresent(JvnAnnotation.class)) {
                // Cas d'une lecture
                if(method.getAnnotation(JvnAnnotation.class).operation().equals("read")) {
                    this.object.jvnLockRead();
                }
                // Cas d'une ecriture
                else if(method.getAnnotation(JvnAnnotation.class).operation().equals("write")) {
                    this.object.jvnLockWrite();
                }
                else {
                    throw new JvnException("method invocation error");
                }
            }

            // On invoke la methode puis on relache le verrou
            newObject = method.invoke(this.object.jvnGetSharedObject(), args);
            this.object.jvnUnLock();
        }
        catch (Exception e) {
            System.err.println("JvnProxy invoke error : " + e.toString());
            e.printStackTrace();
        }
        return newObject;
    }
}
