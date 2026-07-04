package com.botica.exception;

public class NombreInvalidoException extends RuntimeException {
    public NombreInvalidoException() {
        super("El nombre del producto no puede estar vacío");
    }
}