package com.inihood.funspace.android.me.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.interfaces.OnInterestSelectedListener;
import com.inihood.funspace.android.me.model.Interest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class InterestAdapter extends RecyclerView.Adapter<InterestAdapter.ViewHolder> {

    private List<Interest> interestList;
    private Context context;
    private int lastPosition = -1;
    private OnInterestSelectedListener listener;
    private FirebaseAuth firebaseAuth;
    private  String interest_id;
    private  String currentUserId;
    private String admin_id;
    private FirebaseFirestore firebaseFirestore;

    public InterestAdapter(List<Interest> interestList, OnInterestSelectedListener listener){
        this.interestList = interestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InterestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_interest_single,
                parent,false);
        context = parent.getContext();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InterestAdapter.ViewHolder holder, int position) {

        Animation animation = AnimationUtils.loadAnimation(context,
                (position > lastPosition) ? R.anim.up_from_bottom
                        : R.anim.down_from_top);
        holder.itemView.startAnimation(animation);
        lastPosition = position;

        bindData(holder, position, listener);
    }

    private void bindData(final ViewHolder holder, int position, final OnInterestSelectedListener listener) {

        try {
        interest_id = interestList.get(position).BlogPostId;
         admin_id = interestList.get(position).getAdmin_user_id();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
    }catch (Exception e){
            e.printStackTrace();
        }

       // check if it is available for everyone to join

        if (admin_id.equals(currentUserId)){
            holder.itemView.setVisibility(View.GONE);
        }

//        try {
//
//            firebaseFirestore.collection("Interest/").document(interest_id).
//                    get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//
//                    try {
//
//                        if (!task.getResult().exists()) {
//
//                           String ask_to_join = task.getResult().getString("ask_to_join");
//                            if (ask_to_join != null && ask_to_join.equals("true")) {
//                                holder.follow.setText(R.string.ask_to_join);
//                                holder.follow.setBackgroundResource(R.drawable.add);
//                            }else {
//                                holder.follow.setText(context.getString(R.string.follow));
//                                holder.follow.setBackgroundResource(R.drawable.add);
//                            }
//
//                        }
//                    }catch (Exception e){
//                        e.printStackTrace();
//                        Toast.makeText(context, "Something is not right", Toast.LENGTH_SHORT).show();
//                    }
//
//                }
//            });
//        }catch (Exception e){
//            e.printStackTrace();
//            Toast.makeText(context, "Something is not right", Toast.LENGTH_SHORT).show();
//        }

       //end


        try {
        String interest_title = interestList.get(position).getTitle();
        if (interest_title != null) {
            holder.setInterst_name(interest_title);
        }

        String interest_cover_image = interestList.get(position).getCover_image();
        if (interest_cover_image != null) {
            holder.setInterst_cover_image(interest_cover_image);
        }

        String admin_iamge = interestList.get(position).getAdmin_image();
        if (admin_iamge != null) {
            holder.setAdmin_image(admin_iamge);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null){
                    listener.onViewClicked(interest_id, admin_id);
                }else {
                    Toast.makeText(context, "cant retrieve the id", Toast.LENGTH_SHORT).show();
                }
            }
        });

            holder.interst_cover_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        listener.onViewClicked(interest_id, admin_id);
                    }else {
                        Toast.makeText(context, "cant retrieve the id", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            holder.follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        listener.onFollowClicked(interest_id, currentUserId, admin_id);
                    }else {
                        Toast.makeText(context, "cant retrieve the id", Toast.LENGTH_SHORT).show();
                    }
                }
            });


//            try {
//
//                firebaseFirestore.collection("Interest/" + interest_id + "/members").
//                        document(currentUserId).
//                        addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                            @Override
//                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
//                                try {
//
//                                    if (documentSnapshot.exists()) {
//
//                                        holder.follow.setText(R.string.unfollow);
//
//                                    } else {
//                                        holder.follow.setText(context.getString(R.string.follow));
//                                        holder.follow.setBackgroundResource(R.drawable.add);
//
//                                    }
//                                } catch (Exception e2) {
//                                    e2.printStackTrace();
//                                }
//
//                            }
//                        });
//            }catch (Exception e){
//                e.printStackTrace();
//            }


    }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return interestList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private ImageView interst_cover_image;
        private CircleImageView admin_imag;
        private TextView interst_name;
        private Button follow;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            interst_cover_image = mView.findViewById(R.id.imageView7);
            admin_imag = mView.findViewById(R.id.person_image);
            interst_name = mView.findViewById(R.id.textView13);
            follow = mView.findViewById(R.id.follow);
            follow.setVisibility(View.GONE);
        }

        public void  setAdmin_image(String admin_image){
            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.profile_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).
                    load(admin_image).into(admin_imag);
        }

        public void setInterst_cover_image(String cover_image){

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.profile_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).
                    load(cover_image).into(interst_cover_image);
        }

        public void setInterst_name(String name){
            interst_name.setText(name);
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }
}
