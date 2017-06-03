package com.edge.weather;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by kim on 2017. 5. 14..
 */

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventHolder> {
    Context context;
    List<Event> events = new ArrayList<>();

    public EventListAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }

    @Override
    public EventHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.eventlist,null);
        return new EventHolder(view);
    }

    @Override
    public void onBindViewHolder(EventHolder holder, int position) {
        Event event = events.get(position);
        int intColor = event.getColor();
        int red = Color.red(intColor);
        int green = Color.green(intColor);
        int blue = Color.blue(intColor);
        Log.d("aaaa",red+","+green+","+blue+","+intColor);
        holder.circleImageView.setColorFilter(Color.argb(100,red,green,blue));
        try {
            LinkedTreeMap<String,Object> map = (LinkedTreeMap<String, Object>) event.getData();
            holder.textView.setText(map != null ? map.get("data").toString() : " ");
        } catch (ClassCastException e){
            ServiceEvent serviceEvent = (ServiceEvent) event.getData();
            holder.textView.setText(serviceEvent != null ? serviceEvent.getData() : null);
        }

    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class EventHolder extends RecyclerView.ViewHolder{
        CircleImageView circleImageView;
        TextView textView;
        public EventHolder(View itemView) {
            super(itemView);
            circleImageView = (CircleImageView) itemView.findViewById(R.id.tag);
            textView = (TextView) itemView.findViewById(R.id.memo);
        }
    }
}
