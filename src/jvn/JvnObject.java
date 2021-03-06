/***
 * JAVANAISE API
 * Contact: 
 *
 * Authors:
 *      Chouaib Mounaime
 *      Emeziem Uwalaka
 */

package jvn;

import java.io.*;

/**
 * Interface of a JVN object. 
 * The serializable property is required in order to be able to transfer
 * a reference to a JVN object remotely
 */

public interface JvnObject extends Serializable {

	/**
	 * Get a Read lock on the object
	 * @throws JvnException
	 **/
	public void jvnLockRead()
			throws JvnException;

	/**
	 * Get a Write lock on the object
	 * @throws JvnException
	 **/
	public void jvnLockWrite()
			throws JvnException;

	/**
	 * Unlock  the object
	 * @throws JvnException
	 **/
	public void jvnUnLock()
			throws JvnException;


	/**
	 * Get the object identification
	 * @throws JvnException
	 **/
	public int jvnGetObjectId()
			throws JvnException;

	/**
	 * Get the object state
	 * @throws JvnException
	 **/
	public Serializable jvnGetSharedObject()
			throws JvnException;


	/**
	 * Invalidate the Read lock of the JVN object
	 * @throws JvnException
	 **/
	public void jvnInvalidateReader()
			throws JvnException;

	/**
	 * Invalidate the Write lock of the JVN object
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public Serializable jvnInvalidateWriter()
			throws JvnException;

	/**
	 * Reduce the Write lock of the JVN object
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public Serializable jvnInvalidateWriterForReader()
			throws JvnException;

	public void setSerializableObject(Serializable ser)
			throws JvnException;

	public void setLockState(LockState ls)
			throws JvnException;
}
