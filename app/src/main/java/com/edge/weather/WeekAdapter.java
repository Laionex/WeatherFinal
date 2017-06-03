package com.edge.weather;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by kim on 2017. 5. 14..
 */

public class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.ViewHolder> {
    private Context context;
    private ArrayList<WeekModel> weekModels = new ArrayList<>();
    private ArrayList<int[]> iconMap =new ArrayList<>();
    private TypedArray icons;
    private String [] weekDay ={"월","화","수","목","금","토","일"};
    Calendar calendar = Calendar.getInstance();
    public WeekAdapter(Context context, ArrayList<WeekModel> weekModels,TypedArray array) {
        setIconMap();
        calendar.setTime(new Date());
        this.context = context;
        this.weekModels = weekModels;
        this.icons = array;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weekitem, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WeekModel item = weekModels.get(position);
        String code = item.getCode();

        try {
            String codeNum= code.substring(code.length()-2,code.length());
            Log.d("aaaaa",code+","+codeNum);
            int[] num = iconMap.get(Integer.parseInt(codeNum));
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if (hour<=18){
                Glide.with(context).load(icons.getResourceId(num[0]-1,0))
                        .into(holder.imageView);
            } else {
                if (num.length>1){
                    Glide.with(context).load(icons.getResourceId(num[1]-1,0))
                            .into(holder.imageView);
                } else {
                    Glide.with(context).load(icons.getResourceId(num[0]-1,0))
                            .into(holder.imageView);
                }

            }
        } catch (Exception e){
            e.printStackTrace();
        }
        String name = item.getName();
        Log.d("aaaa",item.getDayOfWeek()+"");
        if (item.getDayOfWeek() ==1){
            holder.weekday.setText(weekDay[6]);
        } else {
            holder.weekday.setText(weekDay[item.getDayOfWeek()-2]);
        }
        holder.textView.setText(name);

    }

    @Override
    public int getItemCount() {
        return weekModels.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView textView,weekday;
        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.icon);
            textView = (TextView) itemView.findViewById(R.id.name);
            weekday = (TextView) itemView.findViewById(R.id.weekday);
        }
    }
    private void setIconMap(){
        iconMap.add(new int[]{38});
        iconMap.add(new int[]{1,8});
        iconMap.add(new int[]{2,9});
        iconMap.add(new int[]{3,10});
        iconMap.add(new int[]{12,40});
        iconMap.add(new int[]{13,41});
        iconMap.add(new int[]{14,42});
        iconMap.add(new int[]{18});
        iconMap.add(new int[]{21});
        iconMap.add(new int[]{32});
        iconMap.add(new int[]{4});
        iconMap.add(new int[]{29});
        iconMap.add(new int[]{4});
        iconMap.add(new int[]{26});
        iconMap.add(new int[]{27});
        iconMap.add(new int[]{28});
    }
}
