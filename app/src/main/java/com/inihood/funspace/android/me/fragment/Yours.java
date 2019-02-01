package com.inihood.funspace.android.me.fragment;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.adapter.InterestAdapter;
import com.inihood.funspace.android.me.adapter.YoursAdapter;
import com.inihood.funspace.android.me.auth.StartActivity;
import com.inihood.funspace.android.me.discover.CreateInterestActivity;
import com.inihood.funspace.android.me.discover.InterestDetailAcivity;
import com.inihood.funspace.android.me.discover.InterestGetStarted;
import com.inihood.funspace.android.me.helper.RecyclerViewEmptySupport;
import com.inihood.funspace.android.me.interfaces.OnInterestSelectedListener;
import com.inihood.funspace.android.me.model.Interest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Yours extends Fragment implements OnInterestSelectedListener {

    private FloatingActionButton button;
    private RecyclerViewEmptySupport recyclerView;
    private FirebaseAuth firebaseAuth;
    private String user_id;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private View mView;
    private List<Interest> yoursList;
    private YoursAdapter yoursAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;

    public Yours() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_yours, container, false);

        initWidgets();
        initDb();

        yoursList = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        yoursAdapter = new YoursAdapter(yoursList, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(yoursAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstTimeOperation();
            }
        });

        if(firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if(reachedBottom){
                        try {
                           loadMorePost();
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }

                }
            });

           load();

        }else {
            Toast.makeText(getActivity(), "Doesn't seem like you are logged in", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getActivity(), StartActivity.class);
            getActivity().startActivity(intent);
            getActivity().finish();
        }

        return mView;
    }

    private void initWidgets(){
        button = mView.findViewById(R.id.bb);
        recyclerView = mView.findViewById(R.id.recyclerview);
    }

    private void firstTimeOperation(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (!prefs.getBoolean("firstTime", false)) {
            // run first time code
            Intent intent = new Intent(getActivity(), InterestGetStarted.class);
            getActivity().startActivity(intent);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            editor.apply();
        }else {
            Intent intent = new Intent(getActivity(), CreateInterestActivity.class);
            getActivity().startActivity(intent);
        }
    }
    private void initDb() {
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            user_id = firebaseAuth.getCurrentUser().getUid();
            firebaseFirestore = FirebaseFirestore.getInstance();
            storageReference = FirebaseStorage.getInstance().getReference();
        }
    }

    @Override
    public void onViewClicked(String interest_id,  String admin_user_id) {
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
    public void onFollowClicked(String interest_id, String currentUserId, String admin_id) {

    }

    public void load(){
        try {

            Query firstQuery = firebaseFirestore.collection("Interest").whereEqualTo("admin_user_id",user_id)
                    .orderBy("timestamp", Query.Direction.DESCENDING).limit(10);
            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    try {
                        if (!documentSnapshots.isEmpty()) {

                            if (isFirstPageFirstLoad) {

                                lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                                yoursList.clear();

                            }

                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String interstId = doc.getDocument().getId();
                                    Interest interest = doc.getDocument().toObject(Interest.class).withId(interstId);

                                    if (isFirstPageFirstLoad) {

                                        yoursList.add(interest);

                                    } else {

                                        yoursList.add(0, interest);

                                    }

                                    yoursAdapter.notifyDataSetChanged();

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

   public void loadMorePost(){
       try {
           if (firebaseAuth.getCurrentUser() != null) {

               Query nextQuery = firebaseFirestore.collection("Interest").
                       whereEqualTo("admin_user_id",user_id)
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
                                       yoursList.add(blogPost);

                                       yoursAdapter.notifyDataSetChanged();
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