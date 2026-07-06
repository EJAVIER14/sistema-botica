package com.botica.exception;

public class TokenExpiradoException extends RuntimeException {
    public TokenExpiradoException(String mensaje) {
        super(mensaje);
    }
}