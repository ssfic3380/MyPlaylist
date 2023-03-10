package com.mypli.myplaylist.exception;

public class PlaylistNotFoundException extends RuntimeException {

    public PlaylistNotFoundException() {
    }

    public PlaylistNotFoundException(String message) {
        super(message);
    }

    public PlaylistNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlaylistNotFoundException(Throwable cause) {
        super(cause);
    }
}
