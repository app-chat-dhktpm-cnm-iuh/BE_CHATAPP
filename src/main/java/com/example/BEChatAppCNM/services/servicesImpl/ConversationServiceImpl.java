package com.example.BEChatAppCNM.services.servicesImpl;

import com.example.BEChatAppCNM.entities.Conversation;
import com.example.BEChatAppCNM.entities.User;
import com.example.BEChatAppCNM.entities.dto.ConversationResponse;
import com.example.BEChatAppCNM.services.ConversationService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class ConversationServiceImpl implements ConversationService {

    private static final String COLLECTION_NAME = "conversations";

    private final UserServiceImpl userService;
    Firestore db = FirestoreClient.getFirestore();

    public ConversationServiceImpl(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Override
    public Conversation addConversation(Conversation conversation) {
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);
        String documentId = collectionReference.document().getId();
        conversation.setConversation_id(documentId);

        collectionReference.document(documentId).create(conversation);
        return conversation;
    }

    @Override
    public List<ConversationResponse> findListConversationByCreatorPhone(String creator_phone) throws ExecutionException, InterruptedException {
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);
        List<ConversationResponse> conversationResponses = new ArrayList<>();

        collectionReference.whereArrayContains("members", creator_phone)
                .get()
                .get()
                .getDocuments()
                .forEach(document -> {
                    String documentId = document.getId();
                    Conversation conversation = document.toObject(Conversation.class);
                    conversation.setConversation_id(documentId);

                    List<User> userList = new ArrayList<>();
                    conversation.getMembers().forEach(phone -> {
                        try {
                            Optional<User> user = userService.getUserDetailsByPhone(phone);
                            userList.add(user.get());
                        } catch (ExecutionException e) {
                            throw new RuntimeException(e);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    ConversationResponse conversationResponse = ConversationResponse.builder()
                            .conversation(conversation)
                            .memberDetails(userList)
                            .build();
                    conversationResponses.add(conversationResponse);
                });

        return conversationResponses;
    }

    @Override
    public Conversation getConversationById(String conversationId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(conversationId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            Conversation conversation = document.toObject(Conversation.class);
            conversation.setConversation_id(document.getId());
            return conversation;
        } else {
            return null;
        }
    }

}
