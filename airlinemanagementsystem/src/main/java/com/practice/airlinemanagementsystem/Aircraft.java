package com.practice.airlinemanagementsystem;

import java.util.LinkedHashMap;
import java.util.Map;

public class Aircraft {
    private final String model;
    private final Map<String,SeatClass> seatTemplate = new LinkedHashMap<>();

    public Aircraft(String model) {
        this.model = model;
    }

    public String getModel() { return model; }

    public void addSeatTemplate(String seatNumber, SeatClass seatClass) {
        seatTemplate.put(seatNumber, seatClass);
    }

    public Map<String, SeatClass> getSeatTemplate() {
        return seatTemplate;
    }
}
