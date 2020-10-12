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
import jvn.JvnProxy;
import jvn.JvnServerImpl;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;


public class IrcV2 {
	public TextArea	text;
	public TextField data;
	Frame frame;
	Sentence_itf sentence;


	/**
	 * main method
	 * create a JVN object nammed IRC for representing the Chat application
	 **/
	public static void main(String argv[]) throws JvnException {
		if (argv.length != 1 ) {
			System.out.println("The name of your object is required\n" +
					           "Usage : java irc.Irc <object_name>");
			System.exit(1);
		}

		Sentence_itf jo = (Sentence_itf) JvnProxy.newInstance(new Sentence(), argv[0]);
		new IrcV2(jo);

		/*try {
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
		 */
	}

	/**
	 * IRC Constructor
	 @param jo the JVN object representing the Chat
	 **/
	public IrcV2(Sentence_itf jo) {
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
	IrcV2 ircV2;

	public readListenerV2 (IrcV2 i) {
		ircV2 = i;
	}

	/**
	 * Management of user events
	 **/
	public void actionPerformed (ActionEvent e) {
		// invoke the method
		String s = ircV2.sentence.read();

		// display the read value
		ircV2.data.setText(s);

		ircV2.text.append(s+"\n");
	}
}

/**
 * Internal class to manage user events (write) on the CHAT application
 **/
class writeListenerV2 implements ActionListener {
	IrcV2 ircV2;

	public writeListenerV2 (IrcV2 i) {
		ircV2 = i;
	}

	/**
	 * Management of user events
	 **/
	public void actionPerformed (ActionEvent e) {
			// get the value to be written from the buffer
			String s = ircV2.data.getText();

			// invoke the method
			ircV2.sentence.write(s);


	}
}