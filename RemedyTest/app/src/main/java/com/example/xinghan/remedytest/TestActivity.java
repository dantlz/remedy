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

public class TestActivity extends AppCompatActivity {

    private String mBotId;
    Button sendButton;
    EditText messageEditText;
    SlackRealTimeMessagingClient mRtmClient;
    SlackWebApiClient mWebApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        messageEditText = (EditText) findViewById(R.id.messageEditTextID);
        sendButton = (Button) findViewById(R.id.sendButtonID);


        configureUI();

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                   openConnection();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();

    }

    void openConnection(){
        final String slackToken = "xoxp-303668528658-306016894690-305472216801-c7c8459c3abfb8d9d4b67f584db0af80";

        mWebApiClient = SlackClientFactory.createWebApiClient(slackToken);
        String webSocketUrl = mWebApiClient.startRealTimeMessagingApi().findPath("url").asText();
        mRtmClient = new SlackRealTimeMessagingClient(webSocketUrl);
        mRtmClient.addListener(Event.HELLO, new EventListener() {

            @Override
            public void onMessage(JsonNode message) {
                Authentication authentication = mWebApiClient.auth();
                mBotId = authentication.getUser_id();

                System.out.println("User id: " + mBotId);
                System.out.println("Team name: " + authentication.getTeam());
                System.out.println("User name: " + authentication.getUser());
            }
        });

        mRtmClient.addListener(Event.MESSAGE, new EventListener() {

            @Override
            public void onMessage(JsonNode message) {
                String channelId = message.findPath("channel").asText();
                String userId = message.findPath("user").asText();
                String text = message.findPath("text").asText();

                if (userId != null && !userId.equals(mBotId)) {
                    Channel channel;
                    try {
                        channel = mWebApiClient.getChannelInfo(channelId);
                    } catch (SlackResponseErrorException e) {
                        channel = null;
                    }
                    User user = mWebApiClient.getUserInfo(userId);
                    String userName = user.getName();

                    System.out.println("Channel id: " + channelId);
                    System.out.println("Channel name: " + (channel != null ? "#" + channel.getName() : "DM"));
                    System.out.println("User id: " + userId);
                    System.out.println("User name: " + userName);
                    System.out.println("Text: " + text);

                    // Copy cat
                    mWebApiClient.meMessage(channelId, userName + ": " + text);
                }
            }
        });

        mRtmClient.connect();
    }

    void configureUI(){
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = messageEditText.getText().toString();
                sendMessage("general", text, "Xinghan");
            }
        });
    }

    private void sendMessage(final String channel, final String text, final String username){
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    ChatPostMessageMethod message = new ChatPostMessageMethod(channel, text);
                    message.setUsername(username);
                    mWebApiClient.postMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

}
