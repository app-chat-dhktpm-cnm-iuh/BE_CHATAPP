package com.example.BEChatAppCNM.services.servicesImpl;

import com.example.BEChatAppCNM.entities.dto.FriendRequest;
import com.example.BEChatAppCNM.entities.Friend;
import com.example.BEChatAppCNM.entities.Role;
import com.example.BEChatAppCNM.entities.User;
import com.example.BEChatAppCNM.services.UserService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class UserServiceImpl implements UserService {
    private static final String COLLECTION_NAME = "users";
    private final PasswordEncoder passwordEncoder;


    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    Firestore db = FirestoreClient.getFirestore();

    public UserServiceImpl(PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public String saveUser(User user) throws ExecutionException, InterruptedException {
        User userTemp = User.builder()
                .phone(user.getPhone())
                .name(user.getName())
                .password(passwordEncoder.encode(user.getPassword()))
                .role(Role.valueOf("USER"))
                .build();

       db.collection(COLLECTION_NAME)
                .document()
                .create(userTemp);
       String token = jwtService.generateToken(userTemp);
        return token;
    }

    @Override
    public Optional<User> getUserDetailsByPhone(String phone_number) throws ExecutionException, InterruptedException {

        CollectionReference documentReference = db.collection(COLLECTION_NAME);

        ApiFuture<QuerySnapshot> snapshotApiFuture = documentReference.whereEqualTo("phone", phone_number).get();
        List<User> user = snapshotApiFuture.get().toObjects(User.class);
        Optional<User> users = user.stream().findFirst();
        if(!users.isEmpty()) {
            return users;
        } else return null;
    }

    @Override
    public void updateUserDetails(User user) {
        db.collection(COLLECTION_NAME)
                .document(user.getPhone())
                .create(user);
    }

    @Override
    public void updateStatusUser(boolean status, String phone) throws ExecutionException, InterruptedException {

        String documentId = getDocumentIdsByFieldValue(phone);
        db.collection(COLLECTION_NAME).document(documentId).update("_activated", status);
    }

    @Override
    public void addFriend(FriendRequest friendRequest) throws ExecutionException, InterruptedException {
        String documentIdSender = getDocumentIdsByFieldValue(friendRequest.getSender_phone());
        String documentIdReceiver = getDocumentIdsByFieldValue(friendRequest.getReceiver_phone());

        Friend senderFriend = Friend.builder()
                .phone_user(friendRequest.getReceiver_phone())
                .is_blocked(false)
                .build();

        Friend receiverFriend = Friend.builder()
                .phone_user(friendRequest.getSender_phone())
                .is_blocked(false)
                .build();

        db.collection(COLLECTION_NAME).document(documentIdSender).update("friends_list", FieldValue.arrayUnion(senderFriend));
        db.collection(COLLECTION_NAME).document(documentIdReceiver).update("friends_list", FieldValue.arrayUnion(receiverFriend));
    }

    public String getDocumentIdsByFieldValue( Object value) throws ExecutionException, InterruptedException {
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);

        ApiFuture<QuerySnapshot> future = collectionReference.whereEqualTo("phone", value).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        QueryDocumentSnapshot documentSnapshot = documents.get(0);
        String documentId = documentSnapshot.getId();

        return documentId;
    }

    @Override
    public boolean checkExistPhoneNumber(String phone_number) throws ExecutionException, InterruptedException {
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);

        ApiFuture<QuerySnapshot> future = collectionReference.whereEqualTo("phone", phone_number).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        if(!documents.isEmpty()) {
            return true;
        } else return false;
    }

    @Override
    public String checkAccountSignIn(User user) throws ExecutionException, InterruptedException {
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

            User userTemp = getUserDetailsByPhone(user.getPhone()).orElseThrow();
            return jwtService.generateToken(userTemp);
    }
}
