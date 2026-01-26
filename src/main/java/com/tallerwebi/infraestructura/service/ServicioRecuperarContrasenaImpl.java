package com.tallerwebi.infraestructura.service;

import com.tallerwebi.dominio.model.Usuario;
import com.tallerwebi.dominio.repository.RepositorioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Properties;
import java.util.Random;

@Service
public class ServicioRecuperarContrasenaImpl {

    private final RepositorioUsuario usuarioRepositorio;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public ServicioRecuperarContrasenaImpl(RepositorioUsuario usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Transactional
    public String generarTokenRecuperacion(String email) throws MessagingException {
        System.out.println(email + " EMAIL");
        Usuario usuario = usuarioRepositorio.buscar(email);
        System.out.println(usuario + " usuario");
        if (usuario == null) {
            throw new MessagingException("Usuario no encontrado");
        }

        String tokenRecuperacion = generarCodigoAleatorioDe5Digitos();
        usuarioRepositorio.guardarTokenDeRecuperacion(usuario, tokenRecuperacion);
        enviarCorreo(email, tokenRecuperacion);

        return tokenRecuperacion;
    }

    @Transactional
    public String verificarCodigoDeRecuperacion(String codigo, String email) throws MessagingException {
        Usuario usuario = usuarioRepositorio.buscar(email);

        if (usuario == null) {
            throw new MessagingException("Usuario no encontrado");
        }

        if (usuario.getTokenRecuperacion().equals(codigo)) {
            return "Codigo correcto";
        } else {
            throw new MessagingException("Codigo incorrecto");
        }

    }

    @Transactional
    public String confirmacionAmbasContrasenas(String contrasena, String confirmacionContrasena, String email) throws MessagingException {
        Usuario usuario = usuarioRepositorio.buscar(email);
        if (contrasena.equals(confirmacionContrasena)) {
            usuario.setPassword(contrasena);
            usuario.setTokenRecuperacion(null);

            entityManager.merge(usuario);

            return "Contraseña modificada correctamente";
        } else {
            throw new MessagingException("Las contraseñas no coinciden.");
        }
    }

    public String generarCodigoAleatorioDe5Digitos() {
        Random random = new Random();
        int codigo = 10000 + random.nextInt(90000);
        return String.valueOf(codigo);
    }
    public void enviarCorreo(String email, String token) throws MessagingException {
        String to = email;
        String from = "arganarazalan603@gmail.com";

        final String username = "arganarazalan603";
        final String password = "qdai yrfi zbiu kgmn";

        String host = "smtp.gmail.com";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject("Recuperación de Contraseña");
        message.setText("Tu código de recuperación es: " + token);

        Transport.send(message);
    }
}
