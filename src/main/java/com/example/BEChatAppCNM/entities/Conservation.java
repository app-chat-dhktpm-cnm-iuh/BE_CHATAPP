package com.example.BEChatAppCNM.entities;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Conservation {
    private String title;
    private String creator_phone;
    private String ava_conservation_url;
    private boolean is_deleted;
    private List<String> members;
    private List<Message> messages;
}
