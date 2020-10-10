/***
 * Irc class : simple implementation of a chat using JAVANAISE
 * Contact:
 *
 * Authors:
 *      Chouaib Mounaime
 *      Emeziem Uwalaka
 */

package irc;

import jvn.JvnException;
import jvn.JvnObject;
import jvn.JvnServerImpl;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;


public class IrcV2 {
	public TextArea	text;
	public TextField data;
	Frame frame;
	JvnObject sentence;


	/**
	 * main method
	 * create a JVN object nammed IRC for representing the Chat application
	 **/
	public static void main(String argv[]) {
		try {
			// initialize JVN
			JvnServerImpl js = JvnServerImpl.jvnGetServer();
			// look up the IRC object in the JVN server
			// if not found, create it, and register it in the JVN server
			JvnObject jo = js.jvnLookupObject("IRC");
			if (jo == null) {
				jo = js.jvnCreateObject((Serializable) new Sentence());
				// after creation, I have a write lock on the object
				jo.jvnUnLock();
				js.jvnRegisterObject("IRC", jo);
			}
			// create the graphical part of the Chat application
			new Irc(jo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * IRC Constructor
	 @param jo the JVN object representing the Chat
	 **/
	public IrcV2(JvnObject jo) {
		sentence = jo;
		frame=new Frame();
		frame.setLayout(new GridLayout(1,1));
		text=new TextArea(10,60);
		text.setEditable(false);
		text.setForeground(Color.red);
		frame.add(text);
		data=new TextField(40);
		frame.add(data);
		Button read_button = new Button("read");
		read_button.addActionListener(new readListenerV2(this));
		frame.add(read_button);
		Button write_button = new Button("write");
		write_button.addActionListener(new writeListenerV2(this));
		frame.add(write_button);
		frame.setSize(545,201);
		text.setBackground(Color.black);
		frame.setVisible(true);
	}
}


/**
 * Internal class to manage user events (read) on the CHAT application
 **/
class readListenerV2 implements ActionListener {
	IrcV2 irc;

	public readListenerV2 (IrcV2 i) {
		irc = i;
	}

	/**
	 * Management of user events
	 **/
	public void actionPerformed (ActionEvent e) {
		try {
			// lock the object in read mode
			System.out.println("\nBefore lockRead operation :"+ irc.sentence.jvnGetSharedObject().toString());
			irc.sentence.jvnLockRead();
			// invoke the method
			String s = ((Sentence)(irc.sentence.jvnGetSharedObject())).read();

			// unlock the object
			irc.sentence.jvnUnLock();
			System.out.println("After lockRead operation  :"+ irc.sentence.jvnGetSharedObject().toString());

			// display the read value
			irc.data.setText(s);
			irc.text.append(s+"\n");
		} catch (JvnException je) {
			System.out.println("IRC problem : " + je.getMessage());
		}
	}
}

/**
 * Internal class to manage user events (write) on the CHAT application
 **/
class writeListenerV2 implements ActionListener {
	IrcV2 irc;

	public writeListenerV2 (IrcV2 i) {
		irc = i;
	}

	/**
	 * Management of user events
	 **/
	public void actionPerformed (ActionEvent e) {
		try {
			// get the value to be written from the buffer
			String s = irc.data.getText();
			System.out.println("\nBefore lockWrite operation :"+ irc.sentence.jvnGetSharedObject().toString());

			// lock the object in write mode
			irc.sentence.jvnLockWrite();
			System.out.println("After lockWrite operation  :"+ irc.sentence.jvnGetSharedObject().toString());

			// invoke the method
			((Sentence)(irc.sentence.jvnGetSharedObject())).write(s);

			// unlock the object
			irc.sentence.jvnUnLock();
		} catch (JvnException je) {
			System.out.println("IRC problem  : " + je.getMessage());
		}
	}
}