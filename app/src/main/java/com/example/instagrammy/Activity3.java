package com.example.instagrammy;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class Activity3 extends AppCompatActivity {
    private Button logout;
    //TextView t1 = (TextView)findViewById(R.id.textView6);
    //TextView t2 = (TextView)findViewById(R.id.textView7);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);

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