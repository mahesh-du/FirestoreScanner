package com.example.firestorescanner.AlertModels;

import com.google.firebase.firestore.PropertyName;

import java.util.Date;

public class Alert_BatteryModel extends Alert_CommonModel {

    private Long Battery_Remaining;
    private String Battery_Status;
    private String Message;

    public Alert_BatteryModel() {

    }

    public Alert_BatteryModel(String device_Name, String device_Actual_Position, Date created_At, Boolean resolved, String alertType, Long battery_Remaining, String battery_Status, String message) {
        super(device_Name, device_Actual_Position, created_At, resolved, alertType);
        Battery_Remaining = battery_Remaining;
        Battery_Status = battery_Status;
        Message = message;
    }

    @PropertyName("Battery Remaining")
    public Long getBattery_Remaining() {
        return Battery_Remaining;
    }

    @PropertyName("Battery Remaining")
    public void setBattery_Remaining(Long battery_Remaining) {
        Battery_Remaining = battery_Remaining;
    }

    @PropertyName("Battery Status")
    public String getBattery_Status() {
        return Battery_Status;
    }

    @PropertyName("Battery Status")
    public void setBattery_Status(String battery_Status) {
        Battery_Status = battery_Status;
    }

//    @PropertyName("Message")
    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
}
