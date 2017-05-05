package de.smarthome_blogger.smarthome;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.smarthome_blogger.smarthome.adapters.OverviewAdapter;
import de.smarthome_blogger.smarthome.items.OverviewItem;
import de.smarthome_blogger.smarthome.items.RoomItem;
import de.smarthome_blogger.smarthome.system.Dialogs;
import de.smarthome_blogger.smarthome.system.HTTPRequest;
import de.smarthome_blogger.smarthome.system.SaveData;


/**
 * A simple {@link Fragment} subclass.
 */
public class OverviewFragment extends Fragment {

    private View overview;

    //RecyclerView
    private RecyclerView.Adapter overviewAdapter;
    private LinearLayoutManager llm;
    private ArrayList<OverviewItem> overviewItems;
    private RecyclerView overviewArray;

    public OverviewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        overview = inflater.inflate(R.layout.fragment_overview, container, false);

        overviewArray = (RecyclerView) overview.findViewById(R.id.overview_list);
        overviewArray.setHasFixedSize(true);
        llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        overviewArray.setLayoutManager(llm);

        loadRoomData();

        return overview;
    }

    /**
     * Lädt die Sensoren des Raumes
     */
    public void loadRoomData(){
        overview.findViewById(R.id.loading_animation).setVisibility(View.VISIBLE);

        final Map<String, String> requestData = new HashMap<>();
        requestData.put("action", "getsensordata");
        requestData.put("username", SaveData.getUsername(getContext()));
        requestData.put("password", SaveData.getPassword(getContext()));
        requestData.put("room", "all");

        HTTPRequest.sendRequest(getContext(), requestData, SaveData.getServerIp(getContext()), new HTTPRequest.HTTPRequestCallback() {
            @Override
            public void onRequestResult(String result) {
                overview.findViewById(R.id.loading_animation).setVisibility(View.GONE);

                if(result.equals("wrongdata")){
                    Dialogs.fehlermeldung("Anmeldung nicht möglich!\nBitte logge dich erneut ein.", overview.findViewById(R.id.frame));
                }
                else if(result.equals("unknownuser")){
                    Dialogs.fehlermeldung("Dieser Benutzer existiert nicht.\nBitte logge dich erneut ein.", overview.findViewById(R.id.frame));
                }
                else{
                    try{
                        JSONObject jsonObject = null;
                        try{
                            jsonObject = new JSONObject(result);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }

                        overviewItems = new ArrayList<>();

                        JSONArray overviewItemArray = jsonObject.getJSONArray("values");

                        for(int i = 0; i < overviewItemArray.length(); i++){
                            JSONObject o = overviewItemArray.getJSONObject(i);

                            ArrayList<RoomItem> valueList = new ArrayList<>();

                            JSONArray valueArray = o.getJSONArray("value_array");

                            for(int j = 0; j < valueArray.length(); j++){
                                JSONObject v = valueArray.getJSONObject(j);

                                valueList.add(new RoomItem(v.getString("shortform"), v.getString("id"),
                                        v.getString("icon"), v.getString("device_type"), "sensor",
                                        v.getString("wert"),
                                        o.getString("location")));
                            }

                            overviewItems.add(new OverviewItem(o.getString("name"), o.getString("location"), valueList));
                        }

                        //Adapter setzen
                        overviewAdapter = new OverviewAdapter(overviewItems, getActivity(), getContext(),
                                overview.findViewById(R.id.frame));
                        overviewArray.setAdapter(overviewAdapter);
                        overviewAdapter.notifyDataSetChanged();

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(String msg) {
                overview.findViewById(R.id.loading_animation).setVisibility(View.GONE);
                Dialogs.fehlermeldung(msg, overview.findViewById(R.id.frame));
            }
        });
    }

}
