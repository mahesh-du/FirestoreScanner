package com.example.firestorescanner.AlertModels;

import com.google.firebase.firestore.PropertyName;

import java.util.Date;

public class Alert_CommonModel {

    private String Device_Name;
    private String Device_Actual_Position;
    private Date Created_At;
    private Boolean Alert_Generated;
    private String AlertType;

    public Alert_CommonModel() {
    }

    public Alert_CommonModel(String device_Name, String device_Actual_Position, Date created_At, Boolean alertGenerated, String alertType) {
        Device_Name = device_Name;
        Device_Actual_Position = device_Actual_Position;
        Created_At = created_At;
        Alert_Generated = alertGenerated;
        AlertType = alertType;
    }

    @PropertyName("Device Name")
    public String getDevice_Name() {
        return Device_Name;
    }

    @PropertyName("Device Name")
    public void setDevice_Name(String device_Name) {
        Device_Name = device_Name;
    }

    @PropertyName("Device Actual Position")
    public String getDevice_Actual_Position() {
        return Device_Actual_Position;
    }

    @PropertyName("Device Actual Position")
    public void setDevice_Actual_Position(String device_Actual_Position) {
        Device_Actual_Position = device_Actual_Position;
    }

    @PropertyName("Created At")
    public Date getCreated_At() {
        return Created_At;
    }

    @PropertyName("Created At")
    public void setCreated_At(Date created_At) {
        Created_At = created_At;
    }

    @PropertyName("Alert Generated")
    public Boolean getAlert_Generated() {
        return Alert_Generated;
    }

    @PropertyName("Alert Generated")
    public void setAlert_Generated(Boolean alert_Generated) {
        Alert_Generated = alert_Generated;
    }

    @PropertyName("Alert Type")
    public String getAlertType() {
        return AlertType;
    }

    @PropertyName("Alert Type")
    public void setAlertType(String alertType) {
        AlertType = alertType;
    }
}
