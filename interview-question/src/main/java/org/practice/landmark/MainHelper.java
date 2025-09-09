package org.practice.landmark;

import java.util.HashMap;
import java.util.Map;

public class MainHelper {
    int currentVersion;
    Map<Integer, Map<String,String>> versionData; //version ,key,value

    public MainHelper() {
        currentVersion = 1;
        this.versionData = new HashMap<>();
        versionData.put(currentVersion, new HashMap<>());
    }

    public void put(String key, String value) {
        versionData.get(currentVersion).put(key, value);
    }

    public String get(Integer version, String key) {
        Map<String, String> stringStringMap = versionData.get(version);
        return stringStringMap.get(key);
    }

    public void versionUpgrade(){
        this.currentVersion++;
        versionData.put(this.currentVersion, new HashMap<>());
    }

    public int getCurrentVersion() {
        return currentVersion;
    }
}
