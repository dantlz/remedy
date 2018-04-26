package com.remedy.alpha.UI;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.remedy.alpha.R;
import com.remedy.alpha.Support.Utils;
import com.wang.avi.AVLoadingIndicatorView;

import allbegray.slack.SlackClientFactory;
import allbegray.slack.rtm.Event;
import allbegray.slack.rtm.EventListener;
import allbegray.slack.rtm.SlackRealTimeMessagingClient;
import allbegray.slack.type.Authentication;
import allbegray.slack.webapi.method.SlackMethod;
import allbegray.slack.webapi.method.channels.ChannelJoinMethod;
import allbegray.slack.webapi.method.chats.ChatPostMessageMethod;

public class QueueActivity extends AppCompatActivity {

    private String name;
    private String phoneNumber;
    private String notes;
    private String type;

    private AVLoadingIndicatorView avi;
    private TextView statusTextViewOne; //"There are "
    private TextView statusTextViewCount; //"# people"
    private TextView statusTextViewTwo; //" in front of you"

    private int count = 4;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);

        name = getIntent().getStringExtra("NAME");
        phoneNumber = getIntent().getStringExtra("PHONE");
        notes = getIntent().getStringExtra("NOTES");
        type = getIntent().getStringExtra("TYPE");

        statusTextViewOne = findViewById(R.id.statusTextViewOne);
        statusTextViewCount = (TextView)findViewById(R.id.statusTextViewCount);
        statusTextViewCount.setText("4 people");
        statusTextViewTwo = findViewById(R.id.statusTextViewTwo);

        if(type.equals("CALL")){
            statusTextViewOne.setText("Your ");
            statusTextViewCount.setText("call");
            statusTextViewTwo.setText(" will soon be connected");
        } else {
            mHandler = new Handler();
            startStatusUpdate();
        }
        avi = findViewById(R.id.loading_indicator);
        avi.show();

        openConnection();

//        if (type.equals("CHAT"))
//            avi.show(); //avi.smoothToShow();
//            Handler handler = new Handler();
//            Runnable response =  new Runnable() {
//                public void run() {
//
//                }
//            };
//            handler.postDelayed(response, 3000);
//        if(type.equals("CALL")) {
//            avi.show(); //avi.smoothToShow();
//        }

    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if(count > 0){
                    count--;
                    statusTextViewCount.setText(count + " people");
                }
            } finally {
                mHandler.postDelayed(mStatusChecker, 3000);
            }
        }
    };

    void startStatusUpdate() {
        mStatusChecker.run();
    }

    void stopStatusUpdate() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    private void openConnection() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    //TODO Create local reading of token

                    Utils.mWebApiClient = SlackClientFactory.createWebApiClient(Utils.Danny2_slackToken);
                    String webSocketUrl = Utils.mWebApiClient.startRealTimeMessagingApi().findPath("url").asText();
                    Utils.mRtmClient = new SlackRealTimeMessagingClient(webSocketUrl);
                    Utils.mRtmClient.addListener(Event.HELLO, new EventListener() {
                        @Override
                        public void onMessage(JsonNode message) {
                            messageHandler_Initialization(message);
                        }
                    });
                    Utils.mRtmClient.addListener(Event.MESSAGE, new EventListener() {
                        @Override
                        public void onMessage(JsonNode message) {
                            messageHandler_Message(message);
                        }
                    });
                    Utils.mRtmClient.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    private void messageHandler_Initialization(JsonNode message) {

        System.out.println("@@@ Authenticated");

        Authentication authentication = Utils.mWebApiClient.auth();
        authentication.getUser();
        Utils.currID = authentication.getUser_id();
        Utils.currUsername = name;
        Utils.currChannelName = "customer_" + name + "_" + Utils.currID;
        Utils.currChannelName = Utils.currChannelName.toLowerCase();

        establishChannel();
    }

    private void establishChannel(){
        //TODO Talk to queue channel
        final SlackMethod joinMethod = new ChannelJoinMethod("queue"); //TODO
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    JsonNode retNode = Utils.execute(joinMethod);
                    Utils.currChannelID = retNode.findPath("channel").findPath("id").asText();

                    System.out.println("@@@ Channel established, ID: " + Utils.currChannelID);

                    if(type.equals("CALL")) {
                        //TODO Send a message to add myself to queue
                        final String text = "Call me with command: \n@phonebot call +1" + phoneNumber;
                        Utils.postMessage(Utils.currChannelName, Utils.currUsername, text);
                    }

                    //TODO Keep sending a message til we get a ready

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

        //Can only configure here since we need to get channelID before getting history
    }

    private void messageHandler_Message(JsonNode message) {
        System.out.println("###### " + message);

        if(message.get("text").asText().equals("ready")){
            System.out.println("###### " + message);

            if (type.equals("CHAT")){
                System.out.println("###### " + message);

                Intent intent;
                intent = new Intent(QueueActivity.this, ChatActivity.class);

                intent.putExtra("TYPE", type);
                intent.putExtra("NAME", name);
                intent.putExtra("PHONE", phoneNumber);
                intent.putExtra("NOTES", notes);
                startActivity(intent);
            }
            else{
                System.out.println("@@@@@@@@ " + message);

                //TODO Display something saying someone will call you ASAP
            }
        }
        //TODO else if check for queue reduction
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopStatusUpdate();
    }
}
