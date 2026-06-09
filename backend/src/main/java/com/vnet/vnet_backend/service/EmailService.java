package com.vnet.vnet_backend.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final Resend resend;

    @Value("${resend.from-email:VNet <onboarding@resend.dev>}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otpCode, long expiresInMinutes, String purpose) {
        String htmlContent = buildOtpTemplate(otpCode, expiresInMinutes, purpose);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject("VNet - Kode OTP")
                .html(htmlContent)
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(params);
            log.info("Email sent successfully. ID: {}", data.getId());
        } catch (ResendException e) {
            log.error("Failed to send OTP email", e);
            throw new RuntimeException("Failed to send email via Resend");
        }
    }

    private String buildOtpTemplate(String otpCode, long expiresInMinutes, String purpose) {
        String safePurpose = escapeHtml(purpose == null ? "Verifikasi akun" : purpose);
        String safeOtp = escapeHtml(otpCode);

        return """
                <div style="font-family:Arial,sans-serif;max-width:560px;margin:0 auto;padding:24px;background:#f8fafc;color:#0f172a;">
                  <div style="background:#ffffff;border:1px solid #e2e8f0;border-radius:8px;padding:28px;">
                    <h2 style="margin:0 0 12px;font-size:22px;">VNet Authentication</h2>
                    <p style="margin:0 0 18px;line-height:1.6;">Gunakan kode OTP berikut untuk %s.</p>
                    <div style="font-size:32px;font-weight:700;letter-spacing:8px;text-align:center;padding:16px;background:#ecfeff;border-radius:8px;color:#0f766e;">%s</div>
                    <p style="margin:18px 0 0;line-height:1.6;">Kode ini berlaku selama <strong>%d menit</strong>. Jangan bagikan kode ini kepada siapa pun.</p>
                  </div>
                </div>
                """.formatted(safePurpose, safeOtp, expiresInMinutes);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
