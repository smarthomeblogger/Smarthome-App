package de.smarthome_blogger.smarthome.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.smarthome_blogger.smarthome.R;
import de.smarthome_blogger.smarthome.items.RoomItem;
import de.smarthome_blogger.smarthome.system.Icons;
import de.smarthome_blogger.smarthome.system.RoomControl;

/**
 * Created by Sascha on 18.04.2017.
 */

public class HorizontalRoomAdapter extends RoomAdapter {

    public HorizontalRoomAdapter(ArrayList<RoomItem> roomItems, Activity activity, Context context, View view){
        super(roomItems, activity, context, view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int i){
        final RoomItem ri = getRoomItems().get(i);
        final SensorViewHolder sensorViewHolder = (SensorViewHolder) holder;
        sensorViewHolder.icon.setImageResource(Icons.getDeviceIcon(ri.getIcon()));
        sensorViewHolder.value.setText(ri.getValue());
        sensorViewHolder.name.setText(ri.getName());

        sensorViewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoomControl.showOverview(getActivity(), ri, ri.getRoom());
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.value_item_horizontal, viewGroup, false);
        return new SensorViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position){
        return VIEW_TYPE_SENSOR;
    }
}
