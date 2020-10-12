/***
 * Irc class : simple implementation of a chat using JAVANAISE
 * Contact:
 *
 * Authors:
 *      Chouaib Mounaime
 *      Emeziem Uwalaka
 */

package tests;

import jvn.JvnException;
import jvn.JvnObject;
import jvn.JvnServerImpl;

/*
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
*/


public class Burst1 {
	/**
	 * main method
	 * burst test of reads and writes using dynamic proxy
	 **/
	public static void main(String argv[]) {
		try {
			Sentence_itf sentence = (Sentence) JvnProxy.newInstance(new Sentence(), "IRC");
			
			int i = 0, n = 0;
			Random rand = new Random();
			
			while (true) {
				n = rand.nextInt(2);
				if (n == 0) {
					sentence.read();
				}
				else {
					sentence.write("burst write " + ++i);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}




