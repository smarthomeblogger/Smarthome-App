package de.smarthome_blogger.smarthome;

import android.content.Context;

/**
 * Created by Sascha on 19.09.2016.
 */
public class SaveData {

    public static String getUsername(Context context){
        return "Testnutzer";
    }

    public static String getPassword(Context context){
        return "password";
    }

    public static String getServerIp(Context context){
        return "192.168.178.109";
    }

    public static void setLoginData(Context context, String username, String password){}

    public static void setServerIp(Context context, String serverIp){}

    public static void setSaveLoginData(Context context, boolean saveLogin){}

    public static boolean getSaveLoginData(Context context){
        return true;
    }

}
