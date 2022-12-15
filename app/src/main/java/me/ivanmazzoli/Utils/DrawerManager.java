package me.ivanmazzoli.Utils;

import android.content.Context;

import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.List;

import me.ivanmazzoli.Models.SmartFragment;
import me.ivanmazzoli.Models.SmartSettingsFragment;
import me.ivanmazzoli.R;
import me.ivanmazzoli.UI.Fragments.AboutFragment;
import me.ivanmazzoli.UI.Fragments.ListFragment;
import me.ivanmazzoli.UI.Fragments.SettingsFragment;

public class DrawerManager {

    // Variabili classe
    private static DrawerManager instance;
    private final ArrayList<Object> drawerFragments;
    private final ArrayList<IDrawerItem> drawerItems;
    private final Context context;

    private DrawerManager(Context context) {

        // Salvo il context
        this.context = context;

        // Creo le liste filtrate e non
        SmartFragment fullList = ListFragment.getInstance(DrawerManager.LIST_FULL);
        SmartFragment plcList = ListFragment.getInstance(DrawerManager.LIST_PLC);
        SmartFragment cableList = ListFragment.getInstance(DrawerManager.LIST_CABLE);
        SmartFragment thermalList = ListFragment.getInstance(DrawerManager.LIST_THERMAL);
        SmartFragment driveList = ListFragment.getInstance(DrawerManager.LIST_DRIVE);
        SmartFragment buttonList = ListFragment.getInstance(DrawerManager.LIST_BUTTON);
        SmartFragment connectorList = ListFragment.getInstance(DrawerManager.LIST_CONNECTOR);
        SmartFragment consumablesList = ListFragment.getInstance(DrawerManager.LIST_CONSUMABLES);
        SmartSettingsFragment settings = new SettingsFragment();

        // Creo gli oggetti del Drawer laterale
        drawerItems = new ArrayList<>();
        drawerItems.add(getItemFromFragment(fullList));
        drawerItems.add(new DividerDrawerItem());
        drawerItems.add(getItemFromFragment(driveList));
        drawerItems.add(getItemFromFragment(cableList));
        drawerItems.add(getItemFromFragment(connectorList));
        drawerItems.add(getItemFromFragment(consumablesList));
        drawerItems.add(getItemFromFragment(plcList));
        drawerItems.add(getItemFromFragment(buttonList));
        drawerItems.add(getItemFromFragment(thermalList));
        drawerItems.add(new DividerDrawerItem());
        drawerItems.add(getItemFromFragment(settings));
        drawerItems.add(getItemFromFragment(AboutFragment.getInstance()));

        // Creo la lista dei fragment utilizzati nel Drawer
        drawerFragments = new ArrayList<>();
        drawerFragments.add(fullList);
        drawerFragments.add(plcList);
        drawerFragments.add(cableList);
        drawerFragments.add(thermalList);
        drawerFragments.add(driveList);
        drawerFragments.add(buttonList);
        drawerFragments.add(connectorList);
        drawerFragments.add(AboutFragment.getInstance());
        drawerFragments.add(settings);
        drawerFragments.add(consumablesList);
    }

    /**
     * Metodo per ottenere un'instanza del DrawerManager
     *
     * @return l'istanza del DrawerManager se esistente, altrimenti una nuova istanza
     */
    public static DrawerManager getInstance(Context context) {
        if (instance == null) {
            instance = new DrawerManager(context);
        }
        return instance;
    }

    /**
     * Metodo per ottenere una nuova istanza del DrawerManager
     */
    public static DrawerManager getNewInstance(Context context) {
        instance = new DrawerManager(context);
        return instance;
    }

    /**
     * Metodo per ottenere la lista degli elementi da utilizzare nel Drawer laterale
     *
     * @return ArrayList<IDrawerItem> Lista degli elementi da visualizzare
     */
    public List<IDrawerItem> getDrawerItems() {
        return this.drawerItems;
    }

    /**
     * Metodo per ottenere un Drawer Item da un Fragment
     *
     * @param fragment Il fragment di partenza
     * @return IDrawerItem con le info dell'oggetto base
     */
    private IDrawerItem getItemFromFragment(SmartFragment fragment) {
        PrimaryDrawerItem result = new PrimaryDrawerItem();
        result.withIdentifier(fragment.getDrawerID());
        result.withName(fragment.getDrawerTitle());
        result.withIcon(fragment.getDrawerIcon());
        result.withSelectedIconColor(context.getResources().getColor(R.color.accent));
        result.withSelectedTextColor(context.getResources().getColor(R.color.accent));
        result.withIconTintingEnabled(true);
        return result;
    }

    /**
     * Metodo per ottenere un TipsyFragment dal suo identificativo
     *
     * @param id Identificativo del Fragment
     * @return Il fragment corrispondente all'identificativo, se esistente, altrimenti null
     */
    public Object getFragmentFromID(long id) {

        for (Object tipsyFragment : drawerFragments) {
            if (tipsyFragment instanceof SmartFragment) {
                SmartFragment fragment = (SmartFragment) tipsyFragment;
                if (fragment.getDrawerID() == id)
                    return tipsyFragment;
            } else {
                SmartSettingsFragment fragment = (SmartSettingsFragment) tipsyFragment;
                if (fragment.getDrawerID() == id)
                    return tipsyFragment;
            }
        }
        return null;
    }

    public void updateFragmentSearch(SmartFragment listFragment, String query) {
        for (Object tipsyFragment : drawerFragments) {
            if (tipsyFragment instanceof SmartFragment) {
                SmartFragment fragment = (SmartFragment) tipsyFragment;
                if (fragment.getDrawerID() == listFragment.getDrawerID())
                    fragment.setSearchQuery(query);
            }
        }
    }

    public void resetSearchQueries() {
        for (Object myFragment : drawerFragments)
            if (myFragment instanceof SmartFragment)
                ((SmartFragment) myFragment).setSearchQuery(null);
    }

    /**
     * Metodo per ottenere un Drawer Item da un Fragment
     *
     * @param fragment Il fragment di partenza
     * @return IDrawerItem con le info dell'oggetto base
     */
    private IDrawerItem getItemFromFragment(SmartSettingsFragment fragment) {
        PrimaryDrawerItem result = new PrimaryDrawerItem();
        result.withIdentifier(fragment.getDrawerID());
        result.withName(fragment.getDrawerTitle());
        result.withIcon(fragment.getDrawerIcon());
        result.withSelectedIconColor(context.getResources().getColor(R.color.accent));
        result.withSelectedTextColor(context.getResources().getColor(R.color.accent));
        result.withIconTintingEnabled(true);
        return result;
    }

    // Valori drawer ID
    public static long DEBUG = 0;
    public static long LIST_FULL = 1;
    public static long ABOUT = 2;
    public static long LIST_PLC = 3;
    public static long LIST_CABLE = 4;
    public static long LIST_THERMAL = 5;
    public static long LIST_DRIVE = 6;
    public static long LIST_BUTTON = 7;
    public static long LIST_CONNECTOR = 8;
    public static long SETTINGS = 9;
    public static long LIST_CONSUMABLES = 10;
}