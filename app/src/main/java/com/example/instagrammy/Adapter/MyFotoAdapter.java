package com.example.instagrammy.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.bumptech.glide.Glide;
import com.example.instagrammy.CommentsActivity;
import com.example.instagrammy.Model.Post;
import com.example.instagrammy.R;
import com.example.instagrammy.ShowFullSizePhotos;

public class MyFotoAdapter extends RecyclerView.Adapter<MyFotoAdapter.ViewHolder> {

    private Context context;
    private List<Post> mPosts;


    public MyFotoAdapter(Context context, List<Post> mPosts){
        this.context = context;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.fotos_item, viewGroup, false);
        return new MyFotoAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Post post = mPosts.get(i);
        Glide.with(context).load(post.getPostImage()).into(viewHolder.post_image);
        viewHolder.relative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //on clicking the page see full view
                Intent intent = new Intent(context, CommentsActivity.class);
                intent.putExtra("postid", post.getPostId());
                intent.putExtra("publisherid", post.getPublisher());
                intent.putExtra("descriptionImage", post.getDescription());
                intent.putExtra("postImage", post.getPostImage());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                /*Intent intent = new Intent(context, ShowFullSizePhotos.class);
                intent.putExtra("image", post.getPostImage());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);*/

            }
        });

    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView post_image;
        public RelativeLayout relative;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            post_image = itemView.findViewById(R.id.post_image);
            relative = itemView.findViewById(R.id.relative);

        }
    }

}
