package com.example.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "MAIL-SERVICE")
public class MailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("[Super App] Mã xác thực email");
            helper.setText(buildEmailContent(otp), true);

            mailSender.send(message);
            log.info("OTP email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildEmailContent(String otp) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto;">
                    <h2>Xác thực email của bạn</h2>
                    <p>Mã OTP của bạn là:</p>
                    <h1 style="letter-spacing: 8px; color: #e63946;">%s</h1>
                    <p>Mã có hiệu lực trong <strong>10 phút</strong>.</p>
                    <p>Nếu bạn không yêu cầu mã này, hãy bỏ qua email này.</p>
                </div>
                """.formatted(otp);
    }

    public void sendApprovalEmail(String to) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("[Super App] Đơn đăng ký tài xế đã được duyệt");
            helper.setText("""
            <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto;">
                <h2>Chúc mừng! Đơn đăng ký tài xế của bạn đã được duyệt.</h2>
                <p>Bạn có thể đăng nhập và bắt đầu nhận chuyến ngay bây giờ.</p>
            </div>
        """, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send approval email to {}: {}", to, e.getMessage());
        }
    }

    public void sendRejectionEmail(String to, String reason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("[Super App] Đơn đăng ký tài xế không được duyệt");
            helper.setText("""
            <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto;">
                <h2>Đơn đăng ký tài xế của bạn không được duyệt.</h2>
                <p>Lý do: <strong>%s</strong></p>
                <p>Bạn có thể nộp đơn lại sau khi khắc phục vấn đề.</p>
            </div>
        """.formatted(reason), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send rejection email to {}: {}", to, e.getMessage());
        }
    }
}