package org.practice.landmark;

import java.util.HashMap;
import java.util.Map;

/*
 * Question ->  Input : version,key,value
 *                      1, Salman, Sohrab
 *              Output : value corresponding to particular version and key
 *                       1, Salman -> Sohrab(O/P)
 * */
public class Main {
    public static void main(String[] args)
    {
        MainHelper mainHelper = new MainHelper();

        mainHelper.put("Salman", "Sohrab");
        mainHelper.put("Rukhsana", "Ansari");

        mainHelper.versionUpgrade();  //2
        mainHelper.versionUpgrade(); //3

        mainHelper.put("Salman", "Sana");
        mainHelper.put("Sadaf", "Liza");

        String salman = mainHelper.get(1, "Salman");

        System.out.println("Salman: " + salman);

        String sadaf = mainHelper.get(2, "Sadaf");
        System.out.println("Sadaf: " + sadaf);
    }
}
