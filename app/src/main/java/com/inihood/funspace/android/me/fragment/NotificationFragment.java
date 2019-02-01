package com.inihood.funspace.android.me.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.inihood.funspace.android.me.PuplicProfileActivity;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.SinglePostActivity;
import com.inihood.funspace.android.me.adapter.BlogRecyclerAdapter;
import com.inihood.funspace.android.me.adapter.NotificationAdapter;
import com.inihood.funspace.android.me.helper.RecyclerViewEmptySupport;
import com.inihood.funspace.android.me.interfaces.OnNotification;
import com.inihood.funspace.android.me.model.BlogPost;
import com.inihood.funspace.android.me.model.Notification;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment implements OnNotification{

    private RecyclerViewEmptySupport list;
    private View view;
    private List<Notification> blog_list;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private NotificationAdapter blogRecyclerAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;
    private Toolbar mToolbar;
    private String current_user_id;

    public NotificationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notification, container, false);


        list = view.findViewById(R.id.not);
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            current_user_id = firebaseAuth.getCurrentUser().getUid();
        }else {
            Toast.makeText(getActivity(), "cant get your id", Toast.LENGTH_SHORT).show();
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        blogRecyclerAdapter = new NotificationAdapter(blog_list, getContext(), this);
        list.setLayoutManager(layoutManager);
        list.setAdapter(blogRecyclerAdapter);
        list.setHasFixedSize(true);
        list.setEmptyView(view.findViewById(R.id.list_empty));

        if(firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            list.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);


                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if(reachedBottom){

                        databaseProcess();

                    }

                }
            });

        }
        fistDbProcess();
        return view;
    }

    private void databaseProcess() {
        try {
            if (firebaseAuth.getCurrentUser() != null) {

                Query nextQuery = firebaseFirestore.collection("Users").document(current_user_id).collection("notification")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
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
                                        Notification blogPost = doc.getDocument().toObject(Notification.class).withId(blogPostId);
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

    private void fistDbProcess() {
        try {

            Query firstQuery = firebaseFirestore.collection("Users").document(current_user_id).collection("notification").
                    orderBy("timestamp", Query.Direction.DESCENDING).limit(10);
            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
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
                                    Notification blogPost = doc.getDocument().toObject(Notification.class).withId(blogPostId);

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

                }

            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClicked(String post_id, String type) {
        switch (type) {
            case "follow":
                try {
                    Intent intent = new Intent(getActivity(), PuplicProfileActivity.class);
                    intent.putExtra("blogPostUserId", post_id);
                    getActivity().startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case "like": {

                Intent commentIntent = new Intent(getActivity(), SinglePostActivity.class);
                commentIntent.putExtra("blog_post_id", post_id);
                startActivity(commentIntent);

                break;
            }
            case "comment": {
                Intent commentIntent = new Intent(getActivity(), SinglePostActivity.class);
                commentIntent.putExtra("blog_post_id", post_id);
                startActivity(commentIntent);
                break;
            }
        }
    }

    @Override
    public void onUserImageClick(String user_id) {
        if (user_id.equals(current_user_id)){
            Toast.makeText(getActivity(), "This is you", Toast.LENGTH_SHORT).show();
        }else {
            try {
                Intent intent = new Intent(getActivity(), PuplicProfileActivity.class);
                intent.putExtra("blogPostUserId", user_id);
                getActivity().startActivity(intent);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
