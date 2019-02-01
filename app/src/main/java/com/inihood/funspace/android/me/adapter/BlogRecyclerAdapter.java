package com.inihood.funspace.android.me.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.flags.IFlagProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.inihood.funspace.android.me.interfaces.OnPostsListner;
import com.inihood.funspace.android.me.model.BlogPost;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.model.Comments;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public List<BlogPost> blog_list;
    public List<Comments> comment_list;
    public Context context;
    public int previousPosition = 0;
    private int HEADER = 0;
    private int BODY = 1;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private int lastPosition = -1;
    private  View view;
    private OnPostsListner mListner;
    private ProgressBar progressBar;
    private TextView currentTime;
    private TextView totalTime;
    private ImageView platBtn;
    private LinearLayout video_view_layout;
    private VideoView videoView;
    private ProgressBar bufferProgress;
    private boolean isplaying = false;
    private int current = 0;
    private int duration =  0;

    public BlogRecyclerAdapter(List<BlogPost> blog_list, OnPostsListner listner){
        this.blog_list = blog_list;
        mListner = listner;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item,
                    parent, false);
            context = parent.getContext();
            firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseAuth = FirebaseAuth.getInstance();
            comment_list = new ArrayList<>();
            return new BodyViewHolder(view);

    }



    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

//        int view = holder.getItemViewType();
//
//        if (view == 0){
//            TopViewHolder holder1 = (TopViewHolder) holder;
//            holder1.text.setText("Whats up");
//        }else if (view == 1){
            BodyViewHolder holder2 = (BodyViewHolder) holder;
            Animation animation = AnimationUtils.loadAnimation(context,
                    (position > lastPosition) ? R.anim.up_from_bottom
                            : R.anim.down_from_top);
            holder.itemView.startAnimation(animation);
            lastPosition = position;

            bindData(holder2, position, mListner);


    }


    private void bindData(final BodyViewHolder holder1, final int position, final OnPostsListner listener) {

        final String blogPostId = blog_list.get(position).BlogPostId;
        final String blogPostUserId = blog_list.get(position).getUser_id();
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();
//        final String audio = blog_list.get(position).getAudio();
//        final String video = blog_list.get(position).getVideo();
        String image_url = blog_list.get(position).getImage_url();

//        if (image_url.equals("")){
//            holder1.blogImageView.setVisibility(View.INVISIBLE);
//            holder1.setVideoData(video);
//        }else {
//            holder1.blogImageView.setVisibility(View.VISIBLE);
//        }

        String desc_data = blog_list.get(position).getDesc();
        String time = blog_list.get(position).getTime();
        if (desc_data != null && time != null){
            holder1.setDescText(desc_data, time);
        }

        String username = blog_list.get(position).getName();
        String userimage = blog_list.get(position).getUser_image();
        if (username != null && userimage != null){
            holder1.setUserData(username, userimage);
        }


        String thumbUri = blog_list.get(position).getImage_thumb();
        if (image_url != null && thumbUri != null){
            holder1.setBlogImage(image_url, thumbUri);
        }

        try {
            //Get Likes Count
            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").
                    addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                            try {

                                if (!documentSnapshots.isEmpty()) {

                                    int count = documentSnapshots.size();

                                    holder1.updateLikesCount(count, blogPostId, currentUserId);

                                } else {

                                    holder1.updateLikesCount(0, blogPostId, currentUserId);

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
            firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").
                    addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                            try {

                                if (!documentSnapshots.isEmpty()) {

                                    int count = documentSnapshots.size();

                                    holder1.updateCommentCount(count);

                                } else {

                                    holder1.updateCommentCount(0);

                                }
                            }catch (Exception e1){
                                e1.printStackTrace();
                            }

                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }

       // Get Likes
        try {

            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).
                    addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            try {

                                if (documentSnapshot.exists()) {

                                    holder1.blogLikeBtn.setImageDrawable(context.getDrawable(R.drawable.ic_heart_red));

                                } else {

                                    holder1.blogLikeBtn.setImageDrawable(context.getDrawable(R.drawable.ic_heart_outline_grey));

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
        holder1.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listener != null) {
                    listener.onPostLike(blogPostId, currentUserId, holder1.commentViewContainer, blogPostUserId);

                }
            }
        });

        holder1.blogCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onComment(blogPostId);

                }

            }
        });

        holder1.blogImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onBlogImageSelected(blogPostId);

                }
            }
        });

        holder1.blogUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listener != null) {
                    listener.onUserSelected(view, blogPostUserId, currentUserId);
                }
            }
        });

        holder1.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onShare(blogPostId);
                }
            }
        });

        holder1.blogUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listener != null) {
                    listener.onUserSelected(view, blogPostUserId, currentUserId);
                }

            }
        });

        holder1.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onPostSelected(blogPostId);

                }
            }
        });

        holder1.commentViewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listener != null) {
                    listener.onCommentChange(blogPostId, holder1.userComment);

                }


            }
        });

    }


    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class BodyViewHolder extends RecyclerView.ViewHolder {

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
        private ImageView shareBtn;
        private TextView time;

        public BodyViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            shareBtn = mView.findViewById(R.id.imageView4);
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
            currentTime = mView.findViewById(R.id.current_time);
            totalTime = mView.findViewById(R.id.total_time);
            platBtn = mView.findViewById(R.id.platBtn);
            progressBar = mView.findViewById(R.id.progressBar7);
            blogImageView = mView.findViewById(R.id.blog_image);
            video_view_layout = mView.findViewById(R.id.video_view_layout);
            videoView = mView.findViewById(R.id.video_view);
            bufferProgress = mView.findViewById(R.id.progressBar8);
            progressBar.setMax(100);
            time = mView.findViewById(R.id.blog_date);

        }

        public void setDescText(String descText, String timetxt){

            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(descText);
            time.setText(timetxt);

        }

