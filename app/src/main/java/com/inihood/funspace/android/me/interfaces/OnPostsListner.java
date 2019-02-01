package com.inihood.funspace.android.me.interfaces;

import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.TextView;

public interface OnPostsListner{
    void onPostSelected(String blogPostId);
    void onComment(String blogPostId);
    void onPostLike(String blogPostId, String currentUserId, ConstraintLayout commentViewContainer, String postUserId);
    void onUserSelected(View view, String blogPostUserId, String currentUserId);
    void onBlogImageSelected(String blogPostId);
    void onCommentChange(String post_id, TextView userComment);
    void onShare(String post_id);
}
