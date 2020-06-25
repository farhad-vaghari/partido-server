package net.fosforito.partido.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Map;

@Component
public class EmailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);
  private static final String DEFAULT_SENDER_ADDRESS = "no-reply@partido.rocks";
  private final JavaMailSender emailSender;
  private final SpringTemplateEngine thymeleafTemplateEngine;

  @Value("classpath:/mail/logo.png")
  Resource resourceFile;

  public EmailService(JavaMailSender emailSender, SpringTemplateEngine thymeleafTemplateEngine) {
    this.emailSender = emailSender;
    this.thymeleafTemplateEngine = thymeleafTemplateEngine;
  }

  public void sendSimpleMessage(String to, String subject, String text) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(DEFAULT_SENDER_ADDRESS);
    message.setTo(to);
    message.setSubject(subject);
    message.setText(text);
    emailSender.send(message);
  }

  public void sendMessageWithAttachment(String to, String subject, String text, String pathToAttachment) {
    try {
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setFrom(DEFAULT_SENDER_ADDRESS);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(text);
      FileSystemResource file = new FileSystemResource(new File(pathToAttachment));
      helper.addAttachment("Attachment", file);
      emailSender.send(message);
    } catch (MessagingException e) {
      e.printStackTrace();
    }
  }

  public void sendEmailVerificationMail(String to, Map<String, Object> templateModel) {
    Context thymeleafContext = new Context();
    thymeleafContext.setVariables(templateModel);
    String htmlBody = thymeleafTemplateEngine.process("email-verification-mail.html", thymeleafContext);
    try {
      sendHtmlMessage(to, "Partido | Verify your email address", htmlBody);
    } catch (MessagingException e) {
      LOGGER.error("Failed to send email verification mail to {}", to, e);
    }
  }

  private void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException {
    MimeMessage message = emailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
    helper.setFrom(DEFAULT_SENDER_ADDRESS);
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(htmlBody, true);
    helper.addInline("logo.png", resourceFile);
    emailSender.send(message);
  }
}
