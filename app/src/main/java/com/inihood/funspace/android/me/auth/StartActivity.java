package com.inihood.funspace.android.me.auth;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.inihood.funspace.android.me.MainActivity;
import com.inihood.funspace.android.me.R;
import com.inihood.funspace.android.me.call.activities.BaseActivity;
import com.inihood.funspace.android.me.call.activities.LoginActivity;
import com.inihood.funspace.android.me.call.services.CallService;
import com.inihood.funspace.android.me.call.utils.Consts;
import com.inihood.funspace.android.me.call.utils.QBEntityCallbackImpl;
import com.inihood.funspace.android.me.call.utils.UsersUtils;
import com.inihood.funspace.android.me.core.utils.SharedPrefsHelper;
import com.inihood.funspace.android.me.core.utils.Toaster;
import com.inihood.funspace.android.me.helper.ShowToast;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.helper.Utils;
import com.quickblox.users.model.QBUser;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class StartActivity extends BaseActivity {

    private TextInputLayout firstName;
    private TextInputLayout lastName;
    private TextInputLayout nickName;
    private Button Done;
    private String genderClicked = "";
    private CircleImageView setupImage;
    private Uri mainImageURI = null;
    private String user_id;
    private boolean isChanged = false;
    private EditText setupName;
    private Button setupBtn;
    private ProgressBar setupProgress;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private Bitmap compressedImageFile;
    private LinearLayout linear;
    private Phonenumber.PhoneNumber numberProto;
    private String token_id;
    private String TAG = LoginActivity.class.getSimpleName();
    private EditText userNameEditText;
    private EditText chatRoomNameEditText;
    private QBUser userForSave;
    private String first;
    private String last;
    private String nick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            user_id = firebaseAuth.getCurrentUser().getUid();
            firebaseFirestore = FirebaseFirestore.getInstance();
            storageReference = FirebaseStorage.getInstance().getReference();
        }

        initWidgets();
        setupProgress.setVisibility(View.GONE);

        Done.setOnClickListener(v -> register());

    }

    @Override
    protected View getSnackbarAnchorView() {
        return null;
    }

    private void register() {

        if (firstName.getEditText().getText().toString().equals("")){
            firstName.getEditText().setError("required");
        }else if (lastName.getEditText().getText().toString().equals("")){
            lastName.getEditText().setError("required");
        }else if (nickName.getEditText().getText().toString().equals("")){
            nickName.getEditText().setError("required");
        }else if (genderClicked.equals("")){
            Toast.makeText(this, "You must select a gender", Toast.LENGTH_SHORT).show();
        } else {
         String first = firstName.getEditText().getText().toString();
         String last =  lastName.getEditText().getText().toString();
         String nick =  nickName.getEditText().getText().toString();

         doTheMagic(first, last, nick);
        }

    }

    private void doTheMagic(String first, String last, String nick) {
        setupProgress.setVisibility(View.VISIBLE);
        linear.setVisibility(View.GONE);
        Done.setVisibility(View.GONE);

//        hideKeyboard();
//        startSignUpNewUser(createUserWithEnteredData());

        Map<String, String> userMap = new HashMap<>();
        userMap.put("first_name", first);
        userMap.put("phone", "1324536677");
       // userMap.put("token_id", token_id);
        userMap.put("firebase_id", user_id);
        userMap.put("last_name", last);
        userMap.put("nick_name", nick);
        userMap.put("profile_image", "image");
        userMap.put("cover_image", "cover");
        userMap.put("thumb_profile_image", "cover");
        if (genderClicked != null){
            userMap.put("gender", genderClicked);
        }else {
            userMap.put("gender", "unknown");
        }


        firebaseFirestore.collection("Users").document(user_id).set(userMap).
                addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    setupProgress.setVisibility(View.GONE);
                    linear.setVisibility(View.VISIBLE);
                    Done.setVisibility(View.VISIBLE);

                    Toast.makeText(StartActivity.this, "The user Settings are updated.", Toast.LENGTH_LONG).show();
                    Intent mainIntent = new Intent(StartActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(StartActivity.this, "(FIRESTORE Error) : " + error, Toast.LENGTH_LONG).show();
                    setupProgress.setVisibility(View.GONE);
                    linear.setVisibility(View.VISIBLE);
                    Done.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    public void addPhone(Phonenumber.PhoneNumber numberProto){

        Map<String, Object> phone = new HashMap<>();
        phone.put("phone", numberProto.toString());

        firebaseFirestore.collection("Contacts").document("phones").update(phone).
                addOnCompleteListener(task -> {

                    if(task.isSuccessful()){
                        Toast.makeText(StartActivity.this, "add user phone to db", Toast.LENGTH_LONG).show();

                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(StartActivity.this, "(FIRESTORE Error) : " + error, Toast.LENGTH_LONG).show();

                    }

                });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void initWidgets() {
        firstName = findViewById(R.id.first);
        lastName = findViewById(R.id.last);
        nickName = findViewById(R.id.nick);
        Done = findViewById(R.id.done);
        setupProgress = findViewById(R.id.progressBar2);
        linear = findViewById(R.id.linearLayout);

        Objects.requireNonNull(firstName.getEditText()).addTextChangedListener(new TextWatcher() {
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

        Objects.requireNonNull(lastName.getEditText()).addTextChangedListener(new TextWatcher() {
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

        Objects.requireNonNull(nickName.getEditText()).addTextChangedListener(new TextWatcher() {
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
    }

    public void onRadioButtonClicked(View view) {

      boolean checked = ((RadioButton) view).isChecked();

        genderClicked = "";

        switch (view.getId()) {
            case R.id.male:
                if (checked)

                    genderClicked = "male";

                break;
            case R.id.female:
                if (checked)

                    genderClicked = "female";

                break;
        }
    }


//    @TargetApi(Build.VERSION_CODES.KITKAT)
//    @Override
//    protected void onStart() {
//        super.onStart();
//        try {
//
//        token_id = FirebaseInstanceId.getInstance().getToken();
//    }catch (Exception e){
//            e.printStackTrace();
//        }
//
//        if (Objects.requireNonNull(firebaseAuth.getCurrentUser()).getPhoneNumber() != null){
//
//            String phone = firebaseAuth.getCurrentUser().getPhoneNumber();
//
//            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
//            try {
//                Phonenumber.PhoneNumber numberProt = phoneUtil.parse(phone, getCountryZipCode());
//                //Since you know the country you can format it as follows:
//                System.out.println(phoneUtil.format(numberProt, PhoneNumberUtil.PhoneNumberFormat.NATIONAL));
//
//                addPhone(numberProt);
//
//            } catch (NumberParseException e) {
//                System.err.println("NumberParseException was thrown: " + e.toString());
//            }
//        }else {
//            Toast.makeText(this, "Couldn't get your number", Toast.LENGTH_SHORT).show();
//            firebaseAuth.signOut();
//            Intent intent  = new Intent(StartActivity.this, RegisterActivity.class);
//            startActivity(intent);
//            finish();
//        }
//
//        }

    public String getCountryZipCode(){
        String CountryID="";
        String CountryZipCode="";

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        if (manager != null) {
            CountryID= manager.getSimCountryIso().toUpperCase();
        }else {
            Toast.makeText(this, "Couldn't get iso", Toast.LENGTH_SHORT).show();
            firebaseAuth.signOut();
            Intent intent  = new Intent(StartActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }
        String[] rl=this.getResources().getStringArray(R.array.CountryCodes);
        for (String aRl : rl) {
            String[] g = aRl.split(",");
            if (g[1].trim().equals(CountryID.trim())) {
                CountryZipCode = g[0];
                break;
            }
        }
        return CountryZipCode;
    }

    // QUICKBLOX STUFF

    private void hideKeyboard() {
//        KeyboardUtils.hideKeyboard(userNameEditText);
//        KeyboardUtils.hideKeyboard(chatRoomNameEditText);
    }

    private void startSignUpNewUser(final QBUser newUser) {
      //  showProgressDialog(R.string.dlg_creating_new_user);
        requestExecutor.signUpNewUser(newUser, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        loginToChat(result);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        if (e.getHttpStatusCode() == Consts.ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
                            signInCreatedUser(newUser, true);
                        } else {
                            ShowToast showToast = new ShowToast();
                            showToast.toast("Something went wrong", StartActivity.this);
                            setupProgress.setVisibility(View.GONE);
                            linear.setVisibility(View.VISIBLE);
                            Done.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );
    }

    private void loginToChat(final QBUser qbUser) {
        qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);

        userForSave = qbUser;
        startLoginService(qbUser);
    }

//    private void startOpponentsActivity() {
//        OpponentsActivity.start(LoginActivity.this, false);
//        finish();
//    }

    private void saveUserData(QBUser qbUser) {
        SharedPrefsHelper sharedPrefsHelper = SharedPrefsHelper.getInstance();
        sharedPrefsHelper.save(Consts.PREF_CURREN_ROOM_NAME, qbUser.getTags().get(0));
        sharedPrefsHelper.saveQbUser(qbUser);
    }

    private QBUser createUserWithEnteredData() {
        return createQBUserWithCurrentData(first + " " + last
        );
    }

    private QBUser createQBUserWithCurrentData(String userName) {
        QBUser qbUser = null;
        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty("hood")) {
            StringifyArrayList<String> userTags = new StringifyArrayList<>();
            userTags.add("hood");

            qbUser = new QBUser();
            qbUser.setFullName(userName);
            qbUser.setLogin(getCurrentDeviceId());
            qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
            qbUser.setTags(userTags);
        }

        return qbUser;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Consts.EXTRA_LOGIN_RESULT_CODE) {
           // hideProgressDialog();
            boolean isLoginSuccess = data.getBooleanExtra(Consts.EXTRA_LOGIN_RESULT, false);
            String errorMessage = data.getStringExtra(Consts.EXTRA_LOGIN_ERROR_MESSAGE);

            if (isLoginSuccess) {
                saveUserData(userForSave);

                signInCreatedUser(userForSave, false);
            } else {
                Toaster.longToast(getString(R.string.login_chat_login_error) + errorMessage);
//                userNameEditText.setText(userForSave.getFullName());
//                chatRoomNameEditText.setText(userForSave.getTags().get(0));
            }
        }
    }

    private void signInCreatedUser(final QBUser user, final boolean deleteCurrentUser) {
        requestExecutor.signInUser(user, new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser result, Bundle params) {
                if (deleteCurrentUser) {
                    removeAllUserData(result);
                } else {
                   // startOpponentsActivity();
                    ShowToast showToast = new ShowToast();
                    showToast.toast("All Done", StartActivity.this);
                }
            }

            @Override
            public void onError(QBResponseException responseException) {
               // hideProgressDialog();
               // Toaster.longToast(R.string.sign_up_error);
            }
        });
    }

    private void removeAllUserData(final QBUser user) {
        requestExecutor.deleteCurrentUser(user.getId(), new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                UsersUtils.removeUserData(getApplicationContext());
                startSignUpNewUser(createUserWithEnteredData());
            }

            @Override
            public void onError(QBResponseException e) {
               // hideProgressDialog();
               // Toaster.longToast(R.string.sign_up_error);
            }
        });
    }

    private void startLoginService(QBUser qbUser) {
        Intent tempIntent = new Intent(this, CallService.class);
        PendingIntent pendingIntent = createPendingResult(Consts.EXTRA_LOGIN_RESULT_CODE, tempIntent, 0);
        CallService.start(this, qbUser, pendingIntent);
    }

    private String getCurrentDeviceId() {
        return Utils.generateDeviceId(this);
    }



    // ends
}