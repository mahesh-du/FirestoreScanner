package com.example.firestorescanner.EmailModels;

import com.google.firebase.firestore.PropertyName;

public class EmailModel {

    private String Email;
    private String Institution_Path;
    private Boolean Logged_In;
    private String Role;
    private Boolean Blocked;
    private Boolean isEmailCreated;

    public EmailModel() {
    }

    public EmailModel(String email, String institution_Path, Boolean logged_In, String role, Boolean blocked, Boolean isEmailCreated) {
        Email = email;
        Institution_Path = institution_Path;
        Logged_In = logged_In;
        Role = role;
        Blocked = blocked;
        this.isEmailCreated = isEmailCreated;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }

    public String getInstitution_Path() {
        return Institution_Path;
    }

    public void setInstitution_Path(String Institution_Path) {
        this.Institution_Path = Institution_Path;
    }

    public Boolean getLogged_In() {
        return Logged_In;
    }

    public void setLogged_In(Boolean Logged_In) {
        this.Logged_In = Logged_In;
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String Role) {
        this.Role = Role;
    }

    public Boolean getBlocked() {
        return Blocked;
    }

    public void setBlocked(Boolean Blocked) {
        this.Blocked = Blocked;
    }

    @PropertyName("isEmailCreated")
    public Boolean get_isEmailCreated() {
        return isEmailCreated;
    }

    public void set_isEmailCreated(Boolean emailCreated) {
        isEmailCreated = emailCreated;
    }
}
