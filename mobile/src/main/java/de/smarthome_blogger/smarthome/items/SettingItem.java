package de.smarthome_blogger.smarthome.items;

import android.view.View;

/**
 * Created by Sascha on 18.04.2017.
 */

public class SettingItem{
    private String name, type;
    private String value;
    private View.OnClickListener ocl;

    public SettingItem(String name, String type, String value, View.OnClickListener ocl){
        this.name = name;
        this.type = type;
        this.value = value;
        this.ocl = ocl;
    }

    /**
     * Gibt den Namen zurück
     * @return
     */
    public String getName(){
        return name;
    }

    /**
     * Gibt den Typ zurück
     * @return
     */
    public String getType(){
        return type;
    }

    /**
     * Gibt den Wert zurück
     * @return
     */
    public String getValue(){
        return value;
    }

    /**
     * Gibt den OnClickListener zurück
     * @return
     */
    public View.OnClickListener getOnClickListener(){
        return ocl;
    }
}
