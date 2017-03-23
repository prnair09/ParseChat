package com.rakesh.parsechat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import static android.R.attr.data;

public class ChatActivity extends AppCompatActivity {

    static final String TAG = ChatActivity.class.getSimpleName();

    static  final String USER_ID_KEY = "userId";
    static final String BODY_KEY = "body";

    EditText etMessage;
    Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if(ParseUser.getCurrentUser() != null){
            startWithCurrentUser();
        }else{

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
        setUpMessagePosting();
    }

    void setUpMessagePosting(){
        etMessage = (EditText) findViewById(R.id.etMessage);
        btnSend = (Button)findViewById(R.id.btnSend);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = etMessage.getText().toString();

                ParseObject message = ParseObject.create("Message");

                message.put(USER_ID_KEY,ParseUser.getCurrentUser().getObjectId());

                message.put(BODY_KEY,data);

                message.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null){
                            Toast.makeText(ChatActivity.this, "Message created on Parse",Toast.LENGTH_SHORT).show();
                        }else{
                            Log.e(TAG, "Failed to save message", e);
                        }
                    }
                });
            }
        });



    }


}
