package com.example.instagrammy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.Objects;


public class Profile extends AppCompatActivity {
    private Button logout;
    private TextView username, bio;
    FirebaseFirestore fStore;
    FirebaseAuth mAuth;
    String userId;
    ImageView profilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);


        username = findViewById(R.id.textView6);
        bio = findViewById(R.id.textView7);
        profilePicture = findViewById(R.id.imageView2);
        fStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();


        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(mAuth.getCurrentUser().getUid()+"/profile.jpg");
                username.setText(documentSnapshot.getString("Username"));
                bio.setText(documentSnapshot.getString("Bio"));

                Glide.with(Profile.this)
                        .load(storageReference)
                        .circleCrop()
                        .into(profilePicture);
            }
        });






        logout = findViewById(R.id.button7);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getApplicationContext(),
                        "Logged Out",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Profile.this, LoginActivity.class));
            }
        });

    }
}
