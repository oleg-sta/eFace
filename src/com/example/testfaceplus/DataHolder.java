package com.example.testfaceplus;

import java.util.HashMap;
import java.util.Map;

public class DataHolder {
    public Map<String, InfoPhoto> infos = new HashMap<String, InfoPhoto>();
    
    private static final DataHolder holder = new DataHolder();
    public static DataHolder getInstance() {return holder;}
}
