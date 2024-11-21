package com.example.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender javaMailSender;

    // Método para enviar correo
    public void enviarCorreo(String to, String subject, String text) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom("jaispaqui@gmail.com");  // Remitente fijo
            helper.setTo(to);                      // Destinatario (email del usuario registrado)
            helper.setSubject(subject);            // Asunto
            helper.setText(text, true);            // Contenido del correo

            // Enviar correo
            javaMailSender.send(mimeMessage);
        } catch (MessagingException | MailException e) {
            e.printStackTrace();  // Manejo del error de envío
        }
    }
}
