package model.user_util;

import controllers.LoginController;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.Executors;

/**
 * Util for sending email
 */
public class EmailUtil {

    private static final String EMAIL_FROM = "mail.stroodie@gmail.com";
    private static final String EMAIL_PASSWORD = "s1t2r3o4o5d6i7e8";

    private static void sendEmail(final String address, final String header, final String content) {

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                String host = "smtp.gmail.com";
                Properties props = new Properties();
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.host", host);
                props.put("mail.smtp.port", "587");
                Session session;
                try {
                    session = Session.getInstance(props,
                            new javax.mail.Authenticator() {
                                protected PasswordAuthentication getPasswordAuthentication() {
                                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                                }
                            });
                }
                catch (Exception e) {
                    return;
                }

                try {
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(EMAIL_FROM));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(address));
                    message.setSubject(header);
                    message.setContent(content, "text/html; charset=windows-1251");
                    Transport.send(message);

                } catch (MessagingException e) {
                    //
                }
            }
        });
    }

    public static void sendAccountActivationEmail(String address, String name, String activateUrl) {
        sendEmail(address, "Stroodie: активация аккаунта", getAccountActivationEmailContent(name, activateUrl));
    }

    private static String getAccountActivationEmailContent(String name, String activateUrl) {
        return "Здравствуйте, " + name + "!<br><br>Вы успешно зарегистрировались в системе Stroodie.<br>" +
                "Для подтверждения регистрации <a href=\"" + activateUrl + "\">нажмите сюда</a>. Ссылка действует " +
                "в течение " + Integer.toString(LoginController.DAYS_OF_TOKEN_EXPIRATION) + " дней.<br><br>Успехов!";
    }

    public static void sendPasswordRecoveryEmail(String address, String name, String activateUrl) {
        sendEmail(address, "Stroodie: восстановление пароля", getPasswordRecoveryEmailContent(name, activateUrl));
    }

    private static String getPasswordRecoveryEmailContent(String name, String activateUrl) {
        return "Здравствуйте, " + name + "!<br><br>От Вас получен запрос на восстановление пароля.<br>" +
                "Для восстановления <a href=\"" + activateUrl + "\">нажмите сюда</a>. Ссылка действует " +
                "в течение " + Integer.toString(LoginController.DAYS_OF_TOKEN_EXPIRATION) + " дней.<br>" +
                "Если Вы не запрашивали восстановление пароля, просто проигнорируйте это письмо.<br><br>Успехов!";
    }
}
