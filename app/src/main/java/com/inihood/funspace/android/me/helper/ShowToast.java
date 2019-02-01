package com.inihood.funspace.android.me.helper;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ShowToast {

    public ShowToast(){

    }

    public void toast(String sms, Context context){
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, sms, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
