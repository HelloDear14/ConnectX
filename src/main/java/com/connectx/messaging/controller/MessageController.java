package com.connectx.messaging.controller;

import com.connectx.messaging.dto.MessageDto;
import com.connectx.messaging.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/history/{channelId}")
    public ResponseEntity<Page<MessageDto>> getHistory(
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(messageService.getHistory(channelId, page, size));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId,
                                               Authentication authentication) {
        Long requesterId = (Long) authentication.getPrincipal();
        messageService.deleteMessage(messageId, requesterId);
        return ResponseEntity.noContent().build();
    }
}
