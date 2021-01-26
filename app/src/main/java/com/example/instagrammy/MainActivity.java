package com.example.instagrammy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText emailT,passwordT;
    private Button login, signup;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fStore = FirebaseFirestore.getInstance();
        login = (Button) findViewById(R.id.button);
        signup = (Button) findViewById(R.id.button2);
        emailT = (EditText) findViewById(R.id.editTextTextEmailAddress);
        passwordT = (EditText) findViewById(R.id.editTextTextPassword);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        mAuth = FirebaseAuth.getInstance();
        login.setOnClickListener(this);
        signup.setOnClickListener(this);


    }
    //Main event
    public void onClick(View v) {
            switch (v.getId()){
                case R.id.button:
                    userLogin();
                    break;
                case R.id.button2:
                    startActivity(new Intent(MainActivity.this, Activity2.class));
                    break;

            }
    }

    private void userLogin(){
        String email = emailT.getText().toString().trim();
        String password = passwordT.getText().toString().trim();
        if(email.isEmpty()){
            emailT.setError("Email is required!");
            emailT.requestFocus();
            return;
        }
        if(password.isEmpty()){
            passwordT.setError("Password is required!");
            passwordT.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailT.setError("Please enter a valid email!");
            emailT.requestFocus();
            return;
        }
        if(password.length()<4){
            passwordT.setError("Min password length is 4 characters!");
            passwordT.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {

                Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_LONG).show();
                //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                //fetchData();

                
                //redirect to user profile
                startActivity(new Intent(MainActivity.this, Activity3.class));
                progressBar.setVisibility(View.GONE);

                    //if user id does not exists then message: sign up as a new user
            } else {
                Toast.makeText(MainActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });



    }

    public void fetchData() {
        DocumentReference  documentReference = fStore.collection("users").document("0Wjoz8MqNYfEsyeh0KkFCGAJgw22");
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){

                }else{
                    Toast.makeText(getApplicationContext(),"Row Not found",Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Failed to fetch data", Toast.LENGTH_LONG).show();
            }
        });
    }


}