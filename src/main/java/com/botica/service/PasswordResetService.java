package com.botica.service;

import com.botica.exception.EmailNoEncontradoException;
import com.botica.exception.PasswordInvalidaException;
import com.botica.exception.TokenExpiradoException;
import com.botica.exception.TokenInvalidoException;
import com.botica.model.PasswordResetToken;
import com.botica.model.Usuario;
import com.botica.repository.PasswordResetTokenRepository;
import com.botica.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@Transactional
public class PasswordResetService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PasswordResetTokenRepository tokenRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${resend.api-key}")
    private String resendApiKey;

    private static final String RESEND_URL = "https://api.resend.com/emails";
    private static final String REMITENTE = "onboarding@resend.dev";

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();

    private static final Pattern TIENE_LETRA = Pattern.compile(".*[A-Za-z].*");
    private static final Pattern TIENE_NUMERO = Pattern.compile(".*\\d.*");
    private static final int MINUTOS_VALIDEZ_CODIGO = 10;

    public void solicitarCodigo(String email) {
        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Solicitud de codigo para un correo no registrado: {}", email);
                    return new EmailNoEncontradoException("No existe una cuenta asociada a ese correo");
                });

        String codigo = generarCodigo();
        PasswordResetToken resetToken = new PasswordResetToken(
                codigo, usuario.getId(), LocalDateTime.now().plusMinutes(MINUTOS_VALIDEZ_CODIGO));
        tokenRepo.save(resetToken);
        logger.debug("Codigo generado para usuarioId={}, expira en {} minutos", usuario.getId(), MINUTOS_VALIDEZ_CODIGO);

        enviarCorreo(email, codigo);
        logger.info("Codigo de recuperacion enviado correctamente a {}", email);
    }

    public void restablecerConCodigo(String email, String codigo, String nuevaPassword) {
        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Intento de restablecer password con correo no registrado: {}", email);
                    return new TokenInvalidoException("Correo o código inválido");
                });

        PasswordResetToken resetToken = tokenRepo.findByToken(codigo)
                .orElseThrow(() -> {
                    logger.warn("Codigo invalido ingresado para email={}", email);
                    return new TokenInvalidoException("El código ingresado no es válido");
                });

        if (!resetToken.getUsuarioId().equals(usuario.getId())) {
            logger.warn("Codigo no corresponde al correo indicado: email={}", email);
            throw new TokenInvalidoException("El código no corresponde a este correo");
        }
        if (resetToken.isUsado()) {
            logger.warn("Intento de reutilizar un codigo ya usado: email={}", email);
            throw new TokenInvalidoException("Este código ya fue utilizado");
        }
        if (resetToken.estaExpirado()) {
            logger.warn("Codigo expirado usado por email={}", email);
            throw new TokenExpiradoException("El código ha expirado, solicita uno nuevo");
        }

        validarPassword(nuevaPassword);

        usuario.setPassword(encoder.encode(nuevaPassword));
        usuarioRepo.save(usuario);

        resetToken.setUsado(true);
        tokenRepo.save(resetToken);
        logger.info("Contraseña restablecida exitosamente via codigo para email={}", email);
    }

    private String generarCodigo() {
        int numero = 100000 + random.nextInt(900000);
        return String.valueOf(numero);
    }

    private void enviarCorreo(String email, String codigo) {
        Map<String, Object> body = new HashMap<>();
        body.put("from", REMITENTE);
        body.put("to", List.of(email));
        body.put("subject", "Sistema Botica - Código de recuperación");
        body.put("text", "Hola,\n\nTu código para restablecer la contraseña es:\n\n" +
                codigo + "\n\n" +
                "Este código es válido por " + MINUTOS_VALIDEZ_CODIGO + " minutos.\n" +
                "Si no solicitaste esto, ignora este correo.\n\nSistema Botica");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(RESEND_URL, request, String.class);
        } catch (RestClientException e) {
            logger.error("Error critico: fallo el envio del codigo de recuperacion a {}", email, e);
            throw new RuntimeException("Error al enviar el código de recuperación", e);
        }
    }

    private void validarPassword(String password) {
        if (password == null || password.length() < 8) {
            throw new PasswordInvalidaException("debe tener al menos 8 caracteres");
        }
        if (!TIENE_LETRA.matcher(password).matches()) {
            throw new PasswordInvalidaException("debe contener al menos una letra");
        }
        if (!TIENE_NUMERO.matcher(password).matches()) {
            throw new PasswordInvalidaException("debe contener al menos un número");
        }
    }
}