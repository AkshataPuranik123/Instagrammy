package com.example.instagrammy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.instagrammy.Adapter.CommentAdapter;
import com.example.instagrammy.Model.Comment;
import com.example.instagrammy.Model.Post;
import com.example.instagrammy.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.instagrammy.Registration.TAG;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommentAdapter commentsAdapter;
    private List<Comment> commentList;

    EditText addcomment;
    ImageView image_profile, image_bitmap, deleteButton;
    TextView post, description;

    String postid;
    String publisherid;
    String descriptionImage;
    String postImage;
    String userId;

    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();*/
        setContentView(R.layout.activity_comments);

        /*Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/

        addcomment = findViewById(R.id.add_comment);
        image_profile = findViewById(R.id.image_profile);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);
        image_bitmap = findViewById(R.id.image_bitmap);
        deleteButton = findViewById(R.id.deleteButton);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        commentList = new ArrayList<>();
        commentsAdapter = new CommentAdapter(this, commentList);
        recyclerView.setAdapter(commentsAdapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = firebaseUser.getUid();

        Intent intent = getIntent();
        postid = intent.getStringExtra("postid");
        publisherid = intent.getStringExtra("publisherid");
        descriptionImage = intent.getStringExtra("descriptionImage");
        postImage = intent.getStringExtra("postImage");

        if (userId.equals(publisherid)){
            deleteButton.setVisibility(View.VISIBLE);
        }


        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addcomment.getText().toString().equals("")){
                    Toast.makeText(CommentsActivity.this, "Please enter something", Toast.LENGTH_SHORT).show();
                } else {
                    addcomment();
                }
            }
        });


        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String imageName = getImageName(postImage);

                //delete post from storage
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(userId +"/"+ imageName);


                storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //delete post info from database
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(String.valueOf(postid));
                        reference.removeValue();
                        //delete its comments
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Comments").child(String.valueOf(postid));
                        databaseReference.removeValue();

                        Toast.makeText(getApplicationContext(),"Photo deleted", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Oops! Something went wrong! Try again.", Toast.LENGTH_LONG).show();

                    }
                });

                //return to Profile page
                startActivity(new Intent(CommentsActivity.this, Profile.class));
            }
        });

        getImage();
        getImageBitmap();
        getDesc();
        readComments();
    }
    private void addcomment(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment", addcomment.getText().toString());
        hashMap.put("publisher", firebaseUser.getUid());

        reference.push().setValue(hashMap);
        addcomment.setText("");
    }

    private void getImage(){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "/profile.jpg");
        Glide.with(getApplicationContext()).load(storageReference).into(image_profile);

    }
    private void getImageBitmap(){

        Glide.with(getApplicationContext()).load(postImage).into(image_bitmap);
    }

    private void getDesc() {

        if (descriptionImage != null) {
            description.setText(descriptionImage);
        } else {
            description.setText("No Description");
        }
    }

    public String getImageName(String imageLink){

        String name = null;
        Pattern pattern = Pattern.compile("([0-9]{13})(?:.jpg)");
        Matcher matcher = pattern.matcher(imageLink);
        if (matcher.find()){
            name = matcher.group(0);
            Log.d(TAG, "Filename" + name);
        }

        return name;
    }

    private void readComments(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Comment comment = snapshot.getValue(Comment.class);
                    commentList.add(comment);
                }
                commentsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}