package com.botica.exception;

public class StockInvalidoException extends RuntimeException {
    public StockInvalidoException(Integer stock) {
        super("El stock no puede ser negativo. Valor recibido: " + stock);
    }
}