package com.edge.weather;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by kim on 2017. 5. 26..
 */

public class AddressActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    SearchView search;
    private List<Address> autoCompleteSuggestionAddresses;
    private String latitude, longitude;
    private ArrayAdapter<String> autoCompleteAdapter;
    ArrayList<String> addresses = new ArrayList<>();
    ArrayList<GPSData> gpsDatas = new ArrayList<>();
    GetSuggestions getSuggestions = new GetSuggestions();
    String address;
    AddressAdapter addressAdapter;
    RecyclerView recyclerView;
    Geocoder geocoder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geocoder = new Geocoder(this, Locale.KOREA);
        setContentView(R.layout.address);
        search = (SearchView) findViewById(R.id.search);
        search.setOnQueryTextListener(this);
        recyclerView = (RecyclerView) findViewById(R.id.addresslist);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                search.setQuery(addressAdapter.getItem(position),false);
                setResult(gpsDatas.get(position),addressAdapter.getItem(position));
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));
        setRecyclerView();
    }

    private void setRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        addressAdapter = new AddressAdapter(addresses, getApplicationContext());
        recyclerView.setAdapter(addressAdapter);

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //서치뷰를 이용하여 택스트 변화에 따라 지오코더를 통한 주소정보 열람
        if (getSuggestions != null) {
            getSuggestions.cancel(true);
            Log.d("noh", "aaaaa2");
        }

        getSuggestions = new GetSuggestions();
        getSuggestions.execute(newText);
        return false;
    }
    //주소 클릭하면 이전 다이얼로그에 주소정보 세팅
    private void setResult (GPSData gpsData,String address){
        Intent intent =new Intent();
        intent.putExtra("address",address);
        intent.putExtra("gps",gpsData);
        setResult(EventAddDialog.REQUSET_CODE,intent);
        finish();
    }

    private class GetSuggestions extends AsyncTask<String, Void, ArrayList<String>> {
        protected ArrayList<String> doInBackground(String... search) {
            address = search[0];
            try {
                autoCompleteSuggestionAddresses = geocoder.getFromLocationName(address, 10);

                //notifyResult(autoCompleteSuggestionAddresses);

                latitude = longitude = null;
                addresses.clear();
                gpsDatas.clear();
                for (int i = 0; i < autoCompleteSuggestionAddresses.size(); i++) {
                    Address a = autoCompleteSuggestionAddresses.get(i);
                    Log.v("Nohsib", a.toString());
                    String temp = "" + a.getAddressLine(0);
                    addresses.add(temp);
                    if (a.hasLatitude()&&a.hasLongitude()){
                        GPSData gpsData = new GPSData(a.getLongitude(),a.getLatitude());
                        gpsDatas.add(gpsData);
                    }

                }

            } catch (IOException ex) {
                Log.d("aaaa", "Failed to get autocomplete suggestions", ex);
            }
            return addresses;
        }

        protected void onPostExecute(ArrayList<String> result) {
            addressAdapter.notifyDataSetChanged();


        }
    }
}
