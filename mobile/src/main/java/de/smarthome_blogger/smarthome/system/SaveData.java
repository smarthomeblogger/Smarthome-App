package de.smarthome_blogger.smarthome.system;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Sascha on 19.09.2016.
 */
public class SaveData {

    private static String username = null;
    private static String password = null;
    private static String serverIp = null;

    private static String usernameFile = "uname.ini";
    private static String passwordFile = "psw.ini";
    private static String serverFile = "serverip.ini";

    /**
     * Gibt den Nutzernamen zurück
     * @param context Kontext der App
     * @return der Nutzername
     */
    public static String getUsername(Context context){
        String savedUsername = readFromFile(context, usernameFile);

        if(savedUsername != null){
            username = savedUsername;
        }

        return username;
    }

    /**
     * Gibt das Password zurück
     * @param context Kontext der App
     * @return das Passwort
     */
    public static String getPassword(Context context){
        String savedPassword = readFromFile(context, passwordFile);

        if(savedPassword != null){
            password = savedPassword;
        }

        return password;
    }

    /**
     * Gibt die IP zurück
     * @param context Kontext der App
     * @return die IP
     */
    public static String getServerIp(Context context){
        String savedIp = readFromFile(context, serverFile);

        if(savedIp != null){
            serverIp = savedIp;
        }

        return serverIp;
    }

    /**
     * Setzt die Login-Daten des Nutzers
     * @param context Kontext der App
     * @param uname Nutzername
     * @param pw Passwort
     * @param saveData true, wenn Daten gespeichert werden sollen
     *                 false, wenn nicht
     */
    public static void setLoginData(Context context, String uname, String pw, boolean saveData){
        if(saveData){
            writeToFile(context, usernameFile, uname);
            writeToFile(context, passwordFile, pw);
        }

        username = uname;
        password = pw;
    }

    public static void setServerIp(Context context, String serverIp){}

    /**
     * Gibt zurück, ob die Nutzerdaten gespeichert wurden
     * @param context Kontext der App
     * @return
     */
    public static boolean getSaveLoginData(Context context){
        boolean saveLoginData = (getUsername(context) != null && getPassword(context) != null);

        return saveLoginData;
    }

    /**
     * Schreibt den übergebenen Text in die angegebene Datei
     * @param context Kontext der App
     * @param filePath Pfad der Datei
     * @param text der zu schreibende Text
     */
    private static void writeToFile(Context context, String filePath, String text){
        try{
            FileOutputStream fos = context.openFileOutput(filePath, Context.MODE_PRIVATE);
            fos.write(text.getBytes());
            fos.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Gibt den Text der ausgegebenen Datei aus
     * @param context Kontext der App
     * @param filePath Pfad der zu lesenden Datei
     * @return String mit dem Inhalt der Datei oder null, falls Datei leer ist oder es einen Fehler gab
     */
    private static String readFromFile(Context context, String filePath){
        String text = "";

        try{
            FileInputStream fis = context.openFileInput(filePath);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null){
                sb.append(line);
            }
            text = ""+sb;

            if(text.equals("")){
                return null;
            }
        }catch(Exception e){
            e.printStackTrace();
            text = null;
        }

        return text;
    }

    /**
     * Löscht alle gespeicherten Nutzerdaten
     * @param context Kontext der App
     */
    public static void deleteAllUserData(Context context){
        writeToFile(context, usernameFile, "");
        writeToFile(context, passwordFile, "");
        writeToFile(context, serverFile, "");

        username = null;
        password = null;
        serverIp = null;
    }
}
