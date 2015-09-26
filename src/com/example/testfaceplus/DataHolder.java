package com.example.testfaceplus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataHolder {
    public Map<String, InfoPhoto> infos = new HashMap<String, InfoPhoto>();
    public List<Group> groupsFace = new ArrayList<Group>();
    public Map<String, Face> photos = new HashMap<String, Face>();
    
    private static final DataHolder holder = new DataHolder();
    public static DataHolder getInstance() {return holder;}
    
    // читерство, так делать нельзя
    //public ArrayList<String> catnames = new ArrayList<String>();
}
