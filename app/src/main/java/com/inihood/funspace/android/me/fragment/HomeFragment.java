package com.inihood.funspace.android.me.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.inihood.funspace.android.me.CommentsActivity;
import com.inihood.funspace.android.me.PuplicProfileActivity;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.ShareActivity;
import com.inihood.funspace.android.me.SinglePostActivity;
import com.inihood.funspace.android.me.adapter.BlogRecyclerAdapter;
import com.inihood.funspace.android.me.auth.LoginActivity;
import com.inihood.funspace.android.me.call.activities.SplashActivity;
import com.inihood.funspace.android.me.discover.CreateInterestActivity;
import com.inihood.funspace.android.me.discover.CreateInterestSecondStep;
import com.inihood.funspace.android.me.discover.PostNewInterestActivity;
import com.inihood.funspace.android.me.helper.HidingScrollListener;
import com.inihood.funspace.android.me.helper.RecyclerViewEmptySupport;
import com.inihood.funspace.android.me.helper.ShowToast;
import com.inihood.funspace.android.me.interfaces.OnPostsListner;
import com.inihood.funspace.android.me.model.BlogPost;
import com.inihood.funspace.android.me.post.Audio;
import com.inihood.funspace.android.me.post.Video;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HomeFragment extends Fragment implements OnPostsListner {

    private RecyclerViewEmptySupport blog_list_view;
    public String followingIds;
    private List<BlogPost> blog_list;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;
    private Toolbar mToolbar;
    private View view;
    private String current_user_id;
    private FloatingActionButton addPostBtn;
    private String first;
    private String last;
    private String thumb;
    private String username;
    private String postUserId;
    FloatingActionMenu materialDesignFAM;
    com.github.clans.fab.FloatingActionButton floatingActionButton1,
            floatingActionButton2, floatingActionButton3,
            floatingActionButton4, floatingActionButton5,
            floatingActionButton6;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_home, container, false);
        initToolbar();

        blog_list = new ArrayList<>();
        blog_list_view = view.findViewById(R.id.blog_list_view);
        fab();

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            current_user_id = firebaseAuth.getCurrentUser().getUid();
        }else {
            Toast.makeText(getActivity(), "cant get your id", Toast.LENGTH_SHORT).show();
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list, this);
        blog_list_view.setLayoutManager(layoutManager);
        blog_list_view.setAdapter(blogRecyclerAdapter);
        blog_list_view.setHasFixedSize(true);
        blog_list_view.setEmptyView(view.findViewById(R.id.list_empty));

        //getAllFollowersId();

