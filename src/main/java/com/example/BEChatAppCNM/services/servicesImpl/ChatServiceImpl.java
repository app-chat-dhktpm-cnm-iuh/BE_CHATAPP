package com.example.BEChatAppCNM.services.servicesImpl;

import com.example.BEChatAppCNM.entities.Conversation;
import com.example.BEChatAppCNM.entities.Message;
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
        Conversation conversation = conversationService.getConversationById(messageRequest.getConversation_id());

        Message message = Message.builder()
                .sender_phone(messageRequest.getSender_phone())
                .is_read(messageRequest.is_read())
                .attaches(messageRequest.getAttaches())
                .content(messageRequest.getContent())
                .is_deleted(messageRequest.is_deleted())
                .sent_date_time(messageRequest.getSent_date_time())
                .build();

        conversation.getMessages().add(message);

        collectionReference.document(messageRequest.getConversation_id()).set(conversation);
    }

}
