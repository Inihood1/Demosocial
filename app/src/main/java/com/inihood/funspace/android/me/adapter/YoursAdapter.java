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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.interfaces.OnInterestSelectedListener;
import com.inihood.funspace.android.me.model.Interest;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class YoursAdapter extends RecyclerView.Adapter<YoursAdapter.YoursviewHolder> {

    private List<Interest> interestList;
    private Context context;
    private int lastPosition = -1;
    private OnInterestSelectedListener listener;
    private FirebaseAuth firebaseAuth;
    private String interest_id;
    private String currentUserId;
    private FirebaseFirestore firebaseFirestore;
    private String admin_id;

    public YoursAdapter(List<Interest> interestList, OnInterestSelectedListener listener) {
        this.interestList = interestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public YoursviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_interest_single_your,
                parent, false);
        context = parent.getContext();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        return new YoursviewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull YoursviewHolder holder, int position) {
        Animation animation = AnimationUtils.loadAnimation(context,
                (position > lastPosition) ? R.anim.up_from_bottom
                        : R.anim.down_from_top);
        holder.itemView.startAnimation(animation);
        lastPosition = position;

        bindData(holder, position, listener);
    }

    private void bindData(final YoursviewHolder holder, int position, final OnInterestSelectedListener listener) {

        try {
            interest_id = interestList.get(position).BlogPostId;
             admin_id = interestList.get(position).getAdmin_user_id();
            currentUserId = firebaseAuth.getCurrentUser().getUid();
        }catch (Exception e){
            e.printStackTrace();
        }

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
                    }
                }
            });

            holder.interst_cover_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        listener.onViewClicked(interest_id, admin_id);
                    }
                }
            });

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        return interestList.size();
    }

    public class YoursviewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private ImageView interst_cover_image;
        private CircleImageView admin_imag;
        private TextView interst_name;

        public YoursviewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            interst_cover_image = mView.findViewById(R.id.imageView7);
            admin_imag = mView.findViewById(R.id.person_image);
            interst_name = mView.findViewById(R.id.textView13);
        }

        public void setAdmin_image(String admin_image) {
            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.profile_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).
                    load(admin_image).into(admin_imag);
        }

        public void setInterst_cover_image(String cover_image) {

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.profile_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).
                    load(cover_image).into(interst_cover_image);
        }

        public void setInterst_name(String name) {
            interst_name.setText(name);
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull YoursviewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }
}
