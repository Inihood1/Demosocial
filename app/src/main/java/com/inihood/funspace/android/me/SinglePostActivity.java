package com.inihood.funspace.android.me;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.inihood.funspace.android.me.adapter.BlogRecyclerAdapter;
import com.inihood.funspace.android.me.auth.RegisterActivity;
import com.inihood.funspace.android.me.auth.SetupActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SinglePostActivity extends AppCompatActivity implements View.OnClickListener{

    private Toolbar postToolbar;
    private ImageView blogUserImage, blogLikeBtn, blogCommentBtn, blogImage;
    private TextView blogUserName, blogLikeCount;
    private ConstraintLayout commentViewContainer;
    private String blog_post_id;
    private String user_id;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private TextView blogpost;
    private String user_name;
    private CircleImageView user_post_toolbar;
    private TextView toolbar_name;
    private TextView blog_comment_count;
    private String user_image;
    private String post_image;
    private String post_user_id;
    private String fullname;
    private String thumb_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post);

        initActionBar();
        iniWidgets();
        initDb();
        retriveCurentUserDetails();
        blog_post_id = getIntent().getStringExtra("blog_post_id");
        if (blog_post_id == null){
            finish();
            Toast.makeText(this, "Something went wrong try again", Toast.LENGTH_LONG).show();
        }

        blogImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SinglePostActivity.this, PhotoViewer.class);
                intent.putExtra("image", post_image);
                startActivity(intent);
            }
        });

        blogUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SinglePostActivity.this, PhotoViewer.class);
                intent.putExtra("image", user_image);
                startActivity(intent);
            }
        });

        try {
            firebaseFirestore.collection("Posts").document(blog_post_id).get().
                    addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (task.isSuccessful()) {
                                try {
                                    if (task.getResult().exists()) {

                                        user_image = task.getResult().getString("user_image");
                                        user_name = task.getResult().getString("name");
                                        String post_thumb = task.getResult().getString("image_thumb");
                                        post_image = task.getResult().getString("image_url");
                                        String post_text = task.getResult().getString("desc");
                                        post_user_id = task.getResult().getString("user_id");

                                        if (user_image != null) {
                                            setUserImage(user_image);
                                            setToolbarImage(user_image);
                                        }

                                        if (user_name != null) {
                                            setUserName(user_name);
                                        }

                                        if (post_image != null) {
                                            setPostImage(post_image, post_thumb);
                                        }

                                        if (post_text != null) {
                                            setPostText(post_text);
                                        }


                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } else {

                                String error = task.getException().getMessage();
                                Toast.makeText(SinglePostActivity.this, "(FIRESTORE Retrieve Error) : " + error, Toast.LENGTH_LONG).show();

                            }


                        }

                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            firebaseFirestore.collection("Posts/" + blog_post_id + "/Likes").
                    addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                            try {
                                if (!documentSnapshots.isEmpty()) {

                                    int count = documentSnapshots.size();

                                    updateLikesCount(count, blog_post_id, user_id);

                                } else {

                                    updateLikesCount(0, blog_post_id, user_id);

                                }
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
        //Get Likes
        try{
        firebaseFirestore.collection("Posts/" + blog_post_id + "/Likes").document(user_id).
                addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        try {
                            if (documentSnapshot.exists()) {

                                blogLikeBtn.setImageDrawable(SinglePostActivity.this.getDrawable(R.drawable.ic_heart_red));

                            } else {

                                blogLikeBtn.setImageDrawable(SinglePostActivity.this.getDrawable(R.drawable.ic_heart_outline_grey));

                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                    }
                });
    }catch (Exception e){
            e.printStackTrace();
        }


        try {
            //Get comment Count
            firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments").
                    addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                            try {

                                if (!documentSnapshots.isEmpty()) {

                                    int count = documentSnapshots.size();

                                    updateCommentCount(count);

                                } else {

                                    updateCommentCount(0);

                                }
                            }catch (Exception e1){
                                e1.printStackTrace();
                            }

                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    private void retriveCurentUserDetails() {
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                try {

                    if (task.isSuccessful()){
                        try {
                            if (task.getResult().exists()){
                                thumb_image = task.getResult().getString("thumb_profile_image");
                                String first = task.getResult().getString("first_name");
                                String last = task.getResult().getString("last_name");
                                fullname = first + " " + last;
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
    }

    private void updateCommentCount(int count) {
        if (count != 0){
            blog_comment_count.setText(Integer.toString(count));
        }
    }

    private void setToolbarImage(String user_image) {

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.image_placeholder);
        requestOptions.fitCenter();

        Glide.with(getApplicationContext()).applyDefaultRequestOptions(requestOptions).
                load(user_image).into(user_post_toolbar);
    }

    private void setPostText(String post_text) {
        blogpost.setText(post_text);
    }

    private void setPostImage(String post_image, String thumb) {

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.image_placeholder);
        requestOptions.centerCrop();
        requestOptions.fitCenter();

        Glide.with(getApplicationContext()).applyDefaultRequestOptions(requestOptions).
                load(post_image).thumbnail(Glide.with(SinglePostActivity.this).
                load(thumb)).into(blogImage);
    }

    private void setUserName(String user_name) {
        blogUserName.setText(user_name);
        toolbar_name.setText(user_name);
    }

    private void setUserImage(String user_image) {
        RequestOptions placeholderRequest = new RequestOptions();
        placeholderRequest.placeholder(R.drawable.profile_placeholder);
        Glide.with(getApplicationContext()).setDefaultRequestOptions(placeholderRequest).
                load(user_image).into(blogUserImage);
    }


    private void initDb() {
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            user_id = firebaseAuth.getCurrentUser().getUid();
            firebaseFirestore = FirebaseFirestore.getInstance();
            storageReference = FirebaseStorage.getInstance().getReference();
        }
    }

    private void initActionBar() {
        postToolbar = findViewById(R.id.comment_toolbar);
       // setSupportActionBar(postToolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle("This post");
    }

    private void iniWidgets() {
        blog_comment_count = findViewById(R.id.blog_comment_count);
        user_post_toolbar = findViewById(R.id.toolbar_image);
        blogpost = findViewById(R.id.blog_desc);
        blogImage = findViewById(R.id.blog_image);
        blogUserImage = findViewById(R.id.blog_user_image);
        blogUserName = findViewById(R.id.blog_user_name);
        blogLikeBtn = findViewById(R.id.blog_like_btn);
        blogCommentBtn = findViewById(R.id.blog_comment_icon);
        commentViewContainer = findViewById(R.id.constraintLayout);
        blogLikeCount = findViewById(R.id.blog_like_count);
        toolbar_name = findViewById(R.id.toolbar_text);

        blogLikeBtn.setOnClickListener(this);
        blogCommentBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.blog_like_btn:
                like();
                break;

            case R.id.blog_comment_icon:
                comment();
                break;
            }
        }

    private void comment() {
        Intent commentIntent = new Intent(SinglePostActivity.this, CommentsActivity.class);
        commentIntent.putExtra("blog_post_id", blog_post_id);
        startActivity(commentIntent);
    }

    private void like() {
        try {
        commentViewContainer.setVisibility(View.VISIBLE);
        firebaseFirestore.collection("Posts/" + blog_post_id + "/Likes").document(user_id).
                get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                try {
                if (!task.getResult().exists()) {

                    Map<String, Object> likesMap = new HashMap<>();
                    likesMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Posts/" + blog_post_id + "/Likes").
                            document(user_id).set(likesMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if (!user_id.equals(post_user_id)){
                                final String randomName = UUID.randomUUID().toString();
                                try {

                                    Calendar c = Calendar.getInstance();
                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    String formattedDate = df.format(c.getTime());


                                    Map<String, Object> likesMap = new HashMap<>();
                                    likesMap.put("timestamp", FieldValue.serverTimestamp());
                                    likesMap.put("user_id_who_trigger", user_id);
                                    likesMap.put("the_text", fullname + " like your post");
                                    likesMap.put("user_image_who_trigger", thumb_image);
                                    likesMap.put("type", "like");
                                    likesMap.put("id_of_the_thing_that_was_triggerd", blog_post_id);
                                    likesMap.put("time", formattedDate);

                                    firebaseFirestore.collection("Users").document(post_user_id).
                                            collection("notification").document(randomName).
                                            set(likesMap);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                } else {

                    firebaseFirestore.collection("Posts/" + blog_post_id + "/Likes").
                            document(user_id).delete();

                }
            }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(SinglePostActivity.this, "Couldn't add likes", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void updateLikesCount(final int count, String postId, String user_id) {
        try {
        firebaseFirestore.collection("Posts/" + postId + "/Likes").document(user_id).
                addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        if (count == 1) {

                            blogLikeCount.setText(count + "");

                        } else if (count > 1 && documentSnapshot.exists()) {

                            blogLikeCount.setText(count + " including you");

                        } else if (count > 1) {

                            blogLikeCount.setText(count + "");
                        } else {
                            blogLikeCount.setText("");
                        }
                    }

                });
    }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}

