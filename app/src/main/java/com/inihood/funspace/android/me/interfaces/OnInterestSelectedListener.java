package com.inihood.funspace.android.me.interfaces;

public interface OnInterestSelectedListener {
    void onViewClicked(String interest_id, String adminId);
    void onFollowClicked(String interest_id, String currentUserId, String admin_id);
}
