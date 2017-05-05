package de.smarthome_blogger.smarthome.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.smarthome_blogger.smarthome.R;
import de.smarthome_blogger.smarthome.items.OverviewItem;
import de.smarthome_blogger.smarthome.system.Icons;

/**
 * Created by Sascha on 18.04.2017.
 */

public class OverviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int lastPosition = -1;

    private ArrayList<OverviewItem> overviewItems;
    private Activity activity;
    private Context context;
    private View view;

    public OverviewAdapter(ArrayList<OverviewItem> overviewItems, Activity activity, Context context, View view){
        this.overviewItems = overviewItems;
        this.activity = activity;
        this.context = context;
        this.view = view;
    }

    @Override
    public int getItemCount(){
        return overviewItems.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i){
        if(holder instanceof OverviewHolder){
            final OverviewItem oi = overviewItems.get(i);
            final OverviewHolder overviewHolder = (OverviewHolder) holder;
            overviewHolder.name.setText(oi.getName());

            //Adapter setzen
            RoomAdapter valueAdapter = new HorizontalRoomAdapter(oi.getValueList(), activity, context, view);
            overviewHolder.valueArray.setAdapter(valueAdapter);
            valueAdapter.notifyDataSetChanged();

            if(oi.getValueList().isEmpty()){
                overviewHolder.valueArray.setVisibility(View.GONE);

                overviewHolder.container.findViewById(R.id.empty_item).setVisibility(View.VISIBLE);
                ((ImageView) overviewHolder.container.findViewById(R.id.empty_icon)).setImageResource(Icons.getRoomIcon(oi.getLocation()));
                ((TextView) overviewHolder.container.findViewById(R.id.empty_title)).setText("Raum leer");
                ((TextView) overviewHolder.container.findViewById(R.id.empty_info)).setText("Diesem Raum wurden noch keine Sensoren hinzugefügt.");
            }

            setAnimation(overviewHolder.container, i);
        }
    }

    private void setAnimation(View viewToAnimate, int position){
        if(position > lastPosition){
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.recycler_animation);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.overview_item, viewGroup,false);
        return new OverviewHolder(itemView);
    }

    public class OverviewHolder extends RecyclerView.ViewHolder{
        protected TextView name;
        protected RecyclerView valueArray;
        protected View container;

        public OverviewHolder(View v){
            super(v);
            container = v.findViewById(R.id.container);
            name = (TextView) v.findViewById(R.id.title);
            valueArray = (RecyclerView) v.findViewById(R.id.value_list);

            valueArray.setHasFixedSize(true);
            LinearLayoutManager llm = new LinearLayoutManager(context);
            llm.setOrientation(LinearLayoutManager.HORIZONTAL);
            valueArray.setLayoutManager(llm);
        }
    }

}
