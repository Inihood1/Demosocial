package com.inihood.funspace.android.me.auth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.inihood.funspace.android.me.MainActivity;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.discover.PostNewInterestActivity;
import com.inihood.funspace.android.me.helper.ShowToast;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


public class SetupActivity extends AppCompatActivity {

    private Uri mainImageURI = null;
    private boolean isChanged = false;
    private Bitmap compressedImageFile;
    private String user_id;
    private Button setupBtn;
    private ProgressBar setupProgress;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private CircleImageView profileimage;
    private static final int GALLARY_PICK = 123;
    private Uri coverUri;
    private Uri destinationUri;
    private String firstname;
    private String lastname;
    private String gender;
    private String phone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Toolbar setupToolbar = findViewById(R.id.setupToolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Account Setup");

        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        setupBtn = findViewById(R.id.setup_btn);
        setupProgress = findViewById(R.id.setup_progress);
        profileimage = findViewById(R.id.circleImageView2);

        setupProgress.setVisibility(View.VISIBLE);
        setupBtn.setEnabled(false);

        firebaseFirestore.collection("Users").document(user_id).get().
                addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                try {
                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {
                        try {
                        String cover_image = task.getResult().getString("cover_image");
                        String profile_image = task.getResult().getString("profile_image");
                        firstname = task.getResult().getString("first_name");
                        lastname = task.getResult().getString("last_name");
                        gender = task.getResult().getString("gender");
                        phone = task.getResult().getString("phone");

                        RequestOptions placeholderRequest1 = new RequestOptions();
                        placeholderRequest1.placeholder(R.drawable.profile_placeholder);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest1).
                                load(profile_image).into(profileimage);
                    }catch (Exception e){
                        e.printStackTrace();
                        }

                    }

                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "(FIRESTORE Retrieve Error) : " + error, Toast.LENGTH_LONG).show();
                }
            }catch (Exception e){
                    e.printStackTrace();
                }

                setupProgress.setVisibility(View.INVISIBLE);
                setupBtn.setEnabled(true);

            }
        });

        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileImageManipulation();
            }
        });


        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowToast showToast = new ShowToast();
                showToast.toast("Working.....", SetupActivity.this);
                uplaodImage(mainImageURI);
                uplaodThumb(mainImageURI);
                //post(mainImageURI);

            }

        });


    }

//    private void post(Uri downloadUri4) {
//        try {
//
//            String name = firstname + " " + lastname;
//
//            Map<String, Object> postMap = new HashMap<>();
//            postMap.put("image_url", downloadUri4.toString());
//            postMap.put("image_thumb", downloadUri4.toString());
//            postMap.put("desc", "");
//            postMap.put("user_id", user_id);
//            if (gender.equals("male")){
//                postMap.put("name", name + " changed his profile image");
//            }else {
//                postMap.put("name", name + " changed her profile image");
//            }
//            postMap.put("user_image", downloadUri4.toString());
//            postMap.put("phone", phone);
//            postMap.put("timestamp", FieldValue.serverTimestamp());
//
//            firebaseFirestore.collection("Posts").add(postMap).
//                    addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
//                        @Override
//                        public void onComplete(@NonNull Task<DocumentReference> task) {
//
//                            String link = task.getResult().toString();
//
//                            try {
//                                if (task.isSuccessful()) {
//                                    Toast.makeText(SetupActivity.this, "Done", Toast.LENGTH_SHORT).show();
//                                    finish();
//                                } else {
//
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    ShowToast showToast = new ShowToast();
//                    showToast.toast("Something is not right", SetupActivity.this);
//
//                }
//            });
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }

    private void BringImagePicker() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(60, 60)
                .start(SetupActivity.this);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                try {
                mainImageURI = result.getUri();

                RequestOptions placeholderRequest = new RequestOptions();
                placeholderRequest.fitCenter();
                placeholderRequest.placeholder(R.drawable.profile_placeholder);
                Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).
                        load(mainImageURI).into(profileimage);
            }catch (Exception e){
                    e.printStackTrace();
                }

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }


    }



    private void profileImageManipulation() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

            } else {

                BringImagePicker();

            }

        } else {

            BringImagePicker();

        }
    }
    private void uplaodThumb(Uri mainImageURI) {
        user_id = firebaseAuth.getCurrentUser().getUid();
        if (mainImageURI != null){
            File newImageFile = new File(mainImageURI.getPath());
            try {

                compressedImageFile = new Compressor(this)
                        .setMaxHeight(125)
                        .setMaxWidth(125)
                        .setQuality(50)
                        .compressToBitmap(newImageFile);

            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos1);
            byte[] thumbData = baos1.toByteArray();

            final StorageReference ref = storageReference.child("thumb_profile_images").child(user_id + ".jpg");
            UploadTask image_path= ref.putBytes(thumbData);

            image_path.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri4 = task.getResult();
                        storeThumb(downloadUri4);
                    }
                }
            });

        }else {
            Toast.makeText(SetupActivity.this, "Something is not right", Toast.LENGTH_SHORT).show();
        }
    }
    private void uplaodImage(Uri uri){
        user_id = firebaseAuth.getCurrentUser().getUid();
        if (uri != null){
            File newImageFile = new File(uri.getPath());
            try {

                compressedImageFile = new Compressor(this)
                        .setMaxHeight(125)
                        .setMaxWidth(125)
                        .setQuality(100)
                        .compressToBitmap(newImageFile);

            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] thumbData = baos.toByteArray();

            final StorageReference ref = storageReference.child("profile_images").child(user_id + ".jpg");
            UploadTask  image_path = ref.putBytes(thumbData);

            image_path.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri4 = task.getResult();
                        storeImage(downloadUri4);
                    }
                }
            });

        }else {
            Toast.makeText(SetupActivity.this, "Could not get the image", Toast.LENGTH_SHORT).show();
        }
    }
    private void storeImage(Uri uri) {

            Map<String, Object> userMap = new HashMap<>();
            if (uri != null) {
                userMap.put("profile_image", uri.toString());
            }

            firebaseFirestore.collection("Users").document(user_id).update(userMap).
                    addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                Toast.makeText(SetupActivity.this, "The user Settings are updated.", Toast.LENGTH_LONG).show();

                            } else {

                                String error = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this, "(FIRESTORE Error) : " + error, Toast.LENGTH_LONG).show();

                            }

                        }
                    });



    }

    private void storeThumb(Uri uri) {

            Map<String, Object> userMap = new HashMap<>();
            if (uri != null) {
                userMap.put("thumb_profile_image", uri.toString());
            }

            firebaseFirestore.collection("Users").document(user_id).update(userMap).
                    addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                Toast.makeText(SetupActivity.this, "The user Settings are updated.", Toast.LENGTH_LONG).show();

                            } else {

                                String error = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this, "(FIRESTORE Error) : " + error, Toast.LENGTH_LONG).show();

                            }

                        }
                    });

    }
}
