package com.example.journal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import adapter.RecyclerViewAdapter;
import model.Journal;
import util.JournalApi;

public class JournalListActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference = db.collection("Journal");

    private TextView noJournalEntry;
    private ImageButton shareButton;

    private List<Journal> journalList;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);

        // Action bar customization
        ActionBar bar = getSupportActionBar();
        assert bar != null;
        //bar.setElevation(0);
        //bar.setBackgroundDrawable(new ColorDrawable(Color.rgb(255,175,123)));
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        bar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        bar.setTitle("");
        //bar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.my_rect));

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        noJournalEntry = findViewById(R.id.list_no_thoughts);
        journalList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        // todo: add click listener to adapter
        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(JournalListActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
            }
        });
//        shareButton = findViewById(R.id.journal_row_share_button);
//        shareButton.setOnClickListener(view -> {
//            Intent intent = new Intent();
//            intent.setAction(Intent.ACTION_SEND);
//            intent.setType("text/plain");
//
//        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            // Take users to add Journal
            if (user != null & auth != null) {
                startActivity(new Intent(JournalListActivity.this, PostJournalActivity.class));
                //finish();
            }

        } else if (item.getItemId() == R.id.action_sign_out) {
            // Sign user out
            if (user != null & auth != null) {
                auth.signOut();

                startActivity(new Intent(JournalListActivity.this, MainActivity.class));
                finish();
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // get all data
        collectionReference.whereEqualTo("userId", JournalApi.getInstance().
                getUserId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot journals : queryDocumentSnapshots) {
                            Journal journal = journals.toObject(Journal.class);
                            journalList.add(journal);
                        }

                        // Invoke RecyclerView
                        recyclerViewAdapter = new RecyclerViewAdapter(JournalListActivity.this, journalList);
                        recyclerView.setAdapter(recyclerViewAdapter);
                        recyclerViewAdapter.notifyDataSetChanged();

                    } else {
                        noJournalEntry.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {

                });
    }
}