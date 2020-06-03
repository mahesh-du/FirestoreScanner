package com.example.firestorescanner.EmailModels;

import java.util.List;

public class Email_List {

    private List<EmailModel> Emails;

    public Email_List() {
    }

    public Email_List(List<EmailModel> Emails) {
        this.Emails = Emails;
    }

    public List<EmailModel> getEmails() {
        return Emails;
    }

    public void setEmails(List<EmailModel> Emails) {
        this.Emails = Emails;
    }
}
