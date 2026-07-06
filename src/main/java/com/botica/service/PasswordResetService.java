package com.botica.service;

import com.botica.exception.EmailNoEncontradoException;
import com.botica.exception.PasswordInvalidaException;
import com.botica.exception.TokenExpiradoException;
import com.botica.exception.TokenInvalidoException;
import com.botica.model.PasswordResetToken;
import com.botica.model.Usuario;
import com.botica.repository.PasswordResetTokenRepository;
import com.botica.repository.UsuarioRepository;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional
public class PasswordResetService {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PasswordResetTokenRepository tokenRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final String RESEND_URL = "https://api.resend.com/emails";
    private static final String REMITENTE = "onboarding@resend.dev";

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private static final Pattern TIENE_LETRA = Pattern.compile(".*[A-Za-z].*");
    private static final Pattern TIENE_NUMERO = Pattern.compile(".*\\d.*");
    private static final int HORAS_VALIDEZ_TOKEN = 1;

    public void solicitarReset(String email) {
        Usuario usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new EmailNoEncontradoException(
                        "No existe una cuenta asociada a ese correo"));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(
                token, usuario.getId(), LocalDateTime.now().plusHours(HORAS_VALIDEZ_TOKEN));
        tokenRepo.save(resetToken);

        enviarCorreo(email, token);
    }

    public void restablecerPassword(String token, String nuevaPassword) {
        PasswordResetToken resetToken = tokenRepo.findByToken(token)
                .orElseThrow(() -> new TokenInvalidoException("El enlace de recuperación no es válido"));

        if (resetToken.isUsado()) {
            throw new TokenInvalidoException("Este enlace ya fue utilizado");
        }
        if (resetToken.estaExpirado()) {
            throw new TokenExpiradoException("El enlace de recuperación ha expirado");
        }

        validarPassword(nuevaPassword);

        Usuario usuario = usuarioRepo.findById(resetToken.getUsuarioId())
                .orElseThrow(() -> new TokenInvalidoException("Usuario no encontrado"));

        usuario.setPassword(encoder.encode(nuevaPassword));
        usuarioRepo.save(usuario);

        resetToken.setUsado(true);
        tokenRepo.save(resetToken);
    }

    private void enviarCorreo(String email, String token) {
        String enlace = baseUrl + "/restablecer-password?token=" + token;

        Map<String, Object> body = new HashMap<>();
        body.put("from", REMITENTE);
        body.put("to", List.of(email));
        body.put("subject", "Sistema Botica - Recuperación de contraseña");
        body.put("text", "Hola,\n\nRecibimos una solicitud para restablecer tu contraseña.\n" +
                "Haz clic en el siguiente enlace (válido por " + HORAS_VALIDEZ_TOKEN + " hora):\n\n" +
                enlace + "\n\n" +
                "Si no solicitaste esto, ignora este correo.\n\nSistema Botica");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(RESEND_URL, request, String.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Error al enviar el correo de recuperación", e);
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