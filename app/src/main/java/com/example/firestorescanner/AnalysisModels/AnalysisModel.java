package com.example.firestorescanner.AnalysisModels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AnalysisModel implements Serializable {

    private List<Today> Data;

    public AnalysisModel() {
        Data = new ArrayList<>(1000);
    }

    public AnalysisModel(List<Today> data) {
        Data.addAll(data);
    }

    public List<Today> getData() {
        return Data;
    }

    public void setData(List<Today> data) {
        Data = data;
    }
}
