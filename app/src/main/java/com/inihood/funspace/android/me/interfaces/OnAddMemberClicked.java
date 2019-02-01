package com.inihood.funspace.android.me.interfaces;

import android.support.v7.widget.CardView;
import android.widget.Button;

public interface OnAddMemberClicked {
    void onAddClicked(String other_person_id, String thumb, String fullname, Button button, CardView cardView);
    void onClicked(String viewClicked, String member_Id);
}
