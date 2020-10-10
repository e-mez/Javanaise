package irc;

import jvn.Annotation;

public interface Sentence_itf {
    @Annotation(methodType="write")
    public void write(String text);

    @Annotation(methodType="read")
    public String read();
}


