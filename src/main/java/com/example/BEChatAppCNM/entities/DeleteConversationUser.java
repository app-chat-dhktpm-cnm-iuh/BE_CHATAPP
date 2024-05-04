package com.example.BEChatAppCNM.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteConversationUser {
    private String user_phone;
    private Date deleted_at;
}
