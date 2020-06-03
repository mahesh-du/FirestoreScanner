package com.example.firestorescanner.AlertModels;

import java.util.List;

public class Alert_Battery_ListModel {

    private List<Alert_BatteryModel> Alert;

    public Alert_Battery_ListModel() {
    }

    public Alert_Battery_ListModel(List<Alert_BatteryModel> alert) {
        Alert = alert;
    }

    public List<Alert_BatteryModel> getAlert() {
        return Alert;
    }

    public void setAlert(List<Alert_BatteryModel> alert) {
        Alert = alert;
    }
}
