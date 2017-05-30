package de.smarthome_blogger.smarthome.items;

/**
 * Created by Sascha on 30.05.2017.
 */

public class GraphDayItem {
    private String date;
    private float minVal, maxVal;

    public GraphDayItem(String date, float minVal, float maxVal){
        this.date = date;
        this.minVal = minVal;
        this.maxVal = maxVal;
    }

    /**
     * Gibt das Datum zurück
     * @return
     */
    public String getDate(){
        return date;
    }

    /**
     * Gibt den minimalen Wert zurück
     * @return
     */
    public float getMinVal(){
        return minVal;
    }

    /**
     * Gibt den maximalen Wert zurück
     * @return
     */
    public float getMaxVal(){
        return maxVal;
    }
}
