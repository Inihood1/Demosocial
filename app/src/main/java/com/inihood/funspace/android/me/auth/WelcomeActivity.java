package com.inihood.funspace.android.me.auth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.inihood.funspace.android.me.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button bbtn_continue = findViewById(R.id.button2);

        bbtn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invockFirebaseUi();
            }
        });
    }

    private void invockFirebaseUi() {
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
