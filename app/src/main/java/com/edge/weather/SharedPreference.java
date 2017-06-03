package com.edge.weather;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class SharedPreference {

    // Avoid magic numbers.
    private static final int MAX_SIZE = 3;


    public SharedPreference() {
        super();
    }

    private final static String PREF_NAME = "weather.pref";
    private final static String COOKIE_NAME = "weather.pref";



    public void put(Context context,String key, String value) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(key, value);
        editor.apply();
    }

    public void put(Context context, String key, HashSet<String> value){
        SharedPreferences pref = context.getSharedPreferences(COOKIE_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putStringSet(key,value);
        editor.apply();
    }
    public void put(Context context,String key, boolean value) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean(key, value);
        editor.apply();
    }

    public void put(Context context,String key, int value) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt(key, value);
        editor.apply();
    }
    public void remove(Context context){
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();
    }
    public void removeCookie(Context context){
        SharedPreferences pref = context.getSharedPreferences(COOKIE_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();
    }

    public String getValue(Context context,String key, String dftValue) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME,
                MODE_PRIVATE);

        try {
            return pref.getString(key, dftValue);
        } catch (Exception e) {
            return dftValue;
        }

    }
    public Set<String> getValue(Context context, String key, HashSet<String> value){
        SharedPreferences pref = context.getSharedPreferences(COOKIE_NAME,MODE_PRIVATE);
        try {
            return pref.getStringSet(key,value);
        }catch (Exception e){
            return value;
        }
    }
    public int getValue(Context context,String key, int dftValue) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME,
                MODE_PRIVATE);

        try {
            return pref.getInt(key, dftValue);
        } catch (Exception e) {
            return dftValue;
        }

    }

    public boolean getValue(Context context ,String key, boolean dftValue) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME,
                MODE_PRIVATE);

        try {
            return pref.getBoolean(key, dftValue);
        } catch (Exception e) {
            return dftValue;
        }
    }

    public static void storeList(Context context, String pref_name, String key, List countries) {

        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(pref_name, MODE_PRIVATE);
        editor = settings.edit();
        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(countries);
        editor.putString(key, jsonFavorites);
        editor.apply();
    }

    public  ArrayList<String> loadList(Context context, String key) {

        SharedPreferences settings;
        List<String> favorites;
        settings = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        if (settings.contains(key)) {
            String jsonFavorites = settings.getString(key, null);
            Gson gson = new Gson();
            String[] favoriteItems = gson.fromJson(jsonFavorites, String[].class);
            favorites = Arrays.asList(favoriteItems);
            favorites = new ArrayList<>(favorites);
        } else
            return null;
        return (ArrayList<String>) favorites;
    }

    public  void addList(Context context, String key, String country) {
        List<String> favorites = loadList(context, key);
        if (favorites == null)
            favorites = new ArrayList<>();
        if (favorites.contains(country)) {
            favorites.remove(country);
        }
        favorites.add(country);
        storeList(context, PREF_NAME, key, favorites);
    }
    public  void deleteEvent(Context context, String key, String country) {
        List<String> favorites = loadList(context, key);
        if (favorites == null)
            return;
        if (favorites.contains(country)) {
            favorites.remove(country);
        } else {
            Log.d("com.w",favorites.toString()+country);
        }
        storeList(context, PREF_NAME, key, favorites);
    }

//    public static void removeList(Context context,String pref_name, String key, String country) {
//        ArrayList favorites = loadList(context, pref_name,key);
//        if (favorites != null) {
//            favorites.remove(country);
//            storeList(context, pref_name, key, favorites);
//        }
//    }
    public  void deleteList(Context context, String pref_name) {

        SharedPreferences myPrefs = context.getSharedPreferences(PREF_NAME,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = myPrefs.edit();
        editor.clear();
        editor.apply();
    }
}