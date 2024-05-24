package com.example.BEChatAppCNM.services.servicesImpl;

import com.example.BEChatAppCNM.entities.*;
import com.example.BEChatAppCNM.entities.dto.ConversationResponse;
import com.example.BEChatAppCNM.entities.dto.MessageRequest;
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
    private final ChatServiceImpl chatService;
    Firestore db = FirestoreClient.getFirestore();

    public ConversationServiceImpl(@Lazy UserServiceImpl userService, @Lazy ChatServiceImpl chatService) {
        this.userService = userService;
        this.chatService = chatService;
    }

    @Override
    public ConversationResponse addConversation(Conversation conversation) {
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);

        UUID messageId = UUID.randomUUID();
        List<String> phoneDeleteList = new ArrayList<>();
        List<String> images = new ArrayList<>();
        List<Attach> attaches = new ArrayList<>();
        Date sentDateTime = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

        if(conversation.getConversation_id() != null && !conversation.is_group()) {
            String documentId = conversation.getConversation_id();

            List<User> userList = new ArrayList<>();

            Message message = Message
                    .builder()
                    .message_id(messageId.toString())
                    .phoneDeleteList(phoneDeleteList)
                    .images(images)
                    .sent_date_time(sentDateTime)
                    .attaches(attaches)
                    .is_notification(true)
                    .content("Các bạn đã được kết nối")
                    .build();

            conversation.setConversation_id(documentId);
            conversation.getMessages().add(message);
            conversation.setUpdated_at(sentDateTime);
            collectionReference.document(documentId).create(conversation);

            conversation.getMembers().forEach(phone -> {
                try {
                    Optional<User> user = userService.getUserDetailsByPhone(phone);
                    userList.add(user.get());
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            return ConversationResponse
                    .builder()
                    .conversation(conversation)
                    .memberDetails(userList)
                    .build();
        } else {
            String documentId = collectionReference.document().getId();

            List<User> userList = new ArrayList<>();
            Message message = Message
                    .builder()
                    .message_id(messageId.toString())
                    .phoneDeleteList(phoneDeleteList)
                    .images(images)
                    .sent_date_time(sentDateTime)
                    .attaches(attaches)
                    .is_notification(true)
                    .content("Hãy trò chuyện vui vẻ nào")
                    .build();

            List<Message> messages = new ArrayList<>();
            messages.add(message);

            conversation.setConversation_id(documentId);
            conversation.setMessages(messages);
            conversation.setUpdated_at(sentDateTime);

            collectionReference.document(documentId).create(conversation);

            conversation.getMembers().forEach(phone -> {
                try {
                    Optional<User> user = userService.getUserDetailsByPhone(phone);
                    userList.add(user.get());
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            return ConversationResponse
                    .builder()
                    .conversation(conversation)
                    .memberDetails(userList)
                    .build();
        }
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

                    if (deleteConversationUserList.isEmpty()) {
                        List<Message> messages = chatService.getListMessageAfterDeleteConversation(conversation, creator_phone);
                        conversation.setMessages(messages);
                        ConversationResponse conversationResponse = ConversationResponse.builder()
                                .conversation(conversation)
                                .memberDetails(userList)
                                .build();
                        conversationResponses.add(conversationResponse);
                    } else if (checkContainDeleteConversationUser(deleteConversationUserList, creator_phone)) {
                        List<Message> messages = chatService.getListMessageAfterDeleteConversation(conversation, creator_phone);

                        if (!messages.isEmpty()) {
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
        for (DeleteConversationUser deleteConversationUser : deleteConversationUsers) {
            if (deleteConversationUser.getUser_phone().equals(phoneUser)) {
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

        if(document.exists()) {
            Conversation conversation = document.toObject(Conversation.class);

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
        } else return ConversationResponse
                .builder()
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

        if (deleteConversationUserList.isEmpty()) {
            List<Message> messages = chatService.getListMessageAfterDeleteConversation(conversation, currentPhone);
            conversation.setMessages(messages);
            return ConversationResponse.builder()
                    .conversation(conversation)
                    .memberDetails(memberList)
                    .build();

        } else if (checkContainDeleteConversationUser(deleteConversationUserList, currentPhone)) {
            List<Message> messages = chatService.getListMessageAfterDeleteConversation(conversation, currentPhone);
            conversation.setMessages(messages);

            return ConversationResponse.builder()
                    .conversation(conversation)
                    .memberDetails(memberList)
                    .build();
        } else {
            return ConversationResponse.builder()
                    .conversation(conversation)
                    .memberDetails(memberList)
                    .build();
        }
    }

    @Override
    public ConversationResponse getConversationBySenderPhoneAndReceiverPhone(String currentPhone, String userPhone) throws ExecutionException, InterruptedException {
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);

        List<String> members = new ArrayList<>();
        members.add(currentPhone);
        members.add(userPhone);

        ConversationResponse conversationResponse = new ConversationResponse();
        List<User> memberList = new ArrayList<>();

        QuerySnapshot documentSnapshot = collectionReference
                .whereArrayContainsAny("members", members)
                .get()
                .get();
        if (!documentSnapshot.getDocuments().isEmpty()) {
            for (QueryDocumentSnapshot document : documentSnapshot.getDocuments()) {
                Conversation conversationTemp = document.toObject(Conversation.class);
                boolean compare = conversationTemp.getMembers().size() == 2 && conversationTemp.getMembers().containsAll(members) && !conversationTemp.is_group();
                if (conversationTemp.getMembers().size() == 2 && conversationTemp.getMembers().containsAll(members) && !conversationTemp.is_group()) {
                    List<DeleteConversationUser> deleteConversationUserList = conversationTemp.getDeleteConversationUsers();

                    members.forEach(phone -> {
                        try {
                            User mem = userService.getUserDetailsByPhone(phone).get();
                            memberList.add(mem);
                        } catch (ExecutionException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    if (deleteConversationUserList.isEmpty()) {
                        List<Message> messages = chatService.getListMessageAfterDeleteConversation(conversationTemp, currentPhone);
                        conversationTemp.setMessages(messages);
                        return ConversationResponse.builder()
                                .conversation(conversationTemp)
                                .memberDetails(memberList)
                                .build();

                    } else if (checkContainDeleteConversationUser(deleteConversationUserList, currentPhone)) {
                        List<Message> messages = chatService.getListMessageAfterDeleteConversation(conversationTemp, currentPhone);
                        conversationTemp.setMessages(messages);

                        return ConversationResponse.builder()
                                .conversation(conversationTemp)
                                .memberDetails(memberList)
                                .build();
                    } else {
                        return ConversationResponse.builder()
                                .conversation(conversationTemp)
                                .memberDetails(memberList)
                                .build();
                    }
                }
            }
            List<DeleteConversationUser> deleteConversationUserList = new ArrayList<>();
            Conversation conversation = Conversation
                    .builder()
                    .members(members)
                    .deleteConversationUsers(deleteConversationUserList)
                    .is_group(false)
                    .build();

            return createFakeConversation(conversation);
        } else {
            List<DeleteConversationUser> deleteConversationUserList = new ArrayList<>();
            Conversation conversation = Conversation
                    .builder()
                    .members(members)
                    .deleteConversationUsers(deleteConversationUserList)
                    .is_group(false)
                    .build();
            return createFakeConversation(conversation);
        }
    }

    public ConversationResponse createFakeConversation(Conversation conversation) {
        UUID messageId = UUID.randomUUID();
        String documentId = UUID.randomUUID().toString();
        List<String> phoneDeleteList = new ArrayList<>();
        List<String> images = new ArrayList<>();
        List<Attach> attaches = new ArrayList<>();
        Date sentDateTime = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<User> userList = new ArrayList<>();

        Message message = Message
                .builder()
                .message_id(messageId.toString())
                .phoneDeleteList(phoneDeleteList)
                .images(images)
                .sent_date_time(sentDateTime)
                .attaches(attaches)
                .is_notification(true)
                .content("Hãy trò chuyện vui vẻ nào")
                .build();

        List<Message> messages = new ArrayList<>();
        messages.add(message);

        conversation.setConversation_id(documentId);
        conversation.setMessages(messages);
        conversation.setUpdated_at(sentDateTime);

        conversation.getMembers().forEach(phone -> {
            try {
                Optional<User> user = userService.getUserDetailsByPhone(phone);
                userList.add(user.get());
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        return ConversationResponse
                .builder()
                .conversation(conversation)
                .memberDetails(userList)
                .build();
    }

    @Override
    public void deleteConversation(String conversationId, String currentPhone) throws ExecutionException, InterruptedException {
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);
        ConversationResponse conversationResponse = getConversationById(conversationId);
        Conversation conversation = conversationResponse.getConversation();
        List<String> deleteConversationUserPhone = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        Date deleted_at = calendar.getTime();

        for (DeleteConversationUser deleteConversationUserItem : conversation.getDeleteConversationUsers()) {
            deleteConversationUserPhone.add(deleteConversationUserItem.getUser_phone());
        }

        DeleteConversationUser deleteConversationUser = DeleteConversationUser
                .builder()
                .user_phone(currentPhone)
                .deleted_at(deleted_at)
                .build();

        if (!conversation.getDeleteConversationUsers().isEmpty()) {
            if (!deleteConversationUserPhone.contains(currentPhone)) {
                conversation.getDeleteConversationUsers().add(deleteConversationUser);
            } else {
                for (DeleteConversationUser conversationUser : conversation.getDeleteConversationUsers()) {
                    if (conversationUser.getUser_phone().equals(currentPhone)) {
                        conversationUser.setDeleted_at(deleteConversationUser.getDeleted_at());
                    }
                }
            }
        } else {
            conversation.getDeleteConversationUsers().add(deleteConversationUser);
        }
        collectionReference.document(conversationId).set(conversation);
    }

    @Override
    public void updateGroupChatDetail(Conversation conversation) throws ExecutionException, InterruptedException {
        if (conversation.getTitle() != null) {
            db.collection(COLLECTION_NAME)
                    .document(conversation.getConversation_id())
                    .update("title", conversation.getTitle());
        }

        if (conversation.getAva_conversation_url() != null) {
            db.collection(COLLECTION_NAME)
                    .document(conversation.getConversation_id())
                    .update("ava_conversation_url", conversation.getAva_conversation_url());
        }

        String message_id = UUID.randomUUID().toString();
        List<String> phoneDeleteList = new ArrayList<>();
        List<String> images = new ArrayList<>();
        List<Attach> attaches = new ArrayList<>();
        MessageRequest messageRequest = new MessageRequest();

        User keyUser = userService.getUserDetailsByPhone(conversation.getCreator_phone()).get();

        if(conversation.getTitle() != null) {

            messageRequest = MessageRequest
                                .builder()
                                .conversation_id(conversation.getConversation_id())
                                .message_id(message_id)
                                .is_read(false)
                                .phoneDeleteList(phoneDeleteList)
                                .images(images)
                                .sent_date_time(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()))
                                .attaches(attaches)
                                .is_notification(true)
                                .content(keyUser.getName() + "đã cập nhật tên nhóm là " + conversation.getTitle())
                                .build();
        } else if (conversation.getAva_conversation_url() != null){
            messageRequest = MessageRequest
                    .builder()
                    .conversation_id(conversation.getConversation_id())
                    .message_id(message_id)
                    .is_read(false)
                    .phoneDeleteList(phoneDeleteList)
                    .images(images)
                    .sent_date_time(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()))
                    .attaches(attaches)
                    .is_notification(true)
                    .content(keyUser.getName() + "đã cập nhật ảnh đại diện nhóm")
                    .build();
        }



        chatService.saveMessage(messageRequest);
    }
}
