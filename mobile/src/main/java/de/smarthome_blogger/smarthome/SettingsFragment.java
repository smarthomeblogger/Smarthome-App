package de.smarthome_blogger.smarthome;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.smarthome_blogger.smarthome.adapters.SettingAdapter;
import de.smarthome_blogger.smarthome.items.SettingItem;
import de.smarthome_blogger.smarthome.system.HTTPRequest;
import de.smarthome_blogger.smarthome.system.Icons;
import de.smarthome_blogger.smarthome.system.SaveData;

import static de.smarthome_blogger.smarthome.system.Dialogs.fehlermeldung;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    //RecyclerView
    RecyclerView.Adapter settingsAdapter;
    GridLayoutManager glm;
    ArrayList<SettingItem> settingItems;
    RecyclerView settingArray;

    View settingsView;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        settingsView = inflater.inflate(R.layout.fragment_settings, container, false);

        settingArray = (RecyclerView) settingsView.findViewById(R.id.settings_list);
        settingArray.setHasFixedSize(true);
        glm = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.grid_columns));
        glm.setOrientation(GridLayoutManager.VERTICAL);
        settingArray.setLayoutManager(glm);

        settingItems = new ArrayList<>();

        loadSystemInfo();

        return settingsView;
    }

    /**
     * Lädt die Server-Infos vom Server herunter und zeigt sie an
     */
    public void loadSystemInfo(){
        settingsView.findViewById(R.id.loading_animation).setVisibility(View.VISIBLE);

        final Map<String, String> requestData = new HashMap<>();
        requestData.put("action", "getsysteminfo");
        requestData.put("username", SaveData.getUsername(getContext()));
        requestData.put("password", SaveData.getPassword(getContext()));

        HTTPRequest.sendRequest(getContext(), requestData, SaveData.getServerIp(getContext()), new HTTPRequest.HTTPRequestCallback() {
            @Override
            public void onRequestResult(String result) {
                settingsView.findViewById(R.id.loading_animation).setVisibility(View.GONE);

                //Ergebnis is Log schreiben
                Log.i("GetSystemInfo-Result", result);

                if(result.equals("wrongdata")){
                    fehlermeldung("Anmeldung nicht möglich!\nBitte logge dich erneut ein.", settingsView.findViewById(R.id.frame));
                }
                else if(result.equals("unknownuser")){
                    fehlermeldung("Dieser Nutzer existiert nicht!\nBitte logge dich erneut ein.", settingsView.findViewById(R.id.frame));
                }
                else{
                    try{
                        JSONObject jsonObj = null;
                        try{
                            jsonObj = new JSONObject(result);
                        }
                        catch(JSONException e){
                            e.printStackTrace();
                        }

                        JSONArray systemInfo = jsonObj.getJSONArray("systeminfo");

                        for(int i = 0; i < systemInfo.length(); i++){
                            JSONObject o = systemInfo.getJSONObject(i);

                            settingItems.add(new SettingItem(o.getString("name"), o.getString("type"), o.getString("value"), null));
                        }

                        //Statische Menüpunkte
                        settingItems.add(new SettingItem("Szenen", "scenes", "", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Szenen-Verwaltung öffnen
                                fehlermeldung("Szenen-Verwaltung", settingsView.findViewById(R.id.frame));
                            }
                        }));

                        settingItems.add(new SettingItem("Nutzer", "user", "", new View.OnClickListener(){
                            @Override
                            public void onClick(View v){
                                //Nutzer-Verwaltung öffnen
                                fehlermeldung("Nutzer-Verwaltung", settingsView.findViewById(R.id.frame));
                            }
                        }));

                        settingItems.add(new SettingItem("Automation", "automation", "", new View.OnClickListener(){
                            @Override
                            public void onClick(View v){
                                //Automations-Verwaltung
                                fehlermeldung("Automations-Verwaltung", settingsView.findViewById(R.id.frame));
                            }
                        }));

                        //Adapter setzen
                        settingsAdapter = new SettingAdapter(settingItems, getContext());
                        settingArray.setAdapter(settingsAdapter);
                        settingsAdapter.notifyDataSetChanged();
                    }
                    catch(Exception e){
                        fehlermeldung("Fehler beim Laden der Systeminformationen", settingsView.findViewById(R.id.frame));
                    }
                }
            }

            @Override
            public void onError(String msg) {
                settingsView.findViewById(R.id.loading_animation).setVisibility(View.GONE);
                fehlermeldung(msg, settingsView.findViewById(R.id.frame));
            }
        });

    }


}
