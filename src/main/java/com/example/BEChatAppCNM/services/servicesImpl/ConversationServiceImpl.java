package com.example.BEChatAppCNM.services.servicesImpl;

import com.example.BEChatAppCNM.entities.Conversation;
import com.example.BEChatAppCNM.entities.DeleteConversationUser;
import com.example.BEChatAppCNM.entities.Message;
import com.example.BEChatAppCNM.entities.User;
import com.example.BEChatAppCNM.entities.dto.ConversationResponse;
import com.example.BEChatAppCNM.services.ConversationService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class ConversationServiceImpl implements ConversationService {

    private static final String COLLECTION_NAME = "conversations";

    private final UserServiceImpl userService;
    private  final ChatServiceImpl chatService;
    Firestore db = FirestoreClient.getFirestore();

    public ConversationServiceImpl(@Lazy UserServiceImpl userService, @Lazy ChatServiceImpl chatService) {
        this.userService = userService;
        this.chatService = chatService;
    }

    @Override
    public ConversationResponse addConversation(Conversation conversation) {
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);
        String documentId = collectionReference.document().getId();

        List<User> userList = new ArrayList<>();

        conversation.setConversation_id(documentId);
        conversation.setUpdated_at(conversation.getUpdated_at());
        collectionReference.document(documentId).create(conversation);

        conversation.getMembers().forEach(phone -> {
            try {
                Optional<User> user = userService.getUserDetailsByPhone(phone);
                userList.add(user.get());
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        ConversationResponse conversationResponse = ConversationResponse
                .builder()
                .conversation(conversation)
                .memberDetails(userList)
                .build();

        return conversationResponse;
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
                    Conversation conversation = document.toObject(Conversation.class);
                    List<User> userList = new ArrayList<>();
                    List<DeleteConversationUser> deleteConversationUserList = conversation.getDeleteConversationUsers();

                    conversation.getMembers().forEach(phone -> {
                        try {
                            Optional<User> user = userService.getUserDetailsByPhone(phone);
                            userList.add(user.get());
                        } catch (ExecutionException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    if(deleteConversationUserList.isEmpty()) {
                        ConversationResponse conversationResponse = ConversationResponse.builder()
                                .conversation(conversation)
                                .memberDetails(userList)
                                .build();
                        conversationResponses.add(conversationResponse);
                    } else if(checkContainDeleteConversationUser(deleteConversationUserList, creator_phone)) {
                        List<Message> messages = chatService.getListMessageAfterDeleteConversation(conversation, creator_phone);

                        if(!messages.isEmpty()) {
                            conversation.setMessages(messages);

                            ConversationResponse conversationResponse = ConversationResponse.builder()
                                    .conversation(conversation)
                                    .memberDetails(userList)
                                    .build();
                            conversationResponses.add(conversationResponse);
                        }
                    } else {
                        ConversationResponse conversationResponse = ConversationResponse.builder()
                                .conversation(conversation)
                                .memberDetails(userList)
                                .build();
                        conversationResponses.add(conversationResponse);
                    }
                });
        Collections.sort(conversationResponses, Comparator.comparing(ConversationResponse::getConversationUpdateAt).reversed());
        return conversationResponses;
    }

    public boolean checkContainDeleteConversationUser(List<DeleteConversationUser> deleteConversationUsers, String phoneUser) {
        for(DeleteConversationUser deleteConversationUser : deleteConversationUsers) {
            if(deleteConversationUser.getUser_phone().equals(phoneUser)) {
                return true;
            } else return false;
        }
        return false;
    }


    @Override
    public ConversationResponse getConversationById(String conversationId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(conversationId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        Conversation conversation = document.toObject(Conversation.class);
        conversation.setConversation_id(document.getId());
        List<User> meberList = new ArrayList<>();
        conversation.getMembers().forEach(phone -> {
            try {
                Optional<User> user = userService.getUserDetailsByPhone(phone);
                meberList.add(user.get());
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        return ConversationResponse
                .builder()
                .conversation(conversation)
                .memberDetails(meberList)
                .build();
    }

//    @Override
//    public ConversationResponse getConversationBySenderPhoneAndReceiverPhone(String senderPhone, String receiverPhone) throws ExecutionException, InterruptedException {
//        CollectionReference collectionReference = db.collection(COLLECTION_NAME);
//
//        List<String> members = new ArrayList<>();
//        members.add(senderPhone);
//        members.add(receiverPhone);
//
//        ConversationResponse conversationResponse = new ConversationResponse();
//        List<User> mems = new ArrayList<>();
//
//        QuerySnapshot documentSnapshot =  collectionReference
//                .whereIn("members", members)
//                .get()
//                .get();
//        System.out.println(documentSnapshot);
//                .forEach(conversation -> {
//                    Conversation conversationTemp = conversation.toObject(Conversation.class);
//                    if(conversationTemp.getMembers().size() == 2) {
//                        conversationResponse.setConversation(conversationTemp);
//                        members.forEach(phone -> {
//                            try {
//                                User mem = userService.getUserDetailsByPhone(phone).get();
//                                mems.add(mem);
//                            } catch (ExecutionException | InterruptedException e) {
//                                throw new RuntimeException(e);
//                            }
//                        });
//
//                        conversationResponse.setMemberDetails(mems);
//                    }
//                });
//        return conversationResponse;
//    }

    @Override
    public void deleteConversation(String conversationId, String currentPhone) throws ExecutionException, InterruptedException {
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);
        ConversationResponse conversationResponse = getConversationById(conversationId);
        Conversation conversation = conversationResponse.getConversation();

        DeleteConversationUser deleteConversationUser = DeleteConversationUser
                .builder()
                .user_phone(currentPhone)
                .deleted_at(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .build();

        conversation.getDeleteConversationUsers().forEach(deleteConversationUserItem -> {
            if(!deleteConversationUserItem.getUser_phone().equals(currentPhone)) {
                conversationResponse.getConversation().getDeleteConversationUsers().add(deleteConversationUser);
            } else {
                deleteConversationUserItem.setDeleted_at(deleteConversationUser.getDeleted_at());
            }
        });

        collectionReference.document(conversationId).set(conversation);
    }

}
