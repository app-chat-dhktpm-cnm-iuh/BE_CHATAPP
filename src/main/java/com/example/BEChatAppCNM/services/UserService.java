package com.example.BEChatAppCNM.services;

import com.example.BEChatAppCNM.entities.dto.FriendRequest;
import com.example.BEChatAppCNM.entities.User;
import com.example.BEChatAppCNM.entities.dto.LoginRegisterResponse;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public interface UserService {
    public LoginRegisterResponse saveUser(User user) throws ExecutionException, InterruptedException;
    public Optional<User> getUserDetailsByPhone(String phone_number) throws ExecutionException, InterruptedException;
    public void updateUserDetails(User user);
    public boolean checkExistPhoneNumber(String phone_number) throws ExecutionException, InterruptedException;
    public LoginRegisterResponse checkAccountLogin(User user) throws ExecutionException, InterruptedException;

    public void updateStatusUser(boolean status, String phone) throws ExecutionException, InterruptedException;

    public void addFriend(FriendRequest friendRequest) throws ExecutionException, InterruptedException;

    public List<User> getAllUserOnline() throws ExecutionException, InterruptedException;
}
