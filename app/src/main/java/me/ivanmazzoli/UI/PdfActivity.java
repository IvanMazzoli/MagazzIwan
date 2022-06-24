package me.ivanmazzoli.UI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.ivanmazzoli.R;

@SuppressLint("NonConstantResourceId")
public class PdfActivity extends AppCompatActivity {

    // View classe
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.txtProgress) TextView downloadProgressText;
    @BindView(R.id.txtCurrentPage) EditText currentPage;
    @BindView(R.id.txtTotalPages) TextView totalPages;
    @BindView(R.id.pdfControls) View pdfControls;
    @BindView(R.id.pdfView) PDFView pdfView;
    @BindView(R.id.toolbar) Toolbar toolbar;

    // Variabili classe
    private String pdfUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
        ButterKnife.bind(this);

        // Mostro solo in portait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Sostituisco l'Action Bar con una Toolbar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0F);

        // Rinomino la toolbar ed aggiungo il back button
        Objects.requireNonNull(getSupportActionBar()).setTitle("Documentazione articolo");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Imposto il select all su focus dell'EditText e listener su focus perso
        currentPage.setSelectAllOnFocus(true);
        currentPage.setOnFocusChangeListener((v, hasFocus) -> {
            // Nascondo la tastiera
            InputMethodManager inputManager = (InputMethodManager) PdfActivity.this
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        });

        // Ottengo dall'intent il nome file per il PDF
        String fileName = getIntent().getStringExtra("docName") + ".pdf";
        String dirPath = getApplicationContext().getFilesDir().getAbsolutePath();

        // Controllo se già esiste, in tal caso mostro da file
        File file = new File(dirPath, fileName);
        if (file.exists()) {
            displayPdfFromFile(file);
            return;
        }

        // Creo un PRDownloader per scaricare il PDF
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder().setReadTimeout(30_000)
                .setConnectTimeout(30_000).build();
        PRDownloader.initialize(getApplicationContext(), config);

        // Ottengo l'url di download del PDF
        pdfUrl = getIntent().getStringExtra("docUrl");

        // Avvio il download del file PDF
        PRDownloader.download(pdfUrl, dirPath, fileName).build()
                .setOnProgressListener(progress -> {
                    // Calcolo il progresso del download e lo mostro
                    long progressPercent = progress.currentBytes * 100 / progress.totalBytes;
                    progressBar.setProgress((int) progressPercent);
                    downloadProgressText.setText(getProgressDisplayLine(progress.currentBytes, progress.totalBytes));
                    progressBar.setIndeterminate(false);
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        // Mostro il PDF appena scaricato
                        displayPdfFromFile(new File(dirPath, fileName));
                    }

                    @Override
                    public void onError(Error error) {
                        // Mostro il messaggio di errore
                        new AlertDialog.Builder(PdfActivity.this)
                                .setTitle("Errore durante il download")
                                .setMessage(error.getConnectionException().getMessage())
                                .setPositiveButton("OK", (dialog, which) -> finish())
                                .create().show();
                    }
                });
    }

    /**
     * Metodo per impostare il menù sulla toolbar
     *
     * @param menu Menu da impostare
     * @return true se il menu viene impostato
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.open_externally, menu);
        return true;
    }

    /**
     * Metodo per gestire i click sul menu toolbar
     *
     * @param item elemento cliccato
     * @return se l'evento è stato consumato o meno
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Tasto back
            case android.R.id.home:
                this.onBackPressed();
                return true;
            // Tasto per aprire con Chrome
            case R.id.action_open_external:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(pdfUrl));
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Metodo per mostrare un file PDF in una view da un file
     *
     * @param pdf File pdf da mostrare
     */
    public void displayPdfFromFile(File pdf) {

        // Mostro/nascondo le view necessarie
        progressBar.setVisibility(View.GONE);
        downloadProgressText.setVisibility(View.GONE);
        pdfControls.setVisibility(View.VISIBLE);

        // Apro e mostro il PDF
        pdfView.fromFile(pdf)
                .password(null)
                .defaultPage(0)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .onPageChange((page, pageCount) -> {
                    currentPage.setText(String.valueOf(page + 1));
                    totalPages.setText("/" + pageCount);
                })
                .onPageError((page, t) -> Toast.makeText(PdfActivity.this, "Errore alla pagina $page", Toast.LENGTH_LONG).show())
                .load();

        // Aggiungo un listener per il fine edit del numero di pagina corrente
        currentPage.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event != null &&
                            event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (event == null || !event.isShiftPressed()) {
                    // Provo a cambiare pagina (sanitize input)
                    try {
                        jumpToPage(Integer.parseInt(view.getText().toString()) - 1);
                    } catch (Exception ignored) {
                        view.setText(String.valueOf(pdfView.getCurrentPage() + 1));
                    }
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Metodo per andare ad una pagina specifica del file PDF
     * Rimuove inoltre il focus dalla view di input pagina, chiudendo la tastiera
     *
     * @param pageNo numero di pagina (0 > max) verso la quale spostarsi
     */
    public void jumpToPage(int pageNo) {
        pdfView.jumpTo(pageNo);
        currentPage.clearFocus();
    }

    /**
     * Metodo per ottenere il numero di megabyte scaricati sul totale
     *
     * @param currentBytes bytes salvati correntemente
     * @param totalBytes   byte totali del file
     * @return numero di megabyte scaricati sul totale
     */
    public String getProgressDisplayLine(long currentBytes, long totalBytes) {
        return getBytesToMBString(currentBytes) + "/" + getBytesToMBString(totalBytes);
    }

    /**
     * Metodo per formattare in megabyte lo stato del file
     *
     * @param bytes byte da convertire
     * @return stringa formattata con dati in megabyte
     */
    private String getBytesToMBString(long bytes) {
        return String.format(Locale.ENGLISH, "%.2fMb", bytes / (1024.00 * 1024.00));
    }


}
