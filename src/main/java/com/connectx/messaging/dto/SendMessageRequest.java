package com.connectx.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotNull(message = "Channel ID is required")
    private Long channelId;

    @NotBlank(message = "Content cannot be blank")
    private String content;

    private String messageType = "TEXT";

    private String fileUrl;
}
