package com.inihood.funspace.android.me.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.discover.AddMembersAcivity;
import com.inihood.funspace.android.me.interfaces.OnAddMemberClicked;
import com.inihood.funspace.android.me.interfaces.OnMemberClicked;
import com.inihood.funspace.android.me.interfaces.SendTheId;
import com.inihood.funspace.android.me.model.Following;
import com.inihood.funspace.android.me.model.Members;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddMemberAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public List<Following> contact_list;
    public Context context;
    public int previousPosition = 0;
    private int HEADER = 0;
    private int BODY = 1;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String interestId;
    private OnAddMemberClicked mListner;


    public AddMemberAdapter(List<Following> contact_list, OnAddMemberClicked listner){
        this.contact_list = contact_list;
        mListner = listner;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_member_list_item, parent, false);

        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ContactView(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ContactView holder1 = (ContactView) holder;
        bindData(holder1, position, mListner);
    }

    private void bindData(final ContactView holder1, final int position, final OnAddMemberClicked mListner) {

        final String contact_position_id = contact_list.get(position).BlogPostId;
        final String other_person_id = contact_list.get(position).getId();
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();
        final String thumb = contact_list.get(position).getThumb();
        String first = contact_list.get(position).getFirst();
        String last = contact_list.get(position).getLast();
        final String full_name = first + " " + last;


        if (thumb != null){
            holder1.setThumb(thumb, context);
        }


        if (first != null && last != null){
            holder1.setName(first,last);
        }



        holder1.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListner != null) {
                    mListner.onClicked(other_person_id, currentUserId);

                }
            }
        });

        holder1.videoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListner != null) {
                    mListner.onAddClicked(other_person_id, thumb, full_name, holder1.videoCall, holder1.cardView);

                }
            }
        });

        try {

            firebaseFirestore.collection("Interest/" + interestId + "/members").
                    document(other_person_id).
                    addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            try {

                                if (documentSnapshot.exists()) {

                                    holder1.videoCall.setText("Remove");

                                } else {

                                    holder1.videoCall.setText("Add");

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

    @Override
    public int getItemCount() {
        return contact_list.size();
    }


    public static class ContactView extends RecyclerView.ViewHolder{

        public View mView;
        private CircleImageView user_image;
        private TextView username;
        private Button videoCall;
        private CardView cardView;

        public ContactView(View itemView) {
            super(itemView);
            mView = itemView;

            user_image = mView.findViewById(R.id.image);
            username = mView.findViewById(R.id.name);
            videoCall = mView.findViewById(R.id.imageView3);
            cardView = mView.findViewById(R.id.bb);
        }

        public void setThumb(String thumb, Context context) {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.image_placeholder);
            requestOptions.centerCrop();
            requestOptions.fitCenter();

            Glide.with(context).applyDefaultRequestOptions(requestOptions).
                    load(thumb).into(user_image);
        }

        public void setName(String first_name, String lastname) {
            String name = first_name + " " + lastname;
            username.setText(name);
        }

    }

}
