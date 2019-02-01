package com.inihood.funspace.android.me;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.inihood.funspace.android.me.adapter.BlogRecyclerAdapter;
import com.inihood.funspace.android.me.auth.LoginActivity;
import com.inihood.funspace.android.me.auth.SetupActivity;
import com.inihood.funspace.android.me.fragment.AccountFragment;
import com.inihood.funspace.android.me.fragment.HomeFragment;
import com.inihood.funspace.android.me.fragment.NotificationFragment;
import com.inihood.funspace.android.me.helper.RevealBackgroundView;
import com.inihood.funspace.android.me.helper.ShowToast;
import com.inihood.funspace.android.me.interfaces.OnPostsListner;
import com.inihood.funspace.android.me.model.BlogPost;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class PuplicProfileActivity extends AppCompatActivity implements
        RevealBackgroundView.OnStateChangeListener, OnPostsListner {

    private List<BlogPost> blog_list;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String current_user_id;
    public  static String otherId;
    private String first;
    private String last;
    private String image;
    private String nick;
    private String thumb;
    private String phone;
    private String gender;
    private RevealBackgroundView vRevealBackground;
    public static final String ARG_REVEAL_START_LOCATION = "reveal_start_location";

    // NEW WIDGETS
    private ImageView back;
    private TextView gender_of_person;
    private TextView usernamee;
    private TextView followingCount;
    private TextView followerCount;
    private CircleImageView user_image;
    private TextView postCount;
    private TextView nickName;
    private Button followBtn;
    private RecyclerView feed;
    private LinearLayout vUserProfileRoot;
    private TabLayout tlUserProfileTabs;
    private static final int USER_OPTIONS_ANIMATION_DELAY = 300;
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator();
    private LinearLayout vUserDetails;
    private LinearLayout vUserStats;
    private boolean lockedAnimations = false;
    private String postUserId;
    private String currentUserFullname;
    private String currentUserFirstname;
    private String currentUserLastname;
    private String currentUserThumbImage;
    //END

    public static void startUserProfileFromLocation(int[] startingLocation, Activity startingActivity) {
        Intent intent = new Intent(startingActivity, PuplicProfileActivity.class);
        intent.putExtra(ARG_REVEAL_START_LOCATION, startingLocation);
        startingActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_profile);

        initWidgets();
        setupRevealBackground(savedInstanceState);
        dbStuff();
        checkRelationShipState();
        getFollowersCount();
        getFollowingCount();
        followUser();
        iniPost();
        db();
        user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PuplicProfileActivity.this, PhotoViewer.class);
                intent.putExtra("image", image);
                startActivity(intent);
            }
        });
        if (otherId != null) {
            try {
                firebaseFirestore.collection("Users").document(otherId).get().
                        addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                try {
                                    if (task.isSuccessful()) {

                                        if (task.getResult().exists()) {

                                            try {
                                                first = task.getResult().getString("first_name");
                                                last = task.getResult().getString("last_name");
                                                nick = task.getResult().getString("nick_name");
                                                image = task.getResult().getString("profile_image");
                                                thumb = task.getResult().getString("thumb_profile_image");
                                                phone = task.getResult().getString("phone");
                                                gender = task.getResult().getString("gender");
                                                String cover = task.getResult().getString("cover_image");

                                                String username = first + " " + last;
                                                loadUserData(username, nick, image, cover, gender);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                        }

                                    } else {

                                        String error = task.getException().getMessage();
                                        Toast.makeText(PuplicProfileActivity.this, "Couldn't retrieve user details", Toast.LENGTH_LONG).show();

                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(this, "Something is not right", Toast.LENGTH_SHORT).show();
        }

        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followBtn.setEnabled(false);

                try {
                    firebaseFirestore.collection("Users/" + current_user_id + "/following").
                            document(otherId).
                            get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            try {
                                if (!task.getResult().exists()) {

                                    Map<String, Object> friendsMap = new HashMap<>();
                                    friendsMap.put("id", otherId);
                                    friendsMap.put("phone", phone);
                                    friendsMap.put("thumb", thumb);
                                    friendsMap.put("image", image);
                                    friendsMap.put("first", first);
                                    friendsMap.put("last", last);
                                    friendsMap.put("nick", nick);
                                    friendsMap.put("timestamp", FieldValue.serverTimestamp());

                                    firebaseFirestore.collection("Users/" + current_user_id + "/following").
                                            document(otherId).set(friendsMap);

                                    Map<String, Object> friendMap = new HashMap<>();
                                    friendMap.put("id", current_user_id);

                                    firebaseFirestore.collection("Users/" + otherId + "/followers").
                                            document(current_user_id).set(friendMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            followBtn.setEnabled(true);

                                            final String randomName = UUID.randomUUID().toString();
                                            try {

                                                Calendar c = Calendar.getInstance();
                                                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                String formattedDate = df.format(c.getTime());

                                                Map<String, Object> likesMap = new HashMap<>();
                                                likesMap.put("timestamp", FieldValue.serverTimestamp());
                                                likesMap.put("user_id_who_trigger", current_user_id);
                                                likesMap.put("the_text", currentUserFullname + " followed you");
                                                likesMap.put("user_image_who_trigger", currentUserThumbImage);
                                                likesMap.put("type", "follow");
                                                likesMap.put("id_of_the_thing_that_was_triggerd", otherId);
                                                likesMap.put("time", formattedDate);

                                                firebaseFirestore.collection("Users").document(otherId).
                                                        collection("notification").document(randomName).set(likesMap);
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            ShowToast showToast = new ShowToast();
                                            showToast.toast("Something went wrong, check your connection", PuplicProfileActivity.this);
                                            followBtn.setEnabled(true);
                                        }
                                    });

                                } else {

                                    firebaseFirestore.collection("Users/" + current_user_id + "/following").
                                            document(otherId).delete();


                                    firebaseFirestore.collection("Users/" + otherId + "/followers").
                                            document(current_user_id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            followBtn.setEnabled(true);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            ShowToast showToast = new ShowToast();
                                            showToast.toast("Something went wrong, check your connection", PuplicProfileActivity.this);
                                            followBtn.setEnabled(true);
                                        }
                                    });


                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(PuplicProfileActivity.this, "Couldn't Follower, check your connection", Toast.LENGTH_SHORT).show();
                                followBtn.setEnabled(true);
                            }

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(PuplicProfileActivity.this, "Couldn't Follower, check your connection", Toast.LENGTH_SHORT).show();
                    followBtn.setEnabled(true);
                }

            }
        });


        firebaseFirestore.collection("Users").document(current_user_id).get().
                addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                try{
                    if (task.isSuccessful()){
                        if (task.getResult().exists()){
                            currentUserFirstname = task.getResult().getString("first_name");
                            currentUserLastname = task.getResult().getString("last_name");
                            currentUserThumbImage = task.getResult().getString("profile_thumb_image");
                            currentUserFullname = currentUserFirstname + " " + currentUserLastname;
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }


    private void iniPost() {
        blog_list = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(PuplicProfileActivity.this);

        firebaseAuth = FirebaseAuth.getInstance();
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list, this);
        feed.setLayoutManager(layoutManager);
        feed.setAdapter(blogRecyclerAdapter);
        feed.setHasFixedSize(true);

        if(firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            feed.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if(reachedBottom){

                        moreDbTask();

                    }

                }
            });

        }


    }

    private void loadUserData(String username, String nick, String image, String cover, String gender) {

        try {
        if (username != null) {
            usernamee.setText(username);
        }

        if (gender != null){
            gender_of_person.setText(gender);
        }

        if (nick != null) {
            nickName.setText(nick);
        }

        if (image != null) {
            RequestOptions placeholderRequest1 = new RequestOptions();
            placeholderRequest1.placeholder(R.drawable.profile_placeholder);
            Glide.with(PuplicProfileActivity.this).setDefaultRequestOptions(placeholderRequest1).
                    load(image).into(user_image);
        }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initWidgets() {
        try {
            otherId = getIntent().getStringExtra("blogPostUserId");
        if (otherId == null) {
            finish();
            ShowToast showToast = new ShowToast();
            showToast.toast("Something is wrong, The id couldn't be retrieved", PuplicProfileActivity.this);
        }
            feed = findViewById(R.id.rvUserProfile);
            usernamee = findViewById(R.id.name);
            nickName = findViewById(R.id.nick);
            followBtn = findViewById(R.id.btnFollow);
            postCount = findViewById(R.id.post_count);
            followerCount = findViewById(R.id.follower_count);
            followingCount = findViewById(R.id.following_count);
            vUserProfileRoot = findViewById(R.id.vUserProfileRoot);
            user_image = findViewById(R.id.ivUserProfilePhoto);
            vUserDetails = findViewById(R.id.vUserDetails);
            vUserStats = findViewById(R.id.vUserStats);
            vRevealBackground = findViewById(R.id.vRevealBackground);
            gender_of_person = findViewById(R.id.gender);
            back = findViewById(R.id.back);

            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void dbStuff() {
        try {
            firebaseAuth = FirebaseAuth.getInstance();
            if (firebaseAuth.getCurrentUser() != null) {
                firebaseAuth = FirebaseAuth.getInstance();
                current_user_id = firebaseAuth.getCurrentUser().getUid();
                firebaseFirestore = FirebaseFirestore.getInstance();
                storageReference = FirebaseStorage.getInstance().getReference();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkRelationShipState();
    }

    public void checkRelationShipState() {

        try {

        firebaseFirestore.collection("Users").document(current_user_id).
                collection("following").document(otherId).
                addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        try {
                            if (documentSnapshot.exists()) {

                                followBtn.setText(getString(R.string.unfollow));

                            } else {

                                followBtn.setText(getString(R.string.follow));

                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                    }
                });
    }catch (Exception e){
            e.printStackTrace();
            ShowToast toast = new ShowToast();
            toast.toast("Something is not right", PuplicProfileActivity.this);
        }
    }

    public void followUser() {


    }

    private void getFollowingCount() {

        firebaseFirestore.collection("Users/" + otherId + "/following").
                addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        try {
                            if (!documentSnapshots.isEmpty()) {

                                int count = documentSnapshots.size();

                                followingCount.setText(Integer.toString(count));

                            } else {

                                followingCount.setText(Integer.toString(0));

                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                    }
                });

    }

    private void getFollowersCount() {

        firebaseFirestore.collection("Users/" + otherId + "/follower").
                addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        try {
                            if (!documentSnapshots.isEmpty()) {

                                int count = documentSnapshots.size();

                                followerCount.setText(Integer.toString(count));

                            } else {

                                followerCount.setText(Integer.toString(0));

                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                    }
                });

    }

    private void setupRevealBackground(Bundle savedInstanceState) {
//        vRevealBackground.setOnStateChangeListener(this);
//        if (savedInstanceState == null) {
//            final int[] startingLocation = getIntent().getIntArrayExtra(ARG_REVEAL_START_LOCATION);
//            vRevealBackground.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//                @Override
//                public boolean onPreDraw() {
//                    vRevealBackground.getViewTreeObserver().removeOnPreDrawListener(this);
//                    vRevealBackground.startFromLocation(startingLocation);
//                    return true;
//                }
//            });
//        } else {
//            vRevealBackground.setToFinishedFrame();
//            //userPhotosAdapter.setLockedAnimations(true);
//        }
    }

    @Override
    public void onStateChange(int state) {
//        if (RevealBackgroundView.STATE_FINISHED == state) {
//            vUserProfileRoot.setVisibility(View.VISIBLE);
//            tlUserProfileTabs.setVisibility(View.VISIBLE);
//            vUserProfileRoot.setVisibility(View.VISIBLE);
//            iniPost();
//            animateUserProfileOptions();
//            animateUserProfileHeader();
//        } else {
//            tlUserProfileTabs.setVisibility(View.INVISIBLE);
//            vUserProfileRoot.setVisibility(View.INVISIBLE);
//            vUserProfileRoot.setVisibility(View.INVISIBLE);
//        }
    }

    private void animateUserProfileOptions() {
        tlUserProfileTabs.setTranslationY(-tlUserProfileTabs.getHeight());
        tlUserProfileTabs.animate().translationY(0).setDuration(300).setStartDelay(USER_OPTIONS_ANIMATION_DELAY).setInterpolator(INTERPOLATOR);
    }

    private void animateUserProfileHeader() {
        vUserProfileRoot.setTranslationY(-vUserProfileRoot.getHeight());
        user_image.setTranslationY(-user_image.getHeight());
        vUserDetails.setTranslationY(-vUserDetails.getHeight());
        vUserStats.setAlpha(0);

        vUserProfileRoot.animate().translationY(0).setDuration(300).setInterpolator(INTERPOLATOR);
        user_image.animate().translationY(0).setDuration(300).setStartDelay(100).setInterpolator(INTERPOLATOR);
        vUserDetails.animate().translationY(0).setDuration(300).setStartDelay(200).setInterpolator(INTERPOLATOR);
        vUserStats.animate().alpha(1).setDuration(200).setStartDelay(400).setInterpolator(INTERPOLATOR).start();
    }

    @Override
    public void onPostSelected(String blogPostId) {
        Intent commentIntent = new Intent(PuplicProfileActivity.this, SinglePostActivity.class);
        commentIntent.putExtra("blog_post_id", blogPostId);
        startActivity(commentIntent);
    }

    @Override
    public void onComment(String blogPostId) {
        Intent commentIntent = new Intent(PuplicProfileActivity.this, CommentsActivity.class);
        commentIntent.putExtra("blog_post_id", blogPostId);
        commentIntent.putExtra("post_user_id", postUserId);
        startActivity(commentIntent);
    }

    @Override
    public void onPostLike(final String blogPostId, final String currentUserId, ConstraintLayout
            commentViewContainer, String post_user_id) {
        postUserId = post_user_id;
        try {
            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).
                    get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    try {
                        if (!task.getResult().exists()) {

                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").
                                    document(currentUserId).set(likesMap);


                        } else {

                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").
                                    document(currentUserId).delete();

                        }
                    } catch (Exception w) {
                        w.printStackTrace();
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!current_user_id.equals(post_user_id)){
        final String randomName = UUID.randomUUID().toString();
        try {

            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = df.format(c.getTime());

            Map<String, Object> likesMap = new HashMap<>();
            likesMap.put("timestamp", FieldValue.serverTimestamp());
            likesMap.put("user_id_who_trigger", current_user_id);
            likesMap.put("the_text", currentUserFullname + " like your post");
            likesMap.put("user_image_who_trigger", currentUserThumbImage);
            likesMap.put("type", "like");
            likesMap.put("id_of_the_thing_that_was_triggerd", blogPostId);
            likesMap.put("time", formattedDate);

            firebaseFirestore.collection("Users").document(post_user_id).
                    collection("notification").document(randomName).set(likesMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    }

    @Override
    public void onUserSelected(View view, String blogPostUserId, String currentUserId) {

    }

    @Override
    public void onBlogImageSelected(String blogPostId) {
        Intent commentIntent = new Intent(PuplicProfileActivity.this, SinglePostActivity.class);
        commentIntent.putExtra("blog_post_id", blogPostId);
        startActivity(commentIntent);
    }

    @Override
    public void onCommentChange(String post_id, TextView userComment) {

    }

    @Override
    public void onShare(String post_id) {
        Intent commentIntent = new Intent(PuplicProfileActivity.this, ShareActivity.class);
        commentIntent.putExtra("blog_post_id", post_id);
       startActivity(commentIntent);
    }

    public void db(){

        try {
            Query firstQuery = firebaseFirestore.collection("Posts").
                    orderBy("timestamp", Query.Direction.DESCENDING).
                    whereEqualTo("user_id", otherId).limit(10);
            firstQuery.addSnapshotListener(PuplicProfileActivity.this,
                    new EventListener<QuerySnapshot>() {
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
                                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                                            if (isFirstPageFirstLoad) {

                                                blog_list.add(blogPost);

                                            } else {

                                                blog_list.add(0, blogPost);

                                            }

                                            blogRecyclerAdapter.notifyDataSetChanged();

                                        }
                                    }

                                    isFirstPageFirstLoad = false;

                                }
                            } catch (Exception e4) {
                                e4.printStackTrace();
                            }

                        }


                    });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void moreDbTask(){
        if(firebaseAuth.getCurrentUser() != null) {
            try {
                Query nextQuery = firebaseFirestore.collection("Posts")
                        .orderBy("timestamp", Query.Direction.DESCENDING).whereEqualTo("user_id", otherId)
                        .startAfter(lastVisible)
                        .limit(5);

                nextQuery.addSnapshotListener(PuplicProfileActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        try {
                            if (!documentSnapshots.isEmpty()) {

                                lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                    if (doc.getType() == DocumentChange.Type.ADDED) {

                                        String blogPostId = doc.getDocument().getId();
                                        BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                        blog_list.add(blogPost);

                                        blogRecyclerAdapter.notifyDataSetChanged();
                                    }

                                }
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
    }
}
