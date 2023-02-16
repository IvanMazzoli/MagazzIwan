package me.ivanmazzoli.UI.Activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ivanmazzoli.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DebugActivity extends AppCompatActivity {

    // Binding views
    @BindView(R.id.txtCategories) TextView categories;
    @BindView(R.id.txtMissing) TextView missingCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        ButterKnife.bind(this);

        // Scarico e popolo da API
        downloadShit();
    }

    /**
     * Metodo per scaricare la merda da endpoint. URL HARDCODED!
     */
    private void downloadShit() {
        // Creo un nuovo client HTTP
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .cache(null)
                .build();

        String api = "http://imazzoli.duckdns.org:8080/ilpra/cms/api/collections/get/products?token=account-c81f17303abcbb928cda89a1dddc2d";
        Request request = new Request.Builder().url(api).build();

        // Invio la richiesta in modalità async in un nuovo thread
        new Thread(() -> {

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
                    categories.setText("ERRORE API");
                    missingCategories.setText("ERRORE API");
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
                runOnUiThread(() -> {
                    categories.setText("ERRORE PARSING");
                    missingCategories.setText("ERRORE PARSING");
                });
            }

            // Controllo se l'object è null
            if (object == null) {
                runOnUiThread(() -> {
                    categories.setText("ERRORE OBJ NULL");
                    missingCategories.setText("ERRORE OBJ NULL");
                });
                return;
            }

            String lista = object.get("fields").getAsJsonObject()
                    .get("type").getAsJsonObject()
                    .get("options").getAsJsonObject()
                    .get("options").getAsString();
            String[] veraLista = lista.split(",");
            for (int x = 0; x < veraLista.length; x++)
                veraLista[x] = veraLista[x].trim();
            String[] esistenti = {"PLC", "Cavo", "Termica", "Azionamento", "Componenti Pulsanti e Gemme", "Connettore", "Materiale di consumo"};
            HashSet<String> s1 = new HashSet<String>(Arrays.asList(veraLista));
            s1.removeAll(Arrays.asList(esistenti));

            runOnUiThread(() -> {
                categories.setText(lista);
                missingCategories.setText(String.join(", ", s1));
            });

        }).start();
    }
}