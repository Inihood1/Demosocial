package com.inihood.funspace.android.me.discover;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
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
import com.inihood.funspace.android.me.MainActivity;
import com.inihood.funspace.android.me.NewPostActivity;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.SinglePostActivity;
import com.inihood.funspace.android.me.helper.ShowToast;

import java.util.HashMap;
import java.util.Map;

public class CreateInterestActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private TextInputLayout textInputLayout;
    private Button Continue;
    private FirebaseAuth firebaseAuth;
    private String user_id;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private String firstname;
    private String lastname;
    private String image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_interest);

        initwidgets();
        initDb();


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

                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } else {

                                String error = task.getException().getMessage();
                                Toast.makeText(CreateInterestActivity.this, "(Retrieve Error) : " + error, Toast.LENGTH_LONG).show();

                            }
                        }

                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        textInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("Funspace")){

                    textInputLayout.setError("This name is not allowed");
                    Continue.setEnabled(false);

                }else if (s.toString().endsWith("Funspace")){
                    textInputLayout.setError("This name is not allowed");
                    Continue.setEnabled(false);

                }else if (s.toString().endsWith("funspace")){
                    textInputLayout.setError("This name is not allowed");
                    Continue.setEnabled(false);
                }else if (s.toString().contains("funspace")){
                    textInputLayout.setError("This name is not allowed");
                    Continue.setEnabled(false);
                }else {
                    Continue.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                textInputLayout.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
            }
        });
        Continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkConnection(CreateInterestActivity.this)){
                    finalizeTask();
                }else {
                    ShowToast showToast = new ShowToast();
                    showToast.toast("Doesn't seem like you are connected", CreateInterestActivity.this);                }

            }
        });
    }

    public boolean checkConnection(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void finalizeTask() {
        if (textInputLayout.getEditText() != null &&
                textInputLayout.getEditText().getText().toString().length() >= 3 &&
                !textInputLayout.getEditText().getText().toString().equals("")) {
            progressBar.setVisibility(View.VISIBLE);
            Continue.setEnabled(false);
            textInputLayout.getEditText().setEnabled(false);

            try {
            String title = textInputLayout.getEditText().getText().toString();
            String name = firstname + " " + lastname;

            Map<String, Object> postMap = new HashMap<>();
            postMap.put("admin_user_id", user_id);
            postMap.put("admin_name", name);
            postMap.put("admin_image", image);
            postMap.put("title", title);
            postMap.put("posts", "post");
            postMap.put("cover_image", "cover");
            postMap.put("timestamp", FieldValue.serverTimestamp());

            firebaseFirestore.collection("Interest").add(postMap).
                    addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {

                            if (task.isSuccessful()){

                            DocumentReference link = task.getResult();
                            String key = link.getId();

                            try {
                                Intent intent = new Intent(CreateInterestActivity.this, CreateInterestSecondStep.class);
                                intent.putExtra("interest_id", key);
                                startActivity(intent);
                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                if (task.isSuccessful()) {
                                    Toast.makeText(CreateInterestActivity.this, "Done", Toast.LENGTH_SHORT).show();
                                } else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Continue.setEnabled(true);
                                    textInputLayout.getEditText().setEnabled(true);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    ShowToast showToast = new ShowToast();
                    showToast.toast("Something is not right", CreateInterestActivity.this);
                    progressBar.setVisibility(View.INVISIBLE);
                    Continue.setEnabled(true);
                    textInputLayout.getEditText().setEnabled(true);
                }
            });
        }catch (Exception e){
                e.printStackTrace();
            }

        }else {
            ShowToast showToast = new ShowToast();
            showToast.toast("Maybe the character length is too short", CreateInterestActivity.this);
        }
    }

    private void initDb() {
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            user_id = firebaseAuth.getCurrentUser().getUid();
            firebaseFirestore = FirebaseFirestore.getInstance();
            storageReference = FirebaseStorage.getInstance().getReference();
        }
    }

    private void initwidgets() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        firebaseAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.progressBar5);
        textInputLayout = findViewById(R.id.textInputLayout);
        Continue = findViewById(R.id.button);
        progressBar.setVisibility(View.GONE);
        Continue.setEnabled(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() == null){
            finish();
        }
    }
}