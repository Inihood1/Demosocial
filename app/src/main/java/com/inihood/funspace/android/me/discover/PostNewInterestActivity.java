package com.inihood.funspace.android.me.discover;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
import com.inihood.funspace.android.me.MainActivity;
import com.inihood.funspace.android.me.NewPostActivity;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.helper.ShowToast;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class PostNewInterestActivity extends AppCompatActivity {

    private static final int GALLARY_PICK = 334;
    private Toolbar toolbar;
    private ImageView imageView;
    private EditText post_text;
    private Button addPhotot;
    private Button postBtn;
    private ProgressBar progressBar;
    private Uri imageUri;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_new_interest);

        initWidgets();
        initDb();

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

                                        Glide.with(PostNewInterestActivity.this).applyDefaultRequestOptions(requestOptions).
                                                load(image).thumbnail(Glide.with(PostNewInterestActivity.this).
                                                load(image)).into(user_image);

                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } else {

                                String error = task.getException().getMessage();
                                Toast.makeText(PostNewInterestActivity.this, "(Retrieve Error) : " + error, Toast.LENGTH_LONG).show();

                            }


                        }

                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkConnection(PostNewInterestActivity.this)){
                    finalizeTask();
                }else {
                    Toast.makeText(PostNewInterestActivity.this, "Doesn't seem like you're connected", Toast.LENGTH_LONG).show();
                }
            }
        });

        addPhotot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               verifyPermision();
            }
        });

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
                    .start(PostNewInterestActivity.this);

        }else {
            verifyPermision();
        }
    }

    private void finalizeTask() {
        if (imageUri != null && !post_text.getText().toString().equals("")){
        final String randomName = UUID.randomUUID().toString();
        progressBar.setVisibility(View.VISIBLE);
        postBtn.setEnabled(false);
        post_text.setEnabled(false);

        if (imageUri != null) {
            File newImageFile = new File(imageUri.getPath());
            try {

                compressedImageFile = new Compressor(PostNewInterestActivity.this)
                        .setQuality(200)
                        .compressToBitmap(newImageFile);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        final StorageReference ref = storageReference.child("Interest_post_images").child(randomName + ".jpg");
        UploadTask uploadTask = ref.putBytes(imageData);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
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
                    post(downloadUri4);
                }
            }
        });
    }else {
            Toast.makeText(this, "You must add something", Toast.LENGTH_LONG).show();
        }
    }

    private void post(Uri downloadUri4) {
        try {

            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = df.format(c.getTime());

            String title = post_text.getText().toString();
            String name = firstname + " " + lastname;

            Map<String, Object> postMap = new HashMap<>();
            postMap.put("image_url", downloadUri4.toString());
            postMap.put("image_thumb", downloadUri4.toString());
            if (!title.equals("")){
                postMap.put("desc", title);
            }else {
                postMap.put("desc", "");
            }
            postMap.put("user_id", user_id);
            postMap.put("name", name);
            postMap.put("user_image", image);
            postMap.put("phone", phone);
            postMap.put("timestamp", FieldValue.serverTimestamp());
            postMap.put("time", formattedDate);

            firebaseFirestore.collection("Posts").add(postMap).
                    addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {

                            String link = task.getResult().toString();

                            try {
                                if (task.isSuccessful()) {
                                    progressBar.setVisibility(View.GONE);
                                    Intent intent = new Intent(PostNewInterestActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                    Toast.makeText(PostNewInterestActivity.this, "Done", Toast.LENGTH_SHORT).show();
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
                    showToast.toast("Something is not right", PostNewInterestActivity.this);                    progressBar.setVisibility(View.INVISIBLE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                imageUri = result.getUri();
                imageView.setImageURI(imageUri);
                imageView.setVisibility(View.VISIBLE);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
                ShowToast showToast = new ShowToast();
                showToast.toast(error.toString(), PostNewInterestActivity.this);

            }
        }
    }
}
