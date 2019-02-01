package com.inihood.funspace.android.me.auth;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.inihood.funspace.android.me.MainActivity;
import com.inihood.funspace.android.me.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WorkingActivity extends AppCompatActivity {

    private ProgressBar dialog;
    private FirebaseAuth firebaseAuth;
    private Phonenumber.PhoneNumber numberProto;
    private String user_id;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar();
        setContentView(R.layout.activity_working);

        dialog = findViewById(R.id.progressBar3);
        firebaseAuth  = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            user_id = firebaseAuth.getCurrentUser().getUid();
            firebaseFirestore = FirebaseFirestore.getInstance();
            storageReference = FirebaseStorage.getInstance().getReference();
        }
        permission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        hideStatusBar();
        permission();
    }
    public void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
    public void permission(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if(ContextCompat.checkSelfPermission(WorkingActivity.this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED){

                Toast.makeText(WorkingActivity.this, "You can't continue without granting the permission", Toast.LENGTH_LONG).show();
                finish();
                ActivityCompat.requestPermissions(WorkingActivity.this, new
                        String[]{Manifest.permission.READ_CONTACTS}, 1);

            } else {

                formateContact();

            }

        } else {

            formateContact();

        }
    }

    private void formateContact() {

        Cursor contacts = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER},
                null,
                null,
                null
        );

        List<String> num = new ArrayList<>();

        if(contacts!=null) {
            while(contacts.moveToNext()){

                num.add(contacts.getString(contacts.getColumnIndex(ContactsContract.
                        CommonDataKinds.Phone.NUMBER)));

            }
            contacts.close();

            for (String nn : num){
                startOperation(nn);
            }
        }
    }

    public void startOperation(String number){

        if (Objects.requireNonNull(firebaseAuth.getCurrentUser()).getPhoneNumber() != null){

            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            try {
                numberProto = phoneUtil.parse(number, getCountryZipCode());
                phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
                Log.d("TAG", numberProto.toString());
                dialog.setVisibility(View.GONE);
                doTheMagic(numberProto);

                System.out.println(phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.NATIONAL));


            } catch (NumberParseException e) {
                System.err.println("NumberParseException was thrown: " + e.toString());
            }
        }else {
            Toast.makeText(this, "Couldn't get your number", Toast.LENGTH_SHORT).show();
            firebaseAuth.signOut();
            Intent intent  = new Intent(WorkingActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }

    }

    private void doTheMagic(Phonenumber.PhoneNumber numberProto) {

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("contact", numberProto.toString());


        firebaseFirestore.collection("Users").document(user_id).collection("Contact").
                document("phones").update(userMap).addOnCompleteListener(task -> {

                    if(task.isSuccessful()){
                        Intent mainIntent = new Intent(WorkingActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        finish();

                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(WorkingActivity.this, "(FIRESTORE Error) : " + error, Toast.LENGTH_LONG).show();
                    }

                });
    }

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
            Intent intent  = new Intent(WorkingActivity.this, RegisterActivity.class);
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

    @Override
    public void onBackPressed() {

    }
}
