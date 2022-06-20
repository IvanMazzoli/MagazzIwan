package me.ivanmazzoli.UI;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ivanmazzoli.IlpraItem;
import me.ivanmazzoli.ItemAdapter;
import me.ivanmazzoli.R;
import me.ivanmazzoli.SmartFragment;
import me.ivanmazzoli.Utils.CommonUtils;
import me.ivanmazzoli.Utils.DrawerManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ListFragment extends SmartFragment implements SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener {

    // View classe
    @BindView(R.id.pullToRefresh) SwipeRefreshLayout pullToRefresh;
    @BindView(R.id.recyclerView) RecyclerView recyclerView;

    // Variabili classe
    private final List<IlpraItem> pois = new ArrayList<>();
    private static ListFragment fragment;
    private DrawerManager drawerManager;
    private SearchView searchView;
    private ItemAdapter adapter;
    private String filter;

    public ListFragment() {
    }

    public ListFragment(long listID) {
        this.setDrawerID(listID);
        switch ((int) listID) {
            case 1:
                this.setDrawerTitle("Tutti gli articoli");
                this.setDrawerIcon(R.drawable.ic_list);
                this.setActivityTitle("Tutti gli articoli");
                break;
            case 3:
                this.setDrawerTitle("PLC ed espansioni");
                this.setDrawerIcon(R.drawable.ic_plc);
                this.setActivityTitle("PLC ed espansioni");
                this.filter = "PLC";
                break;
            case 4:
                this.setDrawerTitle("Cavi");
                this.setDrawerIcon(R.drawable.ic_cable);
                this.setActivityTitle("Cavi");
                this.filter = "Cavo";
                break;
            case 5:
                this.setDrawerTitle("Termiche salvamotore");
                this.setDrawerIcon(R.drawable.ic_thermal);
                this.setActivityTitle("Termiche salvamotore");
                this.filter = "Termica";
                break;
            case 6:
                this.setDrawerTitle("Azionamenti");
                this.setDrawerIcon(R.drawable.ic_motor);
                this.setActivityTitle("Azionamenti");
                this.filter = "Azionamento";
                break;
            case 7:
                this.setDrawerTitle("Pulsanti e gemme");
                this.setDrawerIcon(R.drawable.ic_button);
                this.setActivityTitle("Pulsanti e gemme");
                this.filter = "Componenti Pulsanti e Gemme";
                break;
            case 8:
                this.setDrawerTitle("Connettori");
                this.setDrawerIcon(R.drawable.ic_connector);
                this.setActivityTitle("Connettori");
                this.filter = "Connettore";
                break;
            default:
                break;
        }
    }

    public static ListFragment getInstance(long listID) {

        if (fragment == null)
            fragment = new ListFragment(listID);

        if (fragment.getDrawerID() != listID)
            fragment = new ListFragment(listID);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drawerManager = DrawerManager.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);

        pullToRefresh.setColorSchemeResources(R.color.accent_dark);
        pullToRefresh.setOnRefreshListener(this);

        recyclerView = view.findViewById(R.id.recyclerView);

        setupList();

        if (savedInstanceState != null)
            setSearchQuery(savedInstanceState.getString("search"));

        // Restituisco la view del fragment
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("search", searchView.getQuery().toString());
        drawerManager.updateFragmentSearch(this, getSearchQuery());
    }

    /**
     * Metodo per ottenere lo StaggeredLayoutManager per mostrare la lista
     *
     * @return StaggeredLayoutManager personalizzato
     */
    private StaggeredGridLayoutManager getRecyclerLayoutManager() {
        return new StaggeredGridLayoutManager(1, 1);
    }

    /**
     * Metodo per creare un tasto search nella toolbar
     *
     * @param menu     Menu da mostrare
     * @param inflater Inflater delle view
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Se non ho POI mi fermo
        if (pois.size() == 0)
            return;

        inflater.inflate(R.menu.search_update, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Cerca");

        /*MenuItem refresh = menu.findItem(R.id.action_update);
        refresh.setOnMenuItemClickListener(menuItem -> {
            Intent i = new Intent(getActivity(), SplashActivity.class);
            Bundle b = new Bundle();
            b.putBoolean("forceUpdate", true);
            i.putExtras(b);
            requireActivity().startActivity(i);
            getActivity().finish();
            return true;
        });*/

        if (getSearchQuery() != null && !getSearchQuery().equals("")) {
            searchView.setQuery(getSearchQuery(), true);
            searchView.setIconified(false);
        }
    }

    /**
     * Callback di invio di testo scritto
     *
     * @param query testo scritto dall'user
     * @return se l'evento è stato consumato - sempre true
     */
    @Override
    public boolean onQueryTextSubmit(String query) {

        setSearchQuery(query);
        drawerManager.updateFragmentSearch(this, query);

        // Se non ho POI mi fermo
        if (pois.size() == 0)
            return true;

        // Filtro la lista
        adapter.filter(query);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        return true;
    }

    /**
     * Callback di modifica testo scritto
     *
     * @param query testo scritto dall'user
     * @return se l'evento è stato consumato - sempre true
     */
    @Override
    public boolean onQueryTextChange(String query) {

        setSearchQuery(query);
        drawerManager.updateFragmentSearch(this, query);

        // Se non ho POI mi fermo
        if (pois.size() == 0)
            return true;

        // Filtro la lista
        adapter.filter(query);
        return true;
    }

    public static ArrayList<IlpraItem> getPoiList(Context context, String type) {

        String jsonData;
        try {
            String dataFile = context.getApplicationContext().getFilesDir().getPath() + "/data.json";
            jsonData = CommonUtils.getInstance(context).getStringFromFile(dataFile);
        } catch (Exception e) {
            Log.e("Tipsy", e.getMessage());
            return new ArrayList<>();
        }

        // Converto il Json in una lista di poi
        Gson gson = new Gson();
        JsonObject object = gson.fromJson(jsonData, JsonObject.class);
        JsonElement element = object.get(type);
        Type listType = new TypeToken<List<IlpraItem>>() {
        }.getType();
        ArrayList<IlpraItem> pois = gson.fromJson(element, listType);

        if (pois == null)
            return new ArrayList<>();
        return pois;
    }

    @Override
    public void onRefresh() {

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
                String api = "https://www.tipsyapp.it/ilpra/api/update.php?timestamp=" + new DateTime().getMillis();
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
                    getActivity().runOnUiThread(() -> {
                        pullToRefresh.setRefreshing(false);
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
                    getActivity().runOnUiThread(() -> pullToRefresh.setRefreshing(false));
                }

                // Controllo se devo aggiornare i dati dell'app
                if (!object.get("status").getAsString().equals("update_available")) {
                    getActivity().runOnUiThread(() -> pullToRefresh.setRefreshing(false));
                    return;
                }

                // Salvo il nuovo JSON nella memoria dell'app
                try {
                    CommonUtils.getInstance(getActivity().getApplicationContext()).saveGsonToFile(object, "data.json", "data");
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }

                // Avvio l'app
                getActivity().runOnUiThread(() -> {
                    setupList();
                });

            }
        }).start();
    }

    private void setupList() {
        pois.clear();
        pois.addAll(getPoiList(getContext(), "items"));

        // Se ho un filtro, filtro
        if (this.filter != null) {
            Iterator<IlpraItem> it = pois.iterator();
            while (it.hasNext()) {
                IlpraItem item = it.next();
                if (!item.getType().equals(filter)) {
                    it.remove();
                }
            }
        }

        // Ordino i POI per nome
        Collections.sort(pois, (firstElement, secondElement) ->
                firstElement.getName().compareToIgnoreCase(secondElement.getName()));

        // Imposto il layout manager e la recycler view
        recyclerView.setLayoutManager(getRecyclerLayoutManager());
        adapter = new ItemAdapter(getContext(), pois);
        recyclerView.setAdapter(adapter);

        pullToRefresh.setRefreshing(false);
    }
}