package com.example.firestorescanner.AnalysisModels;

import java.io.Serializable;
import java.util.Date;

public class Time_map implements Serializable {

    private Date Entry_Time;
    private Date Exit_Time;

    public Time_map() {
    }

    public Time_map(Date entry_time, Date exit_time) {
        Entry_Time = entry_time;
        Exit_Time = exit_time;
    }

    public Date getEntry_Time() {
        return Entry_Time;
    }

    public void setEntry_Time(Date entry_time) {
        Entry_Time = entry_time;
    }

    public Date getExit_Time() {
        return Exit_Time;
    }

    public void setExit_Time(Date exit_time) {
        Exit_Time = exit_time;
    }
}
