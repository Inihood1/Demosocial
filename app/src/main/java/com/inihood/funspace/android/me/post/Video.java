package com.inihood.funspace.android.me.post;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.inihood.funspace.android.me.MainActivity;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.helper.ShowToast;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class Video extends AppCompatActivity {

    private static final int SELECT_VIDEO = 333;
    private TextView username;
    private CircleImageView userImage;
    private EditText post_text;
    private FloatingActionButton video;
    private ImageView backBtn;
    private Button post;
    private StorageReference mStorage;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String current_user_id;
    private  String imagePath;
    private ProgressBar progressbar;
    private String fullname;
    private String image;
    private String phone;
    private String randomName;
    private Uri selectedvideoUri;
    private String videopath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        randomName = UUID.randomUUID().toString();
        iniWidgets();
        dbStuff();
        userDetails();

        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyPermision();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkConnection(Video.this)){
                    upload();
                }else {
                    Toast.makeText(Video.this, "Doesn't seem like you're connected", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private void dbStuff() {
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            current_user_id = firebaseAuth.getCurrentUser().getUid();
            firebaseFirestore = FirebaseFirestore.getInstance();
            storageReference = FirebaseStorage.getInstance().getReference();
        }
    }
    private void iniWidgets() {

        video = findViewById(R.id._button);
        progressbar = findViewById(R.id.progressBar6);
        post_text = findViewById(R.id.post_edittxet);
        username = findViewById(R.id.textView11);
        userImage = findViewById(R.id.user_image);
        post = findViewById(R.id.postTextView);
        backBtn = findViewById(R.id.back);
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
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Video "), SELECT_VIDEO);

        }else {
            verifyPermision();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_VIDEO){
            if (resultCode == RESULT_OK){
                selectedvideoUri = data.getData();
                videopath = getpath(selectedvideoUri);
                MediaPlayer mp = MediaPlayer.create(this, selectedvideoUri);
                int duration = mp.getDuration();
                mp.release();
                /*convert millis to appropriate time*/
               // Toast.makeText(this, "time " + convert(duration), Toast.LENGTH_SHORT).show();


            }
        }
    }

    private String convert(int duration){
        return String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        );
    }

    public String getpath(Uri uri){
        String[] projection  = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null){
            int columb_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(columb_index);
        }else {
            return null;
        }
    }

    public boolean checkConnection(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void upload() {
        progressbar.setVisibility(View.VISIBLE);
        video.setEnabled(false);
        if (selectedvideoUri != null) {
            try {

                final StorageReference reference = storageReference.child("Video").child(randomName + ".mp4");
                UploadTask uploadTask = reference.putFile(selectedvideoUri);
                Toast.makeText(Video.this, "uri stage", Toast.LENGTH_SHORT).show();
                reference.putFile(selectedvideoUri).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                        //displaying percentage in progress dialog
                        Toast.makeText(Video.this, "working "  + ((int) progress) + "%...", Toast.LENGTH_SHORT).show();
                    }
                });
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return reference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Video.this, "finish upload", Toast.LENGTH_SHORT).show();
                            Uri uri = task.getResult();
                            sendToDb(uri);
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }



    }

    private void sendToDb(Uri uri) {
        post.setEnabled(false);
        post_text.setEnabled(false);
        Toast.makeText(Video.this, "map section", Toast.LENGTH_SHORT).show();

        try {
            String input = post_text.getText().toString();

            Map<String, Object> postMap = new HashMap<>();
            postMap.put("image_url", "");
            postMap.put("image_thumb", "");
            postMap.put("video", uri.toString());
            if (!input.equals("")){
                postMap.put("desc", input);
            }else {
                postMap.put("desc", "");
            }
            postMap.put("user_id", current_user_id);
            postMap.put("name", fullname);
            postMap.put("user_image", image);
            postMap.put("phone", phone);
            postMap.put("timestamp", FieldValue.serverTimestamp());
            Toast.makeText(Video.this, "done  maping", Toast.LENGTH_SHORT).show();

            firebaseFirestore.collection("Posts").add(postMap).
                    addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {

                            String link = task.getResult().toString();

                            try {
                                if (task.isSuccessful()) {
                                    progressbar.setVisibility(View.GONE);
                                    Intent intent = new Intent(Video.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                    Toast.makeText(Video.this, "Done", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    progressbar.setVisibility(View.INVISIBLE);
                                    post.setEnabled(true);
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
                    showToast.toast("Something is not right", Video.this);
                    progressbar.setVisibility(View.INVISIBLE);
                    post.setEnabled(true);
                    video.setEnabled(true);
                    post_text.setEnabled(true);
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void userDetails() {
        firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                try {
                    if (task.isSuccessful()) {

                        if (task.getResult().exists()) {
                            try {
                               String firstname = task.getResult().getString("first_name");
                                String lastname = task.getResult().getString("last_name");
                                image = task.getResult().getString("thumb_profile_image");
                                phone = task.getResult().getString("phone");
                                fullname = firstname + " " + lastname;

                                username.setText(firstname);

                                RequestOptions requestOptions = new RequestOptions();
                                requestOptions.placeholder(R.drawable.profile_placeholder);
                                requestOptions.centerCrop();
                                requestOptions.fitCenter();

                                Glide.with(Video.this).applyDefaultRequestOptions(requestOptions).
                                        load(image).into(userImage);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
