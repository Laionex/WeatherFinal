package com.edge.weather;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    LocationManager mLM;
    Geocoder geocoder;
    static final String TAG ="com.weather";
    ImageView weatherIcon;
    TextView weatherText,myAddress,wind,rain,temp;
    RelativeLayout getLocation,info,goSchedule,menu;
    String liststring;
    AVLoadingIndicatorView loadingView;
    Calendar calendar;
    TypedArray icons;
    ArrayList<int[]> iconMap2 =new ArrayList<>();
    Map<String,String> wthTextMap = new HashMap<>();
    Map<String ,String > queryMap=new LinkedHashMap<>();
    Animation animation;
    double longitude =0;
    double latitude=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calendar = Calendar.getInstance();
        goService();
        animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate);
        setIconMap();
        setWthTextMap();
        icons = getResources().obtainTypedArray(R.array.icon);
        geocoder = new Geocoder(getApplicationContext());
        mLM = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
        myGPSCheck();
        setContentView(R.layout.activity_main);
        initView("",43,"","","","");
        longitude = CalendarService.longitude;
        latitude = CalendarService.latitude;
        if (latitude==0&&longitude==0) {
            registerLocationUpdates("first");
        } else {
            info.setVisibility(View.GONE);
            Log.d("aaaa",latitude+","+longitude);
            queryMap.put("version",ApiService.VERSION);
            queryMap.put("lat", String.valueOf(latitude));
            queryMap.put("lon",String.valueOf(longitude));
            getAddress(latitude,longitude);
            getMinWeather(queryMap);
        }


    }
    //기상코드에 따른 날씨 정보 텍스트
    private void setWthTextMap(){
        wthTextMap.put("맑음","기분 좋은 날씨에요");
        wthTextMap.put("구름조금","구름이 살짝 있어요");
        wthTextMap.put("구름많음","구름이 많아요");
        wthTextMap.put("구름많고 비","비가와요 우산 챙기세요");
        wthTextMap.put("구름많고 눈","눈이내려와요 얼음길 조심하세요");
        wthTextMap.put("구름많고 비 또는 눈","눈이나 비가내려요 감기조심하세요");
        wthTextMap.put("흐림","날씨가 흐리지만 항상 밝게!");
        wthTextMap.put("흐리고 비","비가와요 빗소리에 커피한잔 해봐요");
        wthTextMap.put("흐리고 비 또는 눈","눈이나 비가내려요 감기조심하세요");
        wthTextMap.put("흐리고 낙뢰","벼락 맞지 말고 돈벼락 맞으세요");
        wthTextMap.put("뇌우,비","천둥 번개와 비가와요! 우루루쾅쾅!");
        wthTextMap.put("뇌우,눈","거센 눈보라가 쳐요 감기조심하세요");
    }
    //typearray에서 기상코드에따라 열람할 수 있는 배열 생성
    private void setIconMap(){
        iconMap2.add(new int[]{38});
        iconMap2.add(new int[]{1,8});
        iconMap2.add(new int[]{2,9});
        iconMap2.add(new int[]{3,10});
        iconMap2.add(new int[]{12,40});
        iconMap2.add(new int[]{13,41});
        iconMap2.add(new int[]{14,42});
        iconMap2.add(new int[]{18});
        iconMap2.add(new int[]{21});
        iconMap2.add(new int[]{32});
        iconMap2.add(new int[]{4});
        iconMap2.add(new int[]{29});
        iconMap2.add(new int[]{4});
        iconMap2.add(new int[]{26});
        iconMap2.add(new int[]{27});
        iconMap2.add(new int[]{28});
    }
    //레트로핏을 이용한 실시간 기상정보 api 통신
    private void getMinWeather(Map<String, String> query) {
        Call<JsonObject> minWeather = SetRetrofit.setRetrofit(getApplicationContext()).minWeather(ApiService.APP_KEY, query);
        minWeather.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    jsonParsing(response.body(),"minutely");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }
    private void getHourWeather(Map<String, String> query) {
        Call<JsonObject> minWeather = SetRetrofit.setRetrofit(getApplicationContext()).hourWeather(ApiService.APP_KEY, query);
        minWeather.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    jsonParsing(response.body(),"hourly");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });
    }
    //현재 위치 받아오기
    private void registerLocationUpdates(String time) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            if (time.equals("first")){
                info.setVisibility(View.GONE);
                loadingView.show();
            }
            try {
                mLM.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        100, 5, mLocationListener);
                mLM.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        100, 5, mLocationListener);
            } catch (IllegalArgumentException e){
                e.printStackTrace();
            }
        }

