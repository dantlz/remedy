package com.remedy.alpha.UI;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.remedy.alpha.Support.ChatAdapter;
import com.remedy.alpha.R;
import com.remedy.alpha.Support.Utils;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import allbegray.slack.SlackClientFactory;
import allbegray.slack.rtm.Event;
import allbegray.slack.rtm.EventListener;
import allbegray.slack.rtm.SlackRealTimeMessagingClient;
import allbegray.slack.type.Authentication;
import allbegray.slack.webapi.SlackWebApiClient;
import allbegray.slack.webapi.method.SlackMethod;
import allbegray.slack.webapi.method.channels.ChannelHistoryMethod;
import allbegray.slack.webapi.method.channels.ChannelJoinMethod;
import allbegray.slack.webapi.method.chats.ChatPostMessageMethod;
import com.remedy.alpha.model.RemedyMessage;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView mMessageRecycler;
    private ChatAdapter mMessageAdapter;
    private String currID;
    private String currUsername;
    private String currRealName;
    private Button sendButton;
    private EditText messageEditText;
    private SlackRealTimeMessagingClient mRtmClient;
    private SlackWebApiClient mWebApiClient;
    private String token;
    private String currChannelName;
    private String currChannelID;
    private List<RemedyMessage> channelHistory;

    private String name;
    private String phoneNumber;
    private String notes;
    private String type;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        name = getIntent().getStringExtra("NAME");
        phoneNumber = getIntent().getStringExtra("PHONE");
        notes = getIntent().getStringExtra("NOTES");
        type = getIntent().getStringExtra("TYPE");

        openConnection();

        if(type.equals("CALL"))
            moveTaskToBack(true);

        configureUI();
    }

    private void callProtocol(){
        final String text = "Call me with command: \n/yodel +1" + phoneNumber;
        final ChatPostMessageMethod message = new ChatPostMessageMethod(currChannelName, text);
        message.setUsername(currUsername);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println("### POSTING: " + text);
                    mWebApiClient.postMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

        Intent intent = new Intent(ChatActivity.this, QueueActivity.class);
        intent.putExtra("TYPE", "STOP");
        intent.putExtra("NAME", name);
        intent.putExtra("PHONE", phoneNumber);
        intent.putExtra("NOTES", notes);
        startActivity(intent);
    }

    private void configureUI() {
        messageEditText = findViewById(R.id.edittext_chatbox);
        sendButton = findViewById(R.id.button_chatbox_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                messageEditText.setText("");
            }
        });

        mMessageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        channelHistory = new LinkedList<RemedyMessage>();
        mMessageAdapter = new ChatAdapter(this, channelHistory);
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    private void openConnection() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    //TODO Create local reading of token

                    mWebApiClient = SlackClientFactory.createWebApiClient(Utils.Danny2_slackToken);
                    String webSocketUrl = mWebApiClient.startRealTimeMessagingApi().findPath("url").asText();
                    mRtmClient = new SlackRealTimeMessagingClient(webSocketUrl);
                    mRtmClient.addListener(Event.HELLO, new EventListener() {
                        @Override
                        public void onMessage(JsonNode message) {
                            messageHandler_Initialization(message);
                        }
                    });
                    mRtmClient.addListener(Event.MESSAGE, new EventListener() {
                        @Override
                        public void onMessage(JsonNode message) {
                            messageHandler_Message(message);
                        }
                    });
                    mRtmClient.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    private void messageHandler_Initialization(JsonNode message) {
        System.out.println("@@@ Initialized: " + message);

        Authentication authentication = mWebApiClient.auth();
        authentication.getUser();
        currID = authentication.getUser_id();
        currUsername = name;
        currChannelName = "customer_" + name + "_" + currID;
        currChannelName = currChannelName.toLowerCase();
//        currUsername = message.findPath("user").asText();
        if (type.equals("CHAT"))
            establishChannel();
        else
            callProtocol();
    }

    //Create a new channel if it doesn't exist
    private void establishChannel(){
        final SlackMethod joinMethod = new ChannelJoinMethod(currChannelName);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    JsonNode retNode = Utils.execute(joinMethod);
                    currChannelID = retNode.findPath("channel").findPath("id").asText();
                    System.out.println("@@@ Channel established, ID: " + currChannelID);
                    getChannelHistory();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

        //Can only configure here since we need to get channelID before getting history
    }

    private List<RemedyMessage> getChannelHistory(){
        final ChannelHistoryMethod history = new ChannelHistoryMethod(currChannelID);
        channelHistory = new LinkedList<RemedyMessage>();
        Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    JsonNode retNode = Utils.execute(history);
                    String start = retNode.toString();
                    final JsonNode arrNode = new ObjectMapper().readTree(start).get("messages");
                    if (arrNode.isArray()) {
                        for (final JsonNode objNode : arrNode) {

                            RemedyMessage message = new RemedyMessage();
                            String user = objNode.findPath("user").asText();
                            boolean sentByCustomer = (user.equals("")) ? true : false;
                            if((!user.equals("")) && currID.equals(user))
                                sentByCustomer = true;
                            message.setSentByCustomer(sentByCustomer);
                            if(sentByCustomer)
                                message.setCustomerID(user);
                            else
                                message.setAgentID(user);
                            message.setMessage(objNode.findPath("text").asText());
                            message.setSendDate(new Date( (long) (objNode.findPath("ts").asInt()) * 1000));
                            channelHistory.add(message);

                        }
                    }
                    Collections.reverse(channelHistory);
                    System.out.println("@@@: " + channelHistory);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

        while(thread.isAlive()){
            try {
                thread.join(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mMessageAdapter = new ChatAdapter(this, channelHistory);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("@@@: SETTING ADAPTER");
                mMessageRecycler.setAdapter(mMessageAdapter);
                mMessageAdapter.notifyDataSetChanged();
            }
        });
        return null;
    }

    private void sendMessage() {
        Utils.createRemedyMessage(null, currID, true, Calendar.getInstance().getTime());
        final ChatPostMessageMethod message = new ChatPostMessageMethod(currChannelName, messageEditText.getText().toString());
        message.setUsername(currUsername);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    mWebApiClient.postMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    private void messageHandler_Message(JsonNode message) {
        getChannelHistory();
//        mMessageAdapter.notifyDataSetChanged();
//
//        //From Sean
//        if(message.findPath("channel").asText().equals("random"))
//            return;
//        if(message.findPath("text").asText().contains("@phonebot") || message.findPath("bot_id").equals("B9C75Q6P6")) {
//            return;
//        }
//
//        //Grab all information from JSON
//        String channelId = message.findPath("channel").asText();
//        String userId = message.findPath("user").asText();
//        String text = message.findPath("text").asText();
//
//        //From self
//        if (userId == null) {
//            return;
//        }
//        if (userId.equals(currID)) {
//            return;
//        }
//        if (userId.equals("")) {
//            return;
//        }
//
//        //Grab all information from WebApiClient
//        User user = mWebApiClient.getUserInfo(userId);
//        Channel channel;
//        try {
//            channel = mWebApiClient.getChannelInfo(channelId);
//        } catch (SlackResponseErrorException e) {
//            channel = null;
//        }
//        String username = user.getName();
//        String realName = user.getProfile().getReal_name();
//
//
//        //Print all available info
//        System.out.println("@@@ channelId: " + channelId);
//        System.out.println("@@@ Channel Name: " + (channel != null ? "#" + channel.getName() : "DM"));
//        System.out.println("@@@ userId: " + userId);
//        System.out.println("@@@ text: " + text);
//        System.out.println("@@@ username: " + username);
//        System.out.println("@@@ realName: " + realName);
//        System.out.println("@@@ userInfo: " + user);
//
//        //Create remedyMessage
//        Utils.createRemedyMessage(userId, currID, false, Calendar.getInstance().getTime());
//
//        // Copy cat - bot duplicate messages on other people's message
//        mWebApiClient.meMessage(channelId, userName + ": " + text);
    }
}


//TODO Grab files and other multimedia

