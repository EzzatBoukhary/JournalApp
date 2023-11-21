package com.example.journal;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

import util.JournalApi;

public class LoginActivity extends AppCompatActivity {

    // FireStore Connection
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");
    // Widgets
    private Button loginButton, createAcctButton;
    private EditText passwordEditText;
    private ProgressBar progressBar;
    private AutoCompleteTextView emailAddress;
    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Action bar customization
        ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setElevation(0);
        bar.hide();
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        firebaseAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.login_progress_bar);
        passwordEditText = findViewById(R.id.password);
        emailAddress = findViewById(R.id.email);
        loginButton = findViewById(R.id.email_sign_in_button);
        createAcctButton = findViewById(R.id.create_acct_button_login);

        createAcctButton.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
        });

        loginButton.setOnClickListener(view -> {
            loginEmailPasswordUser(emailAddress.getText().toString().trim(),
                    passwordEditText.getText().toString().trim());
        });
    }

    private void loginEmailPasswordUser(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);

        if (!email.isEmpty()
                && !password.isEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        assert user != null;
                        String currentUserId = user.getUid();

                        // Loop through the documents and search for user id
                        collectionReference
                                .whereEqualTo("userId", currentUserId)
                                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                                    if (error != null) {
                                    }

                                    assert queryDocumentSnapshots != null;
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                            JournalApi journalApi = JournalApi.getInstance();
                                            journalApi.setUsername(snapshot.getString("username"));
                                            journalApi.setUserId(snapshot.getString("userId"));

                                            // Go to ListActivity
                                            startActivity(new Intent(LoginActivity.this, JournalListActivity.class));
                                        }
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.INVISIBLE);

                    });
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
        }
    }
}