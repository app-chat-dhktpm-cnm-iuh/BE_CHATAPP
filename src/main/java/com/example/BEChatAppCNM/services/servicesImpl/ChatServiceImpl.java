package com.example.BEChatAppCNM.services.servicesImpl;

import com.example.BEChatAppCNM.entities.Conversation;
import com.example.BEChatAppCNM.entities.DeleteConversationUser;
import com.example.BEChatAppCNM.entities.Message;
import com.example.BEChatAppCNM.entities.User;
import com.example.BEChatAppCNM.entities.dto.ConversationResponse;
import com.example.BEChatAppCNM.entities.dto.MessageRequest;
import com.example.BEChatAppCNM.services.ChatService;
import com.example.BEChatAppCNM.services.ConversationService;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
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
                .phoneDeleteList(deleteMessageUsers)
                .sent_date_time(messageRequest.getSent_date_time())
                .build();

        conversationResponse.getConversation().getMessages().add(message);
        conversationResponse.getConversation().setUpdated_at(messageRequest.getSent_date_time());

        collectionReference.document(messageRequest.getConversation_id()).set(conversationResponse.getConversation());
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
        Conversation conversation = conversationResult.getConversation();
        List<User> listMember = new ArrayList<>();

        if(!keyPhone.equals(conversation.getCreator_phone())) {
            return null;
        } else {
            conversation.getMembers().add(memPhone);
            collectionReference.document(conversationId).set(conversation);

            for (User user : conversationResult.getMemberDetails()) {
                if(user.getPhone().equals(memPhone)) {
                    listMember.add(user);
                    conversationResult.setMemberDetails(listMember);
                    conversationResult.setConversation(conversation);
                }
            }

            return conversationResult;
        }
    }

    @Override
    public ConversationResponse deleteMemberFromGroupChat(String conversationId, String memPhone, String keyPhone) throws ExecutionException, InterruptedException {
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);
        ConversationResponse conversationResult = conversationService.getConversationById(conversationId);
        Conversation conversation = conversationResult.getConversation();
        List<User> listMember = new ArrayList<>();

        if(!keyPhone.equals(conversation.getCreator_phone())) {
            return null;
        } else {
            conversation.getMembers().remove(memPhone);
            collectionReference.document(conversationId).set(conversation);

            for (User user : conversationResult.getMemberDetails()) {
                if(user.getPhone().equals(memPhone)) {
                    listMember.add(user);
                    conversationResult.setMemberDetails(listMember);
                    conversationResult.setConversation(conversation);
                }
            }

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

        for (int i = 0; i < deleteConversationUserList.size(); i++) {
            if(deleteConversationUserList.get(i).getUser_phone().equals(userPhone)) {
                deleteConversationUser = deleteConversationUserList.get(i);
            }
        }

        for (int i = 0; i < conversation.getMessages().size(); i++) {
            boolean result = conversation.getMessages().get(i).getSent_date_time().after(deleteConversationUser.getDeleted_at());
            Date date_sent = conversation.getMessages().get(i).getSent_date_time();
            Date delete_at = deleteConversationUser.getDeleted_at();

            if(conversation.getMessages().get(i).getSent_date_time().after(deleteConversationUser.getDeleted_at())) {
                messageListReturn.add(conversation.getMessages().get(i));
            }
        }

        return  messageListReturn;
    }


}
