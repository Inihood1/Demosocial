package com.inihood.funspace.android.me.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.interfaces.OnInterestClicked;
import com.inihood.funspace.android.me.model.Comments;
import com.inihood.funspace.android.me.model.InterestDetail;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class InterestDetailAdapter extends RecyclerView.Adapter<InterestDetailAdapter.InterestViewHolder> {

    private View view;
    private List<InterestDetail> interestDetailList;
    public OnInterestClicked onInterestClicked;
    private int lastPosition = -1;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public InterestDetailAdapter(List<InterestDetail> interestDetailList, OnInterestClicked onInterestClicked){
        this.interestDetailList = interestDetailList;
        this.onInterestClicked = onInterestClicked;
    }

    @NonNull
    @Override
    public InterestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new InterestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InterestViewHolder holder, int position) {
        Animation animation = AnimationUtils.loadAnimation(context,
                (position > lastPosition) ? R.anim.up_from_bottom
                        : R.anim.down_from_top);
        holder.itemView.startAnimation(animation);
        lastPosition = position;
        bindData(holder, position, onInterestClicked);
    }

    private void bindData(final InterestViewHolder holder, int position, final OnInterestClicked onInterestClicked) {

        final String blogPostId = interestDetailList.get(position).BlogPostId;
        final String blogPostUserId = interestDetailList.get(position).getUser_id();
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();

        String desc_data = interestDetailList.get(position).getDesc();
        if (desc_data != null){
            holder.setDescText(desc_data);
        }

        String username = interestDetailList.get(position).getName();
        String userimage = interestDetailList.get(position).getUser_image();
        if (username != null && userimage != null){
            holder.setUserData(username, userimage);
        }

        String image_url = interestDetailList.get(position).getImage_url();
        String thumbUri = interestDetailList.get(position).getImage_thumb();
        if (image_url != null && thumbUri != null){
            holder.setBlogImage(image_url, thumbUri);
        }


        try {
            //Get Likes Count
            firebaseFirestore.collection("Interest/" +" /Post"+ blogPostId + "/Likes").
                    addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                            try {

                                if (!documentSnapshots.isEmpty()) {

                                    int count = documentSnapshots.size();

                                    holder.updateLikesCount(count, blogPostId, currentUserId);

                                } else {

                                    holder.updateLikesCount(0, blogPostId, currentUserId);

                                }
                            }catch (Exception e1){
                                e1.printStackTrace();
                            }

                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            //Get comment Count
            firebaseFirestore.collection("Interest/" +" /Post"+ blogPostId + "/Comments").
                    addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                            try {

                                if (!documentSnapshots.isEmpty()) {

                                    int count = documentSnapshots.size();

                                    holder.updateCommentCount(count);

                                } else {

                                    holder.updateCommentCount(0);

                                }
                            }catch (Exception e1){
                                e1.printStackTrace();
                            }

                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }

        //Get Likes
        try {

            firebaseFirestore.collection("Interest/" +" /Post"+ blogPostId + "/Likes").document(currentUserId).
                    addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            try {

                                if (documentSnapshot.exists()) {

                                    holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.drawable.action_like_accent));

                                } else {

                                    holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.drawable.action_like_gray));

                                }
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }

                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }

        //Likes Feature
        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (onInterestClicked != null) {
                    onInterestClicked.onPostLike(blogPostId, currentUserId);

                }
            }
        });

        holder.blogCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onInterestClicked != null) {
                    onInterestClicked.onComment(blogPostId);

                }

            }
        });

        holder.blogImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onInterestClicked != null) {
                    onInterestClicked.onBlogImageSelected(blogPostId);

                }
            }
        });

        holder.blogUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (onInterestClicked != null) {
                    onInterestClicked.onUserSelected(blogPostUserId, currentUserId, blogPostUserId);
                }
            }
        });

        holder.blogUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (onInterestClicked != null) {
                    onInterestClicked.onUserSelected(blogPostUserId, currentUserId, blogPostUserId);
                }

            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onInterestClicked != null) {
                    onInterestClicked.onPostSelected(blogPostId);

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return interestDetailList.size();
    }

    public class InterestViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView descView;
        private ImageView blogImageView;
        private TextView blogDate;
        private TextView blogUserName;
        private CircleImageView blogUserImage;
        private ImageView blogLikeBtn;
        private TextView blogLikeCount;
        private ImageView blogCommentBtn;
        private ConstraintLayout commentViewContainer;
        private CircleImageView commentUserImage;
        private TextView userComment;
        private TextView userCommentName;
        private TextView blog_comment_count;

        public InterestViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            blogUserImage = mView.findViewById(R.id.blog_user_image);
            blogUserName = mView.findViewById(R.id.blog_user_name);
            blogLikeBtn = mView.findViewById(R.id.blog_like_btn);
            blogCommentBtn = mView.findViewById(R.id.blog_comment_icon);
            commentViewContainer = mView.findViewById(R.id.constraintLayout);
            blogLikeCount = mView.findViewById(R.id.blog_like_count);
            commentUserImage = mView.findViewById(R.id.comment_user_image);
            userCommentName = mView.findViewById(R.id.textView5);
            userComment = mView.findViewById(R.id.tt);
            blog_comment_count = mView.findViewById(R.id.blog_comment_count);
        }

        public void setDescText(String descText){

            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(descText);

        }

        public void setBlogImage(final String downloadUri, String thumbUri){

            blogImageView = mView.findViewById(R.id.blog_image);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.image_placeholder);
            requestOptions.centerCrop();
            requestOptions.fitCenter();

            Glide.with(context).applyDefaultRequestOptions(requestOptions).
                    load(downloadUri).thumbnail(Glide.with(context).
                    load(thumbUri)).into(blogImageView);

        }

        public void setTime(String date) {
            blogDate = mView.findViewById(R.id.blog_date);
            blogDate.setText(date);

        }

        public void setUserData(String name, String image){
            blogUserName.setText(name);
            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.profile_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(blogUserImage);

        }

        public void updateLikesCount(final int count, String postId, String user_id){

            firebaseFirestore.collection("Posts/" + postId + "/Likes").document(user_id).
                    addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                            if (count == 1){

                                blogLikeCount.setText(count + " Like");

                            }else if (count > 1 && documentSnapshot.exists()){

                                blogLikeCount.setText(count + " Including you");

                            }else if (count > 1){

                                blogLikeCount.setText(count + " likes");
                            }else {
                                blogLikeCount.setText("");
                            }
                        }

                    });

        }

        public void updateCommentCount(int count) {
            if (count != 0){
                blog_comment_count.setText(Integer.toString(count));
            }
        }

    }

    @Override
    public void onViewDetachedFromWindow(@NonNull InterestViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }
}
