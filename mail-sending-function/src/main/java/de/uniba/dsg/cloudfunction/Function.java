package de.uniba.dsg.cloudfunction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class Function implements BackgroundFunction<Event> {
    private static final Logger logger = Logger.getLogger(Function.class.getName());

    @Override
    public void accept(Event event, Context context) throws MessagingException, IOException {

        String mail = event.getMetadata().get("email");
        String bucket = event.getBucket();
        String fileName = event.getName();

        sendMail(mail, bucket, fileName);
    }

    private void sendMail(String mailTo, String bucket, String fileName) throws MessagingException, IOException {

        Storage storage = StorageOptions.getDefaultInstance().getService();
        Bucket b = storage.get(bucket);
        b.get(fileName).downloadTo(Paths.get("/tmp/" + fileName));

        Session session = initializeMailing();
        Message message = generateMessage(session, fileName, mailTo);

        Transport.send(message);
    }

    private Message generateMessage(Session session, String fileName, String mailTo) throws MessagingException, IOException {
        Message message = new MimeMessage(session);
        // TODO add the mail which corresponds to the PasswordAuthentication
        message.setFrom(new InternetAddress("dsamprojekt@gmail.com"));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(mailTo));
        message.setSubject("Demanded Beverage Information");

        String msg = "Hi,<br/><br/>" +
                "attached you find the Beverage Information you demanded. Have fun with it :)<br/><br/>" +
                "Kind regards,<br/>" +
                "DSAM Group 15 :)";

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msg, "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        attachmentBodyPart.attachFile(new File("/tmp/" + fileName));

        multipart.addBodyPart(attachmentBodyPart);

        message.setContent(multipart);

        return message;
    }

    private Session initializeMailing() {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");




        return Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // TODO add user name (baXXXX) and your corresponding password
                return new PasswordAuthentication("dsamprojekt@gmail.com", "dsam2020");
            }
        });
    }
}