package com.example.instagrammy;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Activity3 extends AppCompatActivity {
    private Button logout;
    private TextView username, bio;
    //private FirebaseFirestore fStore;
    //private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);


        //username = (TextView)findViewById(R.id.textView6);
        //bio = (TextView)findViewById(R.id.textView7);
        //fStore = FirebaseFirestore.getInstance();
        //userRef = fStore.getRefe               .collection("users");







        logout = (Button)findViewById(R.id.button7);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getApplicationContext(),
                        "Logged Out",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Activity3.this, MainActivity.class));
            }
        });

        //Intent intent = getIntent();
        //String displayname = intent.getExtras().getString("name");
        //t1.setText(displayname);
        //String displaybio = intent.getExtras().getString("bio");
        //t2.setText(displaybio);
    }
}