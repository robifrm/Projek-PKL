package com.vnet.vnet_backend.controller;

import com.vnet.vnet_backend.dto.auth.AuthMessageResponse;
import com.vnet.vnet_backend.dto.auth.AuthResponse;
import com.vnet.vnet_backend.dto.auth.ForgotPasswordRequest;
import com.vnet.vnet_backend.dto.auth.LoginRequest;
import com.vnet.vnet_backend.dto.auth.RegisterRequest;
import com.vnet.vnet_backend.dto.auth.ResetPasswordRequest;
import com.vnet.vnet_backend.dto.auth.VerifyOtpRequest;
import com.vnet.vnet_backend.service.AuthService;
import com.vnet.vnet_backend.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final CaptchaService captchaService;
    private final AuthService authService;

    @GetMapping("/captcha")
    public ResponseEntity<Map<String, String>> getCaptcha() {
        return ResponseEntity.ok(captchaService.generateCaptcha());
    }

    @PostMapping("/register")
    public ResponseEntity<AuthMessageResponse> register(@RequestBody RegisterRequest request) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Fitur registrasi dinonaktifkan");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthMessageResponse> verifyOtp(@RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(authService.login(request, userAgent, getClientIp(servletRequest)));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthMessageResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthMessageResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @PostMapping("/register-init")
    public ResponseEntity<Map<String, Object>> registerInit(@RequestBody Map<String, Object> payload) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Fitur registrasi dinonaktifkan");
    }

    @PostMapping("/register-confirm")
    public ResponseEntity<Map<String, Object>> registerConfirm(@RequestBody Map<String, Object> payload) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Fitur registrasi dinonaktifkan");
    }


    @PostMapping("/login-init")
    public ResponseEntity<AuthResponse> loginInit(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            HttpServletRequest servletRequest) {
        validateCaptchaIfPresent(payload);

        LoginRequest request = new LoginRequest();
        request.setEmail(firstText(payload, "email", "username"));
        request.setPassword(firstText(payload, "password"));
        return ResponseEntity.ok(authService.login(request, userAgent, getClientIp(servletRequest)));
    }

    @PostMapping("/login-confirm")
    public ResponseEntity<Map<String, String>> loginConfirm() {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP tidak digunakan saat login");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", ex.getReason());
        body.put("message", ex.getReason());
        body.put("status", ex.getStatusCode().value());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", ex.getMessage());
        body.put("message", ex.getMessage());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(body);
    }

    private void validateCaptchaIfPresent(Map<String, Object> payload) {
        boolean hasCaptcha = payload.containsKey("captchaId") || payload.containsKey("captchaAnswer");
        if (!hasCaptcha) {
            return;
        }

        String captchaId = firstText(payload, "captchaId");
        String captchaAnswer = firstText(payload, "captchaAnswer");
        if (!captchaService.validateCaptcha(captchaId, captchaAnswer)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kode Captcha salah atau sudah kedaluwarsa");
        }
    }

    private String firstText(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Object value = payload.get(key);
            if (value != null && !value.toString().isBlank()) {
                return value.toString();
            }
        }
        return null;
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Logout berhasil");
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private Map<String, Object> responseBody(AuthMessageResponse response) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", response.getMessage());
        body.put("email", response.getEmail());
        body.put("expiresInMinutes", response.getExpiresInMinutes());
        body.put("verified", response.getVerified());
        return body;
    }
}
