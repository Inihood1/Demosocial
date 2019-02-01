package com.inihood.funspace.android.me.discover;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
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

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.fragment.CreatingInterestFragment;
import com.inihood.funspace.android.me.dialogsFragment.InterestSelectionFragment;
import com.inihood.funspace.android.me.helper.ShowDialoge;
import com.inihood.funspace.android.me.helper.ShowToast;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class CreateInterestSecondStep extends AppCompatActivity implements InterestSelectionFragment.Capture{

    private static final int GALLARY_PICK = 332;
    private ImageView back;
    private ImageView cover_image;
    private Switch ask_to_join;
    private Switch make_public;
    private TextView show_selection;
    private Button select;
    private Button done;
    private String incoming;
    private FragmentManager fragmentManager;
    private InterestSelectionFragment selectionFragment;
    private Uri uri  = null;
    private String ask;
    private String visible;
    private FirebaseAuth firebaseAuth;
    private String user_id;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private CreatingInterestFragment creatingInterestFragment;
    private Bitmap compressedImageFile;
    private Uri downloadUri;
    private String selected;
    private  File newImageFile;

    @Override
    public void onClickListner(String txt) {
        selected = txt;
        show_selection.setText(txt);
        show_selection.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_interest_second_step);

        fragmentManager = getFragmentManager();
        selectionFragment = new InterestSelectionFragment();
        creatingInterestFragment = new CreatingInterestFragment();

        initWidgets();
        initDb();
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectionFragment.show(fragmentManager, "Interest");
            }
        });

        cover_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 300)
                        .start(CreateInterestSecondStep.this);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkConnection(CreateInterestSecondStep.this)) {
                    finalizeTask();
                } else {
                    ShowToast showToast = new ShowToast();
                    showToast.toast("Doesn't seem like you are connected", CreateInterestSecondStep.this);
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

        make_public.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    ShowDialoge showDialoge = new ShowDialoge();
                    showDialoge.showDialog(CreateInterestSecondStep.this,
                            "Sure about That?",
                            "This interest will only be visible to you and your members and this cannot be change later");
                    visible = "false";
                }else {
                    visible = "true";
                }
            }
        });
    }

    private void finalizeTask() {

        try {
            if (uri != null && selected != null) {

                newImageFile = new File(uri.getPath());
                creatingInterestFragment.show(fragmentManager, "Just a sec....");
                creatingInterestFragment.setCancelable(false);
                final String randomName = UUID.randomUUID().toString();

                compressedImageFile = new Compressor(CreateInterestSecondStep.this)
                        .setQuality(100)
                        .compressToBitmap(newImageFile);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageData = baos.toByteArray();

                try {
                final StorageReference ref = storageReference.child("Interest_Cover_images").child(incoming).child(randomName + ".jpg");
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
                           Uri downloadUri1 = task.getResult();
                            puInToDb(downloadUri1);
                            creatingInterestFragment.getDialog().dismiss();
                        }
                    }
                });

            }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
                }


            }else {
                Toast.makeText(this, "image or a category must be selected", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void puInToDb(Uri downloadUri1) {
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("visible_to_public", "true");
        postMap.put("ask_to_join", "false");
        postMap.put("cover_image", downloadUri1.toString());
        postMap.put("category", selected);
        postMap.put("timestamp", FieldValue.serverTimestamp());
        try {
            firebaseFirestore.collection("Interest").document(incoming).update(postMap).
                   addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {

                           if (task.isSuccessful()){
                               ShowToast showToast = new ShowToast();
                               showToast.toast("Created", CreateInterestSecondStep.this);
                               Intent intent = new Intent(CreateInterestSecondStep.this, Discover.class);
                               startActivity(intent);
                               finish();
                           }else {
                               ShowToast showToast = new ShowToast();
                               showToast.toast("Something is not right", CreateInterestSecondStep.this);
                           }
                       }
                   }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    ShowToast showToast = new ShowToast();
                    showToast.toast("Something is not right", CreateInterestSecondStep.this);
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initWidgets() {
        incoming = getIntent().getStringExtra("interest_id");
        if (incoming == null){
            finish();
            ShowToast showToast = new ShowToast();
            showToast.toast("Something is not right", CreateInterestSecondStep.this);

        }
        back = findViewById(R.id.back);
        cover_image = findViewById(R.id.imageView9);
        ask_to_join = findViewById(R.id.switch1);
        make_public = findViewById(R.id.switch2);
        select = findViewById(R.id.textView30);
        show_selection = findViewById(R.id.textView31);
        done = findViewById(R.id.save);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                uri = result.getUri();
                cover_image.setImageURI(uri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
                ShowToast showToast = new ShowToast();
                showToast.toast(error.toString(), CreateInterestSecondStep.this);

            }
        }
    }
    public void showDialog(){
        final AlertDialog.Builder builder  = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("If you go back at this page your previous settings will be lost");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                // finish activity and delete key from db
                firebaseFirestore.collection("Interest").document(incoming).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        try {
                            dialog.dismiss();
                            Intent intent = new Intent(CreateInterestSecondStep.this, CreateInterestActivity.class);
                            startActivity(intent);
                            finish();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        ShowToast showToast = new ShowToast();
                        showToast.toast("Something went wrong while navigating back", CreateInterestSecondStep.this);
                    }
                });
            }
        });

        builder.setNegativeButton("Stay on page", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        showDialog();
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
    public boolean checkConnection(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }



}
