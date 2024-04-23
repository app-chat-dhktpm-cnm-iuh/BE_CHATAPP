package com.example.BEChatAppCNM.services.servicesImpl;

import com.example.BEChatAppCNM.entities.dto.FriendRequest;
import com.example.BEChatAppCNM.services.FriendService;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class FriendServiceImpl implements FriendService {
    private static final String COLLECTION_NAME = "invites-friend";
    Firestore db = FirestoreClient.getFirestore();
    CollectionReference collectionReference = db.collection(COLLECTION_NAME);
    String documentId = collectionReference.document().getId();
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
}
