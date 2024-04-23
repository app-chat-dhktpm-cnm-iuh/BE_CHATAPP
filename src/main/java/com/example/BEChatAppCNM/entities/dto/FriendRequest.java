package com.example.BEChatAppCNM.entities.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FriendRequest {
    private String id;
    private String sender_phone;
    private String receiver_phone;
    private boolean aceppted;
}
