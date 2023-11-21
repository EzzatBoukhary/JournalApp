package com.example.journal;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;

import model.Journal;
import util.JournalApi;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {

    private final int GALLERY_CODE = 1;

    // Connection to FireStore
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Journal");

    // Widgets
    private TextView currentUserTextView;
    private Button saveButton;
    private ProgressBar progressBar;
    private ImageView addPhotoButton, imageView;
    private EditText titleEditText, thoughtsEditText;
    private String currentUserId;
    private String currentUserName;
    private Uri imageUri;

    // FireBase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        // Action bar & status bar customization
        ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setElevation(0);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        bar.hide();
        //bar.setBackgroundDrawable(new ColorDrawable(Color.rgb(255,175,123)));

        // object instantiation
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        progressBar = findViewById(R.id.post_progress_bar);
        titleEditText = findViewById(R.id.post_title_et);
        thoughtsEditText = findViewById(R.id.post_description_et);
        currentUserTextView = findViewById(R.id.post_username_textview);
        imageView = findViewById(R.id.post_imageView);
        saveButton = findViewById(R.id.post_save_journal_button);
        saveButton.setOnClickListener(this);
        addPhotoButton = findViewById(R.id.postCameraButton);
        addPhotoButton.setOnClickListener(this);


        JournalApi journalApi = JournalApi.getInstance();

        if (journalApi != null) {
            currentUserId = journalApi.getUserId();
            currentUserName = journalApi.getUsername();

            currentUserTextView.setText(currentUserName);
        }

        // it's a good idea to have the listener even tho we don't really need it
        authStateListener = firebaseAuth -> {
            user = firebaseAuth.getCurrentUser();

            if (user != null) {

            } else {

            }
        };

    }

    @Override
    public void onClick(View view) {

        // Save Journal Button
        if (view.getId() == R.id.post_save_journal_button) {

            saveJournal();
        }

        // Add Image Button
        else if (view.getId() == R.id.postCameraButton) {

            // get an image from phone
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*"); // anything image related will do
            startActivityForResult(intent, GALLERY_CODE);
        }
    }

    private void saveJournal() {
        progressBar.setVisibility(View.VISIBLE);

        String title = titleEditText.getText().toString().trim();
        String thoughts = thoughtsEditText.getText().toString().trim();

        if (!title.isEmpty() &&
                !thoughts.isEmpty() &&
                imageUri != null) {

            // Create image file with unique ID and store file path
            final StorageReference filepath = storageReference // .../journal_images/our_image.jpeg
                    .child("journal_images") // create folder to hold images
                    .child("my_image" + Timestamp.now().getSeconds()); // my_image_881234

            // Put image into image file
            filepath.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {

                        // Retrieve Image file URI
                        filepath.getDownloadUrl().addOnSuccessListener(uri -> {

                            String imageUrl = uri.toString();

                            // Set up Journal Object
                            Journal journal = new Journal();
                            journal.setTitle(title);
                            journal.setThought(thoughts);
                            journal.setImageUrl(imageUrl);
                            journal.setTimeAdded(new Timestamp(new Date()));
                            journal.setUserName(currentUserName);
                            journal.setUserId(currentUserId);

                            // Create a document holding our journal data
                            collectionReference.add(journal)
                                    .addOnSuccessListener(documentReference -> {

                                        progressBar.setVisibility(View.INVISIBLE);
                                        startActivity(new Intent(PostJournalActivity.this, JournalListActivity.class));
                                        finish(); // get rid of this activity

                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d("PostJournalActivity", "saveJournal: " + e.getMessage());
                                    });
                        });

                    })
                    .addOnFailureListener(e -> {
                        Log.d("PostJournalActivity", "saveJournal: " + e.getMessage());
                        progressBar.setVisibility(View.INVISIBLE);

                    });
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Getting Image data from Camera Button intent result
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();
                imageView.setImageURI(imageUri); // show image
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}