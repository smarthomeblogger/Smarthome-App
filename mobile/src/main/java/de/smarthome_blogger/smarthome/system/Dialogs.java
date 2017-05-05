package de.smarthome_blogger.smarthome.system;

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by Sascha on 18.04.2017.
 */

public class Dialogs {

    /**
     * Stellt die angegebene Nachricht in einer Snackbar dar
     * @param msg die Nachricht
     * @param v die View, in der die Nachricht angezeigt werden soll
     */
    public static void fehlermeldung(String msg, View v){
        Snackbar.make(v, msg, Snackbar.LENGTH_SHORT).show();
    }

}
