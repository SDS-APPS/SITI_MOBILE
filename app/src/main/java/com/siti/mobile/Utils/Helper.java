package com.siti.mobile.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.siti.mobile.Model.Room.RM_LiveStreamCategory;
import com.siti.mobile.R;
import com.siti.mobile.network.keys.NetworkPackageKeys;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Helper {
    SharedPreferences.Editor preferencesEditor;
    SharedPreferences mPreferences;


    public static String timeStamp(String date) {
        java.util.Date dates;
        if(date == null) return "";
        if (date.equals("null")) {
            dates = new Date();
        }else{
            long currentDate = Long.parseLong(date);
            dates = new java.util.Date((long) currentDate * 1000);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/MM/yyyy");
        return dateFormat.format(dates);

    }

    public static int daysRemaining(String date) {
        if (date == null || date.equals("null") || date.isEmpty()) {
            return 0;
        }

        try {
            long targetTime = Long.parseLong(date) * 1000L; // convertir a milisegundos
            long currentTime = System.currentTimeMillis();

            long diffMillis = targetTime - currentTime;

            if (diffMillis <= 0) {
                return 0; // ya pasó la fecha
            }

            long diffDays = diffMillis / (1000 * 60 * 60 * 24); // convertir a días
            return (int) diffDays;

        } catch (NumberFormatException e) {
            return 0; // en caso de que el string no sea un número válido
        }
    }


    public void clearChannelPreference() {
        preferencesEditor = mPreferences.edit();
        preferencesEditor.putString("LiveStream", "null");
        preferencesEditor.putString("LiveCategory", "null");
        preferencesEditor.putString("VODStream", "null");
        preferencesEditor.putString("VODCategory", "null");
        preferencesEditor.putString("SeriesStream", "null");
        preferencesEditor.putString("SeriesCategory", "null");
        preferencesEditor.putInt(NetworkPackageKeys.LIVE_TV, 0);
        preferencesEditor.putInt(NetworkPackageKeys.VOD, 0);
        preferencesEditor.putInt(NetworkPackageKeys.SOD, 0);
        preferencesEditor.putInt(NetworkPackageKeys.MOD, 0);
        preferencesEditor.putString("LAST_PLAYED_URL", "");
        preferencesEditor.putBoolean(NetworkPackageKeys.FIRST_LOAD, true).apply();
        preferencesEditor.apply();
    }

    public static String convert(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap convert(String base64Str, Context context) {
        try {
            byte[] decodedBytes = Base64.decode(
                    base64Str.substring(base64Str.indexOf(",") + 1),
                    Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.app_logo);
            return icon;
        } catch (NullPointerException e) {
            Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.app_logo);
            return icon;
        }

    }

    public static String getCamelCase(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto; // Devuelve la cadena original si está vacía o es nula.
        }

        // Obtener el primer carácter y convertirlo a mayúscula.
        char primerCaracter = texto.charAt(0);
        char primerCaracterMayuscula = Character.toUpperCase(primerCaracter);

        // Concatenar el carácter mayúscula con el resto de la cadena.
        String restoCadena = texto.substring(1);
        return primerCaracterMayuscula + restoCadena;
    }

    public static List<RM_LiveStreamCategory> joinStreamCategory(List<RM_LiveStreamCategory> streamCategory){

        List<RM_LiveStreamCategory> alteredMenu = new ArrayList<>();

        RM_LiveStreamCategory allCategoryItem = new RM_LiveStreamCategory();
        allCategoryItem.setCategory_count(0);
        allCategoryItem.setCategory_id("0001");
        allCategoryItem.setCategory_name("All");
        allCategoryItem.setId(0);
        allCategoryItem.setParent_id(0);
        alteredMenu.add(allCategoryItem);

        RM_LiveStreamCategory favoriteCategoryItem = new RM_LiveStreamCategory();
        favoriteCategoryItem.setCategory_count(0);
        favoriteCategoryItem.setCategory_id("0002");
        favoriteCategoryItem.setCategory_name("Favorites");
        favoriteCategoryItem.setId(0);
        favoriteCategoryItem.setParent_id(0);
        alteredMenu.add(favoriteCategoryItem);
//        categoryData = Stream.concat(alteredMenu.stream(), categoryData.stream()).collect(Collectors.toList());

//        streamCategory.add();
        return  streamCategory;
    }
}
