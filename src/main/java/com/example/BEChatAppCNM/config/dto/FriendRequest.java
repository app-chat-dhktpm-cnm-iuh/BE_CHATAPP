package com.example.BEChatAppCNM.config.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FriendRequest {
    private String sender_phone;
    private String receiver_phone;
    private boolean aceppted;
}
