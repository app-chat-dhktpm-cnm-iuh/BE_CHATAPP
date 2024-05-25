package com.example.BEChatAppCNM.services.servicesImpl;

import com.example.BEChatAppCNM.entities.*;
import com.example.BEChatAppCNM.entities.dto.ConversationResponse;
import com.example.BEChatAppCNM.entities.dto.MessageRequest;
import com.example.BEChatAppCNM.services.ChatService;
import com.example.BEChatAppCNM.services.ConversationService;
import com.example.BEChatAppCNM.services.UserService;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class ChatServiceImpl implements ChatService {

    private static final String COLLECTION_NAME = "conversations";
    Firestore db = FirestoreClient.getFirestore();

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private UserService userService;

    @Override
    public Message saveMessage(MessageRequest messageRequest) throws ExecutionException, InterruptedException {
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);
        ConversationResponse conversationResponse = conversationService.getConversationById(messageRequest.getConversation_id());

        if(conversationResponse.getConversation() != null) {
            List<String> deleteMessageUsers = new ArrayList<>();

            UUID message_id = UUID.randomUUID();

            Message message = Message.builder()
                    .message_id(message_id.toString())
                    .sender_name(messageRequest.getSender_name())
                    .sender_phone(messageRequest.getSender_phone())
                    .is_read(messageRequest.is_read())
                    .images(messageRequest.getImages())
                    .attaches(messageRequest.getAttaches())
                    .content(messageRequest.getContent())
                    .sender_avatar_url(messageRequest.getSender_avatar_url())
                    .phoneDeleteList(deleteMessageUsers)
                    .sent_date_time(messageRequest.getSent_date_time())
                    .build();

            conversationResponse.getConversation().getMessages().add(message);
            conversationResponse.getConversation().setUpdated_at(messageRequest.getSent_date_time());

            collectionReference.document(messageRequest.getConversation_id()).set(conversationResponse.getConversation());
            return message;
        } else {
            List<String> deleteMessageUsers = new ArrayList<>();
            List<DeleteConversationUser> deleteConversationUsers = new ArrayList<>();
            UUID message_id = UUID.randomUUID();
            List<Message> messages = new ArrayList<>();

            Message message = Message.builder()
                    .message_id(message_id.toString())
                    .sender_name(messageRequest.getSender_name())
                    .sender_phone(messageRequest.getSender_phone())
                    .is_read(messageRequest.is_read())
                    .images(messageRequest.getImages())
                    .attaches(messageRequest.getAttaches())
                    .content(messageRequest.getContent())
                    .sender_avatar_url(messageRequest.getSender_avatar_url())
                    .phoneDeleteList(deleteMessageUsers)
                    .sent_date_time(messageRequest.getSent_date_time())
                    .build();
            messages.add(message);

            Conversation conversation = Conversation
                    .builder()
                    .conversation_id(messageRequest.getConversation_id())
                    .title("")
                    .messages(messages)
                    .is_group(false)
                    .updated_at(messageRequest.getSent_date_time())
                    .deleteConversationUsers(deleteConversationUsers)
                    .creator_phone(messageRequest.getSender_phone())
                    .ava_conversation_url("")
                    .members(messageRequest.getMembers())
                    .build();

            conversationService.addConversation(conversation);

            return message;
        }

    }

    @Override
    public void deleteMessage(String conversationId, String messageId, String phoneDelete) throws ExecutionException, InterruptedException {
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);
        ConversationResponse conversationResponse = conversationService.getConversationById(conversationId);
        Conversation conversation = conversationResponse.getConversation();

        conversation.getMessages().forEach(message -> {
            if (message.getMessage_id().equals(messageId)) {
                message.getPhoneDeleteList().add(phoneDelete);
            }
        });

        conversationResponse.setConversation(conversation);

        collectionReference.document(conversationId).set(conversationResponse.getConversation());
    }

    @Override
    public ConversationResponse addMemberToGroupChat(String conversationId, String memPhone, String keyPhone) throws ExecutionException, InterruptedException {
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);
        ConversationResponse conversationResult = conversationService.getConversationById(conversationId);

        conversationResult.getConversation().getMembers().add(memPhone);
        List<String> deleteMessageUsers = new ArrayList<>();

        UUID message_id = UUID.randomUUID();

        List<String> images = new ArrayList<>();

        List<Attach> attaches = new ArrayList<>();

        User keyMem = userService.getUserDetailsByPhone(keyPhone).get();
        User addedMem = userService.getUserDetailsByPhone(memPhone).get();

        String content = keyMem.getName() + " đã thêm " + addedMem.getName() + " vào nhóm";
        Calendar calendar = Calendar.getInstance();
        Date sent_date_time = calendar.getTime();


        Message message = Message.builder()
                .message_id(message_id.toString())
                .is_read(false)
                .images(images)
                .attaches(attaches)
                .content(content)
                .phoneDeleteList(deleteMessageUsers)
                .sent_date_time(sent_date_time)
                .build();

        conversationResult.getConversation().getMessages().add(message);
        conversationResult.getConversation().setUpdated_at(sent_date_time);

        collectionReference.document(conversationId).set(conversationResult.getConversation());
        conversationResult.getMemberDetails().add(addedMem);

        return conversationResult;

    }

    @Override
    public ConversationResponse deleteMemberFromGroupChat(String conversationId, String memPhone, String keyPhone) throws ExecutionException, InterruptedException {
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);
        ConversationResponse conversationResult = conversationService.getConversationById(conversationId);

        if(!keyPhone.equals(conversationResult.getConversation().getCreator_phone())) {
            return null;
        } else {

            List<String> deleteMessageUsers = new ArrayList<>();

            UUID message_id = UUID.randomUUID();

            List<String> images = new ArrayList<>();

            List<Attach> attaches = new ArrayList<>();

            User keyMem = userService.getUserDetailsByPhone(keyPhone).get();
            User deletedMem = userService.getUserDetailsByPhone(memPhone).get();

            String content = keyMem.getName() + " đã xóa " + deletedMem.getName() + " ra khỏi nhóm";

            Calendar calendar = Calendar.getInstance();

            Date sent_date_time = calendar.getTime();

            Message message = Message.builder()
                    .message_id(message_id.toString())
                    .is_read(false)
                    .images(images)
                    .attaches(attaches)
                    .content(content)
                    .phoneDeleteList(deleteMessageUsers)
                    .sent_date_time(sent_date_time)
                    .build();

            conversationResult.getConversation().getMembers().remove(memPhone);
            conversationResult.getConversation().getMessages().add(message);
            conversationResult.getConversation().setUpdated_at(sent_date_time);


            collectionReference.document(conversationId).set(conversationResult.getConversation());

            conversationResult.getMemberDetails().remove(deletedMem);

            return conversationResult;
        }
    }

    @Override
    public Conversation disbandGroupChat(String conversationId, String keyPhone) throws ExecutionException, InterruptedException {
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);
        ConversationResponse conversationResult = conversationService.getConversationById(conversationId);
        Conversation conversation = conversationResult.getConversation();
        if(!keyPhone.equals(conversationResult.getConversation().getCreator_phone())) {
            return null;
        } else {
            collectionReference.document(conversationId).delete();
            return conversation;
        }
    }

    public List<Message> getListMessageAfterDeleteConversation(Conversation conversation, String userPhone)  {
        List<Message> messageListReturn = new ArrayList<>();
        DeleteConversationUser deleteConversationUser = new DeleteConversationUser();
        List<DeleteConversationUser> deleteConversationUserList = conversation.getDeleteConversationUsers();

        if(!deleteConversationUserList.isEmpty()) {
            for (DeleteConversationUser conversationUser : deleteConversationUserList) {
                if (conversationUser.getUser_phone().equals(userPhone)) {
                    deleteConversationUser = conversationUser;
                }
            }

            for (int i = 0; i < conversation.getMessages().size(); i++) {
                if(conversation.getMessages().get(i).getSent_date_time().after(deleteConversationUser.getDeleted_at()) && !conversation.getMessages().get(i).getPhoneDeleteList().contains(userPhone)) {
                    messageListReturn.add(conversation.getMessages().get(i));
                }
            }
        } else {
            for (int i = 0; i < conversation.getMessages().size(); i++) {
                if(!conversation.getMessages().get(i).getPhoneDeleteList().contains(userPhone)) {
                    messageListReturn.add(conversation.getMessages().get(i));
                }
            }
        }

        return  messageListReturn;
    }


}
