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

import de.smarthome_blogger.smarthome.adapters.RoomAdapter;
import de.smarthome_blogger.smarthome.items.RoomItem;
import de.smarthome_blogger.smarthome.system.HTTPRequest;
import de.smarthome_blogger.smarthome.system.Icons;
import de.smarthome_blogger.smarthome.system.SaveData;

import static de.smarthome_blogger.smarthome.system.Dialogs.fehlermeldung;


/**
 * A simple {@link Fragment} subclass.
 */
public class RoomFragment extends Fragment {

    String location, title;

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
                    fehlermeldung("Anmeldung nicht möglich!\nBitte logge dich erneut ein.", roomView.findViewById(R.id.frame));
                }
                else if(result.equals("unknownuser")){
                    fehlermeldung("Dieser Benutzer existiert nicht.\nBitte logge dich erneut ein.", roomView.findViewById(R.id.frame));
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
                                    c.getString("device_type"), c.getString("type"), c.getString("value"), location));
                        }

                        //Adapter setzen
                        roomAdapter = new RoomAdapter(roomItems, getActivity(), getContext(), roomView.findViewById(R.id.frame));
                        roomArray.setAdapter(roomAdapter);
                        roomAdapter.notifyDataSetChanged();
                    }
                    catch(Exception e){
                        fehlermeldung("Fehler beim Laden der Raumdaten", roomView.findViewById(R.id.frame));
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
                fehlermeldung(msg, roomView.findViewById(R.id.frame));
            }
        });

    }
}
