package com.example.BEChatAppCNM.services.servicesImpl;

import com.example.BEChatAppCNM.entities.Conversation;
import com.example.BEChatAppCNM.services.ConversationService;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ConversationServiceImpl implements ConversationService {

    private static final String COLLECTION_NAME = "conversations";
    Firestore db = FirestoreClient.getFirestore();

    @Override
    public Conversation addConversation(Conversation conversation) {
       CollectionReference collectionReference = db.collection(COLLECTION_NAME);
       String documentId = collectionReference.getId();
       conversation.setConversation_id(documentId);

       collectionReference.document(documentId).create(conversation);
       return conversation;
    }

    @Override
    public List<Conversation> findListConversationByCreatorPhone(String creator_phone) throws ExecutionException, InterruptedException {
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);
        List<Conversation> conversationList = new ArrayList<>();

        collectionReference.whereEqualTo("creator_phone", creator_phone)
                .get()
                .get()
                .getDocuments()
                .forEach(document -> {
                    String documentId = document.getId();
                    Conversation conversation = document.toObject(Conversation.class);
                    conversation.setConversation_id(documentId);
                    conversationList.add(conversation);
                });

        return conversationList;
    }
}
