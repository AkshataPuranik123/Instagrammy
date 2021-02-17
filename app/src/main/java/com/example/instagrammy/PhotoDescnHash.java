package com.example.instagrammy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/*import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.FirebaseVision;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;*/



/*import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;*/

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public class PhotoDescnHash extends AppCompatActivity {

    public static final int CAMERA_PERM_CODE = 101;

    Uri imageUri;
    Bitmap imageBitmap;
    SwitchCompat switchCompat;
    String myUrl = "";
    StorageReference storageReference;
    String profileId;
    FirebaseAuth mAuth;

    ImageView close, image_added;
    TextView post;
    EditText description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_descn_hash);

        close = findViewById(R.id.close);
        image_added = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);
        switchCompat = findViewById(R.id.switchCompat);
        mAuth = FirebaseAuth.getInstance();
        profileId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();



        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PhotoDescnHash.this, Profile.class));
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload(mAuth.getCurrentUser(),imageBitmap);
            }
        });

        askCameraPermissions();

        //Enable hashtags
        switchCompat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchCompat.isChecked()){
                    //enable MLKit
                    generateHashtags(imageBitmap);
                } else{
                    description.getText().clear();

                }
            }
        });


    }


    private void generateHashtags(Bitmap bitmap){
        int rotationDegree = 0;
        InputImage image = InputImage.fromBitmap(bitmap, rotationDegree);
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        labeler.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {
                        // Task completed successfully
                        // ...
                        for (ImageLabel label : labels) {
                            String text = label.getText();
                            float confidence = label.getConfidence();
                            int index = label.getIndex();
                            if (confidence >= 0.5){
                                description.append("#"+text+" ");
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                        Toast.makeText(PhotoDescnHash.this, "Hashtags cannot be generated", Toast.LENGTH_SHORT).show();
                    }
                });
    }




    //Buttons functionality
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
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(PhotoDescnHash.this);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                imageBitmap = null;
                try {
                    imageBitmap = getThumbnail(imageUri);
                    image_added.setImageBitmap(imageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();

            }
        } else {
            Toast.makeText(this, "Something has gone wrong!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PhotoDescnHash.this, Profile.class));
            finish();
        }
    }


    public Bitmap getThumbnail(Uri uri) throws FileNotFoundException, IOException{
        InputStream input = this.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither=true;//optional
        onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
            return null;
        }

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;
        int THUMBNAIL_SIZE = 1024;

        double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true; //optional
        bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//
        input = this.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
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

        //image = ((BitmapDrawable) profileImage.getDrawable()).getBitmap();

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
                                hashMap.put("publisher", profileId);
                                hashMap.put("description", description.getText().toString()); //for caption

                                reference.child(postId).setValue(hashMap);
                            }
                        });

                        Toast.makeText(PhotoDescnHash.this, "Photo Uploaded", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(PhotoDescnHash.this, "Upload Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        startActivity(new Intent(PhotoDescnHash.this, Profile.class));
    }

}