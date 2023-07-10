package com.maple.task.controller;

import com.maple.common.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;

/**
 * @author 陈其丰
 */

@RequestMapping("/mail")
@RestController
public class MailController {

    @Autowired
    private JavaMailSenderImpl javaMailSender;

    @Value("${spring.mail.username}")
    private String mailName;

    @GetMapping("/send")
    public Result send(){
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setSubject("spring 邮件主题");
        mailMessage.setText("spring 邮件内容");
        mailMessage.setFrom(mailName); // 谁发的
        mailMessage.setTo(mailName); // 发给我自己
        javaMailSender.send(mailMessage);
        return Result.success();
    }

    @GetMapping("/mimeSend")
    public Result mimeSend() throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        helper.setSubject("复杂邮件主题");
        helper.setText("<div>html格式的正文</div>",true);
        helper.addAttachment("附件名.jpg",new File("filepath"));
        helper.setFrom(mailName); // 谁发的
        helper.setTo(mailName); // 发给我自己
        javaMailSender.send(mimeMessage);
        return Result.success();
    }
}
