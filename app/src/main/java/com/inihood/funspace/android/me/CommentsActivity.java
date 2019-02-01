package com.inihood.funspace.android.me;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.inihood.funspace.android.me.adapter.CommentsRecyclerAdapter;
import com.inihood.funspace.android.me.interfaces.OnCommentClicked;
import com.inihood.funspace.android.me.model.Comments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommentsActivity extends AppCompatActivity implements OnCommentClicked{

    private Toolbar commentToolbar;
    private EditText comment_field;
    private ImageSwitcher comment_post_btn;
    private RecyclerView comment_list;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private List<Comments> commentsList;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String blog_post_id;
    private String current_user_id;
    private TextView error_text;
    private TextView post_text;
    private String thumb_image;
    private String firstname;
    private String last_name;
    private String fullname;
    private String post_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        commentToolbar = findViewById(R.id.comment_toolbar);
        //setSupportActionBar(commentToolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle("Comments");

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            firebaseFirestore = FirebaseFirestore.getInstance();
            current_user_id = firebaseAuth.getCurrentUser().getUid();
            blog_post_id = getIntent().getStringExtra("blog_post_id");
            post_user_id = getIntent().getStringExtra("post_user_id");
            if (blog_post_id == null && post_user_id == null){
                finish();
                Toast.makeText(this, "Something is not right", Toast.LENGTH_SHORT).show();
            }
        }

        firebaseFirestore.collection("Users").document(current_user_id).get().
                addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                firstname = task.getResult().getString("first_name");
                last_name = task.getResult().getString("last_name");
                thumb_image = task.getResult().getString("thumb_profile_image");
                fullname = firstname + " " + last_name;
            }
        });

        comment_field = findViewById(R.id.comment_field);
        comment_post_btn = findViewById(R.id.comment_post_btn);
        comment_list = findViewById(R.id.comment_list);
        post_text = findViewById(R.id.name_post);
        comment_post_btn.setEnabled(false);

        comment_post_btn.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView myView = new ImageView(getApplicationContext());
                myView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                myView.setLayoutParams(new ImageSwitcher.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                myView.setEnabled(false);
                myView.setImageResource(R.drawable.send);
                return myView;
            }
        });

        comment_field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().length() > 2){
                    comment_post_btn.setEnabled(true);
                    comment_post_btn.setImageResource(R.drawable.send);
                }

            }
        });

        //RecyclerView Firebase List
        commentsList = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentsList, this);
        comment_list.setHasFixedSize(true);
        comment_list.setLayoutManager(new LinearLayoutManager(this));
        comment_list.setAdapter(commentsRecyclerAdapter);

        if (firebaseAuth.getCurrentUser() != null) {
            if (blog_post_id != null) {

                firebaseFirestore.collection("Posts").document(blog_post_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {

                            if (task.getResult().exists()) {

                                String name = task.getResult().getString("name");
                                post_text.setText(name + "'s post");

                            }

                        } else {

                            String error = task.getException().getMessage();
                            Toast.makeText(CommentsActivity.this, "(FIRESTORE Retrieve Error) : " + error, Toast.LENGTH_LONG).show();

                        }
                    }
                });

                firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments")
                        .addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                                if (!documentSnapshots.isEmpty()) {

                                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                        if (doc.getType() == DocumentChange.Type.ADDED) {

                                            String commentId = doc.getDocument().getId();
                                            Comments comments = doc.getDocument().toObject(Comments.class);
                                            commentsList.add(comments);
                                            commentsRecyclerAdapter.notifyDataSetChanged();


                                        }
                                    }

                                }

                            }
                        });
            }else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
        comment_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                String comment_message = comment_field.getText().toString();

                if (!comment_message.equals("")) {

                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedDate = df.format(c.getTime());

                    Map<String, Object> commentsMap = new HashMap<>();
                    commentsMap.put("message", comment_message);
                    commentsMap.put("user_id", current_user_id);
                    commentsMap.put("timestamp", FieldValue.serverTimestamp());
                    commentsMap.put("name", fullname);
                    commentsMap.put("image", thumb_image);
                    commentsMap.put("time", formattedDate);

                    firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments")
                            .add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {

                            if (!task.isSuccessful()) {

                                Toast.makeText(CommentsActivity.this, "Error Posting Comment : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                comment_list.smoothScrollToPosition(commentsList.size() - 1);
                                if (!current_user_id.equals(post_user_id)){
                                final String randomName = UUID.randomUUID().toString();
                                try {

                                    Calendar c = Calendar.getInstance();
                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    String formattedDate = df.format(c.getTime());

                                    Map<String, Object> likesMap = new HashMap<>();
                                    likesMap.put("timestamp", FieldValue.serverTimestamp());
                                    likesMap.put("user_id_who_trigger", current_user_id);
                                    likesMap.put("the_text", fullname + " commented on your post");
                                    likesMap.put("user_image_who_trigger", thumb_image);
                                    likesMap.put("type", "comment");
                                    likesMap.put("id_of_the_thing_that_was_triggerd", blog_post_id);
                                    likesMap.put("time", formattedDate);

                                    firebaseFirestore.collection("Users").document(post_user_id).
                                            collection("notification").document(randomName).
                                            set(likesMap);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            } else {

                                comment_field.setText("");
                                comment_list.smoothScrollToPosition(commentsList.size() - 1);

                            }
                        }
                    });

                } else {
                    Toast.makeText(CommentsActivity.this, "Cant send empty message", Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                e.printStackTrace();
            }   try {
                    comment_field.setText("");
                    comment_list.smoothScrollToPosition(commentsList.size() - 1);
                }catch (Exception e){
                e.printStackTrace();
                }

            }
        });


    }

    @Override
    public void onUsersCliked(String other_id, String current_user_id) {

        if (other_id.equals(current_user_id)){
            Toast.makeText(CommentsActivity.this, "This is you", Toast.LENGTH_SHORT).show();
        }else {
            try {
                Intent intent = new Intent(CommentsActivity.this, PuplicProfileActivity.class);
                intent.putExtra("blogPostUserId", other_id);
                startActivity(intent);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
