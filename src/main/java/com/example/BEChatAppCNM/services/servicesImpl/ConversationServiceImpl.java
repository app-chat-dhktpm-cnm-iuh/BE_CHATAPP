package com.example.BEChatAppCNM.services.servicesImpl;

import com.example.BEChatAppCNM.entities.*;
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
        collectionReference.document(documentId).create(conversation);
        List<Message> messages = new ArrayList<>();
        UUID messageId = UUID.randomUUID();
        List<String> phoneDeleteList = new ArrayList<>();
        List<String> images = new ArrayList<>();
        List<Attach> attaches = new ArrayList<>();

        Message message = Message
                .builder()
                .message_id(messageId.toString())
                .phoneDeleteList(phoneDeleteList)
                .images(images)
                .sent_date_time(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .attaches(attaches)
                .is_notification(true)
                .content("Hãy trò chuyện vui vẻ nào")
                .build();

        messages.add(message);

        conversation.getMembers().forEach(phone -> {
            try {
                Optional<User> user = userService.getUserDetailsByPhone(phone);
                userList.add(user.get());
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        conversation.setMessages(messages);

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
        boolean result = false;
        for(DeleteConversationUser deleteConversationUser : deleteConversationUsers) {
            if(deleteConversationUser.getUser_phone().equals(phoneUser)) {
                result = true;
            } else result = false;
        }
        return result;
    }


    @Override
    public ConversationResponse getConversationById(String conversationId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(conversationId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        Conversation conversation = document.toObject(Conversation.class);

//        conversation.setConversation_id(document.getId());

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

    @Override
    public ConversationResponse getConversationByIdAndCurrentPhone(String conversationId, String currentPhone) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(conversationId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        Conversation conversation = document.toObject(Conversation.class);

        List<User> memberList = new ArrayList<>();
        conversation.getMembers().forEach(phone -> {
            try {
                Optional<User> user = userService.getUserDetailsByPhone(phone);
                memberList.add(user.get());
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        List<DeleteConversationUser> deleteConversationUserList = conversation.getDeleteConversationUsers();

        if(deleteConversationUserList.isEmpty()) {

            return ConversationResponse.builder()
                    .conversation(conversation)
                    .memberDetails(memberList)
                    .build();

        } else if(checkContainDeleteConversationUser(deleteConversationUserList, currentPhone)) {
            List<Message> messages = chatService.getListMessageAfterDeleteConversation(conversation, currentPhone);
            conversation.setMessages(messages);

            return ConversationResponse.builder()
                    .conversation(conversation)
                    .memberDetails(memberList)
                    .build();
//            if(!messages.isEmpty()) {
//                conversation.setMessages(messages);
//
//                return ConversationResponse.builder()
//                        .conversation(conversation)
//                        .memberDetails(memberList)
//                        .build();
//            } else {
//                return ConversationResponse.builder()
//                        .conversation(conversation)
//                        .memberDetails(memberList)
//                        .build();
//            }
        } else {
            return ConversationResponse.builder()
                    .conversation(conversation)
                    .memberDetails(memberList)
                    .build();
        }
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
        List<String> deleteConversationUserPhone = new ArrayList<>();

        for (DeleteConversationUser deleteConversationUserItem : conversation.getDeleteConversationUsers()) {
            deleteConversationUserPhone.add(deleteConversationUserItem.getUser_phone());
        }

        DeleteConversationUser deleteConversationUser = DeleteConversationUser
                .builder()
                .user_phone(currentPhone)
                .deleted_at(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .build();

        if(!conversation.getDeleteConversationUsers().isEmpty()) {
            if(!deleteConversationUserPhone.contains(currentPhone)) {
                conversation.getDeleteConversationUsers().add(deleteConversationUser);
            } else {
                for (DeleteConversationUser conversationUser : conversation.getDeleteConversationUsers()) {
                    if(conversationUser.getUser_phone().equals(currentPhone)) {
                        conversationUser.setDeleted_at(deleteConversationUser.getDeleted_at());
                    }
                }
            }
        } else {
            conversation.getDeleteConversationUsers().add(deleteConversationUser);
        }
        collectionReference.document(conversationId).set(conversation);
    }

}
