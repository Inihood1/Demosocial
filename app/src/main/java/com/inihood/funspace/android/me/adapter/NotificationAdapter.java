package com.inihood.funspace.android.me.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.interfaces.OnNotification;
import com.inihood.funspace.android.me.model.Notification;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public List<Notification> notificationList;
    private Context context;
    private View view;
    private OnNotification onNotification;

    public NotificationAdapter(List<Notification> notificationList, Context context, OnNotification onNotification){
        this.notificationList = notificationList;
        this.context = context;
        this.onNotification = onNotification;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_single_item, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        bindData(holder, position, onNotification);
    }

    private void bindData(ViewHolder holder, int position, final OnNotification onNotification) {

        final String id_of_the_thing_that_was_triggerd = notificationList.get(position).getId_of_the_thing_that_was_triggerd();
        final String user_id_who_trigger = notificationList.get(position).getUser_id_who_trigger();
        final String user_image_who_trigger = notificationList.get(position).getUser_image_who_trigger();
        final String the_text = notificationList.get(position).getThe_text();
        final String time = notificationList.get(position).getTime();
        final String type = notificationList.get(position).getType();

        if (the_text != null && user_image_who_trigger != null && time != null){
            holder.setUserDaata(the_text, user_image_who_trigger, time);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onNotification != null && id_of_the_thing_that_was_triggerd != null){
                onNotification.onClicked(id_of_the_thing_that_was_triggerd, type);
            }
        });

        holder.imageView.setOnClickListener(v -> {
            try {

                if (user_id_who_trigger != null) {
                    if (onNotification != null) {
                        onNotification.onUserImageClick(user_id_who_trigger);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        });

    }

    @Override
    public int getItemCount() {
        if (notificationList != null){
            return notificationList.size();
        }else {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView imageView;
        private TextView mtext;
        private TextView time;
        private View mView;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            imageView = mView.findViewById(R.id.image);
            mtext = mView.findViewById(R.id.text);
            time = mView.findViewById(R.id.time);

        }

        public void setUserDaata(String text, String user_image_who_trigger, String timetxt) {
            try {
            mtext.setText(text);
            time.setText(timetxt);
            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.profile_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(user_image_who_trigger).
                    into(imageView);
        }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
