package com.example.BEChatAppCNM.entities;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Friend {
    private String phone_user;
    private boolean is_blocked;
}
