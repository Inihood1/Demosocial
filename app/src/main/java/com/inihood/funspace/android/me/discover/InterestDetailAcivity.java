package com.inihood.funspace.android.me.discover;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.inihood.funspace.android.me.CommentsActivity;
import com.inihood.funspace.android.me.PuplicProfileActivity;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.SinglePostActivity;
import com.inihood.funspace.android.me.adapter.BlogRecyclerAdapter;
import com.inihood.funspace.android.me.adapter.InterestDetailAdapter;
import com.inihood.funspace.android.me.helper.RecyclerViewEmptySupport;
import com.inihood.funspace.android.me.helper.ShowToast;
import com.inihood.funspace.android.me.interfaces.OnInterestClicked;
import com.inihood.funspace.android.me.interfaces.OnInterestSelectedListener;
import com.inihood.funspace.android.me.model.BlogPost;
import com.inihood.funspace.android.me.model.InterestDetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class InterestDetailAcivity extends AppCompatActivity implements
        OnInterestClicked{

    private FirebaseAuth firebaseAuth;
    private String incoming;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private String current_user_id;
    private TextView interest_titletxt;
    private CircleImageView img1;
    private CircleImageView img2;
    private CircleImageView img3;
    private CircleImageView img4;
    private TextView members_count;
    private FloatingActionButton newpost;
    private RecyclerViewEmptySupport recyclerView;
    private List<InterestDetail> blog_list;
    private InterestDetailAdapter interestDetailAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;
    private ImageView settings;
    private String admin_user_id;
    private String member;
    private String visible_to_the_public;
    private String ask_to_join;
    private String interest_title;
    private String name;
    private String first_name;
    private String last_name;
    private String fullname;
    private String thumb;
    private static final String TAG = "InterestDetailAcivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_single_acivity);

        initWidgets();
        dbStuff();
        checkInterestState();
        checkSettingBtnState();
        currentUersDetails();
        //checkjoinState();
        getInterestTitle();


        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (admin_user_id.equals(current_user_id)) {
                    settings.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(InterestDetailAcivity.this, InterestSettingActivity.class);
                    intent.putExtra("id", incoming);
                    intent.putExtra("title", interest_title);
                    startActivity(intent);
                }else {
                    ShowToast toast = new ShowToast();
                    toast.toast("You don't have permission to access this page", InterestDetailAcivity.this);
                }
            }
        });

        blog_list = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(InterestDetailAcivity.this);
        interestDetailAdapter = new InterestDetailAdapter(blog_list, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(interestDetailAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);

       loadPost();

        if(firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);


                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if(reachedBottom){

                        loadmore();

                    }

                }
            });

        }

        newpost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                       gotoNext();

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

    }

    public void showDialog(){
        final AlertDialog.Builder builder  = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("This interest is not open to the public, you need to send a membership request to the admin");

        builder.setPositiveButton("Send request", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {

                Toast.makeText(InterestDetailAcivity.this, "join the group function", Toast.LENGTH_SHORT).show();
                //jointhegroup();

            }
        });

        builder.setNegativeButton("No am good", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    public void currentUersDetails(){
        firebaseFirestore.collection("Users").document(current_user_id).get().
                addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                try {
                    first_name = task.getResult().getString("first_name");
                    last_name = task.getResult().getString("last_name");
                    thumb = task.getResult().getString("thumb_profile_image");
                    fullname = first_name + " " + last_name;

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void checkInterestState() {
        try {
            firebaseFirestore.collection("Interest").document(incoming).get().
                    addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    try {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                visible_to_the_public = task.getResult().getString("visible_to_public");
                                String admin_user_id = task.getResult().getString("admin_user_id");
                                if (admin_user_id != null){
                                    if (admin_user_id.equals(current_user_id)){
                                        //Toast.makeText(InterestDetailAcivity.this, "Welcome back", Toast.LENGTH_SHORT).show();
                                    }else {
                                        if (visible_to_the_public != null && visible_to_the_public.equals("false")) {
                                            finish();
                                            ShowToast toast = new ShowToast();
                                            toast.toast("Sorry this interest is not public", InterestDetailAcivity.this);
                                        }
                                    }
                                }
                            }

                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    ShowToast toast = new ShowToast();
                    toast.toast("Please make sure your are connected to the internet", InterestDetailAcivity.this);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

//        try {
//
//            firebaseFirestore.collection("Interest/" + incoming + "/members").
//                    document(current_user_id).
//                    addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                        @Override
//                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
//                            try {
//
//                                if (!documentSnapshot.exists()) {
//                                    Toast.makeText(InterestDetailAcivity.this, "not exist", Toast.LENGTH_SHORT).show();
//                                    newpost.setImageResource(R.drawable.ic_group_add_big);
//                                }else {
//                                    Toast.makeText(InterestDetailAcivity.this, "exist", Toast.LENGTH_SHORT).show();
//                                }
//                            } catch (Exception e2) {
//                                e2.printStackTrace();
//                            }
//
//                        }
//                    });
//        }catch (Exception e){
//            e.printStackTrace();
//        }

    }

    private void gotoNext() {
        newpost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InterestDetailAcivity.this, PostNewInterestActivity.class);
                intent.putExtra("id", incoming);
                intent.putExtra("title", interest_title);
                startActivity(intent);
            }
        });

    }

