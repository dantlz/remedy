package com.remedy.alpha.Support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import allbegray.slack.RestUtils;
import allbegray.slack.exception.SlackException;
import allbegray.slack.exception.SlackResponseErrorException;
import allbegray.slack.rtm.SlackRealTimeMessagingClient;
import allbegray.slack.webapi.SlackWebApiClient;
import allbegray.slack.webapi.SlackWebApiConstants;
import allbegray.slack.webapi.method.SlackMethod;
import allbegray.slack.webapi.method.chats.ChatPostMessageMethod;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import com.remedy.alpha.model.RemedyMessage;


public class Utils {

    public static final String Danny2_slackToken = ""; //TODO PUT TOKEN HERE
    public static SlackRealTimeMessagingClient mRtmClient;
    public static SlackWebApiClient mWebApiClient;
    public static String currID;
    public static String currUsername;
    public static String currChannelName;
    public static String currChannelID;


    public static RemedyMessage createRemedyMessage(String agentID, String userID, boolean sentByCustomer, Date sentDate){
        RemedyMessage rMessage = new RemedyMessage();
        rMessage.setCustomerID(userID);
        rMessage.setAgentID(agentID);
        rMessage.setSendDate(sentDate);
        rMessage.setSentByCustomer(sentByCustomer);
        return rMessage;
    }

    public static JsonNode execute(SlackMethod method) {
        Map<String, String> parameters = method.getParameters();
        if (method.isRequiredToken()) {
            parameters.put("token", Danny2_slackToken);
        }

        String apiUrl = SlackWebApiConstants.SLACK_WEB_API_URL + "/" + method.getMethodName();
        HttpEntity httpEntity = RestUtils.createUrlEncodedFormEntity(parameters);
        CloseableHttpClient httpClient = RestUtils.createHttpClient(100000000);

        String retContent = RestUtils.execute(httpClient, apiUrl, httpEntity);

        JsonNode retNode;
        ObjectMapper mapper = new ObjectMapper();
        try {
            retNode = mapper.readTree(retContent);
        } catch (IOException e) {
            throw new SlackException(e);
        }

        boolean retOk = retNode.findPath("ok").asBoolean();
        if (!retOk) {
            String error = retNode.findPath("error").asText();
            throw new SlackResponseErrorException(error + ". check the link " + SlackWebApiConstants.SLACK_WEB_API_DOCUMENT_URL + "/" + method.getMethodName());
        }

        return retNode;
    }

    public static void postMessage(String channel, String username, String text){
        final ChatPostMessageMethod message = new ChatPostMessageMethod(Utils.currChannelName, text);
        message.setUsername(username);
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Utils.mWebApiClient.postMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }
}
