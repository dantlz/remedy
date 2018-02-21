package model;

import java.util.Date;

/**
 * Created by tianlinz on 2/20/18.
 */

public class RemedyMessage {
    private Date sendDate;
    private String agentID;
    private String customerID;
    private boolean sentByCustomer;

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
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

    public boolean isSentByCustomer() {
        return sentByCustomer;
    }

    public void setSentByCustomer(boolean sentByCustomer) {
        this.sentByCustomer = sentByCustomer;
    }
}
