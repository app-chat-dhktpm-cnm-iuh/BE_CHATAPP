package com.example.BEChatAppCNM.entities;

import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Message {
    private String sender_phone;
    private String content;
    private List<String> attaches;
    private Date sent_date_time;
    private boolean is_deleted;
}
