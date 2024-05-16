package com.example.BEChatAppCNM.entities.dto;
import com.example.BEChatAppCNM.entities.Attach;
import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class MessageRequest {
    private String conversation_id;
    private List<String> members;
    private String message_id;
    private String sender_phone;
    private String sender_name;
    private String content;
    private String sender_avatar_url;
    private List<String> images;
    private List<Attach> attaches;
    private Date sent_date_time;
    private List<String> phoneDeleteList;
    private boolean is_read;
    private boolean is_notification;
}
