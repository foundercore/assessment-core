package com.assessment.common;

import lombok.extern.slf4j.Slf4j;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

@Slf4j
public class AmazonSESUtility {
    static final String FROMNAME = "Assessment";

    /*  ses credentails */
    private static final String FROM = "xxx@gmail.com";
    private static final String SMTP_USERNAME = "";
    private static final String SMTP_PASSWORD = "";
    private static final String HOST = "email-smtp.ap-south-1.amazonaws.com";
    private static final int PORT = 587;

    public static void sendMailNotification(String subject, String body, List<String> to, List<String> cc, List<String> bcc) throws UnsupportedEncodingException, MessagingException {

        String mode = ConfigUtility.instance().getProperty("app.notification.mode", true);
        if (!"enabled".equalsIgnoreCase(mode)){
            log.info("App Notification mode disabled. To get notification, set property `app.notification.mode` to `enabled`");
            return;
        }
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        // Create a Session object to represent a mail session with the specified properties.
        Session session = Session.getDefaultInstance(props);

        // Create a message with the specified information.
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM,FROMNAME));
        if (to != null) {
            for (String r : to) {
                msg.addRecipient(Message.RecipientType.TO, new InternetAddress(r));
            }
        }
        if (cc != null){
            for (String r: cc){
                msg.addRecipient(Message.RecipientType.CC, new InternetAddress(r));
            }
        }
        if (bcc != null){
            for (String r: bcc){
                msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(r));
            }
        }
        msg.setSubject(subject);
        msg.setContent(body,"text/html");

        Transport transport = session.getTransport();
        try {
            log.info("Sending Email...");
            transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);
            transport.sendMessage(msg, msg.getAllRecipients());
            log.info("Email Sent!");
        }catch (Exception e){
            log.error(e.getMessage(), e);
            throw e;
        }finally {
            transport.close();
        }
    }
}
