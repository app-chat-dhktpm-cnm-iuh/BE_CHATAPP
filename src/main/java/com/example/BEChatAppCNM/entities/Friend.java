package com.example.BEChatAppCNM.entities;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Friend {
    private String user_id;
    private boolean is_blocked;
}
