package com.inihood.funspace.android.me;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class PhotoViewer extends AppCompatActivity {

    private String incoming;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);

        initWidgets();
        ImageView snappyImageViewer = findViewById(R.id.photo_view);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.image_placeholder);
        requestOptions.fitCenter();

        Glide.with(getApplicationContext()).applyDefaultRequestOptions(requestOptions).
                load(incoming).into(snappyImageViewer);

    }

    private void initWidgets() {
        incoming = getIntent().getStringExtra("image");
        if (incoming == null){
            finish();
        }
    }
}
