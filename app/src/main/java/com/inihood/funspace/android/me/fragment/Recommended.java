package com.inihood.funspace.android.me.fragment;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.adapter.BlogRecyclerAdapter;
import com.inihood.funspace.android.me.adapter.InterestAdapter;
import com.inihood.funspace.android.me.auth.StartActivity;
import com.inihood.funspace.android.me.discover.InterestDetailAcivity;
import com.inihood.funspace.android.me.helper.RecyclerViewEmptySupport;
import com.inihood.funspace.android.me.helper.ShowDialoge;
import com.inihood.funspace.android.me.helper.ShowToast;
import com.inihood.funspace.android.me.interfaces.OnInterestSelectedListener;
import com.inihood.funspace.android.me.model.BlogPost;
import com.inihood.funspace.android.me.model.Comments;
import com.inihood.funspace.android.me.model.Interest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Recommended extends Fragment implements OnInterestSelectedListener{

    private RecyclerViewEmptySupport recyclerView;
    private View view;
    private List<Interest> interestList;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private InterestAdapter interestAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;
    private String ask_to_join;
    private String interest_title;
    private String name;
    private String lastname;
    private String thumb;
    private String full_name = name + " " + lastname;

    public Recommended() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       view = inflater.inflate(R.layout.fragment_recommended, container, false);

        iniWidgets();

        interestList = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        LinearLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        interestAdapter = new InterestAdapter(interestList, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(interestAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setEmptyView(view.findViewById(R.id.list_empty));

        if(firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if(reachedBottom){
                        try {
                            loadMore();
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }

                }
            });

            loadPost();

        }else {
            Toast.makeText(getActivity(), "Doesn't seem like you are logged in", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getActivity(), StartActivity.class);
            getActivity().startActivity(intent);
            getActivity().finish();
        }

        return view;
    }

    private void iniWidgets() {
        recyclerView = view.findViewById(R.id.feed);
    }

    @Override
    public void onViewClicked(String interest_id, String admin_user_id) {
        try {
            Intent intent = new Intent(getActivity(), InterestDetailAcivity.class);
            intent.putExtra("id", interest_id);
            intent.putExtra("admin_id", admin_user_id);
            getActivity().startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getActivity(), "Something is not right", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onFollowClicked(final String interest_id, final String currentUserId, final String admin_id) {

        // check if it is available for everyone to join

        try {

            firebaseFirestore.collection("Interest/").document(interest_id).
                    get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    try {

                        if (!task.getResult().exists()) {

                            ask_to_join = task.getResult().getString("ask_to_join");
                            if (ask_to_join != null) {

                                if (ask_to_join.equals("true")) {

                                requestQuery(interest_id, currentUserId, admin_id);

                                } else {
                                joinQuery(interest_id, currentUserId);
                             }
                        }

                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Something is not right", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getActivity(), "Something is not right", Toast.LENGTH_SHORT).show();
        }

        //end


    }

    private void requestQuery(String interest_id, String currentUserId, String admin_id) {

        try {
        firebaseFirestore.collection("Interest").document("interest_id").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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


        try {
            firebaseFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    try {
                        if (task.getResult().exists()) {
                            name = task.getResult().getString("first_name");
                            lastname = task.getResult().getString("last_name");
                            thumb = task.getResult().getString("thumb_profile_image");
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
        if (currentUserId != null && name != null && interest_title != null && interest_id != null){

            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("user_id_who_is_joining", currentUserId);
            requestMap.put("user_name_who_is_joining", name);
            requestMap.put("interest_title", interest_title);
            requestMap.put("interest_id", interest_id);

            firebaseFirestore.collection("Users").document(admin_id).
                    collection("Interest_Notification/" + interest_id).add(requestMap).
                    addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    try {
                    if (task.isSuccessful()) {
                        ShowToast showToast = new ShowToast();
                        showToast.toast("Done", getActivity());
                    } else {
                        ShowToast showToast = new ShowToast();
                        showToast.toast("Couldn't follow", getActivity());
                    }
                }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }else {
            ShowToast showToast = new ShowToast();
            showToast.toast("Something is not right", getActivity());
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
                            likesMap.put("id", currentUserId);
                            likesMap.put("image", thumb);
                            likesMap.put("first", name);
                            likesMap.put("last", lastname);

                            firebaseFirestore.collection("Interest/" + interest_id + "/members").
                                    document(currentUserId).set(likesMap);

                        } else {

                            firebaseFirestore.collection("Interest/" + interest_id + "/members").
                                    document(currentUserId).delete();

                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Couldn't follow", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getActivity(), "Couldn't follow", Toast.LENGTH_SHORT).show();
        }
    }


    public void loadPost(){
        try {

            Query firstQuery = firebaseFirestore.collection("Interest").
                    orderBy("timestamp", Query.Direction.DESCENDING).limit(10);
            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    try {
                        if (!documentSnapshots.isEmpty()) {

                            if (isFirstPageFirstLoad) {

                                lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                                interestList.clear();

                            }

                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String interstId = doc.getDocument().getId();
                                    Interest interest = doc.getDocument().toObject(Interest.class).withId(interstId);

                                    if (isFirstPageFirstLoad) {

                                        interestList.add(interest);

                                    } else {

                                        interestList.add(0, interest);

                                    }

                                    interestAdapter.notifyDataSetChanged();

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
            Toast.makeText(getActivity(), "Couldn't load Interests", Toast.LENGTH_SHORT).show();

        }
    }

    public void loadMore(){
        try {
            if (firebaseAuth.getCurrentUser() != null) {

                Query nextQuery = firebaseFirestore.collection("Interest")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .startAfter(lastVisible)
                        .limit(10);

                nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        try {
                            if (!documentSnapshots.isEmpty()) {

                                lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                    if (doc.getType() == DocumentChange.Type.ADDED) {

                                        String interstId = doc.getDocument().getId();
                                        Interest blogPost = doc.getDocument().toObject(Interest.class).withId(interstId);
                                        interestList.add(blogPost);

                                        interestAdapter.notifyDataSetChanged();
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
            Toast.makeText(getActivity(), "Couldn't load Interests", Toast.LENGTH_SHORT).show();
        }
    }
}
