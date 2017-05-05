package de.smarthome_blogger.smarthome.items;

import java.util.ArrayList;

/**
 * Created by Sascha on 18.04.2017.
 */

public class OverviewItem {

    private String name, location;
    private ArrayList<RoomItem> valueList;

    public OverviewItem(String name, String location, ArrayList<RoomItem> valueList){
        this.name = name;
        this.location = location;
        this.valueList = valueList;
    }

    /**
     * Gibt den Namen des Items zurück
     * @return
     */
    public String getName(){
        return name;
    }

    /**
     * Gibt die Location des Items zurück
     * @return
     */
    public String getLocation(){
        return location;
    }

    /**
     * Gibt die RoomItems zurück
     * @return
     */
    public ArrayList<RoomItem> getValueList(){
        return valueList;
    }
}
