/***
 * Irc class : simple implementation of a chat using JAVANAISE
 * Contact:
 *
 * Authors:
 *      Chouaib Mounaime
 *      Emeziem Uwalaka
 */

package tests;

import irc.Sentence;
import irc.Sentence_itf;
import jvn.JvnException;
import jvn.JvnObject;
import jvn.JvnProxy;
import jvn.JvnServerImpl;

import java.util.Random;

public class Burst1 {
	/**
	 * main method
	 * burst test of reads and writes using dynamic proxy
	 **/
	public static void main(String argv[]) {
		try {
			Sentence_itf sentence = (Sentence_itf) JvnProxy.newInstance(new Sentence(), "IRC");
			int i = 0, n;
			Random rand = new Random();

			while (true) {
				n = rand.nextInt(2);
				if (n == 0) {
					sentence.read();
					System.out.print("Read mode => ");
				}
				else {
					sentence.write("" + ++i);
					System.out.print("Write mode => ");
				}
				System.out.println(sentence.debug() + "\n");
				Thread.sleep(3000);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}




