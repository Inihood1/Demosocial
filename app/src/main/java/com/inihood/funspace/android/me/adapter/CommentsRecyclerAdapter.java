package com.inihood.funspace.android.me.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.inihood.funspace.android.me.interfaces.OnCommentClicked;
import com.inihood.funspace.android.me.model.Comments;
import com.inihood.funspace.android.me.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {

    public List<Comments> commentsList;
    public Context context;
    private OnCommentClicked onCommentClicked;
    private FirebaseAuth firebaseAuth;
    private String current_user_id;

    public CommentsRecyclerAdapter(List<Comments> commentsList, OnCommentClicked onCommentClicked){
        this.commentsList = commentsList;
        this.onCommentClicked = onCommentClicked;

    }

    @Override
    public CommentsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
        context = parent.getContext();
        firebaseAuth = FirebaseAuth.getInstance();
        return new CommentsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CommentsRecyclerAdapter.ViewHolder holder, int position) {
        String commentMessage = commentsList.get(position).getMessage();
        holder.setComment_message(commentMessage);
        String user_image = commentsList.get(position).getImage();
        String user_name = commentsList.get(position).getName();
        String time = commentsList.get(position).getTime();
        final String user_id = commentsList.get(position).getUser_id();
        if (user_image != null && user_name != null && time != null){
            holder.setCommentUserDeatails(user_image, user_name, time);
        }


        if (firebaseAuth.getCurrentUser() != null){
            current_user_id = firebaseAuth.getCurrentUser().getUid();
        }

        holder.comment_user_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCommentClicked != null) {
                    onCommentClicked.onUsersCliked(user_id, current_user_id);

                }
            }
        });

        holder.comment_user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCommentClicked != null) {
                    onCommentClicked.onUsersCliked(user_id, current_user_id);

                }
            }
        });

    }


    @Override
    public int getItemCount() {

        if(commentsList != null) {

            return commentsList.size();

        } else {

            return 0;

        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView comment_message;
        private CircleImageView comment_user_image;
        private TextView comment_user_name;
        private TextView time;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            comment_user_image = mView.findViewById(R.id.comment_image);
            comment_user_name = mView.findViewById(R.id.comment_username);
            time = mView.findViewById(R.id.time);
        }

        public void setCommentUserDeatails(String image, String name, String timetxt){

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.profile_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(comment_user_image);

            comment_user_name.setText(name);
            time.setText(timetxt);
        }

        public void setComment_message(String message){

            comment_message = mView.findViewById(R.id.comment_message);
            comment_message.setText(message);

        }

    }

}
