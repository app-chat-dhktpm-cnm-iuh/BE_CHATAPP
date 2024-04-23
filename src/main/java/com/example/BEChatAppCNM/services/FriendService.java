package com.example.BEChatAppCNM.services;

import com.example.BEChatAppCNM.entities.dto.FriendRequest;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface FriendService {
    public FriendRequest addInviteFriendRequest(FriendRequest friendRequest);

    public List<FriendRequest> getListFriendRequest(String phone) throws ExecutionException, InterruptedException;

    public void deleteFriendRequest(String request_id);
}

