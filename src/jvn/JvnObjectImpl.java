package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {

    private static final long serialVersionUID = 1L;
    private LockState lockState;
    private int objId;
    private Serializable object;

    public JvnObjectImpl() {
        super();
        this.lockState = LockState.W;
    }

    public JvnObjectImpl(int objId, Serializable obj) throws JvnException {
        this.objId = objId;
        this.object = obj;
        this.setLockState(LockState.W);
    }

    public JvnObjectImpl(int id) {
        super();
        this.lockState = LockState.W;
        this.objId = id;
    }

    public void jvnLockRead() throws JvnException {
        switch (this.lockState) {
            case NL:
                this.object = JvnServerImpl.jvnGetServer().jvnLockRead(this.jvnGetObjectId());
                this.lockState = LockState.R;
                break;
            case RC:
            case R:
                this.lockState = LockState.R;
                break;
            case WC:
            case W:
                this.lockState = LockState.RWC;
                break;
        }
    }

    public void jvnLockWrite() throws JvnException {
        switch (this.lockState) {
            case NL:
                this.object = JvnServerImpl.jvnGetServer().jvnLockWrite(this.jvnGetObjectId());
                this.lockState = LockState.W;
                break;
            case RC:
            case R:
                JvnServerImpl.jvnGetServer().jvnLockWrite(this.jvnGetObjectId());
                this.lockState = LockState.W;
                break;
            case W:
            case WC:
            case RWC:
                this.lockState = LockState.W;
                break;
            default:
                throw new JvnException("Lock write error");
        }
    }

    synchronized public void jvnUnLock() throws JvnException {
        switch (this.lockState) {
            case R:
                this.lockState = LockState.RC;
                break;
            case W:
            case RWC:
                this.lockState = LockState.WC;
                break;
            case WC:
            case RC:
                this.lockState = LockState.NL;
                break;
        }
        this.notify();
    }

    public int jvnGetObjectId() throws JvnException {
        return this.objId;
    }

    public Serializable jvnGetSharedObject() throws JvnException {
        return object;
    }

    public void jvnInvalidateReader() throws JvnException {
        switch (this.lockState) {
            case RWC:
            case R:
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            case RC:
                this.lockState = LockState.NL;
                break;
            default:
                throw new JvnException("Reader invalidation error");
        }
    }

    public Serializable jvnInvalidateWriter() throws JvnException {
        switch (this.lockState) {
            case RWC:
            case W:
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            case WC:
                this.lockState = LockState.NL;
                break;

            default:
                throw new JvnException("Writer invalidation error");
        }

        return object;
    }

    public Serializable jvnInvalidateWriterForReader() throws JvnException {

        switch (this.lockState) {
            case RWC:
            case W:
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            case WC:
                this.lockState = LockState.RC;
                break;

            default:
                throw new JvnException("Writer for reader invalidation error");
        }
        return object;
    }


    @Override
    public void setSerializableObject(Serializable ser) throws JvnException {
        this.object = ser;
    }

    @Override
    public void setLockState(LockState ls) throws JvnException {
        this.lockState = ls;
    }
    @Override
    public String toString() {
        return  "ObjectID = " + objId +
                "  Value = " + object;
    }

}
