package me.ivanmazzoli.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.joda.time.DateTime;

import java.util.Arrays;

import me.ivanmazzoli.R;

public class PreferenceHelper {

    private final String FAV_FRAGMENT = "launchScreen";
    private final String IS_DEVELOPER = "isDeveloper";
    private final String NAV_TUTORIAL_SEEN = "navigationTutorialSeen";
    private final String SHOW_TUTORIAL_SEEN = "showTutorialSeen";
    private final String LAST_OPENED = "lastAppLaunch";
    private final String MAP_TYPE = "mapStyle";
    private final String PASS_NAME = "passName";
    private final String FIRST_LAUNCH = "firstLaunch";
    private final String PASS = "holderPass";
    private final String UPDATE = "lastOnlineCheck";
    private final String URL_API = "apiEndpoint";
    private final String URL_UPDATE = "updateEndpoint";
    private final String URL_APK = "apkEndpoint";
    private final String ALWAYS_ON = "enableAlwaysOn";


    private static Context context;
    private static PreferenceHelper instance;
    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    private PreferenceHelper(Context mContext) {
        preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
        context = mContext;
        editor = preferences.edit();
    }

    /**
     * Metodo per ottenere l'istanza del PreferenceHelper
     *
     * @return l'istanza del PreferenceHelper se esiste altrimenti una nuova
     */
    public static PreferenceHelper getInstance(Context mContext) {
        if (instance == null)
            instance = new PreferenceHelper(mContext);
        return instance;
    }

    /**
     * Metodo per ottenere l'ID del Fragment da mostrare ad avvio app
     *
     * @return ID Fragment scelto dall'utente, Dashboard se non modificato
     */
    public long getFavouriteFragment() {
        return Long.parseLong(preferences.getString(FAV_FRAGMENT, "1"));
    }

    public String getFavouriteFragmentName() {
        String[] texts = context.getResources().getStringArray(R.array.launchScreenTexts);
        String[] values = context.getResources().getStringArray(R.array.launchScreenValues);
        int index = Arrays.asList(values).indexOf(String.valueOf(getFavouriteFragment()));
        return texts[index];
    }

    public String getFragmentName(String fav) {
        String[] texts = context.getResources().getStringArray(R.array.launchScreenTexts);
        String[] values = context.getResources().getStringArray(R.array.launchScreenValues);
        int index = Arrays.asList(values).indexOf(fav);
        return texts[index];
    }

    /**
     * Metodo per abilitare o disabilitare la modalità sviluppatore
     *
     * @param isDeveloper booleano per impostare la modalità dev
     */
    public void setDeveloper(boolean isDeveloper) {
        editor.putBoolean(IS_DEVELOPER, isDeveloper);
        editor.commit();
    }

    /**
     * Metodo per ottenere se si è sviluppatori o no
     *
     * @return vero se si è dev, altrimenti falso
     */
    public boolean isDeveloper() {
        return preferences.getBoolean(IS_DEVELOPER, false);
    }

    /**
     * Metodo per sapere se il tutorial della navigazione è già stato visto
     *
     * @return vero se già visto altrimenti falso
     */
    public boolean hasNavigationTutorialAlreadyBeenSeen() {
        return preferences.getBoolean(NAV_TUTORIAL_SEEN, false);
    }

    /**
     * Metodo per impostare che il tutorial della navigazione è già stato visto
     */
    public void setNavigationTutorialAlreadyBeenSeen() {
        editor.putBoolean(NAV_TUTORIAL_SEEN, true);
        editor.commit();
    }

    /**
     * Metodo per sapere se il tutorial degli show è già stato visto
     *
     * @return vero se già visto altrimenti falso
     */
    public boolean hasShowTutorialAlreadyBeenSeen() {
        return preferences.getBoolean(SHOW_TUTORIAL_SEEN, false);
    }

    /**
     * Metodo per impostare che il tutorial degli show è già stato visto
     */
    public void setShowTutorialAlreadyBeenSeen() {
        editor.putBoolean(SHOW_TUTORIAL_SEEN, true);
        editor.commit();
    }

    /**
     * Metodo per ottenere il timestamp dell'ultimo avvio app
     *
     * @return long con ultimo avvio app
     */
    public long getLastAppLaunch() {
        return preferences.getLong(LAST_OPENED, 0l);
    }

    /**
     * Metodo per impostare l'ultimo avvio app
     *
     * @param launched timestamp ultimo avvio app
     */
    public void setLastAppLaunch(long launched) {
        editor.putLong(LAST_OPENED, launched);
        editor.commit();
    }

    /**
     * Metodo per ottenere il tipo di mappa
     *
     * @return 0 stradale, 1 satellitare
     */
    public int getMapStyle() {
        return Integer.parseInt(preferences.getString(MAP_TYPE, "1"));
    }

    /**
     * Metodo per ottenere il nome dell'abbonamento utente
     *
     * @return Nome dell'abbonamento
     */
    public String getPassName() {
        return preferences.getString(PASS_NAME, null);
    }

    /**
     * Metodo per impostare il nome dell'abbonamento utente
     *
     * @param name Nome dell'abbonamento
     */
    public void setPassName(String name) {
        editor.putString(PASS_NAME, name);
        editor.commit();
    }

    /**
     * Metodo per impostare che l'intro app è stata visualizzata
     */
    public void setIntroCompleted() {
        editor.putBoolean(FIRST_LAUNCH, false);
        editor.commit();
    }

    /**
     * Metodo per sapere se è il primo avvio app
     *
     * @return vero se è il primo avvio, falso se è già stata attivata in precedenza
     */
    public boolean isFirstLaunch() {
        return preferences.getBoolean(FIRST_LAUNCH, true);
    }

    public void resetPass() {
        editor.putString(PASS, null);
        editor.commit();
    }

    public void setLastUpdateCheck(long millis) {
        editor.putLong(UPDATE, millis);
        editor.commit();
    }

    public DateTime getLastUpdateCheck() {
        return new DateTime(preferences.getLong(UPDATE, 0));
    }

    public void setDataEndpoint(String url) {
        editor.putString(URL_API, url);
        editor.commit();
    }

    public void setUpdateEndpoint(String url) {
        editor.putString(URL_UPDATE, url);
        editor.commit();
    }

    public void setApkEndpoint(String url) {
        editor.putString(URL_APK, url);
        editor.commit();
    }

    public String getDataEndpoint() {
        return preferences.getString(URL_API, "http://imazzoli.duckdns.org:8080/ilpra/api/update.php");
    }

    public String getUpdateEndpoint() {
        return preferences.getString(instance.URL_UPDATE, "http://imazzoli.duckdns.org:8080/ilpra/release/build.txt");
    }

    public String getApkEndpoint() {
        return preferences.getString(instance.URL_APK, "http://imazzoli.duckdns.org:8080/ilpra/release/app-release.apk");
    }

    public boolean getScreenAlwaysOn() {
        return preferences.getBoolean(ALWAYS_ON, true);
    }
}