package com.example.firestorescanner.AnalysisModels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Details implements Serializable {

    private List<Entry_Model> Entry;
    
    public Details(){
        Entry = new ArrayList<>(1000);
    }

    public Details(List<Entry_Model> entry) {
        Entry = new ArrayList<>(1000);
        Entry.addAll(entry);
    }

    public List<Entry_Model> getEntry() {
        return Entry;
    }

    public void setEntry(List<Entry_Model> entry) {
        Entry = entry;
    }
}
