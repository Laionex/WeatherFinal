package com.edge.weather;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by kim on 2017. 5. 26..
 */

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {
    ArrayList<String> addresses = new ArrayList<>();
    Context context;

    public AddressAdapter(ArrayList<String> addresses, Context context) {
        this.addresses = addresses;
        this.context = context;
    }
    public String getItem (int position){
       return addresses.get(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.addressitem,null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.address.setText(addresses.get(position));
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView address;
        public ViewHolder(View itemView) {
            super(itemView);
            address = (TextView) itemView.findViewById(R.id.address);
        }

    }
}
