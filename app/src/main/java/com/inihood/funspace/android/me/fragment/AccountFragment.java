package com.inihood.funspace.android.me.fragment;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.storage.UploadTask;
import com.inihood.funspace.android.me.CommentsActivity;
import com.inihood.funspace.android.me.PhotoViewer;
import com.inihood.funspace.android.me.PuplicProfileActivity;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.ShareActivity;
import com.inihood.funspace.android.me.SinglePostActivity;
import com.inihood.funspace.android.me.adapter.BlogRecyclerAdapter;
import com.inihood.funspace.android.me.auth.LoginActivity;
import com.inihood.funspace.android.me.auth.SetupActivity;
import com.inihood.funspace.android.me.helper.RecyclerViewEmptySupport;
import com.inihood.funspace.android.me.helper.RevealBackgroundView;
import com.inihood.funspace.android.me.helper.ShowToast;
import com.inihood.funspace.android.me.interfaces.OnPostsListner;
import com.inihood.funspace.android.me.model.BlogPost;
import com.inihood.funspace.android.me.model.Comments;
import com.inihood.funspace.android.me.splash.ProfileSettingsActivity;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;



public class AccountFragment extends Fragment  implements OnPostsListner{

    private View view;
    private String postUserId;
    private List<BlogPost> blog_list;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
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
    private Button SettingsBtn;
    private RecyclerViewEmptySupport feed;
    private LinearLayout vUserProfileRoot;
    private TabLayout tlUserProfileTabs;
    private static final int USER_OPTIONS_ANIMATION_DELAY = 300;
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator();
    private LinearLayout vUserDetails;
    private LinearLayout vUserStats;
    private boolean lockedAnimations = false;
    private String username;

    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_account, container, false);

        initWidgets();
        dbStuff();
        getFollowersCount();
        getFollowingCount();
        iniPost();
        db();

        user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PhotoViewer.class);
                intent.putExtra("image", image);
                startActivity(intent);
            }
        });

        if (current_user_id != null) {
            try {
                firebaseFirestore.collection("Users").document(current_user_id).get().
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

                                                username = first + " " + last;
                                                loadUserData(username, nick, image, cover, gender);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                        }

                                    } else {

                                        String error = task.getException().getMessage();
                                        Toast.makeText(getActivity(), "Couldn't retrieve user details", Toast.LENGTH_LONG).show();

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
            Toast.makeText(getActivity(), "Something is not right", Toast.LENGTH_SHORT).show();
        }

        SettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileSettingsActivity.class);
                startActivity(intent);

            }
        });


        return view;
    }


    private void iniPost() {
        blog_list = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        firebaseAuth = FirebaseAuth.getInstance();
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list, this);
        feed.setLayoutManager(layoutManager);
        feed.setAdapter(blogRecyclerAdapter);
        feed.setHasFixedSize(true);
        feed.setEmptyView(view.findViewById(R.id.list_empty));

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
                Glide.with(getActivity()).setDefaultRequestOptions(placeholderRequest1).
                        load(image).into(user_image);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initWidgets() {
        try {
            feed = view.findViewById(R.id.rvUserProfile);
            usernamee = view.findViewById(R.id.name);
            nickName = view.findViewById(R.id.nick);
            SettingsBtn = view.findViewById(R.id.btnFollow);
            postCount = view.findViewById(R.id.post_count);
            followerCount = view.findViewById(R.id.follower_count);
            followingCount = view.findViewById(R.id.following_count);
            vUserProfileRoot = view.findViewById(R.id.vUserProfileRoot);
            user_image = view.findViewById(R.id.ivUserProfilePhoto);
            vUserDetails = view.findViewById(R.id.vUserDetails);
            vUserStats = view.findViewById(R.id.vUserStats);
            vRevealBackground = view.findViewById(R.id.vRevealBackground);
            gender_of_person = view.findViewById(R.id.gender);

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

    private void getFollowingCount() {

        firebaseFirestore.collection("Users/" + current_user_id + "/following").
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

        firebaseFirestore.collection("Users/" + current_user_id + "/follower").
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


    @Override
    public void onPostSelected(String blogPostId) {
        Intent commentIntent = new Intent(getActivity(), SinglePostActivity.class);
        commentIntent.putExtra("blog_post_id", blogPostId);
        startActivity(commentIntent);
    }

    @Override
    public void onComment(String blogPostId) {
        Intent commentIntent = new Intent(getActivity(), CommentsActivity.class);
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
                    }catch (Exception w){
                        w.printStackTrace();
                    }

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

        if (!post_user_id.equals(currentUserId)){
            try {
                final String randomName = UUID.randomUUID().toString();
                Map<String, Object> likesMap = new HashMap<>();
                likesMap.put("timestamp", FieldValue.serverTimestamp());
                likesMap.put("user_id_who_trigger", currentUserId);
                likesMap.put("the_text", username + " like your post");
                likesMap.put("user_image_who_trigger", thumb);
                likesMap.put("type", "like");
                likesMap.put("id_of_the_thing_that_was_triggerd", blogPostId);

                firebaseFirestore.collection("Users").document(post_user_id).
                        collection("notification").document(randomName).set(likesMap);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onUserSelected(View view, String blogPostUserId, String currentUserId) {

    }

    @Override
    public void onBlogImageSelected(String blogPostId) {
        Intent commentIntent = new Intent(getActivity(), SinglePostActivity.class);
        commentIntent.putExtra("blog_post_id", blogPostId);
        startActivity(commentIntent);
    }

    @Override
    public void onCommentChange(String post_id, TextView userComment) {

    }

    @Override
    public void onShare(String post_id) {
        Intent commentIntent = new Intent(getActivity(), ShareActivity.class);
        commentIntent.putExtra("blog_post_id", post_id);
        getContext().startActivity(commentIntent);
    }

    public void db(){

        try {
            if (current_user_id != null){
            Query firstQuery = firebaseFirestore.collection("Posts").
                    orderBy("timestamp", Query.Direction.DESCENDING).
                    whereEqualTo("user_id", current_user_id).limit(10);
            firstQuery.addSnapshotListener(getActivity(),
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
        }else {
                Toast.makeText(getActivity(), "Something is not right", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void moreDbTask(){
        if(firebaseAuth.getCurrentUser() != null) {
            try {
                if (current_user_id !=null){
                Query nextQuery = firebaseFirestore.collection("Posts")
                        .orderBy("timestamp", Query.Direction.DESCENDING).
                                whereEqualTo("user_id", current_user_id)
                        .startAfter(lastVisible)
                        .limit(5);

                nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
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
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                    }
                });
            }else {
                    Toast.makeText(getActivity(), "Something is not right", Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}