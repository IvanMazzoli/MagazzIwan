package me.ivanmazzoli.Models;

import android.content.Context;

import androidx.fragment.app.Fragment;

import java.io.File;

import me.ivanmazzoli.R;
import me.ivanmazzoli.Utils.DrawerManager;

public class SmartFragment extends Fragment {

    // Variabili classe
    public OnFragmentChangeRequest callback;
    private int drawerIcon = R.drawable.ic_android;
    private long drawerID = DrawerManager.DEBUG;
    private String drawerTitle = "DEBUG";
    private String activityTitle = "DEBUG";
    private boolean hideToolbarElevation = false;
    private String query;

    /**
     * Metodo per ottenere l'icona da mostrare nel drawer laterale
     *
     * @return ID Android dell'icona da mostrare
     */
    public int getDrawerIcon() {
        return drawerIcon;
    }

    /**
     * Metodo per impostare l'icona da mostrare nel drawer laterale
     *
     * @param drawerIcon ID Android dell'icona da mostrare
     */
    public void setDrawerIcon(int drawerIcon) {
        this.drawerIcon = drawerIcon;
    }

    /**
     * Metodo per ottenere l'ID del fragment
     *
     * @return ID unico del fragment
     */
    public long getDrawerID() {
        return drawerID;
    }

    /**
     * Metodo per ottenere l'ID del fragment
     *
     * @param drawerID Identificativo fragment
     */
    public void setDrawerID(long drawerID) {
        this.drawerID = drawerID;
    }

    /**
     * Metodo per ottenere il titolo da mostrare nel drawer laterale
     *
     * @return ID Android della stringa da mostrare nel drawer laterale
     */
    public String getDrawerTitle() {
        return drawerTitle;
    }

    /**
     * Metodo per impostare il titolo da mostare nel drawer laterale
     *
     * @param drawerTitle ID Android della stringa da mostrare nel drawer laterale
     */
    public void setDrawerTitle(String drawerTitle) {
        this.drawerTitle = drawerTitle;
    }

    /**
     * Metodo per ottenere il titolo da mostrare nella toolbar
     *
     * @return ID Android della stringa da mostrare
     */
    public String getActivityTitle() {
        return activityTitle;
    }

    /**
     * Metodo per impostare il titolo da mostrare nella toolbar
     *
     * @param activityTitle ID Android della stringa da mostrare
     */
    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    /**
     * Metodo per sapere se l'activity madre deve nascondere l'elevation della toolbar
     * * @return true/false
     */
    public boolean hideToolbarElevation() {
        return hideToolbarElevation;
    }

    /**
     * Metodo utilizzato per dire all'activity madre di nascondere l'elevation della toolbar
     *
     * @param hide se nascondere o meno la toolbar
     */
    public void setHideToolbarElevation(boolean hide) {
        this.hideToolbarElevation = hide;
    }

    /**
     * Metodo per impostare il callback per cambiare fragment
     *
     * @param callback Callback da usare
     */
    public void setOnFragmentChangeRequest(OnFragmentChangeRequest callback) {
        this.callback = callback;
    }

    public String getSearchQuery() {
        return query;
    }

    public void setSearchQuery(String query) {
        this.query = query;
    }

    /**
     * Interfaccia tra Fragment ed Activity per permettere al fragment di cambiare quale fragment viene mostrato
     */
    public interface OnFragmentChangeRequest {
        /**
         * Metodo per cambiare fragment mostrato
         *
         * @param id Identificatore fragment dichiarato in FragmentManager.class
         */
        void onFragmentChangeRequest(long id);
    }

    /**
     * Metodo per eliminare la cache app
     *
     * @param context context app
     */
    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo per cancellare una directory
     *
     * @param dir Cartella da eliminare
     * @return se la cartella è stata eliminata o meno
     */
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public int intToDP(int value) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (value*scale + 0.5f);
    }
}