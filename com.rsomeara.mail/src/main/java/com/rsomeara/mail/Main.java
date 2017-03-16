package com.rsomeara.mail;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Program which sends an email to a given address, from the same address, via a mail relay
 *
 * <p>
 * Demonstrates use of the Java Mail APIs
 *
 * @author romeara
 */
public class Main {

    private static final String SMTP_HOST_KEY = "mail.smtp.host";

    @Option(name = "--relay", usage = "Address of the mail relay to send mail from", required = true)
    private String relayAddress;

    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    private void run(String args[]) throws Exception {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println();
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            return;
        }

        ConsoleCallbackHandler callback = new ConsoleCallbackHandler();
        NameCallback emailCallback = new NameCallback("Email: ");

        callback.handle(new Callback[] { emailCallback });

        String email = emailCallback.getName();

        sendMessage(relayAddress, email);
    }

    private void sendMessage(String relayAddress, String email) throws AddressException, MessagingException {
        // Get system properties
        Properties props = System.getProperties();

        // Setup mail server
        props.put(SMTP_HOST_KEY, relayAddress);

        // Get session
        Session session = Session.getInstance(props, null);

        // Define message
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(email));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
        message.setSubject("Hello HTML JavaMail");
        message.setContent(getMessageContent(), "text/html; charset=utf-8");

        // Send message
        Transport.send(message);
    }

    private String getMessageContent() {
        StringBuilder builder = new StringBuilder();

        builder.append("<!DOCTYPE html>");
        builder.append("<html lang=\"en\">");
        builder.append("<head>");
        builder.append("<meta charset=\"utf-8\">");
        builder.append("<title>title</title>");
        builder.append("</head>");
        builder.append("<body>");
        builder.append("<h1>Welcome to JavaMail</h1>");
        builder.append("<p>This is mail with HTML formatted content</p>");
        builder.append("</body>");
        builder.append("</html>");

        return builder.toString();
    }
}
