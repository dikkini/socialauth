package com.socialauth.common;

/**
 * akarapetov
 * com.socialauth.facebook
 * socialauth
 * 13.01.2017
 */
public class SocialAuthException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int code;

    public SocialAuthException(String message) {
        super(message);
    }

    public SocialAuthException(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
