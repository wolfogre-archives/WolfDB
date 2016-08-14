package com.wolfogre.wolfdb;

/**
 * Created by wolfogre on 8/14/16.
 */
public class FileDbException extends Exception{

    public FileDbException() {
    }

    public FileDbException(String s) {
        super(s);
    }

    public FileDbException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public FileDbException(Throwable throwable) {
        super(throwable);
    }

    public FileDbException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
