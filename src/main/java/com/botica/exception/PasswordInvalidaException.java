package com.botica.exception;

public class PasswordInvalidaException extends RuntimeException {
    public PasswordInvalidaException(String motivo) {
        super("Contraseña inválida: " + motivo);
    }
}