package com.example.onyshchenko.youtubeparser.handler;

public class ContentNotFountException extends RuntimeException {

    private final int errorCode;

    public ContentNotFountException(Exception ex, int errorCode) {
        super(ex);
        this.errorCode = errorCode;
    }

    public ContentNotFountException(Exception ex) {
        super(ex);
        this.errorCode = 0;
    }

    public int getErrorCode() {
        return errorCode;
    }

}
