package com.example.firestorescanner.AnalysisModels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Today implements Serializable {

    private Date Date;
    private List<Entries_Model> Entries;

    public Today() {
        Entries = new ArrayList<>(1000);
    }

    public Today(Date date, List<Entries_Model> entries) {
        Date = date;
        Entries = new ArrayList<>(1000);
        Entries.addAll(entries);
    }

    public Date getDate() {
        return Date;
    }

    public void setDate(Date date) {
        Date = date;
    }

    public List<Entries_Model> getEntries() {
        return Entries;
    }

    public void setEntries(List<Entries_Model> entries) {
        Entries = entries;
    }
}
