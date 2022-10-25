package me.ivanmazzoli.UI.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import me.ivanmazzoli.R;
import me.ivanmazzoli.Utils.CommonUtils;
import me.ivanmazzoli.Utils.PreferenceHelper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SplashActivity extends AppCompatActivity {

    // Variabili classe
    private PreferenceHelper preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Forzo l'orientazione dello schermo in verticale
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Inizializzo JodaTime e ottengo il PreferenceHelper
        JodaTimeAndroid.init(this);
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Rome")));
        preferences = PreferenceHelper.getInstance(this);

        // Copio i dati che utilizza l'app
        String filename = "data.json";
        String path = getFilesDir().getPath() + "/" + filename;
        if (!new File(path).exists())
            CommonUtils.getInstance(this).copyFile(filename);

        // Ottengo gli extras dal bundle
        boolean forceUpdate = getIntent().getBooleanExtra("forceUpdate", false);

        // Se l'app è stata lanciata menzo di mezz'ora fa avvio subito
        if (new DateTime().minusMinutes(2).isBefore(preferences.getLastAppLaunch()) && !forceUpdate) {
            startApp(true);
            return;
        }

        // Ottengo ultimo update asset salvato
        Gson gson = new Gson();
        JsonObject object = CommonUtils.getInstance(this).getJsonObjectFromFile(gson, "data.json");

        // Se l'object è null avvio l'app
        if (object == null)
            startApp(true);

        // Converto in JsonObject
        long last = object.get("updated").getAsLong();
        DateTime lastUpd = new DateTime().withMillis(last);
        String asset;
        try {
            InputStream in = this.getAssets().open(filename);
            asset = CommonUtils.getInstance(this).convertStreamToString(in);
        } catch (Exception e) {
            Log.e("TipsyApp", e.getMessage());
            startApp(false);
            return;
        }
        object = gson.fromJson(asset, JsonObject.class);
        final long ass = object.get("updated").getAsLong();
        DateTime assetUpd = new DateTime().withMillis(ass);

        // Se l'asset è più recente sovrascrivo
        if (last != ass && assetUpd.isAfter(lastUpd))
            CommonUtils.getInstance(this).copyFile("data.json");

        // Controllo la connessione ad internet
        NetworkInfo netInfo = ((ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (!(netInfo != null && netInfo.isConnectedOrConnecting())) {
            startApp(false);
            return;
        }

        // Creo un nuovo runnable per eseguire le operazioni di rete in background async
        new Thread(new Runnable() {

            public void run() {

                // Creo un nuovo client HTTP
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(2, TimeUnit.SECONDS)
                        .readTimeout(2, TimeUnit.SECONDS)
                        .cache(null)
                        .build();

                // Creo la richiesta all'API endpoint
                String api = preferences.getDataEndpoint() + "?timestamp=" + new DateTime().getMillis();
                Request request = new Request.Builder().url(api).build();

                // Invio la richiesta dei tempi
                Response response;
                String json;
                try {
                    response = client.newCall(request).execute();
                    json = response.body().string();
                } catch (Exception e) {
                    // Se crasha avvio l'app
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        if (PreferenceHelper.getInstance(getBaseContext()).isDeveloper())
                            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        startApp(false);
                    });
                    return;
                }

                // Controllo se la risposta è valida e inizio parsing
                Gson gson = new Gson();
                JsonObject object = null;
                try {
                    object = gson.fromJson(json, JsonObject.class);
                } catch (JsonSyntaxException e) {
                    // Se crasha avvio l'app
                    e.printStackTrace();
                    runOnUiThread(() -> startApp(false));
                }

                // Controllo se l'object è null
                if (object == null) {
                    runOnUiThread(() -> startApp(false));
                    return;
                }

                // Controllo se devo aggiornare i dati dell'app
                if (!object.get("status").getAsString().equals("update_available")) {
                    runOnUiThread(() -> startApp(false));
                    return;
                }

                // Salvo il nuovo JSON nella memoria dell'app
                try {
                    CommonUtils.getInstance(getApplicationContext()).saveGsonToFile(object, "data.json", "data");
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }

                // Avvio l'app
                runOnUiThread(() -> startApp(false));

            }
        }).start();
    }

    // Metodo per avviare l'app dopo un delay
    private void startApp(boolean bypassCooldown) {

        // Aggiorno l'ultimo avvio app
        preferences.setLastAppLaunch(new DateTime().getMillis());

        if (bypassCooldown) {
            Intent intent;
            intent = new Intent(SplashActivity.this, NavigationActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, NavigationActivity.class);
            startActivity(intent);
            finish();
        }, 1000);
    }
}