package com.remedy.alpha.UI;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneOptions;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    private int analysisCounter;
    private boolean firstEntry = true;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        name = getIntent().getStringExtra("NAME");
        phoneNumber = getIntent().getStringExtra("PHONE");
        notes = getIntent().getStringExtra("NOTES");
        type = getIntent().getStringExtra("TYPE");

        openConnection();

//        if(type.equals("CALL"))
//            moveTaskToBack(true);

        configureUI();

        //IBM Sentiment Analysis
        ToneAnalyzer service = new ToneAnalyzer("2017-09-21");
        service.setUsernameAndPassword("3d35e35a-6ade-4659-961a-a0a39bb34340", "vin2ozD8qTtT");
    }

    private class SentimentAnalysis extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... textToAnalyze) {
            ToneAnalyzer service = new ToneAnalyzer("2017-09-21");
            service.setUsernameAndPassword("3d35e35a-6ade-4659-961a-a0a39bb34340", "vin2ozD8qTtT");

            ToneOptions toneOptions = new ToneOptions.Builder()
                    .text(textToAnalyze[0])
                    .build();
            ToneAnalysis tone = service.tone(toneOptions).execute();

            return tone.toString();
        }

        @Override
        protected void onPostExecute(String response) {
            Log.d("wxh", response);

            try {
                JSONObject jsonObject = new JSONObject(response);
                if(jsonObject.has("document_tone")){
                    JSONObject data = jsonObject.getJSONObject("document_tone");
                    if(data.has("tones")){
                        JSONArray jsonArray = data.getJSONArray("tones");
                        JSONObject firstElement = jsonArray.getJSONObject(0);
                        if(firstElement.has("score") && firstElement.has("tone_name")){
                            String type = firstElement.getString("tone_name");
                            Double score = firstElement.getDouble("score");
                            Log.d("wxh", type + " score: " + String.valueOf(score));

                            String result = "This customer has " + type + "score of: " + String.valueOf(score);
                            Log.d("wxh", result);

                            //post the message to the agent
                            Utils.createRemedyMessage(null, currID, true, Calendar.getInstance().getTime());
                            final ChatPostMessageMethod message = new ChatPostMessageMethod(currChannelName, result);
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
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void callProtocol(){
        final String text = "Call me with command: \n@phonebot call +1" + phoneNumber;
        final ChatPostMessageMethod message = new ChatPostMessageMethod(currChannelName, text);
        message.setUsername(currUsername);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println("@@@ " + text);
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
                View curr = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
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
        establishChannel();
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
                    if (type.equals("CHAT"))
                        getChannelHistory();
                    else
                        callProtocol();
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
        channelHistory = new LinkedList<>();

        Thread thread = new Thread() {
        String input = "";
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
                                message.setAgentID("Agent: " + user);
                            //TODO Retrieve agent name from agentID
                            String text = objNode.findPath("text").asText();
                            message.setMessage(text);
                            message.setSendDate(new Date( (long) (objNode.findPath("ts").asInt()) * 1000));
                            channelHistory.add(message);

                            if(firstEntry &&
                                    sentByCustomer &&
                                    !text.contains("Call me with command:") &&
                                    !text.contains("Current customer satisfaction index:")){
                                input = input + (text) + " ";
                            }
                        }

                        if(firstEntry) {
                            // Call the service and get the tone
                            new SentimentAnalysis().execute(new String[]{input});
                            firstEntry = false;
                        }
                    }
                    Collections.reverse(channelHistory);
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
                mMessageRecycler.scrollToPosition(channelHistory.size() - 1);

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
        if (type.equals("CHAT")){

        }
        else {
            getChannelHistory();
        }
        //TODO individual message population
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

