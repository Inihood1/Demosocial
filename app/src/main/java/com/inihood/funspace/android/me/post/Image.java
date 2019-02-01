package com.inihood.funspace.android.me.post;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.inihood.funspace.android.me.NewPostActivity;
import com.inihood.funspace.android.me.PuplicProfileActivity;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.adapter.ImageUploadListAdapter;
import com.inihood.funspace.android.me.helper.Storage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class Image extends AppCompatActivity {

    private static final int CAPTURE_IMAGE = 11;
    private CircleImageView userImage;
    private TextView userName;
    private EditText post_edit_text;
    private ImageView backBtn;
    private Button uplaodbtn;
    private RecyclerView recyclerView;
    private TextView post;
    private static final int LOAD_IMAGE = 1;
    private List<String> filnameList;
    private List<String> fileDonelist;
    private ImageUploadListAdapter imageUploadListAdapter;
    private StorageReference mStorage;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String current_user_id;
    private Dialog myDialog;
    private Uri mImageUri;
    private ImageView single_image;
    private  Uri fileUri = null;
    private String filname;
    private int h;
    private ProgressDialog progressDialog;
    private Bitmap compressedImageFile;
    private String last_name;
    private String phone;
    private String fullname;
    private String  thumb;
    private ConstraintLayout layout;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        iniWidgets();
        dbStuff();
        myDialog = new Dialog(this);
        mStorage = FirebaseStorage.getInstance().getReference();
        filnameList = new ArrayList<>();
        fileDonelist = new ArrayList<>();
        imageUploadListAdapter = new ImageUploadListAdapter(filnameList, fileDonelist);
        recyclerView.setAdapter(imageUploadListAdapter);
        layout = findViewById(R.id.layout);
        progressBar = findViewById(R.id.progressBar);


        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Image.this, "clicked", Toast.LENGTH_SHORT).show();
                upload();
            }
        });

        post_edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        try {
        if (current_user_id != null) {
            firebaseFirestore.collection("Users").document(current_user_id).get().
                    addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            try {
                            if (task.isSuccessful()) {

                                if (task.getResult().exists()) {

                                   String  first = task.getResult().getString("first_name");
                                   thumb = task.getResult().getString("thumb_profile_image");

                                    last_name = task.getResult().getString("last_name");
                                    phone = task.getResult().getString("phone");
                                    if (first != null && last_name != null){
                                        fullname = first + " " + last_name;
                                    }

                                    loadUserData(first, thumb);

                                }


                            }
                        }catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                    });

        } else {
            Toast.makeText(this, "Something is not right", Toast.LENGTH_SHORT).show();
        }
    }catch (Exception e){
            e.printStackTrace();
        }



        uplaodbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowPopup();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void ShowPopup() {

        TextView camera;
        TextView gallery;
        Button close;
        myDialog.setContentView(R.layout.custompopup);

        camera = myDialog.findViewById(R.id.camera);
        gallery = myDialog.findViewById(R.id.gallery);
        close = myDialog.findViewById(R.id.btnfollow);

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
                Toast.makeText(Image.this, R.string.gallery_tip, Toast.LENGTH_LONG).show();
                getFile();
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
                openCam();
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        //myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    private void openCam() {
        try {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAPTURE_IMAGE);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadUserData(String first, String thumb) {
        if (thumb != null) {
            try {
            RequestOptions placeholderRequest1 = new RequestOptions();
            placeholderRequest1.placeholder(R.drawable.profile_placeholder);
            Glide.with(Image.this).setDefaultRequestOptions(placeholderRequest1).
                    load(thumb).into(userImage);
        }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (first != null){
            userName.setText(first);
        }
    }

    private void dbStuff() {
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            current_user_id = firebaseAuth.getCurrentUser().getUid();
            firebaseFirestore = FirebaseFirestore.getInstance();
            storageReference = FirebaseStorage.getInstance().getReference();
        }
    }

    @SuppressLint("InlinedApi")
    private void getFile() {
        try {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, LOAD_IMAGE);
    }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void iniWidgets() {
        userImage = findViewById(R.id.user_image);
        userName = findViewById(R.id.user_name);
        post_edit_text = findViewById(R.id.post_edittxet);
        uplaodbtn = findViewById(R.id.upload_btn);
        recyclerView = findViewById(R.id.recyclerView2);
        backBtn = findViewById(R.id.back);
        post = findViewById(R.id.postTextView);
        single_image = findViewById(R.id.imageView6);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOAD_IMAGE){

            if (resultCode == RESULT_OK){

                if (data.getClipData() != null) {
                    try {
                    int totalItemSelected = data.getClipData().getItemCount();

                    for (int i = 0; i < totalItemSelected; i++) {
                        fileUri = data.getClipData().getItemAt(i).getUri();
                        filname = getFileName(fileUri);
                        filnameList.add(filname);
                        fileDonelist.add("Uploading...");
                        recyclerView.setVisibility(View.VISIBLE);
                        single_image.setVisibility(View.GONE);
                        imageUploadListAdapter.notifyDataSetChanged();
                        h = i;
                    }

                    // Toast.makeText(this, "selected multiple image", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                        e.printStackTrace();
                    }
                }else if (data.getData() != null) {
                    try {
                    Uri uri = data.getData();

                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.placeholder(R.drawable.image_placeholder);

                    Glide.with(this).applyDefaultRequestOptions(requestOptions).
                            load(uri).into(single_image);
                    fileDonelist.clear();
                    filnameList.clear();
                    imageUploadListAdapter.notifyDataSetChanged();
                    single_image.setVisibility(View.VISIBLE);
                }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }
        }

        if (requestCode == CAPTURE_IMAGE){
            if (resultCode == RESULT_OK) {
                try {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                Glide.with(this).
                        load(photo).into(single_image);
                    fileDonelist.clear();
                    filnameList.clear();
                    imageUploadListAdapter.notifyDataSetChanged();
                    single_image.setVisibility(View.VISIBLE);
                single_image.setVisibility(View.VISIBLE);
            }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public String getFileName(Uri uri){
        String result = null;
        if (uri.getScheme().equals("content")){
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try{
                if (cursor != null && cursor.moveToFirst()){
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }finally {
                cursor.close();
            }
        }
        if (result == null){
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1){
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void upload() {
        progressBar.setVisibility(View.VISIBLE);
        try {


            final String randomName = UUID.randomUUID().toString();
            Toast.makeText(this, "compress", Toast.LENGTH_SHORT).show();
            File newImageFile = new File(fileUri.getPath());
            try {

                compressedImageFile = new Compressor(Image.this)
                        .setQuality(100)
                        .compressToBitmap(newImageFile);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "compress", Toast.LENGTH_SHORT).show();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();

            Toast.makeText(this, "done", Toast.LENGTH_SHORT).show();

        UploadTask mFileToUpload = mStorage.child("post_images").child(current_user_id).
                child(randomName + ".jpg").putBytes(imageData);
        mFileToUpload.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                try {

                    String uri = taskSnapshot.getMetadata().toString();

                    List<String> list = new ArrayList<>();
                    list.add(uri);

                    Map<String, Object> postMap = new HashMap<>();
                    postMap.put("image_url", list);
                    postMap.put("image_thumb", list);
                    if (post_edit_text != null) {
                        postMap.put("desc", post_edit_text);
                    } else {
                        postMap.put("desc", "null");
                    }
                    postMap.put("user_id", current_user_id);
                    postMap.put("name", fullname);
                    postMap.put("user_image", thumb);
                    postMap.put("phone", phone);
                    postMap.put("timestamp", FieldValue.serverTimestamp());
                    try {
                    firebaseFirestore.collection("Posts").add(postMap).
                            addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    try {
                                    if (task.isSuccessful()) {

                                        Toast.makeText(Image.this, "Post was added", Toast.LENGTH_LONG).show();
                                        Intent mainIntent = new Intent(Image.this, MainActivity.class);
                                        startActivity(mainIntent);
                                        finish();

                                    } else {


                                    }
                                }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });
                }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                fileDonelist.remove(h);
                fileDonelist.add(h, "done");
                imageUploadListAdapter.notifyDataSetChanged();

            }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Image.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
        progressBar.setVisibility(View.VISIBLE);
    }catch (Exception e){
            e.printStackTrace();
        }
    }


    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, Storage.IMAGE_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + Storage.file_exts[Storage.currentFormat]);
    }

    }
