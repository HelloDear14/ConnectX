package com.connectx.messaging.service;

import com.connectx.messaging.config.KafkaProducerConfig;
import com.connectx.messaging.dto.ChatMessageEvent;
import com.connectx.messaging.dto.MessageDto;
import com.connectx.messaging.dto.SendMessageRequest;
import com.connectx.messaging.entity.Message;
import com.connectx.messaging.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public MessageDto sendMessage(Long senderId, String senderName, String senderEmail,
                                  SendMessageRequest req) {
        Message message = Message.builder()
                .channelId(req.getChannelId())
                .senderId(senderId)
                .senderName(senderName)
                .senderEmail(senderEmail)
                .content(req.getContent())
                .messageType(req.getMessageType() != null ? req.getMessageType() : "TEXT")
                .fileUrl(req.getFileUrl())
                .build();

        Message saved = messageRepository.save(message);

        // Add sender to Redis channel members set
        String membersKey = "channel:members:" + req.getChannelId();
        redisTemplate.opsForSet().add(membersKey, senderId.toString());

        // Publish to Kafka
        ChatMessageEvent event = ChatMessageEvent.builder()
                .id(saved.getId())
                .channelId(saved.getChannelId())
                .senderId(saved.getSenderId())
                .senderName(saved.getSenderName())
                .content(saved.getContent())
                .messageType(saved.getMessageType())
                .createdAt(saved.getCreatedAt())
                .build();

        kafkaTemplate.send(KafkaProducerConfig.CHAT_MESSAGES_TOPIC,
                saved.getChannelId().toString(), event);

        log.debug("Message saved id={} channelId={}", saved.getId(), saved.getChannelId());

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<MessageDto> getHistory(Long channelId, int page, int size) {
        return messageRepository
                .findByChannelIdAndDeletedFalseOrderByCreatedAtDesc(
                        channelId, PageRequest.of(page, size))
                .map(this::toDto);
    }

    @Transactional
    public void deleteMessage(Long messageId, Long requesterId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        if (!message.getSenderId().equals(requesterId)) {
            throw new IllegalStateException("Not authorised to delete this message");
        }
        message.setDeleted(true);
        messageRepository.save(message);
    }

    private MessageDto toDto(Message m) {
        return MessageDto.builder()
                .id(m.getId())
                .channelId(m.getChannelId())
                .senderId(m.getSenderId())
                .senderName(m.getSenderName())
                .senderEmail(m.getSenderEmail())
                .content(m.getContent())
                .messageType(m.getMessageType())
                .fileUrl(m.getFileUrl())
                .createdAt(m.getCreatedAt())
                .editedAt(m.getEditedAt())
                .build();
    }
}
