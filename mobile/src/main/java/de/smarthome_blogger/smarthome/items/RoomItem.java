package de.smarthome_blogger.smarthome.items;

/**
 * Created by Sascha on 18.04.2017.
 */

public class RoomItem{

    private String name, id, icon, type, value, deviceType, room;

    public RoomItem(String name, String id, String icon, String deviceType, String type, String value, String room){
        this.name = name;
        this.id = id;
        this.icon = icon;
        this.type = type;
        this.value = value;
        this.deviceType = deviceType;
        this.room = room;
    }

    /**
     * Gibt den Namen des Items zurück
     * @return
     */
    public String getName(){
        return name;
    }

    /**
     * Gibt den Wert des Items zurück
     * @return
     */
    public String getValue(){
        return value;
    }

    /**
     * Gibt den Typ des Items zurück
     * @return
     */
    public String getType(){
        return type;
    }

    /**
     * Gibt den Icon des Items zurück
     * @return
     */
    public String getIcon(){
        return icon;
    }

    /**
     * Gibt die Id des Items zurück
     * @return
     */
    public String getId(){
        return id;
    }

    /**
     * Gibt den Gerätetypen des Items zurück
     * @return
     */
    public String getDeviceType(){
        return deviceType;
    }

    /**
     * Gibt den Raum des Gerätes zurück
     * @return
     */
    public String getRoom(){
        return room;
    }
}
