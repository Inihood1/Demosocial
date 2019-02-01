package com.inihood.funspace.android.me;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.inihood.funspace.android.me.helper.ShowToast;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ShareActivity extends AppCompatActivity {

    private static final int GALLARY_PICK = 334;
    private Toolbar toolbar;
    private ImageView imageView;
    private EditText post_text;
    private Button addPhotot;
    private Button postBtn;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private String user_id;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private String firstname;
    private String lastname;
    private String image;
    private String phone;
    private Bitmap compressedImageFile;
    private CircleImageView user_image;
    private String incoming;
    private String posttxt;
    private String image1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        initWidgets();
        initDb();
        startResetting();

        post_text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getKeyCode() == KeyEvent.KEYCODE_ENTER){

                    finalizeTask();
                }

                return true;
            }
        });

        try {
            firebaseFirestore.collection("Users").document(user_id).get().
                    addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (task.isSuccessful()) {
                                try {
                                    if (task.getResult().exists()) {

                                        firstname = task.getResult().getString("first_name");
                                        lastname = task.getResult().getString("last_name");
                                        image = task.getResult().getString("thumb_profile_image");
                                        phone = task.getResult().getString("phone");


                                        RequestOptions requestOptions = new RequestOptions();
                                        requestOptions.placeholder(R.drawable.profile_placeholder);
                                        requestOptions.centerCrop();
                                        requestOptions.fitCenter();

                                        Glide.with(ShareActivity.this).applyDefaultRequestOptions(requestOptions).
                                                load(image).thumbnail(Glide.with(ShareActivity.this).
                                                load(image)).into(user_image);

                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } else {

                                String error = task.getException().getMessage();
                                Toast.makeText(ShareActivity.this, "(Retrieve Error) : " + error, Toast.LENGTH_LONG).show();

                            }


                        }

                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkConnection(ShareActivity.this)){
                    finalizeTask();
                }else {
                    Toast.makeText(ShareActivity.this, "Doesn't seem like you're connected", Toast.LENGTH_LONG).show();
                }
            }
        });

//        addPhotot.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//               verifyPermision();
//            }
//        });

    }

    private void startResetting() {
        firebaseFirestore.collection("Posts").document(incoming).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                try {
                    if (task.isSuccessful()){
                        if (task.getResult().exists()){
                            posttxt = task.getResult().getString("desc");
                            image1 = task.getResult().getString("image_url");
                            resetAll(posttxt, image);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void resetAll(String posttxt, String image) {
        post_text.setText(posttxt);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.profile_placeholder);
        requestOptions.centerCrop();
        requestOptions.fitCenter();

        Glide.with(ShareActivity.this).applyDefaultRequestOptions(requestOptions).
                load(image).thumbnail(Glide.with(ShareActivity.this).
                load(image)).into(imageView);
    }

    private void verifyPermision(){
        String[] permissions = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2]) == PackageManager.PERMISSION_GRANTED){

            // do something after permission granted
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(ShareActivity.this);

        }else {
            verifyPermision();
        }
    }

    private void finalizeTask() {
        if (image1 != null && !posttxt.equals("")){
        progressBar.setVisibility(View.VISIBLE);
        postBtn.setEnabled(false);
        post_text.setEnabled(false);
        post();

    }else {
            Toast.makeText(this, "You must add something", Toast.LENGTH_LONG).show();
        }
    }

    private void post() {
        try {

            String name = firstname + " " + lastname;

            Map<String, Object> postMap = new HashMap<>();
            postMap.put("image_url", image1);
            postMap.put("image_thumb", image1);
            if (!posttxt.equals("")){
                postMap.put("desc", posttxt);
            }else {
                postMap.put("desc", "");
            }
            postMap.put("user_id", user_id);
            postMap.put("name", name);
            postMap.put("user_image", image);
            postMap.put("phone", phone);
            postMap.put("timestamp", FieldValue.serverTimestamp());

            firebaseFirestore.collection("Posts").add(postMap).
                    addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {

                            String link = task.getResult().toString();

                            try {
                                if (task.isSuccessful()) {
                                    progressBar.setVisibility(View.GONE);
                                    Intent intent = new Intent(ShareActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                    Toast.makeText(ShareActivity.this, "Done", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    postBtn.setEnabled(true);
                                    post_text.setEnabled(true);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    ShowToast showToast = new ShowToast();
                    showToast.toast("Something is not right", ShareActivity.this);                    progressBar.setVisibility(View.INVISIBLE);
                    postBtn.setEnabled(true);
                    post_text.setEnabled(true);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean checkConnection(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void initWidgets(){
        incoming = getIntent().getStringExtra("blog_post_id");
        if (incoming == null){
            finish();
            ShowToast showToast = new ShowToast();
            showToast.toast("Something went wrong trying to share", ShareActivity.this);
        }
        toolbar = findViewById(R.id.toolbar);
        imageView = findViewById(R.id.image);
        post_text = findViewById(R.id.new_post_desc);
        addPhotot = findViewById(R.id.button4);
        postBtn = findViewById(R.id.button3);
        progressBar = findViewById(R.id.progressBar4);
        user_image = findViewById(R.id.image1);
    }
    private void initDb() {
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            user_id = firebaseAuth.getCurrentUser().getUid();
            firebaseFirestore = FirebaseFirestore.getInstance();
            storageReference = FirebaseStorage.getInstance().getReference();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() == null){
            finish();
        }
    }
}
