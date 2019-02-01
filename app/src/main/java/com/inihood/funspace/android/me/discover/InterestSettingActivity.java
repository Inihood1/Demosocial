package com.inihood.funspace.android.me.discover;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.fragment.CreatingInterestFragment;
import com.inihood.funspace.android.me.helper.ShowToast;
import com.inihood.funspace.android.me.interfaces.OnInterest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class InterestSettingActivity extends AppCompatActivity {

    private static final int GALLARY_PICK = 233;
    private String incoming;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private String current_user_id;
    private ImageView back;
    private ImageView save_changes;
    private String admin_user_id;
    private ImageView interest_cover_image;
    private TextView interest_title;
    private Switch ask_to_join;
    private Button see_all_members;
    private Button add_members;
    private Button change_interest_cover_image;
    private Bitmap compressedImageFile;
    private String downloadUri;
    private Uri uri;
    private String buttn_swich_state_is_public;
    private String title;
    private String image;
    private String ask;
    private FragmentManager fragmentManager;
    private CreatingInterestFragment creatingInterestFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_setting);

        initWidgets();
        dbStuff();
        checkState();
        fragmentManager = getFragmentManager();
        creatingInterestFragment = new CreatingInterestFragment();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        see_all_members.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InterestSettingActivity.this, SeeAlMembersActivity.class);
                intent.putExtra("interest_id", incoming);
                startActivity(intent);
            }
        });

        add_members.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InterestSettingActivity.this, AddMembersAcivity.class);
                intent.putExtra("interest_id", incoming);
                startActivity(intent);
            }
        });

        save_changes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkConnection(InterestSettingActivity.this)) {
                    saveAll();
                } else {
                    ShowToast showToast = new ShowToast();
                    showToast.toast("Doesn't seem like you are connected", InterestSettingActivity.this);
                }
            }
        });

        ask_to_join.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    ask = "true";
                }else {
                    ask = "false";
                }
            }
        });


        change_interest_cover_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLARY_PICK);

            }
        });

    }

    private void saveAll() {
        creatingInterestFragment.show(fragmentManager, "Working");
        creatingInterestFragment.setCancelable(false);

        final String randomName = UUID.randomUUID().toString();
        File newImageFile = new File(uri.getPath());
        try {

            compressedImageFile = new Compressor(InterestSettingActivity.this)
                    .setQuality(100)
                    .compressToBitmap(newImageFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        UploadTask filePath = storageReference.child(incoming).child("Interest_Cover_images").
                child(randomName + ".jpg").putBytes(imageData);
        filePath.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                downloadUri = task.getResult().toString();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(InterestSettingActivity.this, "error " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



        Map<String, Object> postMap = new HashMap<>();
        postMap.put("ask_to_join", ask);
        postMap.put("cover_image", downloadUri);

        try {
            firebaseFirestore.collection("Interest").document(incoming).update(postMap).
                    addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            creatingInterestFragment.getDialog().dismiss();
                            ShowToast showToast = new ShowToast();
                            showToast.toast("Updated", InterestSettingActivity.this);
                            finish();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    creatingInterestFragment.getDialog().dismiss();
                    ShowToast showToast = new ShowToast();
                    showToast.toast("Something is not right", InterestSettingActivity.this);
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void checkState() {
        try {
            firebaseFirestore.collection("Interest/").document("post/" + incoming).get().
                    addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (task.isSuccessful()) {
                                try {
                                    if (task.getResult().exists()) {

                                        admin_user_id = task.getResult().getString("admin_user_id");
                                        buttn_swich_state_is_public = task.getResult().getString("visible_to_public");
                                        image = task.getResult().getString("cover_image");
                                        title = task.getResult().getString("title");

                                        if (buttn_swich_state_is_public != null && image != null && title != null) {
                                            reSetAllWidgets(buttn_swich_state_is_public, image, title);
                                        }

                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } else {

                                String error = task.getException().getMessage();
                                Toast.makeText(InterestSettingActivity.this, "(Retrieve Error) : " + error, Toast.LENGTH_LONG).show();
                                finish();
                            }


                        }

                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
        if (!admin_user_id.equals(current_user_id)) {
            finish();
            ShowToast toast = new ShowToast();
            toast.toast("You don't have permission to access this page", InterestSettingActivity.this);
        }
    }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void reSetAllWidgets(String buttn_swich_state_is_public, String image, String title) {

        if (buttn_swich_state_is_public.equals("true")){
            ask_to_join.setChecked(true);
        }else {
            ask_to_join.setChecked(false);
        }
        interest_title.setText(title);

        RequestOptions placeholderOption = new RequestOptions();
        placeholderOption.placeholder(R.drawable.post_placeholder);
        Glide.with(InterestSettingActivity.this).applyDefaultRequestOptions(placeholderOption).load(image).into(interest_cover_image);
    }

    private void dbStuff() {
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            current_user_id = firebaseAuth.getCurrentUser().getUid();
            firebaseFirestore = FirebaseFirestore.getInstance();
            storageReference = FirebaseStorage.getInstance().getReference();
        }
    }

    private void initWidgets() {
        try {
        incoming = getIntent().getStringExtra("id");
        String title = getIntent().getStringExtra("title");
        if (incoming == null && title == null) {
            finish();
            ShowToast showToast = new ShowToast();
            showToast.toast("Something is not right", InterestSettingActivity.this);
        }
            interest_title.setText(title);
            see_all_members.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(InterestSettingActivity.this, SeeAlMembersActivity.class);
                    intent.putExtra("interest_id", incoming);
                    startActivity(intent);
                }
            });

            add_members.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(InterestSettingActivity.this, AddMembersAcivity.class);
                    intent.putExtra("interest_id", incoming);
                    startActivity(intent);
                }
            });

    }catch (Exception e){
            e.printStackTrace();
        }

        save_changes = findViewById(R.id.save_settings);
        back = findViewById(R.id.back);
        interest_cover_image = findViewById(R.id.imageView10);
        interest_title = findViewById(R.id.textView32);
        ask_to_join = findViewById(R.id.switch3);
        see_all_members = findViewById(R.id.all_member);
        add_members = findViewById(R.id.add_members);
        change_interest_cover_image = findViewById(R.id.button6);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() == null){
            finish();
            ShowToast showToast = new ShowToast();
            showToast.toast("Are you loggedIn?", InterestSettingActivity.this);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLARY_PICK){
            if (resultCode == RESULT_OK){
                try {
                    uri = data.getData();
                    interest_cover_image.setImageURI(uri);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    public boolean checkConnection(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
