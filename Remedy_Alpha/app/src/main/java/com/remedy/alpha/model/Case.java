package com.remedy.alpha.model;

import java.util.List;
import java.util.Map;

/**
 * Created by tianlinz on 2/20/18.
 */

public class Case {
    private String uniqueID;
    private String agentID;
    private String customerID;
    private String descriptionID; //Parse drop down selections into an ID
    private Map<String, String> notes;
    private List<RemedyMessage> chatHistory;
    private String currSentiment;
    private int currSentimentScore;

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getAgentID() {
        return agentID;
    }

    public void setAgentID(String agentID) {
        this.agentID = agentID;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getDescriptionID() {
        return descriptionID;
    }

    public void setDescriptionID(String descriptionID) {
        this.descriptionID = descriptionID;
    }

    public Map<String, String> getNotes() {
        return notes;
    }

    public void setNotes(Map<String, String> notes) {
        this.notes = notes;
    }

    public List<RemedyMessage> getChatHistory() {
        return chatHistory;
    }

    public void setChatHistory(List<RemedyMessage> chatHistory) {
        this.chatHistory = chatHistory;
    }

    public String getCurrSentiment() {
        return currSentiment;
    }

    public void setCurrSentiment(String currSentiment) {
        this.currSentiment = currSentiment;
    }

    public int getCurrSentimentScore() {
        return currSentimentScore;
    }

    public void setCurrSentimentScore(int currSentimentScore) {
        this.currSentimentScore = currSentimentScore;
    }
}
