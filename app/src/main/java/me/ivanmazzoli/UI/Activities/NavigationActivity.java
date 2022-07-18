package me.ivanmazzoli.UI.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.joda.time.DateTime;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.cketti.library.changelog.ChangeLog;
import me.ivanmazzoli.Models.SmartFragment;
import me.ivanmazzoli.Models.SmartSettingsFragment;
import me.ivanmazzoli.R;
import me.ivanmazzoli.Utils.DrawerManager;
import me.ivanmazzoli.Utils.PreferenceHelper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NavigationActivity extends AppCompatActivity implements Drawer.OnDrawerItemClickListener {

    // View classe
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    // Variabili classe
    private final String SELECTED_ID = "selectedID";
    private final String SHOW_POPUP = "showPopup";
    private boolean appCloseRequested = false;
    private boolean showPopup = false;
    private long lastSelection;
    private Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // Associo ButterKnife
        ButterKnife.bind(this);

        // Sostituisco l'Action Bar con una Toolbar
        setSupportActionBar(toolbar);

        // Creo il Drawer laterale di navigazione
        View headerView = getLayoutInflater().inflate(R.layout.layout_drawer_header, null);
        drawer = new DrawerBuilder().withActivity(this)
                .withHeader(headerView)
                .withToolbar(toolbar)
                .withDrawerItems(DrawerManager.getInstance(this).getDrawerItems())
                .withOnDrawerItemClickListener(this)
                .build();

        // Se non ho una SavedInstance carico la lista scelta dall'utente,
        // altrimenti carico la SavedInstance e mostro il fragment precedente
        if (savedInstanceState == null) {
            drawer.setSelection(PreferenceHelper.getInstance(this).getFavouriteFragment(), true);
        } else {
            lastSelection = savedInstanceState.getLong(SELECTED_ID);
            showPopup = savedInstanceState.getBoolean(SHOW_POPUP);
            drawer.setSelection(lastSelection, true);
        }

        ChangeLog cl = new ChangeLog(this);
        if (cl.isFirstRun() || showPopup) {
            showPopup = true;
            AlertDialog ad = cl.getLogDialog();
            ad.setOnDismissListener(dialog -> showPopup = false);
            ad.show();
        }

        // Controllo se ho un update della app
        checkAppUpdate();
    }

    /**
     * Metodo per controllare se è disponibile per il download una nuova versione dell'app
     * <p>
     * Si scarica dall'endpoint remoto il numero di build della versione disponibile per il download
     * e lo compara con la versione di build dell'app in esecuzione. I check vengono eseguiti massimo
     * una volta ogni due ore.
     */
    private void checkAppUpdate() {

        // Ottengo il PreferenceHelper
        PreferenceHelper helper = PreferenceHelper.getInstance(this);

        // Se l'ultimo check è stato fatto MENO di due ore fa mi fermo
        if (new DateTime().minusHours(2).isBefore(helper.getLastUpdateCheck()))
            return;

        // Ottengo numero di build dell'applicazione corrente
        int currentBuild;
        try {
            currentBuild = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            return;
        }

        // Creo un nuovo client HTTP
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .cache(null)
                .build();

        // Creo la richiesta all'API endpoint di versione build remota
        String api = helper.getUpdateEndpoint();
        Request request = new Request.Builder().url(api).build();

        // Invio la richiesta in modalità async in un nuovo thread
        new Thread(() -> {
            // Ottengo la versione di build dall'API
            Response response;
            int webBuild;
            try {
                response = client.newCall(request).execute();
                assert response.body() != null;
                webBuild = Integer.parseInt(response.body().string());
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(NavigationActivity.this, e.toString(), Toast.LENGTH_LONG).show());
                return;
            }

            // Salvo nelle preferences dell'app l'ultimo check effettuato correttamente
            helper.setLastUpdateCheck(new DateTime().getMillis());

            // Se la versione ha numero di build maggiore o uguale a quella remota mi fermo
            if (webBuild <= currentBuild)
                return;

            // Creo un nuovo thread nell'uiThread per mostrare il Dialog di avviso nuova versione
            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(NavigationActivity.this);
                builder.setMessage("Vuoi scaricare la nuova versione dell'app?")
                        .setTitle("Aggiornamento disponibile!");
                builder.setPositiveButton("Ok", (dialog, id) -> {
                    String url = helper.getApkEndpoint();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                });
                builder.setNegativeButton("No", (dialog, id) -> {
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            });
        }).start();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putLong(SELECTED_ID, drawer.getCurrentSelection());
        savedInstanceState.putBoolean(SHOW_POPUP, showPopup);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

        if (drawerItem.getIdentifier() != lastSelection)
            DrawerManager.getInstance(this).resetSearchQueries();

        // Ottengo il fragment legato al Drawer laterale
        Object fragment = DrawerManager.getInstance(this)
                .getFragmentFromID(drawerItem.getIdentifier());

        // Se il fragment é null mi fermo
        if (fragment == null) {
            return false;
        }

        // Mostro il fragment nel container
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Se fragment normale casto TipsyFragment altrimenti TipsyPreferenceFragment
        if (fragment instanceof SmartFragment) {
            fragmentTransaction.replace(R.id.navigationContainer, (SmartFragment) fragment);
            toolbar.setTitle(((SmartFragment) fragment).getActivityTitle());
            //((SmartFragment) fragment).setOnFragmentChangeRequest(this);
            // Controllo se nascondere o mostrare l'elevation della toolbar
            if (((SmartFragment) fragment).hideToolbarElevation())
                getSupportActionBar().setElevation(0);
            else
                getSupportActionBar().setElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()));
        } else {
            getSupportActionBar().setElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()));
            fragmentTransaction.replace(R.id.navigationContainer, (SmartSettingsFragment) fragment);
            toolbar.setTitle(((SmartSettingsFragment) fragment).getActivityTitle());
        }

        // Cambio il fragment
        fragmentTransaction.commit();

        // Setto il titolo all'activity e chiudo il drawer
        if (fragment instanceof SmartFragment)
            getSupportActionBar().setTitle(((SmartFragment) fragment).getActivityTitle());
        //else
        //getSupportActionBar().setTitle(((TipsyPreferenceFragment) fragment).getActivityTitle());
        drawer.closeDrawer();

        // Invalido il menù
        invalidateOptionsMenu();

        return true;
    }

    /**
     * Metodo per gestire il click sul tasto indietro del dispositivo
     */
    public void onBackPressed() {

        // Decido se chiudere l'app o tornare alla dashboard
        if (drawer == null || !drawer.isDrawerOpen()) {
            assert drawer != null;
            if (drawer.getCurrentSelection() != DrawerManager.LIST_FULL) {
                drawer.setSelection(DrawerManager.LIST_FULL, true);
            } else {
                if (!appCloseRequested) {
                    appCloseRequested = true;
                    Toast.makeText(this, "Premi di nuovo per uscire", Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(() -> appCloseRequested = false, 3000);
                } else {
                    super.onBackPressed();
                }
            }
            return;
        }

        // Chiudo il drawer
        drawer.closeDrawer();
    }
}