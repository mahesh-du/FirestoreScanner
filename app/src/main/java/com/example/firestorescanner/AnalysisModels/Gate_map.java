package com.example.firestorescanner.AnalysisModels;

import java.io.Serializable;

public class Gate_map implements Serializable {

    private String Entry_Gate;
    private String Exit_Gate;

    public Gate_map() {
    }

    public Gate_map(String entry_gate, String exit_gate) {
        Entry_Gate = entry_gate;
        Exit_Gate = exit_gate;
    }

    public String getEntry_Gate() {
        return Entry_Gate;
    }

    public void setEntry_Gate(String entry_gate) {
        Entry_Gate = entry_gate;
    }

    public String getExit_Gate() {
        return Exit_Gate;
    }

    public void setExit_Gate(String exit_gate) {
        Exit_Gate = exit_gate;
    }
}
