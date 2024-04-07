package com.example.BEChatAppCNM.services.servicesImpl;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class UserServiceImpl implements UserService {
    private static final String COLLECTION_NAME = "users";
    private final PasswordEncoder passwordEncoder;


    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

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

        Firestore firestore = FirestoreClient.getFirestore();

       firestore.collection(COLLECTION_NAME)
                .document()
                .create(userTemp);
       String token = jwtService.generateToken(userTemp);
        return token;
    }

    @Override
    public Optional<User> getUserDetailsByPhone(String phone_number) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

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
        Firestore firestore = FirestoreClient.getFirestore();

        firestore.collection(COLLECTION_NAME)
                .document(user.getPhone())
                .create(user);
    }

    @Override
    public boolean checkExistPhoneNumber(String phone_number) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);

        ApiFuture<QuerySnapshot> future = collectionReference.whereEqualTo("phone", phone_number).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();


        if(!documents.isEmpty()) {
            return true;
        } else return false;
    }

    @Override
    public String checkAccountSignIn(User user) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

            User userTemp = getUserDetailsByPhone(user.getPhone()).orElseThrow();
            return jwtService.generateToken(userTemp);
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi đăng nhập không thành công";
        }

    }
}
