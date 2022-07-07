package me.ivanmazzoli.Models;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import me.ivanmazzoli.R;
import me.ivanmazzoli.Utils.DrawerManager;

public class SmartSettingsFragment extends PreferenceFragmentCompat {

    /**
     * Metodo per ottenere l'icona da mostrare nel drawer laterale
     *
     * @return ID Android dell'icona da mostrare
     */
    public int getDrawerIcon() {
        return R.drawable.ic_settings;
    }

    /**
     * Metodo per ottenere l'ID del fragment
     *
     * @return ID unico del fragment
     */
    public long getDrawerID() {
        return DrawerManager.SETTINGS;
    }

    /**
     * Metodo per ottenere il titolo da mostrare nel drawer laterale
     *
     * @return ID Android della stringa da mostrare nel drawer laterale
     */
    public String getDrawerTitle() {
        return "Impostazioni";
    }

    /**
     * Metodo per ottenere il titolo da mostrare nella toolbar
     *
     * @return ID Android della stringa da mostrare
     */
    public String getActivityTitle() {
        return "Impostazioni";
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }
}