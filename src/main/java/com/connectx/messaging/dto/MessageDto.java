package com.connectx.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {

    private Long id;
    private Long channelId;
    private Long senderId;
    private String senderName;
    private String senderEmail;
    private String content;
    private String messageType;
    private String fileUrl;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
}
