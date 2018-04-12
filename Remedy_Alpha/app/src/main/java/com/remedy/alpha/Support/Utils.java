package com.remedy.alpha.Support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import allbegray.slack.RestUtils;
import allbegray.slack.exception.SlackException;
import allbegray.slack.exception.SlackResponseErrorException;
import allbegray.slack.webapi.SlackWebApiConstants;
import allbegray.slack.webapi.method.SlackMethod;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import com.remedy.alpha.model.RemedyMessage;


public class Utils {

    public static final String Danny2_slackToken = "xoxp-303668528658-304521448518-326243349906-5ad8bc320a842fda423a74bc89073a04";

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
}
