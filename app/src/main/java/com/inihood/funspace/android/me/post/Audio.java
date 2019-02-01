package com.inihood.funspace.android.me.post;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.devlomi.record_view.OnBasketAnimationEnd;
import com.devlomi.record_view.OnRecordListener;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.inihood.funspace.android.me.MainActivity;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.discover.PostNewInterestActivity;
import com.inihood.funspace.android.me.helper.AppLog;
import com.inihood.funspace.android.me.helper.ShowToast;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.inihood.funspace.android.me.helper.Storage.AUDIO_RECORDER_FILE_EXT_3GP;
import static com.inihood.funspace.android.me.helper.Storage.AUDIO_RECORDER_FILE_EXT_MP4;
import static com.inihood.funspace.android.me.helper.Storage.currentFormat;

public class Audio extends AppCompatActivity{

        private TextView username;
        private CircleImageView userImage;
        private EditText post_text;
        private RecordView recordView;
        private RecordButton recordButton;
        private ImageView backBtn;
        private Button post;
        private StorageReference mStorage;
        private StorageReference storageReference;
        private FirebaseAuth firebaseAuth;
        private FirebaseFirestore firebaseFirestore;
        private String current_user_id;
        private MediaRecorder mediaRecorder = null;
        private String filename = null;
        private String folder = "Funspace";
        private String randomName;
        private ProgressBar progressbar;
        private String fullname;
        private String image;
        private String phone;
    private int currentFormat = 0;
    private int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP };
    private String file_exts[] = { AUDIO_RECORDER_FILE_EXT_MP4, AUDIO_RECORDER_FILE_EXT_3GP };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        iniWidgets();
        dbStuff();
        userDetails();

        randomName = UUID.randomUUID().toString();
        filename = Environment.getExternalStorageDirectory().getAbsolutePath();
        filename += "/record.3gp";
        //filename += folder + "/" + randomName + "/.3gp";

        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                Log.d("RecordView", "onStart");
                verifyPermision();
            }

            @Override
            public void onCancel() {
                //On Swipe To Cancel
                Log.d("RecordView", "onCancel");
                stopRecording();

            }

            @Override
            public void onFinish(long recordTime) {
                //Stop Recording..
               stopRecording();
            }

            @Override
            public void onLessThanSecond() {
                //When the record time is less than One Second
            }
        });

        recordView.setOnBasketAnimationEndListener(new OnBasketAnimationEnd() {
            @Override
            public void onAnimationEnd() {
                Log.d("RecordView", "Basket Animation Finished");
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
                if (checkConnection(Audio.this)){
                    upload();
                }else {
                    Toast.makeText(Audio.this, "Doesn't seem like you're connected", Toast.LENGTH_LONG).show();
                }
            }
        });
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

                            Glide.with(Audio.this).applyDefaultRequestOptions(requestOptions).
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

    private void iniWidgets() {
        progressbar = findViewById(R.id.progressBar6);
        recordView = findViewById(R.id.record_view);
        recordButton = findViewById(R.id.record_button);
        recordButton.setRecordView(recordView);
        post_text = findViewById(R.id.post_edittxet);
        username = findViewById(R.id.textView11);
        userImage = findViewById(R.id.user_image);
        post = findViewById(R.id.postTextView);
        backBtn = findViewById(R.id.back);
    }

    public boolean checkConnection(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


    private void dbStuff() {
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            current_user_id = firebaseAuth.getCurrentUser().getUid();
            firebaseFirestore = FirebaseFirestore.getInstance();
            storageReference = FirebaseStorage.getInstance().getReference();
        }
    }

    private void startRecording(){
        mediaRecorder = new MediaRecorder();

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(filename);

        mediaRecorder.setOnErrorListener(errorListener);
        mediaRecorder.setOnInfoListener(infoListener);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording(){
        if(null != mediaRecorder) {
            try {
                upload();
                mediaRecorder.prepare();
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            AppLog.logString("Error: " + what + ", " + extra);
        }
    };

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            AppLog.logString("Warning: " + what + ", " + extra);
        }
    };

    private void upload() {
        progressbar.setVisibility(View.VISIBLE);
        recordView.setEnabled(false);
        post.setEnabled(false);
        if (filename != null) {
            try {

                final StorageReference reference = storageReference.child("Audio").child(randomName);
                Uri uri = Uri.fromFile(new File(filename));
                UploadTask uploadTask = reference.putFile(uri);


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
        progressbar.setVisibility(View.VISIBLE);
        post.setEnabled(false);
        post_text.setEnabled(false);

        try {
            String input = post_text.getText().toString();

            Map<String, Object> postMap = new HashMap<>();
            postMap.put("image_url", "");
            postMap.put("image_thumb", "");
            postMap.put("audio", uri.toString());
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

            firebaseFirestore.collection("Posts").add(postMap).
                    addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {

                            String link = task.getResult().toString();

                            try {
                                if (task.isSuccessful()) {
                                    progressbar.setVisibility(View.GONE);
                                    Intent intent = new Intent(Audio.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                    Toast.makeText(Audio.this, "Done", Toast.LENGTH_SHORT).show();
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
                    showToast.toast("Something is not right", Audio.this);
                    progressbar.setVisibility(View.INVISIBLE);
                    post.setEnabled(true);
                    post_text.setEnabled(true);
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
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
                startRecording();

            }else {
                verifyPermision();
            }
    }
}