//100은 0.1초마다, 5은 5미터마다 해당 값을 갱신한다는 뜻으로, 딜레이마다 호출하기도 하지만
//위치값을 판별하여 일정 미터단위 움직임이 발생 했을 때에도 리스너를 호출 할 수 있다.
    }

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.

            if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
                longitude = location.getLongitude();    //경도
                latitude = location.getLatitude();         //위도
                float accuracy = location.getAccuracy();        //신뢰도

                //위도 경도 버전 쿼리 값을 맵형태로 세팅
                queryMap.put("version",ApiService.VERSION);
                queryMap.put("lat", String.valueOf(latitude));
                queryMap.put("lon",String.valueOf(longitude));
                getAddress(latitude,longitude);
                CalendarService.longitude=longitude;
                CalendarService.latitude=latitude;
                getMinWeather(queryMap);
            } else {
                //Network 위치제공자에 의한 위치변화
                //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
                longitude = location.getLongitude();    //경도
                latitude = location.getLatitude();          //위도
                float accuracy = location.getAccuracy();        //신뢰도
                CalendarService.longitude=longitude;
                 CalendarService.latitude=latitude;
                //위와 동일
                queryMap.put("version",ApiService.VERSION);
                queryMap.put("lat", String.valueOf(latitude));
                queryMap.put("lon",String.valueOf(longitude));
                getAddress(latitude,longitude);
                getMinWeather(queryMap);
            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
    //뷰세팅
    private void initView(String address,int iconNum,String text,String windtext,String raintext,String temptext){
        weatherIcon = (ImageView) findViewById(R.id.weathericon);
        weatherText = (TextView) findViewById(R.id.weathertext);
        myAddress = (TextView) findViewById(R.id.address);
        info = (RelativeLayout) findViewById(R.id.info);
        wind = (TextView) findViewById(R.id.wind);
        rain = (TextView) findViewById(R.id.rain);
        temp = (TextView) findViewById(R.id.temp);
        menu = (RelativeLayout) findViewById(R.id.menu);
        menu.setOnClickListener(this);
        wind.setText(windtext);
        rain.setText(raintext);
        temp.setText(temptext);
        Typeface type = Typeface.createFromAsset(getApplicationContext().getAssets(), "BMJUA_otf.otf");
        weatherText.setTypeface(type);
        loadingView = (AVLoadingIndicatorView) findViewById(R.id.loadingview);
        getLocation = (RelativeLayout) findViewById(R.id.location);
        getLocation.setOnClickListener(this);
        goSchedule = (RelativeLayout) findViewById(R.id.goschedule);
        goSchedule.setOnClickListener(this);
        if (iconNum<43){
            Glide.with(getApplicationContext()).load(icons.getResourceId(iconNum-1,0))
                    .into(weatherIcon);
        }
        weatherText.setText(text);
        try {
            String [] data = address.split(" ");
            String result = data[2]+" "+data[3]+" "+data[4];
            myAddress.setText(result);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    //위도 경도를 가지고 지오코더를 통해 현재 위치 주소를 가져옴
    public void getAddress(double latitude,double longitude){

        List<Address> list = null;
        try {
            list = geocoder.getFromLocation(
                    latitude, // 위도
                    longitude, // 경도
                    10); // 얻어올 값의 개수
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("test", "입출력 오류 - 서버에서 주소변환시 에러발생");
        }
        if (list != null) {
            if (list.size()==0) {
                String a1 = "주소를 찾지 못하였습니다.";
                liststring = a1;
            } else {
                String a1 = list.get(0).getAddressLine(0);
                liststring = a1;
                Log.d("aaaa",liststring+"");
            }
        } else {
            String a1 = "주소를 찾지 못하였습니다.";
            liststring = a1;
        }
    }
    //현재 날씨 json파싱 클래스 매핑을 하는것도 좋은데 데이터정보가 복잡하여 클래스 매핑도 가독성이 상당히 떨어지는 관계로
    //하드하게 파싱했음
    private void jsonParsing(JsonObject object,String period){
        try {
            JsonObject weather = (JsonObject) object.get("weather");
            JsonArray minutely = (JsonArray) weather.get(period);
            JsonObject jsonObject = (JsonObject) minutely.get(0);
            JsonObject skyJson = (JsonObject) jsonObject.get("sky");
            JsonObject windJson = (JsonObject) jsonObject.get("wind");
            JsonObject temperatureJson = (JsonObject) jsonObject.get("temperature");

            String iconNumSt = skyJson.get("code").toString().replace("\"","");
            String weatherSt = skyJson.get("name").toString().replace("\"","");
            String wind = "풍향 "+windJson.get("wdir").toString().replace("\"","")+"/ "
                    +"풍속 "+windJson.get("wspd").toString().replace("\"","");
            String rain = "1시간 누적 강수량 ";
            try {
                JsonObject rainJson = (JsonObject) jsonObject.get("rain");
                rain+=rainJson.get("sinceOntime").toString().replace("\"","");
            } catch (Exception e){
                e.printStackTrace();
            }
            String temp = temperatureJson.get("tc").toString().replace("\"","");
            String tempMax = temperatureJson.get("tmax").toString().replace("\"","");
            String tempMin = temperatureJson.get("tmin").toString().replace("\"","");
            float f =0;
            if (temp.equals(" ")){
                Log.d("aaaaa",temp+",");
                f= Float.parseFloat(temp);
            } else {
                f = (Float.parseFloat(tempMax)+Float.parseFloat(tempMin))/2;
            }
            String tempRs = "현재 온도 "+String.valueOf(Math.round(f*10.0)/10.0)+"℃";

            //날씨 코드가 대략 "SKY_V01"이런식인데 뒤에 01 부분을 따와서 그부분을 상단에 아이콘 맵 세팅했던부분에 index로 넣어주고 그 value로 typearray에 아이콘을 가져오는 형식
            String a = iconNumSt.substring(iconNumSt.length()-2,iconNumSt.length());
            int[] num =iconMap2.get(Integer.parseInt(a));
            //현재가 오후인지 오전인지 판단하여 날씨 아이콘이 달라짐
            calendar.setTime(new Date());
            int ampm = calendar.get(Calendar.HOUR_OF_DAY);
            Log.d(TAG,ampm+"");
            Log.d("aaaa",liststring+"1111");
            if (ampm <=18){
                initView(liststring,num[0],wthTextMap.get(weatherSt),wind,rain,tempRs);
                loadingView.hide();
                animation.cancel();
                info.setVisibility(View.VISIBLE);
            } else {
                if (num.length>1){
                    initView(liststring,num[1],wthTextMap.get(weatherSt),wind,rain,tempRs);
                    loadingView.hide();
                    animation.cancel();
                    info.setVisibility(View.VISIBLE);
                } else {
                    initView(liststring,num[0],wthTextMap.get(weatherSt),wind,rain,temp);
                    loadingView.hide();
                    animation.cancel();
                    info.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e){
            queryMap.put("version",ApiService.VERSION);
            queryMap.put("lat", String.valueOf(latitude));
            queryMap.put("lon",String.valueOf(longitude));
            getHourWeather(queryMap);
        }

    }
    public void myGPSCheck(){
        //GPS가 켜져있는지 체크
        if(!mLM.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //GPS 설정화면으로 이동
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);
        }
    }

    //서비스 시작 (백그라운드 알림 관리)
    private void goService(){
        Intent service = new Intent(MainActivity.this, CalendarService.class);
        service.setPackage("com.edge.weather");
        service.setAction(TAG);
        startService(service);
    }
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mLM.removeUpdates(mLocationListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.location :
                getLocation.startAnimation(animation);
                mLM.removeUpdates(mLocationListener);
                registerLocationUpdates("re");
                break;
            case R.id.goschedule :
                //캘린더 액티비티에 위도경도 정보를 넘겨줌
                if (latitude!=0&&longitude!=0){
                    Intent intent =new Intent(MainActivity.this,MyCalendar.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("lon",longitude);
                    intent.putExtra("lat",latitude);
                    Log.d("aaaaa",intent+","+longitude+","+latitude);
                    startActivity(intent);
                }
                break;
            case R.id.menu :
                Intent intent = new Intent(this,SettingActivity.class);
                startActivity(intent);
        }
    }
}
