package com.inihood.funspace.android.me.adapter;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.inihood.funspace.android.me.FollowingActivity;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.interfaces.OnContactSelectedListner;
import com.inihood.funspace.android.me.model.Following;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityContactRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public List<Following> contact_list;
    public Context context;
    public int previousPosition = 0;
    private int HEADER = 0;
    private int BODY = 1;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private int lastPosition = -1;
    private FollowingActivity followingActivity;

    private OnContactSelectedListner mListner;

    public ActivityContactRecyclerAdapter(List<Following> contact_list, OnContactSelectedListner listner, Context context){
        this.contact_list = contact_list;
        mListner = listner;
        this.context = context;
        followingActivity = (FollowingActivity) context;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.follower_list_item, parent, false);
        ContactView contactView = new ContactView(view, followingActivity);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return contactView;

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ContactView holder1 = (ContactView) holder;
        bindData(holder1, position, mListner);
    }

    private void bindData(final ContactView holder1, final int position, final OnContactSelectedListner mListner) {

        final String contact_position_id = contact_list.get(position).BlogPostId;
        final String blogPostUserId = contact_list.get(position).getId();
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();
        final String image = contact_list.get(position).getThumb();
        final String thumb = contact_list.get(position).getThumb();

        if (!followingActivity.is_in_action_mode){
            holder1.checkBox.setVisibility(View.GONE);
            holder1.audioCall.setVisibility(View.VISIBLE);
            holder1.videoCall.setVisibility(View.VISIBLE);
            itemClick(contact_position_id, holder1, thumb, image);

        }else {
            holder1.checkBox.setVisibility(View.VISIBLE);
            holder1.checkBox.setChecked(false);
            holder1.audioCall.setVisibility(View.GONE);
            holder1.videoCall.setVisibility(View.GONE);
        }

        if (thumb != null){
            holder1.setThumb(thumb, context);
        }

        String first_name = contact_list.get(position).getFirst();
        String last_name = contact_list.get(position).getLast();
        if (first_name != null && last_name != null){
            holder1.setName(first_name, last_name);
        }

        String nick = contact_list.get(position).getNick();
        if (nick != null){
            holder1.setNick(nick);
        }



        holder1.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mListner != null) {
                    mListner.onLongClicked(followingActivity, holder1.cardView);

                }
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return contact_list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    public static class ContactView extends RecyclerView.ViewHolder implements View.OnClickListener{

        public View mView;
        private CircleImageView user_image;
        private TextView username;
        private TextView nick;
        private CheckBox checkBox;
        private FollowingActivity followingActivity;
        private CardView cardView;
        private ImageView audioCall;
        private ImageView videoCall;

        public ContactView(View itemView, FollowingActivity followingActivity) {
            super(itemView);
            mView = itemView;

            user_image = mView.findViewById(R.id.image);
            username = mView.findViewById(R.id.name);
            nick = mView.findViewById(R.id.textView9);
            checkBox = mView.findViewById(R.id.checkBox);
            this.followingActivity = followingActivity;
            audioCall = mView.findViewById(R.id.imageView2);
            videoCall = mView.findViewById(R.id.imageView3);
            checkBox.setOnClickListener(this);
        }

        public void setThumb(String thumb, Context context) {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.image_placeholder);
            requestOptions.centerCrop();
            requestOptions.fitCenter();

            Glide.with(context).applyDefaultRequestOptions(requestOptions).
                    load(thumb).into(user_image);
        }

        public void setName(String first_name, String last_name) {
            String  name1 = first_name + " " + last_name;
            username.setText(name1);
        }

        public void setNick(String nick1) {
            nick.setText(nick1);
        }

        @Override
        public void onClick(View v) {
            followingActivity.prepareSelection(v, getAdapterPosition());
        }
    }
    public void updateAdapter(ArrayList<Following> list){

        for (Following following : list){
            contact_list.remove(following);
        }
        notifyDataSetChanged();
    }
    public void itemClick(final String contact_position_id, ContactView holder1, final String thumb, final String image){

        holder1.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListner != null) {
                    mListner.onContactSelected(contact_position_id);

                }
            }
        });

        holder1.user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListner != null) {
                    mListner.onImageSelected(image, thumb);

                }
            }
        });

        holder1.audioCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListner != null) {
                    mListner.onAudio(contact_position_id);

                }
            }
        });

        holder1.videoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListner != null) {
                    mListner.onVideo(contact_position_id);

                }
            }
        });

    }
}
