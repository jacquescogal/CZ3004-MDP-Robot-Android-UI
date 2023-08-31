package com.example.mdpremotecontrol.control;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ResponseControl {
    private static ResponseControl instance = null;

    private ArrayList<String> responseList=new ArrayList<String>();
    private ArrayList<String> timeList = new ArrayList<>();

    public static ResponseControl getInstance() {

        if (instance == null) {
            instance = new ResponseControl();
        }
        return instance;
    }

    public ArrayList<String> getResponseList(){
        return responseList;
    }

    public ArrayList<String> getTimeList() {return timeList;}

    public void add(String string,String timeString){

        responseList.add(string);
        timeList.add(timeString);
    }
}
