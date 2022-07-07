package me.ivanmazzoli.UI.Fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import de.cketti.library.changelog.ChangeLog;
import me.ivanmazzoli.R;
import me.ivanmazzoli.Models.SmartFragment;
import me.ivanmazzoli.Utils.DrawerManager;

public class AboutFragment extends SmartFragment {

    // Variabili classe
    private static AboutFragment instance;

    public AboutFragment() {
        this.setDrawerID(DrawerManager.ABOUT);
        this.setDrawerTitle("About");
        this.setDrawerIcon(R.drawable.ic_help);
        this.setActivityTitle("About");
    }

    public static AboutFragment getInstance() {
        if (instance == null)
            instance = new AboutFragment();
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        TextView txtVersion = view.findViewById(R.id.txtNameVersionBuild);

        // Ottengo numero di build
        int currentBuild = -1;
        String versionName = "n/a";
        try {
            currentBuild = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionCode;
            versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
        }

        txtVersion.setText("Ver. " + versionName + " (build " + String.valueOf(currentBuild) + ")");

        view.findViewById(R.id.btnShare).setOnClickListener(view1 -> {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Scarica MagazzIwan da qui: https://bit.ly/39PK6FV");
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, "Condividi con:"));
        });

        view.findViewById(R.id.btnChangelog).setOnClickListener(v -> new ChangeLog(getContext()).getFullLogDialog().show());

        return view;
    }
}