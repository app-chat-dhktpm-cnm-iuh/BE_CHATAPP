package com.example.BEChatAppCNM.services.servicesImpl;

import com.example.BEChatAppCNM.entities.Conversation;
import com.example.BEChatAppCNM.entities.Message;
import com.example.BEChatAppCNM.entities.dto.ConversationResponse;
import com.example.BEChatAppCNM.entities.dto.MessageRequest;
import com.example.BEChatAppCNM.services.ChatService;
import com.example.BEChatAppCNM.services.ConversationService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.ExecutionException;

@Service
public class ChatServiceImpl implements ChatService {

    private static final String COLLECTION_NAME = "conversations";
    Firestore db = FirestoreClient.getFirestore();

    @Autowired
    private ConversationService conversationService;

    @Override
    public void saveMessage(MessageRequest messageRequest) throws ExecutionException, InterruptedException {
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);
        ConversationResponse conversationResponse = conversationService.getConversationById(messageRequest.getConversation_id());
        Message message = Message.builder()
                .sender_name(messageRequest.getSender_name())
                .sender_phone(messageRequest.getSender_phone())
                .is_read(messageRequest.is_read())
                .images(messageRequest.getImages())
                .attaches(messageRequest.getAttaches())
                .content(messageRequest.getContent())
                .is_deleted(messageRequest.is_deleted())
                .sent_date_time(messageRequest.getSent_date_time())
                .build();

        conversationResponse.getConversation().getMessages().add(message);
        conversationResponse.getConversation().setUpdated_at(messageRequest.getSent_date_time());

        collectionReference.document(messageRequest.getConversation_id()).set(conversationResponse.getConversation());
    }

}