//    private void jointhegroup() {
//
//        // check if the interest is available for everyone to join
//        try {
//            Toast.makeText(this, "checking state", Toast.LENGTH_SHORT).show();
//            if (ask_to_join != null){
//                if (ask_to_join.equals("true")) {
//                    Toast.makeText(this, "requst qury", Toast.LENGTH_SHORT).show();
//                requestQuery();
//
//            } else {
//                    Toast.makeText(this, "join qury", Toast.LENGTH_SHORT).show();
//                joinQuery(incoming, current_user_id);
//
//            }
//        }else {
//                Toast.makeText(this, "ask is null", Toast.LENGTH_SHORT).show();
//            }
//
//
//        }catch (Exception e){
//            e.printStackTrace();
//            Toast.makeText(InterestDetailAcivity.this, "Something is not right", Toast.LENGTH_SHORT).show();
//        }
//
//        //end
//    }

    public void checkSettingBtnState() {
        try {
            if (admin_user_id.equals(current_user_id)) {
                settings.setVisibility(View.VISIBLE);
            } else {
                settings.setVisibility(View.GONE);
            }
        }catch (NullPointerException e){
            Toast.makeText(this, "Couldn't get admin id", Toast.LENGTH_SHORT).show();
        }

    }

    private void initWidgets() {
        incoming = getIntent().getStringExtra("id");
        admin_user_id = getIntent().getStringExtra("admin_id");
        Toast.makeText(this, "id  " + incoming, Toast.LENGTH_LONG).show();
        if (incoming == null && admin_user_id == null){
            finish();
            ShowToast showToast = new ShowToast();
            showToast.toast("Couldn't get the id", InterestDetailAcivity.this);
        }
        interest_titletxt = findViewById(R.id.textView17);
        img1 = findViewById(R.id.circleImageView3);
        img2 = findViewById(R.id.circleImageView4);
        img3 = findViewById(R.id.circleImageView5);
        img4 = findViewById(R.id.circleImageView6);
        members_count = findViewById(R.id.textView18);
        recyclerView = findViewById(R.id.recyclerview);
        settings = findViewById(R.id.settings);
        settings.setVisibility(View.GONE);
        newpost = findViewById(R.id.fab);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() == null){
            finish();
        }

        checkInterestState();
        checkSettingBtnState();
       // checkjoinState();
        getInterestTitle();
    }

    private void getInterestTitle() {
        try {
            firebaseFirestore.collection("Interest").document(incoming).
                    get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    try {
                        if (task.getResult().exists()) {
                            interest_title = task.getResult().getString("title");
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

//    private void checkjoinState() {
//        try {
//
//            firebaseFirestore.collection("Interest").document(incoming).
//                    get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//
//                    try {
//                        if (task.isSuccessful()){
//
//                            if (task.getResult().exists()) {
//
//                                ask_to_join = task.getResult().getString("ask_to_join");
//                                Toast.makeText(InterestDetailAcivity.this, "ask " +  ask_to_join, Toast.LENGTH_SHORT).show();
//
//                            }
//                        }else {
//                            Toast.makeText(InterestDetailAcivity.this, "wasn't successful", Toast.LENGTH_SHORT).show();
//                        }
//                    }catch (Exception e){
//                        e.printStackTrace();
//                        Toast.makeText(InterestDetailAcivity.this, "Something is not right ", Toast.LENGTH_SHORT).show();
//                    }
//
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(InterestDetailAcivity.this, "error " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        }catch (Exception e){
//            e.printStackTrace();
//            Toast.makeText(InterestDetailAcivity.this, "Something is not right", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void dbStuff() {
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            current_user_id = firebaseAuth.getCurrentUser().getUid();
            firebaseFirestore = FirebaseFirestore.getInstance();
            storageReference = FirebaseStorage.getInstance().getReference();
        }
    }

    @Override
    public void onPostSelected(String blogPostId) {
        Intent commentIntent = new Intent(InterestDetailAcivity.this, SinglePostActivity.class);
        commentIntent.putExtra("blog_post_id", blogPostId);
        startActivity(commentIntent);
    }

    @Override
    public void onComment(String blogPostId) {
        Intent commentIntent = new Intent(InterestDetailAcivity.this, CommentsActivity.class);
        commentIntent.putExtra("blog_post_id", blogPostId);
        startActivity(commentIntent);
    }

    @Override
    public void onPostLike(final String blogPostId, final String currentUserId) {
        try {

            firebaseFirestore.collection("Interest/" + blogPostId + "/Post"  + "/Likes").
                    document(currentUserId).
                    get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    try {

                        if (!task.getResult().exists()) {

                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Interest/" + blogPostId + "/Post"  + "/Likes").
                                    document(currentUserId).set(likesMap);

                        } else {

                            firebaseFirestore.collection("Interest/" + blogPostId + "/Post"  + "/Likes").
                                    document(currentUserId).delete();

                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(InterestDetailAcivity.this, "Couldn't add likes", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(InterestDetailAcivity.this, "Couldn't add likes", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUserSelected(String blogPostUserId, String currentUserId, String postUserId) {
        if (blogPostUserId.equals(currentUserId)){
            ShowToast showToast = new ShowToast();
            showToast.toast("This is you", InterestDetailAcivity.this);
        }else {
            Intent commentIntent = new Intent(InterestDetailAcivity.this, PuplicProfileActivity.class);
            commentIntent.putExtra("blog_post_user_id", blogPostUserId);
            startActivity(commentIntent);
        }
    }

    @Override
    public void onBlogImageSelected(String blogPostId) {
        Intent commentIntent = new Intent(InterestDetailAcivity.this, SinglePostActivity.class);
        commentIntent.putExtra("blog_post_id", blogPostId);
        startActivity(commentIntent);
    }

    private void requestQuery() {

        if (current_user_id != null && first_name != null && interest_title != null && incoming != null){

            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("user_id_who_is_joining", current_user_id);
            requestMap.put("user_name_who_is_joining", fullname);
            requestMap.put("interest_title", interest_title);
            requestMap.put("user_image_who_is_joining", thumb);
            requestMap.put("interest_id", incoming);

            firebaseFirestore.collection("Users").document(admin_user_id).
                    collection("Interest_Notification/" + incoming).add(requestMap).
                    addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            try {
                                if (task.isSuccessful()) {
                                    ShowToast showToast = new ShowToast();
                                    showToast.toast("Done", InterestDetailAcivity.this);
                                } else {
                                    ShowToast showToast = new ShowToast();
                                    showToast.toast("Couldn't follow", InterestDetailAcivity.this);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
        }else {
            ShowToast showToast = new ShowToast();
            showToast.toast("Something is not right nnn", InterestDetailAcivity.this);
        }

    }

    private void joinQuery(final String interest_id, final String currentUserId) {
        try {

            firebaseFirestore.collection("Interest/" + interest_id + "/members").document(currentUserId).
                    get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    try {

                        if (!task.getResult().exists()) {

                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());
                            likesMap.put("member_id", currentUserId);
                            likesMap.put("thumb", thumb);
                            likesMap.put("name", fullname);
                            likesMap.put("interest_id", incoming);

                            firebaseFirestore.collection("Interest/" + interest_id + "/members").
                                    document(currentUserId).set(likesMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    ShowToast showToast = new ShowToast();
                                    showToast.toast("You are now a member", InterestDetailAcivity.this);
                                    gotoNext();
                                }
                            });

                        }else {
                            gotoNext();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(InterestDetailAcivity.this, "Couldn't follow", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(InterestDetailAcivity.this, "Couldn't follow", Toast.LENGTH_SHORT).show();
        }
    }


    public void loadPost(){
        try {

            Query firstQuery = firebaseFirestore.collection("Interest").document(incoming).
                    collection("post").orderBy("timestamp", Query.Direction.DESCENDING).limit(10);
            firstQuery.addSnapshotListener(InterestDetailAcivity.this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    try {
                        if (!documentSnapshots.isEmpty()) {

                            if (isFirstPageFirstLoad) {

                                lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                                blog_list.clear();

                            }

                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String blogPostId = doc.getDocument().getId();
                                    InterestDetail blogPost = doc.getDocument().toObject(InterestDetail.class).withId(blogPostId);

                                    if (isFirstPageFirstLoad) {

                                        blog_list.add(blogPost);

                                    } else {

                                        blog_list.add(0, blogPost);

                                    }

                                    interestDetailAdapter.notifyDataSetChanged();

                                }
                            }

                            isFirstPageFirstLoad = false;

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

    public void loadmore(){
        try {
            if (firebaseAuth.getCurrentUser() != null) {

                Query nextQuery = firebaseFirestore.collection("Interest").document(incoming).collection("post")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .startAfter(lastVisible)
                        .limit(5);

                nextQuery.addSnapshotListener(InterestDetailAcivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        try {
                            if (!documentSnapshots.isEmpty()) {

                                lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                    if (doc.getType() == DocumentChange.Type.ADDED) {

                                        String blogPostId = doc.getDocument().getId();
                                        InterestDetail blogPost = doc.getDocument().toObject(InterestDetail.class).withId(blogPostId);
                                        blog_list.add(blogPost);

                                        interestDetailAdapter.notifyDataSetChanged();
                                    }

                                }
                            }
                        }catch (Exception e1){
                            e1.printStackTrace();
                        }

                    }
                });
            }
        }catch (Exception e){
            e.printStackTrace();
            ShowToast showToast = new ShowToast();
            showToast.toast("Couldn't load feed", InterestDetailAcivity.this);
        }
    }
}