package com.inihood.funspace.android.me.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.model.Following;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public List<Following> contact_list;
    public Context context;
    public int previousPosition = 0;
    private int HEADER = 0;
    private int BODY = 1;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private int lastPosition = -1;

    public interface OnContactSelectedListner{
        void onContactSelected(String contact_position_id);
        void onImageSelected(String image, String thumb);
    }

    private OnContactSelectedListner mListner;

    public ContactRecyclerAdapter(List<Following> contact_list, OnContactSelectedListner listner){
        this.contact_list = contact_list;
        mListner = listner;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.follower_list_item,
                parent, false);
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

    private void bindData(ContactView holder1, int position, final OnContactSelectedListner mListner) {

        final String contact_position_id = contact_list.get(position).BlogPostId;
        final String blogPostUserId = contact_list.get(position).getId();
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();
        final String image = contact_list.get(position).getThumb();

        final String thumb = contact_list.get(position).getThumb();
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

    }

    @Override
    public int getItemCount() {
        return contact_list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    public static class ContactView extends RecyclerView.ViewHolder{

        public View mView;
        private CircleImageView user_image;
        private TextView username;
        private TextView nick;
        private CheckBox checkBox;

        public ContactView(View itemView) {
            super(itemView);
            mView = itemView;

            user_image = mView.findViewById(R.id.image);
            username = mView.findViewById(R.id.name);
            nick = mView.findViewById(R.id.textView9);
            checkBox = mView.findViewById(R.id.checkBox);
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
    }
}
