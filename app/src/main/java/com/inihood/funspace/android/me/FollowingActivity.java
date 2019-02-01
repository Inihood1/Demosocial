package com.inihood.funspace.android.me;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.inihood.funspace.android.me.adapter.ActivityContactRecyclerAdapter;
import com.inihood.funspace.android.me.call.activities.CallActivity;
import com.inihood.funspace.android.me.call.activities.OpponentsActivity;
import com.inihood.funspace.android.me.call.adapters.OpponentsAdapter;
import com.inihood.funspace.android.me.call.db.QbUsersDbManager;
import com.inihood.funspace.android.me.call.utils.CollectionsUtils;
import com.inihood.funspace.android.me.call.utils.Consts;
import com.inihood.funspace.android.me.call.utils.PermissionsChecker;
import com.inihood.funspace.android.me.call.utils.PushNotificationSender;
import com.inihood.funspace.android.me.call.utils.WebRtcSessionManager;
import com.inihood.funspace.android.me.core.utils.Toaster;
import com.inihood.funspace.android.me.interfaces.OnContactSelectedListner;
import com.inihood.funspace.android.me.model.Following;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FollowingActivity extends AppCompatActivity implements
        OnContactSelectedListner, View.OnLongClickListener {

    public boolean is_in_action_mode = false;
    private RecyclerView contact;
    private List<Following> contact_list;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private ActivityContactRecyclerAdapter followerAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;
    private Toolbar mToolbar;
    private String current_user_id;
    TextView counter_text_view;
    public ArrayList<Following> selection_list = new ArrayList<>();
    private int counter = 0;
    private static final String TAG = OpponentsActivity.class.getSimpleName();

    private static final long ON_ITEM_CLICK_DELAY = TimeUnit.SECONDS.toMillis(10);

    private OpponentsAdapter opponentsAdapter;
    private ListView opponentsListView;
    private QBUser currentUser;
    private ArrayList<QBUser> currentOpponentsList;
    private QbUsersDbManager dbManager;
    private boolean isRunForCall;
    private WebRtcSessionManager webRtcSessionManager;

    private PermissionsChecker checker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);


        initWigetAndToolbar();
        initRecyclerviewAdapter();
        iniDb();
        loadUser();

        if(firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            contact.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if(reachedBottom){

                       loadMoreUsers();

                    }
                }
            });

        }

    }

    private void iniDb() {
        firebaseAuth = FirebaseAuth.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    private void initRecyclerviewAdapter() {
        contact_list = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(FollowingActivity.this);
        followerAdapter = new ActivityContactRecyclerAdapter(contact_list,this, FollowingActivity.this);
        contact.setLayoutManager(layoutManager);
        contact.setAdapter(followerAdapter);
        contact.setNestedScrollingEnabled(false);
        contact.setHasFixedSize(true);
    }

    private void initWigetAndToolbar() {
        mToolbar = findViewById(R.id.include);
        setSupportActionBar(mToolbar);
        contact = findViewById(R.id.contact);
        counter_text_view = findViewById(R.id.counter_text);
        counter_text_view.setVisibility(View.GONE);
    }

    @Override
    public void onAudio(String contact_position_id) {
        Toast.makeText(this, "call this " + contact_position_id, Toast.LENGTH_SHORT).show();
        startCall(false);
    }

    @Override
    public void onVideo(String contact_position_id) {
        Toast.makeText(this, "call this " + contact_position_id, Toast.LENGTH_SHORT).show();
        startCall(true);
    }

    @Override
    public void onContactSelected(String contact_position_id) {
        Toast.makeText(FollowingActivity.this, "postion " + contact_position_id, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onImageSelected(String image, String thumb) {
        Toast.makeText(FollowingActivity.this, "image was selected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLongClicked(FollowingActivity followingActivity, CardView cardView) {
       actionModeCall();
    }

    public void actionModeCall(){
        mToolbar.getMenu().clear();
        mToolbar.inflateMenu(R.menu.menu_call_action_mode);
        counter_text_view.setVisibility(View.VISIBLE);
        is_in_action_mode = true;
        followerAdapter.notifyDataSetChanged();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onLongClick(View v) {

        return true;
    }


    public void loadUser() {

        try {

        Query firstQuery = firebaseFirestore.collection("Users").
                document(current_user_id).collection("following").
                orderBy("timestamp", Query.Direction.DESCENDING).limit(5);
        firstQuery.addSnapshotListener(FollowingActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    try {
                if (!documentSnapshots.isEmpty()) {

                    if (isFirstPageFirstLoad) {

                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                        contact_list.clear();

                    }
                    try {
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String blogPostId = doc.getDocument().getId();
                            Following blogPost = doc.getDocument().toObject(Following.class).withId(blogPostId);

                            if (isFirstPageFirstLoad) {

                                contact_list.add(blogPost);

                            } else {

                                contact_list.add(0, blogPost);

                            }

                            followerAdapter.notifyDataSetChanged();

                        }
                    }
                }catch (Exception e1){
                        e1.printStackTrace();
                    }

                    isFirstPageFirstLoad = false;

                }
            }catch (Exception e1){
                        e1.printStackTrace();
                    }

            }

        });
    }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }

    }


    public void loadMoreUsers(){


        if(firebaseAuth.getCurrentUser() != null) {
            try {
            Query nextQuery = firebaseFirestore.collection("Users").
                    document(current_user_id).collection("following")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(5);

            nextQuery.addSnapshotListener(FollowingActivity.this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    try {
                        if (!documentSnapshots.isEmpty()) {

                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String blogPostId = doc.getDocument().getId();
                                    Following blogPost = doc.getDocument().toObject(Following.class).withId(blogPostId);
                                    contact_list.add(blogPost);

                                    followerAdapter.notifyDataSetChanged();
                                }

                            }
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }

                }
            });
        }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_call, menu);
        return true;

    }
    public void prepareSelection(View view, int position){

        if (((CheckBox)view).isChecked()){
            selection_list.add(contact_list.get(position));
            counter = counter + 1;
            updateCounter(counter);
        }else {
            selection_list.remove(contact_list.get(position));
            counter = counter - 1;
            updateCounter(counter);
        }
    }
    @SuppressLint("SetTextI18n")
    public void updateCounter(int counter){
        if (counter == 0){
            counter_text_view.setText("");
        }else {
            counter_text_view.setText(Integer.toString(counter));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_call){
            ActivityContactRecyclerAdapter adapter = followerAdapter;
            if (adapter.getItemCount() == 0){
                Toast.makeText(this, "Please select atleast one person", Toast.LENGTH_SHORT).show();
            }else if (adapter.getItemCount() > 1){
                adapter.updateAdapter(selection_list);
                clearActionMode();
            }

        }else if (item.getItemId() == android.R.id.home){
            clearActionMode();
            followerAdapter.notifyDataSetChanged();

        }else if (item.getItemId() == R.id.action_confernce){
            actionModeCall();
        }
        return true;
    }
    public void clearActionMode(){
        is_in_action_mode = false;
        mToolbar.getMenu().clear();
        mToolbar.inflateMenu(R.menu.menu_call);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        counter_text_view.setVisibility(View.GONE);
        counter_text_view.setText("");
        counter = 0;
        selection_list.clear();
    }

    @Override
    public void onBackPressed() {
        if (is_in_action_mode){
            clearActionMode();
            followerAdapter.notifyDataSetChanged();
        }else {
            super.onBackPressed();
        }
    }
    private void startCall(boolean isVideoCall) {

        Log.d(TAG, "startCall()");
        ArrayList<Integer> opponentsList = CollectionsUtils.getIdsSelectedOpponents(opponentsAdapter.getSelectedItems());
        QBRTCTypes.QBConferenceType conferenceType = isVideoCall
                ? QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO
                : QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

        QBRTCClient qbrtcClient = QBRTCClient.getInstance(getApplicationContext());

        QBRTCSession newQbRtcSession = qbrtcClient.createNewSessionWithOpponents(opponentsList, conferenceType);

        WebRtcSessionManager.getInstance(this).setCurrentSession(newQbRtcSession);

        PushNotificationSender.sendPushMessage(opponentsList, currentUser.getFullName());

        CallActivity.start(this, false);
        Log.d(TAG, "conferenceType = " + conferenceType);
    }
}
