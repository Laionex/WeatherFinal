package com.edge.weather;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.transition.Visibility;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by kim on 2017. 5. 25..
 */

public class WidgetProvider extends AppWidgetProvider {

    //앱 위젯 프로바이더
    //보통 온리시브와 업데이트를 통해 많이 구현을 하지만 상대적으로 불안전한 부분들이 생김
    //가령 현재 시간같은경우 알람매니저를 통해 setrepeat을 통해 1분단위로 알람매니저를 리프레쉬 해서 배터리소모를 낮추고 할 수 있지만
    //저 메소드 자체가 정확한 시간캐치 불가능 00시 10분인데 10분 00초에 바뀌는것이아닌 10분 30초 이런식으로 위젯이 없데이트되서 시간이 불확실함
    //해서 타임태스크를 통해 1초단위로 앱위젯을 리프레쉬하는데 타임태스크는 앱위젯 프로바이더에 구현하면 os단에서 차단하는 경우도 있음 그래서 settime메소드를 보면
    // 앱위젯을 서비스 단으로 보내 백그라운드에서 timetask를 이용한 앱위젯 실시간 업데이트를 구현함
    // 삼사일간 사용해봤을때 배터리 생각보다 별로안담. 베터리 광탈 체감 불가능 할정도 수준 쓸만함
    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);

    }

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        //앱위젯 업데이트는 엡위젯프로바이더베이직에 보면 기본적으로 업데이트 주기가 명시되어있음 그때마다 실행되는 메소드
        setTime(context);

    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }

    //앱위젯 업데이트 이벤트를 백그라운드로 넘긴다.
    public void setTime(Context context){
        final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        final Intent intent = new Intent(context, CalendarService.class);

        intent.setPackage("com.edge.weather");
        intent.setAction(CalendarService.TAG4);
        PendingIntent service = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Log.d("aaaa","startwidget");
        m.cancel(service);
        m.set(AlarmManager.RTC,System.currentTimeMillis(),service);
    }







}
