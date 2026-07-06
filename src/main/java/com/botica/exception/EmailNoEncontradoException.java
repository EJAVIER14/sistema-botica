package com.botica.exception;

public class EmailNoEncontradoException extends RuntimeException {
    public EmailNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}