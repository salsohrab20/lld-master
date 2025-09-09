package com.practice.airlinemanagementsystem;

public class AirCraftFactory {
    public static Aircraft createA320(){
        Aircraft aircraft = new Aircraft("Airbus A320");
        //template - 6 rows, seat A-F, row 1-2 -> business, other economy
        for(int row=1;row<=6;row++){
            for(char s='A'; s<='F';s++){
                String seat = row + String.valueOf(s);
                if(row<=2) aircraft.addSeatTemplate(seat, SeatClass.BUSINESS);
                else aircraft.addSeatTemplate(seat, SeatClass.ECONOMY);
            }
        }

        return aircraft;
    }
}
