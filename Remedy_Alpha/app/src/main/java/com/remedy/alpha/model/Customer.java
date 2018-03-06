package com.remedy.alpha.model;

import java.util.List;

/**
 * Created by tianlinz on 2/20/18.
 */

public class Customer extends User {
    private String companyAccount;
    private List<Case> cases;

    public String getCompanyAccount() {
        return companyAccount;
    }

    public void setCompanyAccount(String companyAccount) {
        this.companyAccount = companyAccount;
    }

    public List<Case> getCases() {
        return cases;
    }

    public void setCases(List<Case> cases) {
        this.cases = cases;
    }
}
