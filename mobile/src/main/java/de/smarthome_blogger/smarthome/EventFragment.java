package de.smarthome_blogger.smarthome;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.smarthome_blogger.smarthome.system.Dialogs;
import de.smarthome_blogger.smarthome.system.HTTPRequest;
import de.smarthome_blogger.smarthome.system.Icons;
import de.smarthome_blogger.smarthome.system.SaveData;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventFragment extends Fragment {

    private View eventView;

    //RecyclerView
    private RecyclerView.Adapter eventAdapter;
    private GridLayoutManager glm;
    private ArrayList<EventItem> eventItems;
    private RecyclerView eventArray;
    private View loadingAnimation;

    private int visibleItemCount, totalItemCount, pastVisibleItems;
    private boolean loading = false;
    private int offset = 0;
    private boolean endOfItems = false;
    String type = "";

    public EventFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        eventView = inflater.inflate(R.layout.fragment_event, container, false);

        loadingAnimation = eventView.findViewById(R.id.loading_animation);

        //Argumente abfragen
        Bundle bundle = getArguments();

        if(bundle != null && bundle.containsKey(MainActivity.EXTRA_TYPE)){
            type = bundle.getString(MainActivity.EXTRA_TYPE);
        }

        eventArray = (RecyclerView) eventView.findViewById(R.id.event_list);
        eventArray.setHasFixedSize(true);
        glm = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.event_column_count));
        glm.setOrientation(GridLayoutManager.VERTICAL);
        eventArray.setLayoutManager(glm);
        eventArray.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if(dy > 0){
                    visibleItemCount = glm.getChildCount();
                    totalItemCount = glm.getItemCount();
                    pastVisibleItems = glm.findFirstVisibleItemPosition();

                    if(!loading){
                        if((visibleItemCount + pastVisibleItems) >= totalItemCount && !endOfItems){
                            loadEvents(offset, MainActivity.LOAD_ON_SCROLL, type);
                        }
                    }
                }
            }
        });

        eventItems = new ArrayList<>();

        loadEvents(offset, MainActivity.LOAD_ON_START, type);

        return eventView;
    }

    /**
     * Fragt die Ereignisse vom Server ab
     * @param os Verschiebung der Ergebnisse
     * @param s Max-Anzahl der Ergebnisse
     * @param t der Typ der abzufragenden Ereignisse
     */
    public void loadEvents(int os, int s, String t){
        loading = true;

        if(os == 0){
            loadingAnimation.setVisibility(View.VISIBLE);
        }
        else{
            eventItems.add(null);
            eventAdapter.notifyItemInserted(eventItems.size()-1);
        }

        final Map<String, String> requestData = new HashMap<>();
        requestData.put("action", "getevents");
        requestData.put("username", SaveData.getUsername(getContext()));
        requestData.put("password", SaveData.getPassword(getContext()));
        requestData.put("type", t);
        requestData.put("limit", String.valueOf(s));
        requestData.put("offset", String.valueOf(os));

        HTTPRequest.sendRequest(getContext(), requestData, SaveData.getServerIp(getContext()), new HTTPRequest.HTTPRequestCallback() {
            @Override
            public void onRequestResult(String result) {
                loadingAnimation.setVisibility(View.GONE);

                loading = false;

                boolean firstLoad = false;

                if(offset == 0){
                    firstLoad = true;
                }
                else{
                    eventItems.remove(eventItems.size()-1);
                    eventAdapter.notifyItemRemoved(eventItems.size()-1);
                }

                if(result != null){
                    try{
                        JSONObject jsonObject = new JSONObject(result);

                        JSONArray events = jsonObject.getJSONArray("events");

                        for(int i = 0; i < events.length(); i++){
                            offset++;

                            JSONObject o = events.getJSONObject(i);

                            eventItems.add(new EventItem(o.getString("text"), o.getString("type"), o.getLong("time")));
                        }
                    }
                    catch (Exception e){
                        Dialogs.fehlermeldung("Fehler beim Laden der Ereignisse", eventView.findViewById(R.id.frame));
                    }
                }
                else{
                    Dialogs.fehlermeldung("Es konnten keine Ereignisse geladen werden", eventView.findViewById(R.id.frame));
                }

                if(firstLoad){
                    if(eventItems.size() > 0){
                        eventAdapter = new EventAdapter();
                        eventArray.setAdapter(eventAdapter);
                    }
                    else{
                        eventArray.setVisibility(View.GONE);

                        eventView.findViewById(R.id.empty_item).setVisibility(View.VISIBLE);
                        ((ImageView) eventView.findViewById(R.id.empty_icon)).setImageResource(Icons.getDrawerIcon("events"));
                        ((TextView) eventView.findViewById(R.id.empty_title)).setText("Keine Ereignisse");
                        ((TextView) eventView.findViewById(R.id.empty_info)).setText("Es liegen keine Ereignisse vor.");
                    }
                }
                else{
                    if(result.equals("{\"events\":[]}")) endOfItems = true;
                    loading = false;
                    eventAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String msg) {
                loading = false;
                loadingAnimation.setVisibility(View.GONE);

                eventView.findViewById(R.id.empty_item).setVisibility(View.VISIBLE);
                ((ImageView) eventView.findViewById(R.id.empty_icon)).setImageResource(Icons.getDrawerIcon("events"));
                ((TextView) eventView.findViewById(R.id.empty_title)).setText("Keine Ereignisse");
                ((TextView) eventView.findViewById(R.id.empty_info)).setText("Es konnten keine Ereignisse geladen werden.");
            }
        });
    }

    public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        final int VIEW_TYPE_EVENT = 0;
        final int VIEW_TYPE_LOADING = 1;

        int lastPosition = -1;

        @Override
        public int getItemCount(){
            return eventItems.size();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int i){
            if(holder instanceof EventViewHolder){
                final EventItem ei = eventItems.get(i);
                final EventViewHolder eventViewHolder = (EventViewHolder) holder;

                eventViewHolder.event.setText(ei.getText());
                eventViewHolder.type.setText(ei.getType());
                eventViewHolder.time.setText(ei.getFormattedTime());

                setAnimation(((EventViewHolder) holder).container, i);
            }
        }

        /**
         * Startet die Animation auf der View
         * @param viewToAnimate View
         * @param position Index der View
         */
        private void setAnimation(View viewToAnimate, int position){
            if(position > lastPosition){
                lastPosition = position;
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.recycler_animation);
                viewToAnimate.startAnimation(animation);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
            View itemView;
            switch (viewType){
                case VIEW_TYPE_EVENT:
                    itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.event_item, viewGroup, false);
                    return new EventViewHolder(itemView);
                case VIEW_TYPE_LOADING:
                    itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.loading_row, viewGroup, false);
                    return new LoadingViewHolder(itemView);
            }
            return null;
        }

        @Override
        public int getItemViewType(int position){
            return eventItems.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_EVENT;
        }

        public class EventViewHolder extends RecyclerView.ViewHolder{
            protected TextView event, time, type;
            protected View container;

            public EventViewHolder(View v){
                super(v);
                container = v.findViewById(R.id.container);
                event = (TextView) v.findViewById(R.id.event);
                time = (TextView) v.findViewById(R.id.time);
                type = (TextView) v.findViewById(R.id.type);
            }
        }

        public class LoadingViewHolder extends RecyclerView.ViewHolder{
            public LoadingViewHolder(View v){
                super(v);
            }
        }
    }

    private class EventItem{
        private String text, type;
        private long timestamp;

        public EventItem(String text, String type, long timestamp){
            this.text = text;
            this.type = type;
            this.timestamp = timestamp;
        }

        /**
         * Gibt den Text des Items zurück
         * @return
         */
        public String getText(){
            return  text;
        }

        /**
         * Gibt den Typen des Items zurück
         * @return
         */
        public String getType(){
            return type;
        }

        /**
         * Gibt den Zeitstempel des Items zurück
         * @return
         */
        public long getTimestamp(){
            return timestamp;
        }

        /**
         * Gibt den Zeitstempel als formattierten String zurück
         * @return
         */
        public String getFormattedTime(){
            return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new java.util.Date(timestamp*1000));
        }
    }

}
