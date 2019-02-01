package com.inihood.funspace.android.me.interfaces;

import android.support.v7.widget.CardView;

import com.inihood.funspace.android.me.FollowingActivity;

public interface OnContactSelectedListner {
        void onAudio(String contact_position_id);
        void onVideo(String contact_position_id);
        void onContactSelected(String contact_position_id);
        void onImageSelected(String image, String thumb);
        void onLongClicked(FollowingActivity followingActivity, CardView cardView);
}
