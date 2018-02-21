package model;

/**
 * Created by tianlinz on 2/20/18.
 */

public class Agent extends User {
    private String slackUsername;

    public String getSlackUsername() {
        return slackUsername;
    }

    public void setSlackUsername(String slackUsername) {
        this.slackUsername = slackUsername;
    }
}