//        try {
//            firebaseFirestore.collection("Users")
//                    .document(current_user_id).collection("following").get()
//                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//
//                            if (task.isSuccessful()) {
//
//                                for (QueryDocumentSnapshot document : task.getResult()) {
//                                    followingIds = document.getId();
//
//                                    //Toast.makeText(getActivity(),item , Toast.LENGTH_SHORT).show();
//                                }
//
//                            }
//                        }
//                    });
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        blog_list_view.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                //fab.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0).scaleY(0);
                // do your hiding animation here
                hideViews();
            }

            @Override
            public void onShow() {
                //fab.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(1).scaleY(1);
                // do your showing animation here
                showViews();
            }
        });

        if(firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);


                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if(reachedBottom){

                        databaseProcess(followingIds);

                    }

                }
            });

        }


        firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                try {
                    if (task.isSuccessful()) {

                        if (task.getResult().exists()) {

                            try {
                                first = task.getResult().getString("first_name");
                                last = task.getResult().getString("last_name");
                                thumb = task.getResult().getString("thumb_profile_image");

                                username = first + " " + last;

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

            fistDbProcess(followingIds);

        return view;
    }

    private void getAllFollowersId() {
    //ids = ids + "," + user_id;


    }

    @Override
    public void onPostSelected(String blogPostId) {
        Intent commentIntent = new Intent(getActivity(), SinglePostActivity.class);
        commentIntent.putExtra("blog_post_id", blogPostId);
        getContext().startActivity(commentIntent);
    }

    private void initToolbar() {
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
    }

    @Override
    public void onComment(String blogPostId) {
        Intent commentIntent = new Intent(getActivity(), CommentsActivity.class);
        commentIntent.putExtra("blog_post_id", blogPostId);
        commentIntent.putExtra("post_user_id", postUserId);
        getContext().startActivity(commentIntent);
    }

    @Override
    public void onPostLike(final String blogPostId, final String currentUserId,
                           ConstraintLayout commentViewContainer, String post_user_id) {
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
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Couldn't add likes", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Couldn't add likes", Toast.LENGTH_SHORT).show();
        }
        if (!current_user_id.equals(post_user_id)){
        try {

            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = df.format(c.getTime());

            final String randomName = UUID.randomUUID().toString();
            Map<String, Object> likesMap = new HashMap<>();
            likesMap.put("timestamp", FieldValue.serverTimestamp());
            likesMap.put("user_id_who_trigger", current_user_id);
            likesMap.put("the_text", username + " like your post");
            likesMap.put("user_image_who_trigger", thumb);
            likesMap.put("type", "like");
            likesMap.put("id_of_the_thing_that_was_triggerd", blogPostId);
            likesMap.put("time", formattedDate);

            firebaseFirestore.collection("Users").document(post_user_id).
                    collection("notification").document(randomName).
                    set(likesMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    }


    @Override
    public void onUserSelected(View v, String blogPostUserId, String currentUserId) {
        if (blogPostUserId.equals(currentUserId)){
            Toast.makeText(getActivity(), "This is you", Toast.LENGTH_SHORT).show();
        }else {
            try {
                Intent intent = new Intent(getActivity(), PuplicProfileActivity.class);
                intent.putExtra("blogPostUserId", blogPostUserId);
                getActivity().startActivity(intent);
        }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBlogImageSelected(String blogPostId) {
        Intent commentIntent = new Intent(getActivity(), SinglePostActivity.class);
        commentIntent.putExtra("blog_post_id", blogPostId);
        getContext().startActivity(commentIntent);
    }



    @Override
    public void onCommentChange(final String pos_id, final TextView userComment) {

    }

    @Override
    public void onShare(String post_id) {
        Intent commentIntent = new Intent(getActivity(), ShareActivity.class);
        commentIntent.putExtra("blog_post_id", post_id);
        getContext().startActivity(commentIntent);
    }

    public void fistDbProcess(String followingIds){
        try {

            Query firstQuery = firebaseFirestore.collection("Posts").
                    orderBy("timestamp", Query.Direction.DESCENDING).limit(10);
            firstQuery.addSnapshotListener(getActivity(), (documentSnapshots, e) -> {
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
                }catch (Exception e1){
                    e1.printStackTrace();
                }

            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void databaseProcess(String followingIds){
        try {
            if (firebaseAuth.getCurrentUser() != null) {

                Query nextQuery = firebaseFirestore.collection("Posts").
                        orderBy("timestamp", Query.Direction.DESCENDING)
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
                        }catch (Exception e1){
                            e1.printStackTrace();
                        }

                    }
                });
            }
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getActivity(), "Couldn't load feed", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideViews() {
        mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) materialDesignFAM.getLayoutParams();
        int fabBottomMargin = lp.bottomMargin;
        materialDesignFAM.animate().translationY(materialDesignFAM.getHeight()+fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    private void showViews() {
        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        materialDesignFAM.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }
    private void sendToLogin() {

        Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
        startActivity(loginIntent);
        getActivity().finish();

    }
    public void fab(){

        materialDesignFAM = view.findViewById(R.id.social_floating_menu);
        floatingActionButton1 = view.findViewById(R.id.floating_facebook);
        floatingActionButton2 = view.findViewById(R.id.floating_twitter);
        floatingActionButton3 =  view.findViewById(R.id.floating_linkdin);
        floatingActionButton4 =  view.findViewById(R.id.floating_google_plus);

        floatingActionButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PostNewInterestActivity.class);
                startActivity(intent);

            }
        });
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Audio.class);
                startActivity(intent);

            }
        });
        floatingActionButton3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Video.class);
                startActivity(intent);
            }
        });

        floatingActionButton4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), SplashActivity.class);
//                startActivity(intent);
                popDialog();
            }
        });

    }

    public void popDialog(){
        final AlertDialog.Builder builder  = new AlertDialog.Builder(getActivity());
        builder.setTitle("Sure about that?");
        builder.setMessage("You are about to say goodbye");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                // finish activity and delete key from db
                try {
                    dialog.dismiss();
                    firebaseAuth.signOut();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        builder.setNegativeButton("Stay on page", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
