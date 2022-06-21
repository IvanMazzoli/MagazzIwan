package me.ivanmazzoli.UI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

public class PdfActivity extends AppCompatActivity {

    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.txtProgress) TextView progressText;
    @BindView(R.id.pdfView) PDFView pdfView;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
        ButterKnife.bind(this);

        // Sostituisco l'Action Bar con una Toolbar
        setSupportActionBar(toolbar);

        // Rinomino la toolbar ed aggiungo il back button
        Objects.requireNonNull(getSupportActionBar()).setTitle("Documentazione articolo");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String fileName = getIntent().getStringExtra("docName") + ".pdf";
        String dirPath = getApplicationContext().getFilesDir().getAbsolutePath();
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setReadTimeout(30_000)
                .setConnectTimeout(30_000)
                .build();

        url = getIntent().getStringExtra("docUrl");

        PRDownloader.initialize(getApplicationContext(), config);
        PRDownloader.download(url, dirPath, fileName).build()
                .setOnProgressListener(progress -> {
                    long progressPercent = progress.currentBytes * 100 / progress.totalBytes;
                    progressBar.setProgress((int) progressPercent);
                    progressText.setText(getProgressDisplayLine(progress.currentBytes, progress.totalBytes));
                    progressBar.setIndeterminate(false);
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        File downloadedFile = new File(dirPath, fileName);
                        progressBar.setVisibility(View.GONE);
                        progressText.setVisibility(View.GONE);
                        pdfView.fromFile(downloadedFile)
                                .password(null)
                                .defaultPage(0)
                                .enableSwipe(true)
                                .swipeHorizontal(false)
                                .enableDoubletap(true)
                                .onPageError((page, t) -> Toast.makeText(PdfActivity.this, "Errore alla pagina $page", Toast.LENGTH_LONG).show()).load();
                    }

                    @Override
                    public void onError(Error error) {
                        Toast.makeText(PdfActivity.this, "Errore nell'apertura o download del file", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }

    public String getProgressDisplayLine(long currentBytes, long totalBytes) {
        return getBytesToMBString(currentBytes) + "/" + getBytesToMBString(totalBytes);
    }

    private String getBytesToMBString(long bytes) {
        return String.format(Locale.ENGLISH, "%.2fMb", bytes / (1024.00 * 1024.00));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.open_externally, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // Tasto back
            case android.R.id.home:
                this.onBackPressed();
                return true;

            case R.id.action_open_external:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
