package com.botica.exception;

public class PrecioInvalidoException extends RuntimeException {
    public PrecioInvalidoException(Double precio) {
        super("El precio debe ser mayor a cero. Valor recibido: " + precio);
    }
}