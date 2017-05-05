package de.smarthome_blogger.smarthome.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.smarthome_blogger.smarthome.R;
import de.smarthome_blogger.smarthome.items.SettingItem;
import de.smarthome_blogger.smarthome.system.Icons;

/**
 * Created by Sascha on 18.04.2017.
 */

public class SettingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    int lastPosition = -1;

    ArrayList<SettingItem> settingItems;
    Context context;

    public SettingAdapter(ArrayList<SettingItem> settingItems, Context context){
        this.settingItems = settingItems;
        this.context = context;
    }

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
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.recycler_animation);
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
