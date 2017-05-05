package de.smarthome_blogger.smarthome.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

import de.smarthome_blogger.smarthome.R;
import de.smarthome_blogger.smarthome.items.RoomItem;
import de.smarthome_blogger.smarthome.system.Icons;
import de.smarthome_blogger.smarthome.system.RoomControl;

/**
 * Created by Sascha on 18.04.2017.
 */

public class RoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    final int VIEW_TYPE_SWITCH = 0;
    final int VIEW_TYPE_SENSOR = 1;
    final int VIEW_TYPE_HEATING = 2;
    final int VIEW_TYPE_SCENE = 3;

    int lastPosition = -1;

    boolean setState = false;

    ArrayList<RoomItem> roomItems;
    Activity activity;
    Context context;
    View view;

    public RoomAdapter(ArrayList<RoomItem> roomItems, Activity activity, Context context, View view){
        this.roomItems = roomItems;
        this.activity = activity;
        this.context = context;
        this.view = view;
    }

    /**
     * Gibt die RoomItems zurück
     * @return
     */
    public ArrayList<RoomItem> getRoomItems(){
        return roomItems;
    }

    /**
     * Gibt die Activity zurück
     * @return
     */
    public Activity getActivity(){
        return activity;
    }

    @Override
    public int getItemCount(){
        return roomItems.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int i){
        if(holder instanceof SwitchViewHolder){
            final RoomItem ri = roomItems.get(i);
            final SwitchViewHolder switchViewHolder = (SwitchViewHolder) holder;
            switchViewHolder.name.setText(ri.getName());
            switchViewHolder.icon.setImageResource(Icons.getDeviceIcon(ri.getIcon()));

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
                    RoomControl.setModes(view, activity, ri, i, !switchViewHolder.switchView.isChecked(),
                            new RoomControl.OnAdapterNotifyListener() {
                                @Override
                                public void onNotify(int i) {
                                    notifyItemChanged(i);
                                }
                            });
                    switchViewHolder.switchView.setChecked(!switchViewHolder.switchView.isChecked());
                    setState = false;
                }
            });

            setAnimation(((SwitchViewHolder) holder).container, i);
        }
        else if(holder instanceof SensorViewHolder){
            final RoomItem ri = roomItems.get(i);
            final SensorViewHolder sensorViewHolder = (SensorViewHolder) holder;
            sensorViewHolder.icon.setImageResource(Icons.getValueIcon(ri.getIcon()));
            sensorViewHolder.value.setText(ri.getValue());
            sensorViewHolder.name.setText(ri.getName());

            sensorViewHolder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RoomControl.showOverview(activity, ri, ri.getRoom());
                }
            });
        }
        else if(holder instanceof HeatingViewHolder){
            final RoomItem ri = roomItems.get(i);
            final HeatingViewHolder heatingViewHolder = (HeatingViewHolder) holder;
            heatingViewHolder.icon.setImageResource(Icons.getDeviceIcon(ri.getIcon()));
            heatingViewHolder.value.setText(ri.getValue());
            heatingViewHolder.name.setText(ri.getName());

            heatingViewHolder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RoomControl.openHeatingDialog();
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
                    RoomControl.openSceneMenu();
                }
            });
        }
    }

    public void setAnimation(View viewToAnimate, int position){
        if(position > lastPosition){
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.recycler_animation);
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
        if(roomItems.get(position).getType().equals("switch")){
            return VIEW_TYPE_SWITCH;
        }
        else if(roomItems.get(position).getType().equals("sensor")){
            return VIEW_TYPE_SENSOR;
        }
        else if(roomItems.get(position).getType().equals("scene")){
            return VIEW_TYPE_SCENE;
        }
        else if(roomItems.get(position).getType().equals("heating")){
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
