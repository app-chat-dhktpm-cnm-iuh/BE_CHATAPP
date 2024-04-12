package com.example.BEChatAppCNM.entities.dto;

import com.example.BEChatAppCNM.entities.User;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class LoginRegisterResponse {
    private User user;
    private String token;
}
