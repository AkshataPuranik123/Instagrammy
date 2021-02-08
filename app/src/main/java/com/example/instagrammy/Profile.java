package com.example.instagrammy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
//import com.example.instagrammy.Adapter.MyFotoAdapter;
import com.bumptech.glide.request.RequestOptions;
import com.example.instagrammy.Adapter.MyFotoAdapter;
import com.example.instagrammy.Model.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class Profile extends AppCompatActivity {
    private Button logout;
    private FloatingActionButton addpictures;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    ImageView profilePicture;
    private TextView username, bio;
    FirebaseFirestore fStore;
    FirebaseAuth mAuth;
    String userId;
    StorageReference storageReference;
    String myUrl = "";
    String profileId;



    RecyclerView recyclerView;
    MyFotoAdapter myFotoAdapter;
    List<Post> postList;


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
        addpictures = findViewById(R.id.floatingActionButton);
        logout = findViewById(R.id.button7);
        storageReference = FirebaseStorage.getInstance().getReference();
        profileId = userId.toString();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        myFotoAdapter = new MyFotoAdapter(getApplicationContext(), postList);
        recyclerView.setAdapter(myFotoAdapter);


        //Load profile photo, username and bio on login or registration
        //Photo from Storage and username, bio from firestore
        DocumentReference documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(mAuth.getCurrentUser().getUid() + "/profile.jpg");
                username.setText(documentSnapshot.getString("Username"));
                bio.setText(documentSnapshot.getString("Bio"));

                RequestOptions options = new RequestOptions().error(R.drawable.profilepicture);

                //if profile picture is added else display default picture

                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(Profile.this)
                                .load(storageReference)
                                .circleCrop()
                                .into(profilePicture);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Glide.with(Profile.this)
                                .load(storageReference)
                                .circleCrop()
                                .apply(options)
                                .into(profilePicture);

                    }
                });







            }
        });


        //load photos
        myFotos();


        //Action when button is clicked
        logout.setOnClickListener(this::onClick);
        addpictures.setOnClickListener(this::onClick);
    }

    //Buttons functionality
    public void onClick (View v){
        switch (v.getId()) {
            case R.id.button7:
                Logout();
                break;
            case R.id.floatingActionButton:
                askCameraPermissions();
                break;
        }
    }

    private void Logout() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(getApplicationContext(),
                "Logged Out", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Profile.this, LoginActivity.class));
    }

    //For camera
    private void askCameraPermissions(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }else {
            openCamera();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CAMERA_PERM_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openCamera();
            }else {
                Toast.makeText(this, "Camera Permission is Required to Use camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera(){
        //starting camera and clicking image
        Intent camera_intent
                = new Intent(MediaStore
                .ACTION_IMAGE_CAPTURE);
        /*CropImage.activity()
                .setAspectRatio(1,1)
                .start(Profile.this);*/

        startActivityForResult(camera_intent, CAMERA_REQUEST_CODE);

    }
    @Override
    // This method will help to retrieve the image
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            //Uri imageUri = result.getUri();
            //Bitmap image = null;

            Bitmap image = (Bitmap) data.getExtras().get("data");
            upload(mAuth.getCurrentUser(), image);

        } else {
            Toast.makeText(this, "Somethings has gone Wrong!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /*private  String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }*/

    private void upload(FirebaseUser currentUser, Bitmap image) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.show();

        //image = ((BitmapDrawable) profileimage.getDrawable()).getBitmap();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        image.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        //final String currentUserId = UUID.randomUUID().toString();
        StorageReference imageRef = storageReference.child(currentUser.getUid()+"/"+System.currentTimeMillis()+".jpg");



        byte[] b = stream.toByteArray();
        imageRef.putBytes(b)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                        taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Uri downloadUri = uri;
                                myUrl = downloadUri.toString();

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

                                String postId = reference.push().getKey();

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("postId", postId);
                                hashMap.put("postImage", myUrl);
                                hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                //hashMap.put("Description", desc); //for caption

                                reference.child(postId).setValue(hashMap);
                            }
                        });

                        Toast.makeText(Profile.this, "Photo Uploaded", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(Profile.this, "Upload Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        myFotos();
    }



    private void myFotos(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot :dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    if (userId.equals(profileId)){
                        postList.add(post);
                    }
                }
                Collections.reverse(postList);
                myFotoAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }



}
