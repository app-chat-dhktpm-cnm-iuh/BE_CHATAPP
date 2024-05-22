package com.example.BEChatAppCNM.services.servicesImpl;

import com.example.BEChatAppCNM.entities.Friend;
import com.example.BEChatAppCNM.entities.User;
import com.example.BEChatAppCNM.entities.dto.FriendRequest;
import com.example.BEChatAppCNM.services.FriendService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FriendServiceImpl implements FriendService {
    private static final String COLLECTION_NAME = "invites-friend";
    private static final String COLLECTION_USER = "users";
    Firestore db = FirestoreClient.getFirestore();

    private final UserServiceImpl userService;
    CollectionReference collectionReference = db.collection(COLLECTION_NAME);
    String documentId = collectionReference.document().getId();

    public FriendServiceImpl(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Override
    public FriendRequest addInviteFriendRequest(FriendRequest friendRequest) {
        friendRequest.setId(documentId);
        collectionReference.document(documentId).create(friendRequest);
        return friendRequest;
    }

    @Override
    public List<FriendRequest> getListFriendRequest(String phone) throws ExecutionException, InterruptedException {
        List<FriendRequest> requestsSenderPhone;
        List<FriendRequest> requestsReceiverPhone;
        List<FriendRequest> requestListReturn = new ArrayList<>();

        requestsSenderPhone = collectionReference
                .whereEqualTo("sender_phone", phone)
                .get()
                .get()
                .toObjects(FriendRequest.class);

        requestsReceiverPhone = collectionReference
                .whereEqualTo("receiver_phone", phone)
                .get()
                .get()
                .toObjects(FriendRequest.class);

        if(!requestsSenderPhone.isEmpty()) {
            requestListReturn.addAll(requestsSenderPhone);
        } else if (!requestsReceiverPhone.isEmpty()) {
            requestListReturn.addAll(requestsReceiverPhone);
        }
        return requestListReturn;
    }

    @Override
    public void deleteFriendRequest(String request_id) {
        collectionReference.document(request_id).delete();
    }

    @Override
    public void unfriendRequest(String currentPhone, String friendPhone) throws ExecutionException, InterruptedException {
        List<String> phoneList = new ArrayList<>();
        phoneList.add(currentPhone);
        phoneList.add(friendPhone);

        for(String phone : phoneList) {
            String documentId = userService.getDocumentIdsByPhoneValue(phone);

            User userDetail = userService.getUserDetailsByPhone(phone).get();

            List<Friend> updatedFriends = new ArrayList<>();

            for (Friend friend : userDetail.getFriends_list()) {
                if(phone.equals(currentPhone)) {
                    if (!friend.getPhone_user().equals(friendPhone)) {
                        updatedFriends.add(friend);
                    }
                } else {
                    if (!friend.getPhone_user().equals(currentPhone)) {
                        updatedFriends.add(friend);
                    }
                }
            }

            userDetail.setFriends_list(updatedFriends);

            db.collection(COLLECTION_USER).document(documentId).set(userDetail);
        }

    }
}
