package com.example.BEChatAppCNM.services.servicesImpl;

import com.example.BEChatAppCNM.entities.Message;
import com.example.BEChatAppCNM.entities.dto.MessageRequest;
import com.example.BEChatAppCNM.services.ChatService;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

public class ChatServiceImpl implements ChatService {

    private static final String COLLECTION_NAME = "conversations";
    Firestore db = FirestoreClient.getFirestore();

    @Override
    public void saveMessage(MessageRequest messageRequest) {
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);
        Message message = Message.builder()
                .sender_phone(messageRequest.getSender_phone())
                .attaches(messageRequest.getAttaches())
                .content(messageRequest.getContent())
                .is_deleted(messageRequest.is_deleted())
                .sent_date_time(messageRequest.getSent_date_time())
                .build();

        collectionReference.document(messageRequest.getConversation_id()).update("messages", message);
    }
}
