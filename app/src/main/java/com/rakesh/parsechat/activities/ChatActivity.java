package com.rakesh.parsechat.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.rakesh.parsechat.R;
import com.rakesh.parsechat.adapters.ChatListAdapter;
import com.rakesh.parsechat.models.Message;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatActivity extends AppCompatActivity {

    static final String TAG = ChatActivity.class.getSimpleName();

    static  final String USER_ID_KEY = "userId";
    static final String BODY_KEY = "body";
    private static final int MAX_CHAT_MESSAGES_TO_SHOW = 50;

    static final int REFRESH_INTERVAL = 500;

    @BindView(R.id.etMessage)
    EditText etMessage;

    @BindView(R.id.btnSend)
    Button btnSend;

    @BindView(R.id.lvChat)
    ListView lvChat;

    ArrayList<Message> mMessages;
    ChatListAdapter mAdapter;
    // Keep track of initial load to scroll to the bottom of the ListView
    boolean mFirstLoad;

    Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ButterKnife.bind(this);

        mMessages = new ArrayList<Message>();
        mHandler = new Handler();

        if(ParseUser.getCurrentUser() != null){
            startWithCurrentUser();
        }else{
            logIn();
        }
    }



    private void logIn(){
        ParseAnonymousUtils.logIn(new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if(e != null){
                    Log.e(TAG,"Anonymouse login failed", e);
                }else{
                    startWithCurrentUser();
                }
            }
        });
    }

    //Get the userID
    private  void startWithCurrentUser(){
        mHandler.post(autoRefresh);
        setUpMessagePosting();
    }

    void setUpMessagePosting(){
        if(mAdapter == null){
            final String userId = ParseUser.getCurrentUser().getObjectId();
            mAdapter = new ChatListAdapter(ChatActivity.this, userId, mMessages);
            lvChat.setAdapter(mAdapter);
        }

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = etMessage.getText().toString();

                lvChat.setTranscriptMode(1);

                mFirstLoad = true;

                Message message = new Message();

                message.setUserId(ParseUser.getCurrentUser().getObjectId());
                message.setBody(messageText);


                message.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null){


                            refreshMessages();
                            //Toast.makeText(ChatActivity.this, "Message created on Parse",Toast.LENGTH_SHORT).show();
                        }else{
                            Log.e(TAG, "Failed to save message", e);
                        }
                    }
                });
            }
        });
    }


    private void refreshMessages(){
        // Construct query to execute
        ParseQuery<Message> query = ParseQuery.getQuery(Message.class);
        // Configure limit and sort order
        query.setLimit(MAX_CHAT_MESSAGES_TO_SHOW);

        // get the latest 500 messages, order will show up newest to oldest of this group
        query.orderByDescending("createdAt");
        // Execute query to fetch all messages from Parse asynchronously
        // This is equivalent to a SELECT query with SQL
        query.findInBackground(new FindCallback<Message>() {
            public void done(List<Message> messages, ParseException e) {
                if (e == null) {
                    mMessages.clear();
                    mMessages.addAll(messages);
                    mAdapter.notifyDataSetChanged(); // update adapter
                    // Scroll to the bottom of the list on initial load
                    if (mFirstLoad) {
                        lvChat.setSelection(mAdapter.getCount() - 1);
                        mFirstLoad = false;
                    }
                } else {
                    Log.e("message", "Error Loading Messages" + e);
                }
            }
        });

    }

    private Runnable autoRefresh = new Runnable() {
        @Override
        public void run() {
            //Load data from Parse
            refreshMessages();
            mHandler.postDelayed(autoRefresh, REFRESH_INTERVAL);
        }
    };




}
