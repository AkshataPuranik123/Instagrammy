package com.example.instagrammy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class Registration extends AppCompatActivity implements View.OnClickListener {


    public static final String TAG = "TAG";
    //variable declaration
    //firebase
    FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    String userId;
    //firestorage
    StorageReference storageReference;
    //camera
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    private Bitmap image;
    ImageView profileimage;
    Uri pickedImgUri;
    //text fields
    private TextInputLayout textInputEmail;
    private TextInputLayout textInputPass;
    private TextInputLayout textInputConfirmPass;
    private TextInputLayout textInputUsername;
    private TextInputLayout textInputBio;
    private CheckBox cb;
    private ProgressBar pb;

    //buttons
    Button camera_open_id, sign_up;


    //method to check password
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
            "(?=.*[0-9])" +         //at least 1 digit
            "(?=.*[a-z])" +         //at least 1 lower case letter
            "(?=.*[A-Z])" +         //at least 1 upper case letter
            //"(?=.*[a-zA-Z])" +      //any letter
            "(?=.*[@#$%^&+=])" +    //at least 1 special character
            "(?=\\S+$)" +           //no white spaces
            ".{4,}" +               //at least 4 characters
            "$");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        //assigning variables
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        profileimage = (ImageView) findViewById(R.id.imageView3);
        textInputEmail = (TextInputLayout) findViewById(R.id.editTextTextEmailAddress);
        textInputPass = (TextInputLayout) findViewById(R.id.editTextTextPassword);
        textInputConfirmPass = (TextInputLayout) findViewById(R.id.editTextTextPassword1);
        textInputUsername = (TextInputLayout) findViewById(R.id.editText);
        textInputBio = (TextInputLayout) findViewById(R.id.editText1);
        cb = (CheckBox) findViewById(R.id.checkBox);
        camera_open_id = (Button) findViewById(R.id.camera);
        sign_up = (Button) findViewById(R.id.signUp);
        pb = (ProgressBar) findViewById(R.id.progressBar);
        storageReference = FirebaseStorage.getInstance().getReference();


        // Camera_open button is for open the camera
        // and add the setOnClickListener in this button
        camera_open_id.setOnClickListener(this);
        cb.setOnClickListener(this);
        sign_up.setOnClickListener(this);


        //To check if already logged in and direct to profile
        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(Registration.this, Profile.class));
            finish();
        }
    }

    //Main event
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.camera:
                askCameraPermissions();
                break;
            case R.id.checkBox:
                sign_up.setEnabled(true);
                break;
            case R.id.signUp:
                registerUser();
                break;

        }
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
        //Toast.makeText(this, "Camera Open request", Toast.LENGTH_SHORT).show();
        Intent camera_intent
                = new Intent(MediaStore
                .ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera_intent, CAMERA_REQUEST_CODE);
    }
    @Override
    // This method will help to retrieve the image
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            image = getCroppedBitmap(image);
            profileimage.setImageBitmap(image);
        }
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }




    // For details
    // Validate email format
    private boolean validateEmail(){
            String emailInput = textInputEmail.getEditText().getText().toString().trim();
            if (emailInput.isEmpty()) {
                textInputEmail.setError("Field can't be empty");
                return false;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()){
                textInputEmail.setError("Please enter a valid email address");
                return false;
            } else {
                textInputEmail.setError(null);
                return true;
            }
    }

    //For Password
    private boolean validatePassword(){
            String passwordInput = textInputPass.getEditText().getText().toString().trim();
            if (passwordInput.isEmpty()) {
                textInputPass.setError("Field can't be empty");
                return false;
            } if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
            textInputPass.setError("Password must have atleast 4 characters; one uppercase, one lowercase, one digit, one special character and no whitespaces.");
            return false;
        }else {
                textInputPass.setError(null);
                return true;
            }
    }

    // To make sure confirm password is not empty
    private boolean validatePassword1() {
            String passwordInput = textInputConfirmPass.getEditText().getText().toString().trim();
            if (passwordInput.isEmpty()) {
                textInputConfirmPass.setError("Field can't be empty");
                return false;
            } else {
                textInputConfirmPass.setError(null);
                return true;
            }
    }

    // For matching both the passwords
    private boolean matchPasswords(){
        if(!(!textInputPass.getEditText().getText().toString().equals(textInputConfirmPass.getEditText().getText().toString()) ||
                textInputPass.getEditText().getText().toString().isEmpty() || textInputConfirmPass.getEditText().getText().toString().isEmpty())) {
            //Toast.makeText(getApplicationContext(),
            //        "Passwords Match", Toast.LENGTH_SHORT).show();
            return true;
        }else{
            Toast.makeText(getApplicationContext(), "Passwords does not match",Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // Valid username
    private boolean validateUsername(){
        String usernameInput = textInputUsername.getEditText().getText().toString().trim();
        if (usernameInput.isEmpty()) {
            textInputUsername.setError("Field can't be empty");
            return false;
        } else if (usernameInput.length() > 15) {
            textInputUsername.setError("Username too long");
            return false;
        } else {
            textInputUsername.setError(null);
            return true;
        }
    }


    //Valid bio
    private boolean validateBio() {
            String bioInput = textInputBio.getEditText().getText().toString().trim();
            if (bioInput.isEmpty()) {
                textInputBio.setError("Field can't be empty");
                return false;
            } else {
                textInputBio.setError(null);
                return true;
            }
    }

    private void updateUserInfo(Uri pickedImgUri, FirebaseUser currentUser) {
        StorageReference mstorage = FirebaseStorage.getInstance().getReference().child("user_photos");
        StorageReference imageFilePath = mstorage.child(pickedImgUri.getLastPathSegment());
        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image uploaded successfully
                //now get url
                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //url contain user image url
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();
                        currentUser.updateProfile(profileUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            //user info updated successfully
                                            Toast.makeText(Registration.this, "Photo updated successfully!", Toast.LENGTH_LONG).show();

                                        }

                                    }
                                });
                    }
                });
            }
        });
    }
    private void uploadImageToFirebase(Uri imageUri) {
        final StorageReference image = storageReference.child("profile.jpg");
        image.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Toast.makeText(Registration.this, "Image Uploaded.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Registration.this, "Upload Failled.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private String upload(FirebaseUser currentUser) {

        image = ((BitmapDrawable) profileimage.getDrawable()).getBitmap();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        image.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        //final String currentUserId = UUID.randomUUID().toString();
        StorageReference imageRef = storageReference.child(currentUser.getUid()+"/profile.jpg");
        String refLink = imageRef.toString();


        byte[] b = stream.toByteArray();
        imageRef.putBytes(b)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                        taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Uri downloadUri = uri;

                            }
                        });

                        Toast.makeText(Registration.this, "Photo Uploaded", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(Registration.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                    }
                });
        return refLink;
    }



    //Check all constraints before moving on
    private void registerUser() {
        if (!validateEmail() | !validateUsername() | !validatePassword() | !validatePassword1() | !validateBio() | !matchPasswords()) {
            return;
        }
        String email = textInputEmail.getEditText().getText().toString().trim();
        String password = textInputPass.getEditText().getText().toString().trim();
        String username = textInputUsername.getEditText().getText().toString().trim();
        String bio = textInputBio.getEditText().getText().toString().trim();
        pb.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Registration.this, "User registered successfully!", Toast.LENGTH_LONG).show();

                    String refLink = upload(mAuth.getCurrentUser());

                    userId = mAuth.getCurrentUser().getUid();
                    DocumentReference documentReference = fStore.collection("users").document(userId);
                    Map<String,Object> user = new HashMap<>();
                    user.put("Username", username);
                    user.put("Bio", bio);
                    user.put("ProfilePicture", refLink);

                    //updateUserInfo(pickedImgUri, mAuth.getCurrentUser());


                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: User Profile is created for " + userId);

                        }
                    });


                    pb.setVisibility(View.GONE);
                    startActivity(new Intent(Registration.this, Profile.class));

                } else {
                    Toast.makeText(Registration.this, "Failed to register! Try again! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                pb.setVisibility(View.GONE);
            }


        });
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }



}