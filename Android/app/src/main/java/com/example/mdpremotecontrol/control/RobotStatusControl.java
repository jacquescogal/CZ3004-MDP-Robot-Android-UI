package com.example.mdpremotecontrol.control;

import java.util.ArrayList;

public class RobotStatusControl {
    private static RobotStatusControl instance = null;

    private ArrayList<String> robotStatusList=new ArrayList<String>();
    private ArrayList<String> robotTimeList = new ArrayList<>();

    public static RobotStatusControl getInstance() {

        if (instance == null) {
            instance = new RobotStatusControl();
        }
        return instance;
    }

    public ArrayList<String> getRobotStatusList(){
        return robotStatusList;
    }

    public ArrayList<String> getRobotTimeList(){return robotTimeList;}

    public void add(String string,String timeString){

        robotStatusList.add(string);
        robotTimeList.add(timeString);
    }
}
