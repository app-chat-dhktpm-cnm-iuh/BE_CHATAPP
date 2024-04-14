package com.example.BEChatAppCNM.entities.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MessageRequest {
    private String conversation_id;
    private String creator_phone;
    private List<String> members;
    private String content;
    private List<String> attaches;
    private String sender_phone;
    private Date sent_date_time;
    private boolean is_read;
    private  boolean is_deleted;
}
