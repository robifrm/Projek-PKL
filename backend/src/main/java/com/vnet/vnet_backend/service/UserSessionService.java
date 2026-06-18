package com.vnet.vnet_backend.service;

import com.vnet.vnet_backend.entity.User;
import com.vnet.vnet_backend.entity.UserSession;
import com.vnet.vnet_backend.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserSessionRepository userSessionRepository;

    @Transactional
    public void createSession(User user, String token, String userAgent, String ipAddress) {
        String device = parseUserAgent(userAgent);
        
        UserSession session = UserSession.builder()
                .user(user)
                .token(token)
                .device(device)
                .ipAddress(ipAddress != null ? ipAddress : "Unknown IP")
                .lastActive(LocalDateTime.now())
                .build();
                
        userSessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public boolean isTokenValid(String token) {
        return userSessionRepository.existsByToken(token);
    }

    @Transactional
    public void updateLastActive(String token) {
        userSessionRepository.findByToken(token).ifPresent(session -> {
            LocalDateTime now = LocalDateTime.now();
            if (session.getLastActive() == null || session.getLastActive().isBefore(now.minusMinutes(1))) {
                session.setLastActive(now);
                userSessionRepository.save(session);
            }
        });
    }

    @Transactional
    public void updateSessionToken(String oldToken, String newToken) {
        userSessionRepository.findByToken(oldToken).ifPresent(session -> {
            session.setToken(newToken);
            session.setLastActive(LocalDateTime.now());
            userSessionRepository.save(session);
        });
    }

    @Transactional(readOnly = true)
    public List<UserSessionResponse> getActiveSessions(User user, String currentToken) {
        List<UserSession> sessions = userSessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return sessions.stream().map(s -> {
            boolean isCurrent = s.getToken().equals(currentToken);
            String timeStr = formatLastActive(s.getLastActive());
            return new UserSessionResponse(
                    s.getId(),
                    s.getDevice(),
                    s.getIpAddress(),
                    timeStr,
                    isCurrent
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public void revokeSession(Long sessionId, User user) {
        userSessionRepository.findById(sessionId).ifPresent(session -> {
            if (session.getUser().getId().equals(user.getId())) {
                userSessionRepository.delete(session);
            }
        });
    }

    @Transactional
    public void revokeSessionByToken(String token) {
        userSessionRepository.findByToken(token).ifPresent(userSessionRepository::delete);
    }

    private String parseUserAgent(String ua) {
        if (ua == null || ua.isBlank()) {
            return "Unknown Device";
        }
        
        String os = "Unknown OS";
        if (ua.contains("Windows")) {
            os = "Windows";
        } else if (ua.contains("iPhone") || ua.contains("iPad")) {
            os = "iPhone/iPad";
        } else if (ua.contains("Android")) {
            os = "Android";
        } else if (ua.contains("Macintosh") || ua.contains("Mac OS X")) {
            os = "MacOS";
        } else if (ua.contains("Linux")) {
            os = "Linux";
        }

        String browser = "Browser";
        if (ua.contains("Edge") || ua.contains("Edg")) {
            browser = "Edge";
        } else if (ua.contains("Chrome") || ua.contains("CriOS")) {
            browser = "Chrome";
        } else if (ua.contains("Firefox") || ua.contains("FxiOS")) {
            browser = "Firefox";
        } else if (ua.contains("Safari")) {
            browser = "Safari";
        } else if (ua.contains("Opera") || ua.contains("OPR")) {
            browser = "Opera";
        }

        return browser + " on " + os;
    }

    private String formatLastActive(LocalDateTime lastActive) {
        if (lastActive == null) {
            return "Unknown";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return lastActive.format(formatter);
    }

    @lombok.Value
    public static class UserSessionResponse {
        Long id;
        String device;
        String ip;
        String time;
        boolean current;
    }
}