//        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
//        private void setVideoData(String video) {
//            try {
//                videoView.setVideoURI(Uri.parse(video));
//                videoView.requestFocus();
//                videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
//                    @Override
//                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
//                        if (what == mp.MEDIA_INFO_BUFFERING_START) {
//                            bufferProgress.setVisibility(View.VISIBLE);
//                        }else if (what == mp.MEDIA_INFO_BUFFERING_END){
//                            bufferProgress.setVisibility(View.INVISIBLE);
//                        }
//                        return false;
//                    }
//                });
//
//                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                    @Override
//                    public void onPrepared(MediaPlayer mp) {
//                        duration = mp.getDuration() /1000;
//                        String durationString  = String.format("%02d:%02d", duration / 60, duration % 60);
//                        totalTime.setText(durationString);
//                    }
//                });
//
//                videoView.start();
//                isplaying = true;
//                platBtn.setImageResource(R.drawable.pause);
//                new VideoProgress().execute();
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//
//            platBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (isplaying){
//                        videoView.pause();
//                        isplaying = false;
//                        platBtn.setImageResource(R.drawable.play);
//                    }else {
//                        videoView.start();
//                        isplaying = true;
//                        platBtn.setImageResource(R.drawable.pause);
//                    }
//                }
//            });
//        }


        public void setBlogImage(final String downloadUri, String thumbUri){

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.image_placeholder);
            requestOptions.centerCrop();
            requestOptions.fitCenter();

            Glide.with(context).applyDefaultRequestOptions(requestOptions).
                    load(downloadUri).into(blogImageView);

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

                                blogLikeCount.setText(count + "");

                            }else if (count > 1 && documentSnapshot.exists()){

                                blogLikeCount.setText(count + " Including you");

                            }else if (count > 1){

                                blogLikeCount.setText(count + "");
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

    public  class TopViewHolder extends RecyclerView.ViewHolder{

        View mView;
        CircleImageView user_image;
        TextView text;
        ImageView imageView;

        public TopViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            text = mView.findViewById(R.id.textView4);
            imageView = mView.findViewById(R.id.imageView);
        }
        public void setUserData(String image){
            user_image = mView.findViewById(R.id.circleImageView);
            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.profile_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(user_image);
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    public class VideoProgress extends AsyncTask<Void, Integer, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            do {

                if (isplaying){
                    current = videoView.getCurrentPosition() / 1000;
                    publishProgress(current);
                }
            }while (progressBar.getProgress() <= 100);


            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            try {

                int currentPercent = values[0] * 100 / duration;
                publishProgress(currentPercent);

                progressBar.setProgress(values[0]);

                String currerentString  = String.format("%02d:%02d", values[0] / 60, values[0] % 60);
                currentTime.setText(currerentString);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
