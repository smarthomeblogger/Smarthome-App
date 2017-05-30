package de.smarthome_blogger.smarthome.system;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import de.smarthome_blogger.smarthome.GraphActivity;
import de.smarthome_blogger.smarthome.MainActivity;
import de.smarthome_blogger.smarthome.items.RoomItem;

/**
 * Created by Sascha on 18.04.2017.
 */

public class RoomControl {

    /**
     * Öffnet die Heizungssteuerung
     */
    public static void openHeatingDialog(){}

    /**
     * Öffnet das Szenenmenü
     */
    public static void openSceneMenu(){}

    /**
     * Zeigt den Graphen für den übergebenen Sensor an
     * @param activity aufrufende Activity
     * @param item das Item
     * @param location der Ort des Items
     */
    public static void showOverview(Activity activity, RoomItem item, String location){
        //GraphActivity aufrufen
        Intent intent = new Intent(activity, GraphActivity.class);
        intent.putExtra(MainActivity.EXTRA_TITLE, item.getName());
        intent.putExtra(MainActivity.EXTRA_LOCATION, location);
        intent.putExtra(MainActivity.EXTRA_DEVICETYPE, item.getDeviceType());
        intent.putExtra(MainActivity.EXTRA_DEVICE, item.getId());
        activity.startActivity(intent);
    }

    /**
     * Schaltet das angegebene Gerät auf den Zustand mode
     * @param v die View
     * @param activity die aufrufende Activity
     * @param item das Item
     * @param indexOfItem der Index des Items
     * @param mode de Zustand, auf den das item geschaltet werden soll
     * @param notifyListener der NotifyListener
     */
    public static void setModes(final View v, Activity activity, final RoomItem item, final int indexOfItem,
    final boolean mode, final OnAdapterNotifyListener notifyListener){}

    public interface OnAdapterNotifyListener{
        void onNotify(int i);
    }

}
