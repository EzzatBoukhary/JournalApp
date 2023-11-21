package com.example.journal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import util.JournalApi;

public class CreateAccountActivity extends AppCompatActivity {

    // Widgets
    private Button createAcctButton;
    private EditText emailEditText, passwordEditText, userNameEditText;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    // FireStore Connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Action bar customization
        ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setElevation(0);
        bar.hide();
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        firebaseAuth = FirebaseAuth.getInstance();

        createAcctButton = findViewById(R.id.create_acct_button);
        progressBar = findViewById(R.id.create_acct_progress);
        emailEditText = findViewById(R.id.email_account);
        passwordEditText = findViewById(R.id.password_account);
        userNameEditText = findViewById(R.id.username_account);

        authStateListener = firebaseAuth -> {
            currentUser = firebaseAuth.getCurrentUser();

            if (currentUser != null) {
                // user is already logged in...
            } else {
                // no user yet...
            }
        };

        createAcctButton.setOnClickListener(view -> {
            if (!emailEditText.getText().toString().isEmpty()
            && !passwordEditText.getText().toString().isEmpty()
            && !userNameEditText.getText().toString().isEmpty()) {

                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String username = userNameEditText.getText().toString().trim();

                createUserEmailAccount(email, password, username);

            } else {
                Toast.makeText(CreateAccountActivity.this, "Empty Fields Not Allowed", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createUserEmailAccount(String email, String password, String username) {
        if (!email.isEmpty()
        && !password.isEmpty()
        && !username.isEmpty()) {

            progressBar.setVisibility(View.VISIBLE);

            // Create authenticated user
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            currentUser = firebaseAuth.getCurrentUser();
                            assert currentUser != null;
                            String currentUserId = currentUser.getUid();

                            // create a user document
                            Map<String, String> userObj = new HashMap<>();
                            userObj.put("userId", currentUserId);
                            userObj.put("username", username);

                            collectionReference.add(userObj)
                                    .addOnSuccessListener(documentReference -> {

                                        // Get document data and go to next activity
                                        documentReference.get()
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.getResult().exists()) {
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        String name = task1.getResult()
                                                                .getString("username");

                                                        JournalApi journalApi = JournalApi.getInstance(); // Global API
                                                        journalApi.setUserId(currentUserId);
                                                        journalApi.setUsername(name);

                                                        Intent intent = new Intent(CreateAccountActivity.this, PostJournalActivity.class);
                                                        intent.putExtra("username", name);
                                                        intent.putExtra("userId", currentUserId);
                                                        startActivity(intent);

                                                    } else {

                                                    }
                                                });

                                    })
                                    .addOnFailureListener(e -> {
                                        progressBar.setVisibility(View.INVISIBLE);
                                    });

                            // we take user to AddJournalActivity
                        } else {
                            // something went wrong
                        }
                    })
                    .addOnFailureListener(e -> {

                    });

        } else {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}