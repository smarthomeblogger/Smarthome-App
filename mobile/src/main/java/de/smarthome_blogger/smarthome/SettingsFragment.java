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
                    fehlermeldung("Anmeldung nicht möglich!\nBitte logge dich erneut ein.");
                }
                else if(result.equals("unknownuser")){
                    fehlermeldung("Dieser Nutzer existiert nicht!\nBitte logge dich erneut ein.");
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
                                fehlermeldung("Szenen-Verwaltung");
                            }
                        }));

                        settingItems.add(new SettingItem("Nutzer", "user", "", new View.OnClickListener(){
                            @Override
                            public void onClick(View v){
                                //Nutzer-Verwaltung öffnen
                                fehlermeldung("Nutzer-Verwaltung");
                            }
                        }));

                        settingItems.add(new SettingItem("Automation", "automation", "", new View.OnClickListener(){
                            @Override
                            public void onClick(View v){
                                //Automations-Verwaltung
                                fehlermeldung("Automations-Verwaltung");
                            }
                        }));

                        //Adapter setzen
                        settingsAdapter = new SettingsAdapter();
                        settingArray.setAdapter(settingsAdapter);
                        settingsAdapter.notifyDataSetChanged();
                    }
                    catch(Exception e){
                        fehlermeldung("Fehler beim Laden der Systeminformationen");
                    }
                }
            }

            @Override
            public void onError(String msg) {
                settingsView.findViewById(R.id.loading_animation).setVisibility(View.GONE);
                fehlermeldung(msg);
            }
        });

    }

    public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        int lastPosition = -1;

        @Override
        public int getItemCount(){
            return settingItems.size();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int i){
            if(holder instanceof SettingViewHolder){
                final SettingItem si = settingItems.get(i);
                SettingViewHolder settingViewHolder = (SettingViewHolder) holder;

                settingViewHolder.name.setText(si.getName());

                if(!si.getValue().equals("")){
                    settingViewHolder.value.setText(Html.fromHtml(si.getValue()));
                }
                else settingViewHolder.value.setText("");

                settingViewHolder.icon.setImageResource(Icons.getSystemInfoIcon(si.getType()));

                View.OnClickListener ocl = si.getOnClickListener();
                if(ocl != null){
                    settingViewHolder.container.setOnClickListener(ocl);
                }

                setAnimation(((SettingViewHolder) holder).container, i);
            }
        }

        public void setAnimation(View viewToAnimate, int position){
            if(position > lastPosition){
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.recycler_animation);
                viewToAnimate.startAnimation(animation);
                lastPosition = position;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.value_item, viewGroup, false);

            return new SettingViewHolder(itemView);
        }

        public class SettingViewHolder extends RecyclerView.ViewHolder{
            protected TextView value, name;
            protected ImageView icon;
            protected View container;

            public SettingViewHolder(View v){
                super(v);

                container = v.findViewById(R.id.container);
                value = (TextView) v.findViewById(R.id.value);
                name = (TextView) v.findViewById(R.id.name);
                icon = (ImageView) v.findViewById(R.id.icon);
            }
        }
    }

    /**
     * Zeigt übergebene Fehlermeldung an
     * @param msg
     */
    public void fehlermeldung(String msg){
        Snackbar.make(settingsView.findViewById(R.id.frame), msg, Snackbar.LENGTH_SHORT).show();
    }

    class SettingItem{
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

}
