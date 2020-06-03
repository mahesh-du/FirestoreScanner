package com.example.firestorescanner.AnalysisModels;

import java.util.Map;

public class Temp {

    private Map<String, Today> Today_hashMap;

    public Temp() {
    }

    public Temp(Map<String, Today> today_hashMap) {
        Today_hashMap = today_hashMap;
    }

    public Map<String, Today> getToday_hashMap() {
        return Today_hashMap;
    }

    public void setToday_hashMap(Map<String, Today> today_hashMap) {
        Today_hashMap = today_hashMap;
    }
}
