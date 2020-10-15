/***
 * Sentence class : used for representing the text exchanged between users
 * during a chat application
 * Contact:
 *
 * Authors:
 *      Chouaib Mounaime
 *      Emeziem Uwalaka
 */
package irc;

import java.io.Serializable;

public class Sentence implements Serializable, Sentence_itf {

	private static final long serialVersionUID = 1L;
	String data;

	public Sentence() {
		data = new String("-");
	}

	public Sentence(String str) {
		data = new String(str);
	}

	public void write(String text) {
		data = text;
	}
	public String read() {
		return data;
	}
	public String debug() { return "Sentence.data = " + data;}
}