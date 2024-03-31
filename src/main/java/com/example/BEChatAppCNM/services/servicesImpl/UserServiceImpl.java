package com.example.BEChatAppCNM.services.servicesImpl;

import com.example.BEChatAppCNM.entities.User;
import com.example.BEChatAppCNM.services.UserService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class UserServiceImpl implements UserService {
    private static final String COLLECTION_NAME = "users";

    @Override
    public String saveUser(User user) throws ExecutionException, InterruptedException {
        Firestore firestore = FirestoreClient.getFirestore();

       firestore.collection(COLLECTION_NAME)
                .document()
                .create(user);
        return user.getPhone();
    }

    @Override
    public Optional<User> getUserDetails(String phone_number) throws ExecutionException, InterruptedException {
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
    public boolean checkAccountSignIn(User user) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference collectionReference = db.collection(COLLECTION_NAME);

        ApiFuture<QuerySnapshot> futurePhone = collectionReference.whereEqualTo("phone", user.getPhone()).get();
        List<QueryDocumentSnapshot> documentsPhone = futurePhone.get().getDocuments();

        ApiFuture<QuerySnapshot> futurePass = collectionReference.whereEqualTo("password", user.getPassword()).get();
        List<QueryDocumentSnapshot> documentsPass = futurePass.get().getDocuments();
        
        if(documentsPhone.isEmpty() || documentsPass.isEmpty()) {
            return false;
        }
        return true;
    }
}
