package me.ivanmazzoli.UI.Fragments;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jsibbold.zoomage.ZoomageView;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import me.ivanmazzoli.Models.SmartSettingsFragment;
import me.ivanmazzoli.R;
import me.ivanmazzoli.UI.Activities.QrCodeActivity;
import me.ivanmazzoli.Utils.PreferenceHelper;

public class SettingsFragment extends SmartSettingsFragment {

    // Variabili classe
    ActivityResultLauncher<Intent> qrActivityResultLauncher;
    private int PERMISSION_REQUEST_CODE = 100;

    public SettingsFragment() {
    }

    public static SettingsFragment getInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_settings);
        PreferenceHelper ph = PreferenceHelper.getInstance(getActivity());

        qrActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            Gson gson = new Gson();
                            JsonObject object = gson.fromJson(result.getData().getStringExtra("QR_CODE"), JsonObject.class);
                            String api = object.get("apiEndpoint").getAsString();
                            String update = object.get("updateEndpoint").getAsString();
                            String apk = object.get("apkEndpoint").getAsString();
                            if ((api == null || !validateUrl(api)) ||
                                    (update == null || !validateUrl(update)) ||
                                    (apk == null || !validateUrl(apk))) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setMessage("Il codice QR non sembra essere generato per leggere la configurazione del MagazzIwan.\nProva con un codice QR diverso.")
                                        .setTitle("Errore QR Code")
                                        .setPositiveButton("Ok", null).create().show();
                                return;
                            }
                            ph.setDataEndpoint(api);
                            ph.setUpdateEndpoint(update);
                            ph.setApkEndpoint(apk);
                            setPreferenceScreen(null);
                            addPreferencesFromResource(R.xml.app_settings);
                            Toast.makeText(getActivity(), "Configurazione caricata!", Toast.LENGTH_LONG).show();
                        } catch (Exception ignored) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage("Il codice QR non sembra essere generato per leggere la configurazione del MagazzIwan.\nProva con un codice QR diverso.")
                                    .setTitle("Errore QR Code")
                                    .setPositiveButton("Ok", null).create().show();
                            return;
                        }
                    }
                });

        findPreference("apiEndpoint").setOnPreferenceChangeListener((preference, newValue) -> {
            if (!validateUrl(String.valueOf(newValue))) {
                Toast.makeText(getActivity(), "Controlla l'URL e riprova!", Toast.LENGTH_LONG).show();
                return false;
            } else {
                return true;
            }
        });

        findPreference("updateEndpoint").setOnPreferenceChangeListener((preference, newValue) -> {
            if (!validateUrl(String.valueOf(newValue))) {
                Toast.makeText(getActivity(), "Controlla l'URL e riprova!", Toast.LENGTH_LONG).show();
                return false;
            } else {
                return true;
            }
        });

        findPreference("apkEndpoint").setOnPreferenceChangeListener((preference, newValue) -> {
            if (!validateUrl(String.valueOf(newValue))) {
                Toast.makeText(getActivity(), "Controlla l'URL e riprova!", Toast.LENGTH_LONG).show();
                return false;
            } else {
                return true;
            }
        });

        findPreference("resetEndpoint").
                setOnPreferenceClickListener(preference -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Sei sicuro di voler resettare tutti gli endpoint al loro valore di default?")
                            .setTitle("Resetta URL endpoints")
                            .setPositiveButton("Resetta", (dialog, id) -> {
                                ph.setDataEndpoint("https://www.tipsyapp.it/ilpra/api/update.php");
                                ph.setUpdateEndpoint("https://www.tipsyapp.it/ilpra/release/build.txt");
                                ph.setApkEndpoint("https://www.tipsyapp.it/ilpra/release/release.apk");
                                setPreferenceScreen(null);
                                addPreferencesFromResource(R.xml.app_settings);
                            })
                            .setNegativeButton("Annulla", (dialog, id) -> {
                            }).create().show();
                    return false;
                });

        findPreference("loadFromQR").
                setOnPreferenceClickListener(preference -> {
                    if (checkPermission()) {
                        Intent intent = new Intent(getActivity(), QrCodeActivity.class);
                        qrActivityResultLauncher.launch(intent);
                    } else {
                        requestPermission();
                    }
                    return false;
                });

        findPreference("showQR").
                setOnPreferenceClickListener(preference -> {
                    String config = "{\"apiEndpoint\":\"%s\",\"updateEndpoint\":\"%s\",\"apkEndpoint\":\"%s\"}";
                    config = String.format(config, ph.getDataEndpoint(), ph.getUpdateEndpoint(), ph.getApkEndpoint());
                    QRGEncoder qrgEncoder = new QRGEncoder(config, null, QRGContents.Type.TEXT, 512);
                    Bitmap bitmap = qrgEncoder.getBitmap();
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
                    View layout = inflater.inflate(R.layout.dialog_zoom, null);
                    ZoomageView picture = layout.findViewById(R.id.zoomImage);
                    picture.setImageBitmap(bitmap);
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
                    builder.setView(layout);
                    builder.setPositiveButton("Chiudi", (dialog, which) -> {
                    });
                    Dialog dialog = builder.create();
                    dialog.show();
                    return false;
                });

        findPreference("launchScreen").setSummary(
                String.format("Scegli la schermata di default lanciata all'avvio\n\n• Schermata: %s",
                        ph.getFavouriteFragmentName()));
        findPreference("launchScreen").setOnPreferenceChangeListener((preference, newValue) -> {
            preference.setSummary(
                    String.format("Scegli la schermata di default lanciata all'avvio\n\n• Schermata: %s",
                            ph.getFragmentName((String) newValue)));
            return true;
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(Color.WHITE);
    }

    private boolean validateUrl(String url) {
        return Patterns.WEB_URL.matcher(url).matches();
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(getActivity(), QrCodeActivity.class);
                    qrActivityResultLauncher.launch(intent);
                } else {
                    Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("Devi dare l'accesso alla Fotocamera per poter scansionare il codice QR",
                                    (dialog, which) -> {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermission();
                                        }
                                    });
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("Ok", okListener)
                .setNegativeButton("Annulla", null)
                .create()
                .show();
    }
}