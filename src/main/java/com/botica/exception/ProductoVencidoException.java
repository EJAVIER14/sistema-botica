package com.botica.exception;

import java.time.LocalDate;

public class ProductoVencidoException extends RuntimeException {
    public ProductoVencidoException(String nombreProducto, LocalDate fechaVencimiento) {
        super("No se puede vender '" + nombreProducto + "': el producto venció el " + fechaVencimiento);
    }
}