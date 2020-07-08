package com.kreinto.toolbox.util;

import com.sun.mail.smtp.SMTPTransport;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Date;
import java.util.Properties;

public class EmailSender {

    public static final String SMTP_SERVER_PROP = "app.config.mail.smtp.server";
    public static final String SMTP_PORT_PROP = "app.config.mail.smtp.port";
    public static final String LOGIN_PROP = "app.config.mail.login";
    public static final String PASSWORD_PROP = "app.config.mail.password";

    public static final String ADMIN_EMAIL_PROP = "app.config.admin.email";

    public static void sendPlainEmail(String to, String cc, String subject, String body) {

        Properties props = System.getProperties();
        props = FileUtil.loadProperties("my.properties");
        props.put("mail.smtp.host", props.getProperty(SMTP_SERVER_PROP));
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.port", props.getProperty(SMTP_PORT_PROP));
        props.put("mail.smtp.ssl.enable", true);
        props.put("mail.smtp.starttls.enable", true);

        // props.put("mail.smtp.socketFactory.port", SMTP_PORT);
        // props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        props.put("mail.smtp.debug", true);

        Session session = Session.getDefaultInstance(props);
        session.setDebug(true);

        try {
            Message msg = new MimeMessage(session);

            // from
            msg.setFrom(new InternetAddress(props.getProperty(LOGIN_PROP)));

            // to
            msg.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to, false));

            // cc
            if (cc != null && !cc.isEmpty()) {
                msg.setRecipients(Message.RecipientType.CC,
                        InternetAddress.parse(cc, false));
            }
            // subject
            msg.setSubject(subject);

            // content
            msg.setText(body);

            msg.setSentDate(new Date());

            // Get SMTPTransport
            SMTPTransport t = (SMTPTransport) session.getTransport("smtp");

            // connect
            t.connect(props.getProperty(SMTP_SERVER_PROP), props.getProperty(LOGIN_PROP), props.getProperty(PASSWORD_PROP));

            // send
            t.sendMessage(msg, msg.getAllRecipients());

            System.out.println("Response: " + t.getLastServerResponse());

            t.close();

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
