package com.edge.weather;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Created by kim on 2017. 5. 14..
 */

public class EventAddDialog extends Activity implements View.OnClickListener {
    TimePicker timePicker;
    EditText memo;
    RelativeLayout setCancel, setOk,goEventLocation;
    String date;
    static final int REQUSET_CODE = 1010;
    String[] split;
    static final String TAG = "com.weather";
    GPSData gpsData;
    TextView goAddress;
    String address;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.event);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        date = getIntent().getStringExtra("Date");
        split = date.split("-");
        Log.d("aaaaa",date);

        goAddress = (TextView) findViewById(R.id.location);
        goAddress.setOnClickListener(this);
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        memo = (EditText) findViewById(R.id.memo);
        setCancel = (RelativeLayout) findViewById(R.id.cancel);
        setCancel.setOnClickListener(this);
        setOk = (RelativeLayout) findViewById(R.id.ok);
        setOk.setOnClickListener(this);

    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok:
                //타임 피커뷰를 통해 시간정보를 unixstamp 타입으로 변환함
                String min;
                String hour;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    min = String.valueOf(timePicker.getMinute());
                    hour = String.valueOf(timePicker.getHour());
                } else {
                    min = String.valueOf(timePicker.getCurrentMinute());
                    hour = String.valueOf(timePicker.getCurrentHour());
                }
                Log.d("AAAA1",min+","+hour);
                if (min.length()==1){
                    min = "0"+min;
                }
                if (hour.length()==1){
                    hour="0"+hour;
                }
                long time = dateToUnixTime(dateFormat(split, hour, min));
                Log.d("AAAA",time+","+min+","+hour);
                String text = memo.getText().toString();
                goService();
                if (gpsData!=null){
                    sendResult(text,time,gpsData);
                } else {
                    gpsData = (GPSData) getIntent().getSerializableExtra("gps");
                    sendResult(text,time,gpsData);
                }
                break;
            case R.id.cancel:
                finish();
                break;
            case R.id.location:
                Intent intent = new Intent(this,AddressActivity.class);
                startActivityForResult(intent,REQUSET_CODE);
                break;
        }
    }
    private void sendResult(String memo,long time,GPSData gpsData){
        Intent intent = new Intent();
        intent.putExtra("Color",Color.argb(100,253,147,147));
        intent.putExtra("Memo",memo);
        intent.putExtra("Date",time);
        intent.putExtra("gps",gpsData);
        setResult(RESULT_OK,intent);
        finish();
    }
    public String dateFormat(String[] split, String hour, String min) {
        Log.d("aaaa",hour+","+min);
        String result;
        result = split[0] + split[1] + split[2] + hour + min;
        return result;
    }
    private void goService(){
        Intent service = new Intent(EventAddDialog.this, CalendarService.class);
        service.setPackage("com.edge.weather");
        service.setAction(TAG);
        startService(service);
    }
    public long dateToUnixTime(String date) {
        SimpleDateFormat dfm = new SimpleDateFormat("yyyyMMddHHmm");
        long unixtime = 0;
        try {
            unixtime = dfm.parse(date).getTime();
            Log.d("aaaa1",unixtime+","+date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return unixtime;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUSET_CODE){
            if (data!=null){
               gpsData = (GPSData) data.getSerializableExtra("gps");
                address = data.getStringExtra("address");
                goAddress.setText(address);
            }
        }
    }
}
