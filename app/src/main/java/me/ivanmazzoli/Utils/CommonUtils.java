package me.ivanmazzoli.Utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class CommonUtils {

    // Variabili classe
    private static CommonUtils instance;
    private Context context;

    private CommonUtils(Context context) {
        this.context = context;
    }

    /**
     * Metodo per ottenere un'instanza del CommonUtils
     *
     * @return l'istanza del CommonUtils se esistente, altrimenti una nuova istanza
     */
    public static CommonUtils getInstance(Context context) {
        if (instance == null) {
            instance = new CommonUtils(context);
        }
        return instance;
    }

    /**
     * Metodo per copiare un file dagli assets alla memoria dedicata l'app
     *
     * @param filename nome del file da salvare dalla cartella "assets"
     */
    public void copyFile(String filename) {
        AssetManager assetManager = context.getAssets();
        try {
            InputStream in = assetManager.open(filename);
            String newFileName = context.getFilesDir().getPath() + "/" + filename;
            OutputStream out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
        }
    }

    /**
     * Metodo per salvare un Gson in un file
     * @param object Oggetto da salvare
     * @param filename Nome del file
     * @param jsonTree Path di start da salvare del JsonObject
     * @throws IOException
     */
    public void saveGsonToFile(JsonObject object, String filename, String jsonTree) throws IOException {
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
        outputStreamWriter.write(new Gson().toJson(object.get(jsonTree)));
        outputStreamWriter.close();
    }

    public JsonObject getJsonObjectFromFile(Gson gson, String data) {
        String jsonData;
        try {
            String dataFile = context.getFilesDir().getPath() + "/" + data;
            jsonData = getStringFromFile(dataFile);
        } catch (Exception e) {
            Log.e("TipsyApp", e.getMessage());
            return null;
        }

        return gson.fromJson(jsonData, JsonObject.class);
    }

    /**
     * Metodo per aprire un file dagli assets dell'app
     *
     * @param filePath percorso del file da aprire
     * @return Stringa contenete il file aperto
     */
    public static String getStringFromFile(String filePath) throws IOException {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        fin.close();
        return ret;
    }

    /**
     * Metodo per ottenere una stringa da un InputStream
     *
     * @param is InputStream del file da convertire
     * @return Stringa contenente il file letto
     */
    public static String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }
}