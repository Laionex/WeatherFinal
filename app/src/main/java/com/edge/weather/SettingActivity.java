package com.edge.weather;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by kim on 2017. 5. 25..
 */

public class SettingActivity extends AppCompatActivity implements View.OnClickListener,CompoundButton.OnCheckedChangeListener{
    Switch timePush,eventPush,weekPush;
    SharedPreference sharedPreference = new SharedPreference();
    TimePicker timePicker;
    ImageView complete;
    RelativeLayout back;
    static final String TAG ="com.weather";
    boolean setPush =false;
    boolean setEvent= false;
    int hour =0;
    int min = 0;
    Calendar calendar =Calendar.getInstance();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        calendar.setTime(new Date());
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        min = calendar.get(Calendar.MINUTE);
        initView();
        setPush = sharedPreference.getValue(getApplicationContext(),"Push",false);
        setEvent = sharedPreference.getValue(getApplicationContext(),"eventPush",false);
        if (setEvent){
            eventPush.setChecked(true);
        } else {
            eventPush.setChecked(false);
        }
        if (setPush){
            timePicker.setVisibility(View.VISIBLE);
            timePush.setChecked(true);
        } else {
            timePicker.setVisibility(View.GONE);
            timePush.setChecked(false);
        }
        timePush.setOnCheckedChangeListener(this);
        //매일 알람 푸시 시간 설정
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                complete.setEnabled(true);
                hour =hourOfDay;
                min = minute;
            }
        });
    }
    private void initView(){
        timePush = (Switch) findViewById(R.id.pushSwitch);
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        complete = (ImageView) findViewById(R.id.finish);
        eventPush = (Switch) findViewById(R.id.eventSwitch);
        back = (RelativeLayout) findViewById(R.id.back);
        weekPush = (Switch) findViewById(R.id.weekSwitch);

        weekPush.setOnCheckedChangeListener(this);
        back.setOnClickListener(this);
        eventPush.setOnCheckedChangeListener(this);
        complete.setOnClickListener(this);
        complete.setEnabled(false);
    }
    private void goService(){
        Intent service = new Intent(SettingActivity.this, CalendarService.class);
        service.setPackage("com.edge.weather");
        service.setAction(TAG);
        startService(service);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.finish :
                sharedPreference.put(getApplicationContext(),"hour",hour);
                sharedPreference.put(getApplicationContext(),"min",min);
                goService();
                finish();
                break;
            case R.id.back:
                if (hour!=0&&min!=0){
                    sharedPreference.put(getApplicationContext(),"hour",hour);
                    sharedPreference.put(getApplicationContext(),"min",min);
                }
                goService();
                finish();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.pushSwitch :
                Log.d("aaaa","ccccc");
                complete.setEnabled(true);
                if (isChecked){
                    sharedPreference.put(getApplicationContext(),"Push",true);
                    Animation animation =AnimationUtils.loadAnimation(getApplicationContext(),R.anim.visible);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            timePicker.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    timePicker.startAnimation(animation);
                    timePicker.setEnabled(true);
                } else {
                    sharedPreference.put(getApplicationContext(),"Push",false);
                    Animation animation =AnimationUtils.loadAnimation(getApplicationContext(),R.anim.gone);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            timePicker.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    timePicker.startAnimation(animation);
                    timePicker.setEnabled(false);
                }
                break;
            case R.id.eventSwitch:
                complete.setEnabled(true);
                if (isChecked) {
                    sharedPreference.put(getApplicationContext(),"eventPush",true);
                } else {
                    sharedPreference.put(getApplicationContext(),"eventPush",false);
                }
                break;
            case R.id.weekSwitch:
                complete.setEnabled(true);
                if (isChecked) {
                    sharedPreference.put(getApplicationContext(),"weekPush",true);
                } else {
                    sharedPreference.put(getApplicationContext(),"weekPush",false);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (hour!=0&&min!=0){
            sharedPreference.put(getApplicationContext(),"hour",hour);
            sharedPreference.put(getApplicationContext(),"min",min);
        }
        goService();
        finish();
    }
}
