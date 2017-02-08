package de.smarthome_blogger.smarthome;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
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
public class RoomFragment extends Fragment {

    String location, title;

    boolean setState = false;

    //RecyclerView
    RecyclerView.Adapter roomAdapter;
    GridLayoutManager glm;
    ArrayList<RoomItem> roomItems;
    RecyclerView roomArray;

    View roomView;

    public RoomFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        roomView = inflater.inflate(R.layout.fragment_room, container, false);

        Bundle bundle = getArguments();
        location = bundle.getString(MainActivity.EXTRA_LOCATION);
        title = bundle.getString(MainActivity.EXTRA_TITLE);

        roomArray = (RecyclerView) roomView.findViewById(R.id.room_list);
        roomArray.setHasFixedSize(true);
        glm = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.grid_columns));
        glm.setOrientation(GridLayoutManager.VERTICAL);
        roomArray.setLayoutManager(glm);

        roomItems = new ArrayList<>();

        loadRoomData();

        return roomView;
    }

    /**
     * Lädt die Raum-Daten vom Server und zeigt sie an
     */
    public void loadRoomData(){
        roomView.findViewById(R.id.loading_animation).setVisibility(View.VISIBLE);

        final Map<String, String> requestData = new HashMap<>();
        requestData.put("action", "getroomdata");
        requestData.put("username", SaveData.getUsername(getContext()));
        requestData.put("password", SaveData.getPassword(getContext()));
        requestData.put("room", location);

        HTTPRequest.sendRequest(getContext(), requestData, SaveData.getServerIp(getContext()), new HTTPRequest.HTTPRequestCallback() {
            @Override
            public void onRequestResult(String result) {
                roomView.findViewById(R.id.loading_animation).setVisibility(View.GONE);

                //Ergebnis in Log schreiben
                Log.i("GetRoomData-Result", result);

                if(result.equals("wrongdata")){
                    fehlermeldung("Anmeldung nicht möglich!\nBitte logge dich erneut ein.");
                }
                else if(result.equals("unknownuser")){
                    fehlermeldung("Dieser Benutzer existiert nicht.\nBitte logge dich erneut ein.");
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

                        JSONArray roomData = jsonObj.getJSONArray("roomdata");

                        for(int i = 0; i < roomData.length(); i++){
                            JSONObject c = roomData.getJSONObject(i);

                            roomItems.add(new RoomItem(c.getString("name"), c.getString("device"), c.getString("icon"),
                                    c.getString("type"), c.getString("value")));
                        }

                        //Adapter setzen
                        roomAdapter = new RoomAdapter();
                        roomArray.setAdapter(roomAdapter);
                        roomAdapter.notifyDataSetChanged();
                    }
                    catch(Exception e){
                        fehlermeldung("Fehler beim Laden der Raumdaten");
                    }

                    if(roomItems.isEmpty()){
                        roomArray.setVisibility(View.GONE);

                        roomView.findViewById(R.id.empty_item).setVisibility(View.VISIBLE);
                        ((ImageView) roomView.findViewById(R.id.empty_icon)).setImageResource(Icons.getDrawerIcon(location));
                        ((TextView) roomView.findViewById(R.id.empty_title)).setText("Raum leer");
                        ((TextView) roomView.findViewById(R.id.empty_info)).setText("Diesem Raum wurden noch keine Geräte zugewiesen.");
                    }
                }
            }

            @Override
            public void onError(String msg) {
                roomView.findViewById(R.id.loading_animation).setVisibility(View.GONE);
                fehlermeldung(msg);
            }
        });

    }

    /**
     * Schaltet das Gerät device auf den Zustand mode
     * @param device
     * @param mode
     */
    public void setModes(String device, boolean mode){

    }

    /**
     * Zeigt Graphen für den Sensor sensor an
     * @param sensor
     */
    public void showOverview(String sensor){}

    /**
     * Heizungs-Menü öffnen
     */
    public void openHeatingDialog(){}

    /**
     * Szenen-Menü öffnen
     */
    public void openSceneMenu(){}

    public class RoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        final int VIEW_TYPE_SWITCH = 0;
        final int VIEW_TYPE_SENSOR = 1;
        final int VIEW_TYPE_HEATING = 2;
        final int VIEW_TYPE_SCENE = 3;

        int lastPosition = -1;

        @Override
        public int getItemCount(){
            return roomItems.size();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int i){
            if(holder instanceof SwitchViewHolder){
                final RoomItem ri = roomItems.get(i);
                final SwitchViewHolder switchViewHolder = (SwitchViewHolder) holder;
                switchViewHolder.name.setText(ri.getName());
                switchViewHolder.icon.setImageResource(Icons.getDeviceIcon(ri.getDevice()));

                setState = true;
                switchViewHolder.switchView.setChecked(ri.getValue().equals("true"));
                setState = false;

                switchViewHolder.switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(!setState){
                            setState = true;
                            switchViewHolder.switchView.setChecked(!switchViewHolder.switchView.isChecked());
                            setState = false;
                        }
                    }
                });

                switchViewHolder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setState = true;
                        setModes(ri.getDevice(), !switchViewHolder.switchView.isChecked());
                        switchViewHolder.switchView.setChecked(!switchViewHolder.switchView.isChecked());
                        setState = false;
                    }
                });

                setAnimation(((SwitchViewHolder) holder).container, i);
            }
            else if(holder instanceof SensorViewHolder){
                final RoomItem ri = roomItems.get(i);
                final SensorViewHolder sensorViewHolder = (SensorViewHolder) holder;
                sensorViewHolder.icon.setImageResource(Icons.getValueIcon(ri.getDevice()));
                sensorViewHolder.value.setText(ri.getValue());
                sensorViewHolder.name.setText(ri.getName());

                sensorViewHolder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showOverview(ri.getDevice());
                    }
                });
            }
            else if(holder instanceof HeatingViewHolder){
                final RoomItem ri = roomItems.get(i);
                final HeatingViewHolder heatingViewHolder = (HeatingViewHolder) holder;
                heatingViewHolder.icon.setImageResource(Icons.getDeviceIcon(ri.getDevice()));
                heatingViewHolder.value.setText(ri.getValue());
                heatingViewHolder.name.setText(ri.getName());

                heatingViewHolder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openHeatingDialog();
                    }
                });
            }
            else if(holder instanceof SceneViewHolder){
                final RoomItem ri = roomItems.get(i);
                final SceneViewHolder sceneViewHolder = (SceneViewHolder) holder;
                sceneViewHolder.icon.setImageResource(Icons.getSystemInfoIcon(ri.getName()));
                sceneViewHolder.name.setText(ri.getName());

                sceneViewHolder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openSceneMenu();
                    }
                });
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
            View itemView;
            switch(viewType){
                case VIEW_TYPE_SWITCH:
                    itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.switch_item, viewGroup, false);
                    return new SwitchViewHolder(itemView);
                case VIEW_TYPE_HEATING:
                    itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.value_item, viewGroup, false);
                    return new HeatingViewHolder(itemView);
                case VIEW_TYPE_SCENE:
                    itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.value_item, viewGroup, false);
                    return new SceneViewHolder(itemView);
                case VIEW_TYPE_SENSOR:
                    itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.value_item, viewGroup, false);
                    return new SensorViewHolder(itemView);
            }
            return null;
        }

        @Override
        public int getItemViewType(int position){
            if(roomItems.get(position).type.equals("switch")){
                return VIEW_TYPE_SWITCH;
            }
            else if(roomItems.get(position).type.equals("sensor")){
                return VIEW_TYPE_SENSOR;
            }
            else if(roomItems.get(position).type.equals("scene")){
                return VIEW_TYPE_SCENE;
            }
            else if(roomItems.get(position).type.equals("heating")){
                return VIEW_TYPE_HEATING;
            }
            return VIEW_TYPE_SENSOR;
        }

        public class SwitchViewHolder extends RecyclerView.ViewHolder{
            protected TextView name;
            protected ImageView icon;
            protected Switch switchView;
            protected View container;

            public SwitchViewHolder(View v){
                super(v);
                container = v.findViewById(R.id.container);
                name = (TextView) v.findViewById(R.id.name);
                icon = (ImageView) v.findViewById(R.id.icon);
                switchView = (Switch) v.findViewById(R.id.switch_view);
            }
        }

        public class SensorViewHolder extends RecyclerView.ViewHolder{
            protected TextView value, name;
            protected ImageView icon;
            protected View container;

            public SensorViewHolder(View v){
                super(v);
                container = v.findViewById(R.id.container);
                value = (TextView) v.findViewById(R.id.value);
                name = (TextView) v.findViewById(R.id.name);
                icon = (ImageView) v.findViewById(R.id.icon);
            }
        }

        public class HeatingViewHolder extends RecyclerView.ViewHolder{
            protected TextView value, name;
            protected ImageView icon;
            protected View container;

            public HeatingViewHolder(View v){
                super(v);
                container = v.findViewById(R.id.container);
                value = (TextView) v.findViewById(R.id.value);
                name = (TextView) v.findViewById(R.id.name);
                icon = (ImageView) v.findViewById(R.id.icon);
            }
        }

        public class SceneViewHolder extends RecyclerView.ViewHolder{
            protected TextView name;
            protected ImageView icon;
            protected View container;

            public SceneViewHolder(View v){
                super(v);
                container = v.findViewById(R.id.container);
                name = (TextView) v.findViewById(R.id.name);
                v.findViewById(R.id.value).setVisibility(View.GONE);
                icon = (ImageView) v.findViewById(R.id.icon);
            }
        }
    }

    /**
     * Zeigt die übergebene Fehlermeldung an
     * @param msg
     */
    public void fehlermeldung(String msg){
        Snackbar.make(roomView.findViewById(R.id.frame), msg, Snackbar.LENGTH_SHORT).show();
    }

    public class RoomItem{

        private String name, device, icon, type, value;

        public RoomItem(String name, String device, String icon, String type, String value){
            this.name = name;
            this.device = device;
            this.icon = icon;
            this.type = type;
            this.value = value;
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
         * Gibt das Device des Items zurück
         * @return
         */
        public String getDevice(){
            return device;
        }
    }

}
