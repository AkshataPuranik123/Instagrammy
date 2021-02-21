package com.example.instagrammy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class ShowFullSizePhotos extends AppCompatActivity {
    ImageView imageView;
    String image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.activity_show_full_size_photos);
        ImageView imageView = (ImageView) findViewById(R.id.fullScreenimageView);
        image = getIntent().getExtras().getString("image");
        Log.e("Image link", image);
        Glide.with(ShowFullSizePhotos.this)
                .load(image)
                .placeholder(R.drawable.profilepicture)
                .into(imageView);
        //Resources res = getResources();
        //int resId = res.getIdentifier(image , "drawable", getPackageName());
        //imageView.setImageURI(Uri.parse(image));*/
    }
}