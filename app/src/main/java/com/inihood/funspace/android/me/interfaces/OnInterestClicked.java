package com.inihood.funspace.android.me.interfaces;

import android.support.constraint.ConstraintLayout;
import android.widget.TextView;

public interface OnInterestClicked {
    void onPostSelected(String blogPostId);
    void onComment(String blogPostId);
    void onPostLike(String blogPostId, String currentUserId);
    void onUserSelected(String blogPostUserId, String currentUserId, String postUserId);
    void onBlogImageSelected(String blogPostId);
}
