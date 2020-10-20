/***
 * Burst_N_1 class : N clients reading and writing to 1 object in burst mode
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
import jvn.JvnProxy;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Burst_N_1 implements Runnable {
    static final int NB_THREADS = 3;
    static final int NB_WRITES = 10;

    static Sentence_itf sentence;
    static final String FILENAME = "src/tests/burst_N_1.txt";
    static FileWriter fileWriter;
    static PrintWriter printWriter;

    static {
        try {
            sentence = (Sentence_itf) JvnProxy.newInstance(new Sentence(),"IRC");
            fileWriter = new FileWriter(FILENAME);
            printWriter = new PrintWriter(fileWriter);
        } catch (JvnException | IOException e) {
            e.printStackTrace();
        }

    }


    private final Random random;
    private int val;
    private final String name;



    public Burst_N_1(String name) {
        this.name = name;
        this.random = new Random();
    }

    @Override
    public void run() {
        while (val < NB_WRITES) {
            synchronized (sentence) {
                String s = "";
                s += this.name + " in ";

                int mode = random.nextInt(2);
                if (mode == 0) {
                    sentence.read();
                    s += "Read mode => ";
                }
                else {
                    sentence.write("" + ++val);
                    s += "Write mode => ";
                }

                s += sentence.debug() + "\n";

                synchronized (printWriter) {
                    printWriter.println(s);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        printWriter.close();
    }

    /*
    main starts <NB_THREADS> threads
    Each thread will read/write to a common object
    Each thread ends after writing <NB_WRITES> locally unique values
     */
    public static void main(String argv[]) throws InterruptedException {
        List<Burst_N_1> clients = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < Burst_N_1.NB_THREADS; i++) {
            clients.add(i, new Burst_N_1("Thread_" + i));
            threads.add(i, new Thread(clients.get(i)));
        }

        for (int i = 0; i < Burst_N_1.NB_THREADS; i++) {
            threads.get(i).start();
        }

        for (int i = 0; i < Burst_N_1.NB_THREADS; i++) {
            threads.get(i).join();
        }

        System.out.println("All threads done.");
        System.exit(0);

    }
}
