package com.example.BEChatAppCNM.entities;

import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class User {
    private String phone;
    private String password;
    private String name;
    private Date date_of_birth;
    private boolean gender;
    private String avatar_url;
    private boolean is_activated;
    private boolean is_deleted;
    private List<Friend> friends_list;
}
