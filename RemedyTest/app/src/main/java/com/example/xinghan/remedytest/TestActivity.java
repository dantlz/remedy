package com.example.xinghan.remedytest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.fasterxml.jackson.databind.JsonNode;

import allbegray.slack.SlackClientFactory;
import allbegray.slack.exception.SlackResponseErrorException;
import allbegray.slack.rtm.Event;
import allbegray.slack.rtm.EventListener;
import allbegray.slack.rtm.SlackRealTimeMessagingClient;
import allbegray.slack.type.Authentication;
import allbegray.slack.type.Channel;
import allbegray.slack.type.User;
import allbegray.slack.webapi.SlackWebApiClient;
import allbegray.slack.webapi.method.chats.ChatPostMessageMethod;
import model.RemedyMessage;

public class TestActivity extends AppCompatActivity {

    private String currID;
    private Button sendButton;
    private EditText messageEditText;
    private SlackRealTimeMessagingClient mRtmClient;
    private SlackWebApiClient mWebApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        configureUI();

        openConnection();
    }

    void configureUI() {
        messageEditText = findViewById(R.id.messageEditTextID);
        sendButton = findViewById(R.id.sendButtonID);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = messageEditText.getText().toString();
                sendMessage(createMessage("general", text, "Bot name = place holder for client"));
                //SOLUTION: even though it's logged into Danny2, username will be the customer's
            }
        });
    }

    private ChatPostMessageMethod createMessage(final String channel, final String text, final String realName) {
        ChatPostMessageMethod message = new ChatPostMessageMethod("general", text);
        message.setUsername(realName);
        return message;
    }

    private void sendMessage(final ChatPostMessageMethod message) {
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

    void openConnection() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    //TODO Create local reading of token
                    //Token logs into one specific user, have users all log into Danny2
//                    final String Danny_slackToken = "xoxp-303668528658-306016894690-318824757078-32c4eb2f9c0473f6bae7856d1e860519";
                    final String Danny2_slackToken = "xoxp-303668528658-317362446608-318876949830-390456fc01dbc7da3add39a75466db15";

                    mWebApiClient = SlackClientFactory.createWebApiClient(Danny2_slackToken);
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

    void messageHandler_Initialization(JsonNode message) {
        System.out.println("### Event.HELLO");
        System.out.println("### JSON: " + message);


        Authentication authentication = mWebApiClient.auth();
        authentication.getUser();
        currID = authentication.getUser_id();
    }


    void messageHandler_Message(JsonNode message) {
        //For any message, even the currID's
        System.out.println("### Event.MESSAGE");
        System.out.println("### JSON: " + message);


        //Grab all information from JSON
        String channelId = message.findPath("channel").asText();
        String userId = message.findPath("user").asText();
        String text = message.findPath("text").asText();


        //Make sure the received message isn't from the same user as currID
        //Breaks on user_not_found since in sendMessage I passed random username not userID
        //No matter what the userID would be empty since the message sent from curr customer is sent as a bot
        //and the username/userID/real name are not stored as part of that message
        //We also don't have username or real name here even though they're useless anyways
        //we will simply ignore those messages and route it from here within the program

        if (userId == null) {
            return;
        }
        if (userId.equals(currID)) {
            return;
        }
        if (userId.equals("")) {
            System.out.println("### Message is from curr customer");
            return;
        }
        System.out.println("@@@ Message has userID");


        //Grab all information from WebApiClient
        User user = mWebApiClient.getUserInfo(userId);
        Channel channel;
        try {
            channel = mWebApiClient.getChannelInfo(channelId);
        } catch (SlackResponseErrorException e) {
            channel = null;
        }
        String username = user.getName();
        String realName = user.getProfile().getReal_name();


        //Print all available info
        System.out.println("@@@ channelId: " + channelId);
        System.out.println("@@@ Channel Name: " + (channel != null ? "#" + channel.getName() : "DM"));
        System.out.println("@@@ userId: " + userId);
        System.out.println("@@@ text: " + text);
        System.out.println("@@@ username: " + username);
        System.out.println("@@@ realName: " + realName);
        System.out.println("@@@ userInfo: " + user);

        // Copy cat - bot duplicate messages on other people's message
//                    mWebApiClient.meMessage(channelId, userName + ": " + text);
    }

    RemedyMessage createRemedyMessage(){

        return null;
    }

    //TODO Grab files and other multimedia
}
