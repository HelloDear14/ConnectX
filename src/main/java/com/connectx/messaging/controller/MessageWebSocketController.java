package com.connectx.messaging.controller;

import com.connectx.messaging.dto.MessageDto;
import com.connectx.messaging.dto.SendMessageRequest;
import com.connectx.messaging.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MessageWebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate redisTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request,
                            SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            log.warn("Unauthenticated WebSocket message dropped");
            return;
        }

        Long senderId = extractUserId(principal);
        String senderEmail = extractEmail(principal);
        // Derive senderName from email (before @) as a fallback
        String senderName = senderEmail != null
                ? senderEmail.split("@")[0]
                : "Unknown";

        MessageDto dto = messageService.sendMessage(senderId, senderName, senderEmail, request);

        messagingTemplate.convertAndSend(
                "/topic/channels/" + dto.getChannelId(), dto);
    }

    @MessageMapping("/chat.join")
    public void joinChannel(@Payload SendMessageRequest request,
                            SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            return;
        }

        Long userId = extractUserId(principal);
        Long channelId = request.getChannelId();
        if (userId != null && channelId != null) {
            String membersKey = "channel:members:" + channelId;
            redisTemplate.opsForSet().add(membersKey, userId.toString());
            log.debug("User {} joined channel {}", userId, channelId);
        }
    }

    private Long extractUserId(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken token) {
            Object p = token.getPrincipal();
            if (p instanceof Long) return (Long) p;
            if (p instanceof Number) return ((Number) p).longValue();
        }
        return null;
    }

    private String extractEmail(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken token) {
            Object cred = token.getCredentials();
            if (cred instanceof String) return (String) cred;
        }
        return null;
    }
}
