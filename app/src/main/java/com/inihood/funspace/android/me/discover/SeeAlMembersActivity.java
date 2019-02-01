package com.inihood.funspace.android.me.discover;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.inihood.funspace.android.me.FollowingActivity;
import com.inihood.funspace.android.me.PuplicProfileActivity;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.adapter.ActivityContactRecyclerAdapter;
import com.inihood.funspace.android.me.adapter.MembersRecyclerAdapter;
import com.inihood.funspace.android.me.helper.ShowToast;
import com.inihood.funspace.android.me.interfaces.OnContactSelectedListner;
import com.inihood.funspace.android.me.interfaces.OnMemberClicked;
import com.inihood.funspace.android.me.model.Following;
import com.inihood.funspace.android.me.model.Members;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeeAlMembersActivity extends AppCompatActivity implements OnMemberClicked {

    public String incoming;
    private ImageView backBtn;
    private RecyclerView recyclerview;
    private List<Members> contact_list;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private MembersRecyclerAdapter followerAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;
    private String current_user_id;
    private FloatingActionButton add;
    private String first_name;
    private String last_name;
    private String fullname;
    private String thumb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_al_members);

        initWidgets();
        initRecyclerviewAdapter();
        iniDb();
        loadUser();

        if(firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if(reachedBottom){

                        loadMoreUsers();

                    }
                }
            });

        }

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SeeAlMembersActivity.this, AddMembersAcivity.class);
                intent.putExtra("interest_id", incoming);
                startActivity(intent);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initWidgets() {
        incoming = getIntent().getStringExtra("interest_id");
        if (incoming == null){
            finish();
            ShowToast showToast = new ShowToast();
            showToast.toast("We couldn't get an id", SeeAlMembersActivity.this);
        }
        recyclerview = findViewById(R.id.recyclerview);
        backBtn = findViewById(R.id.back);
        add = findViewById(R.id.fab);
    }

    private void iniDb() {
        firebaseAuth = FirebaseAuth.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    private void initRecyclerviewAdapter() {
        contact_list = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(SeeAlMembersActivity.this);
        followerAdapter = new MembersRecyclerAdapter(contact_list,this);
        recyclerview.setLayoutManager(layoutManager);
        recyclerview.setAdapter(followerAdapter);
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setHasFixedSize(true);
    }

    @Override
    public void onBackPressed() {
        finish();
    }


    public void loadUser() {

        try {

            Query firstQuery = firebaseFirestore.collection("Interest").
                    document(incoming).collection("members").
                    orderBy("timestamp", Query.Direction.DESCENDING).limit(5);
            firstQuery.addSnapshotListener(SeeAlMembersActivity.this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    try {
                        if (!documentSnapshots.isEmpty()) {

                            if (isFirstPageFirstLoad) {

                                lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                                contact_list.clear();

                            }
                            try {
                                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                    if (doc.getType() == DocumentChange.Type.ADDED) {

                                        String blogPostId = doc.getDocument().getId();
                                        Members blogPost = doc.getDocument().toObject(Members.class).withId(blogPostId);

                                        if (isFirstPageFirstLoad) {

                                            contact_list.add(blogPost);

                                        } else {

                                            contact_list.add(0, blogPost);

                                        }

                                        followerAdapter.notifyDataSetChanged();

                                    }
                                }
                            }catch (Exception e1){
                                e1.printStackTrace();
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
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }

    }


    public void loadMoreUsers(){


        if(firebaseAuth.getCurrentUser() != null) {
            try {
                Query nextQuery = firebaseFirestore.collection("Interest").
                        document(incoming).collection("members")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .startAfter(lastVisible)
                        .limit(5);

                nextQuery.addSnapshotListener(SeeAlMembersActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        try {
                            if (!documentSnapshots.isEmpty()) {

                                lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                    if (doc.getType() == DocumentChange.Type.ADDED) {

                                        String blogPostId = doc.getDocument().getId();
                                        Members blogPost = doc.getDocument().toObject(Members.class).withId(blogPostId);
                                        contact_list.add(blogPost);

                                        followerAdapter.notifyDataSetChanged();
                                    }

                                }
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }

                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onAddClicked(final String other_person_id, final String thumb1, final String fullname1, final CardView cardView) {
        try {

            firebaseFirestore.collection("Interest" + "/" + incoming + "/members").
                    document(other_person_id).
                    get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    try {

                        if (!task.getResult().exists()) {

                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());
                            likesMap.put("member_id", other_person_id);
                            likesMap.put("thumb", thumb1);
                            likesMap.put("name", fullname1);

                            likesMap.put("interest_id", incoming);

                            firebaseFirestore.collection("Interest/" + incoming + "/members").
                                    document(other_person_id).set(likesMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    cardView.setVisibility(View.GONE);
                                }
                            });

                        } else {

                            firebaseFirestore.collection("Interest/" + incoming + "/members").
                                    document(other_person_id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    cardView.setVisibility(View.GONE);
                                }
                            });

                        }

                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(SeeAlMembersActivity.this, "Couldn't remove", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(SeeAlMembersActivity.this, "Couldn't remove", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClicked(String viewClicked, String currentUserId) {

        if (viewClicked.equals(currentUserId)){
            Toast.makeText(SeeAlMembersActivity.this, "This is you", Toast.LENGTH_SHORT).show();
        }else {
            try {
                Intent intent = new Intent(SeeAlMembersActivity.this, PuplicProfileActivity.class);
                intent.putExtra("blogPostUserId", viewClicked);
                startActivity(intent);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
