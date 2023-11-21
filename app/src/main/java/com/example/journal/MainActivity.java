package com.example.journal;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import util.JournalApi;

public class MainActivity extends AppCompatActivity {

    private Button getStartedButton;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        authStateListener = firebaseAuth -> {
            currentUser = auth.getCurrentUser();

            if (currentUser != null) {
                String currentUserId = currentUser.getUid();

                collectionReference.whereEqualTo("userId", currentUserId)
                        .addSnapshotListener((value, error) -> {
                            if (error != null) {
                                return;
                            }
                            String name;

                            if (!value.isEmpty()) {
                                for (QueryDocumentSnapshot snapshot : value) {
                                    JournalApi journalApi = JournalApi.getInstance();
                                    journalApi.setUserId(snapshot.getString("userId"));
                                    journalApi.setUsername(snapshot.getString("username"));

                                    startActivity(new Intent(MainActivity.this, JournalListActivity.class));
                                    finish();
                                }
                            }
                        });
            } else {

            }
        };

        getStartedButton = findViewById(R.id.startButton);

        // Action bar customization
        ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setElevation(0);
        bar.hide();
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        getStartedButton.setOnClickListener(view -> {
            // go to LoginActivity
            startActivity(new Intent(MainActivity.this,
                    LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = auth.getCurrentUser();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (auth != null) {
            auth.removeAuthStateListener(authStateListener);
        }
    }
}