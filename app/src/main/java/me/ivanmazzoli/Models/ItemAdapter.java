package me.ivanmazzoli.Models;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jsibbold.zoomage.ZoomageView;

import java.util.ArrayList;
import java.util.List;

import me.ivanmazzoli.R;
import me.ivanmazzoli.UI.Activities.PdfActivity;
import me.xdrop.fuzzywuzzy.FuzzySearch;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    // Variabili classe
    private final Context context;
    private final List<IlpraItem> poiList;
    private final List<IlpraItem> poiListCopy;

    /**
     * Costruttore per l'adapter dei POI
     *
     * @param items   lista dei POI
     * @param context App context
     */
    public ItemAdapter(Context context, List<IlpraItem> items) {
        this.poiList = new ArrayList<>(items);
        this.poiListCopy = new ArrayList<>(items);
        this.context = context;
    }

    /**
     * Metodo per ottenere la view legata al POI
     *
     * @param parent   parent della view
     * @param viewType tipo di view
     * @return ItemViewHolder con la view richiesta
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view, position -> {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.dialog_zoom, null);
            ZoomageView picture = layout.findViewById(R.id.zoomImage);
            Glide.with(picture).load(poiList.get(position).getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .placeholder(R.drawable.ic_android)
                    .error(R.drawable.ic_android)
                    .dontAnimate()
                    .into(picture);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(layout);

            if (poiList.get(position).getDocs() != null)
                builder.setPositiveButton("Apri Documentazione", (dialog, which) -> {
                    Intent docs = new Intent(context, PdfActivity.class);
                    docs.putExtra("docUrl", poiList.get(position).getDocs());
                    docs.putExtra("docName", poiList.get(position).getMakeCode());
                    context.startActivity(docs);
                });

            Dialog dialog = builder.create();
            dialog.show();
        });
    }

    /**
     * Metodo per popolare la view con le info
     *
     * @param holder   View del POI
     * @param position posizione del POI nella lista
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        // Ottengo il POI e popolo la view
        IlpraItem poi = poiList.get(position);
        holder.name.setText(poi.getName());
        holder.makerCode.setText(poi.getMakeCode());
        holder.maker.setText(poi.getBrand());
        holder.ilpraInfo.setText(poi.getIlpraInfo());

        Glide.with(holder.picture).load(poi.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .placeholder(R.drawable.ic_android)
                .error(R.drawable.ic_android)
                .dontAnimate()
                .into(holder.picture);

        if (poi.getDocs() != null)
            holder.docs.setVisibility(View.VISIBLE);
        else
            holder.docs.setVisibility(View.GONE);
    }

    /**
     * Metodo per ottenere la dimensione della lista POI
     *
     * @return la dimensione della lista POI
     */
    @Override
    public int getItemCount() {
        return poiList.size();
    }

    /**
     * Metodo per filtrare i POI
     *
     * @param text POI da cercare
     */
    public void filter(String text) {
        if (text.isEmpty()) {
            poiList.clear();
            poiList.addAll(poiListCopy);
        } else {
            ArrayList<IlpraItem> result = new ArrayList<>();
            text = text.toLowerCase();
            for (IlpraItem item : poiListCopy) {
                if (FuzzySearch.partialRatio(item.getName().toLowerCase(), text.toLowerCase()) >= 75
                        || FuzzySearch.partialRatio(item.getMakeCode().toLowerCase(), text.toLowerCase()) >= 90
                        || FuzzySearch.partialRatio(item.getIlpraCode().toLowerCase(), text.toLowerCase()) >= 95) {
                    result.add(item);
                }
            }
            poiList.clear();
            poiList.addAll(result);
        }

        notifyDataSetChanged();
    }

    /**
     * Metodo chiamato quando una view viene scollegata dalla finestra
     *
     * @param holder ViewHolder della view scollegata
     */
    @Override
    public void onViewDetachedFromWindow(final ViewHolder holder) {
    }

    /**
     * Metodo chiamato quando una view viene collegata alla finestra
     *
     * @param holder ViewHolder della view collegata
     */
    @Override
    public void onViewAttachedToWindow(final ViewHolder holder) {
    }

    /**
     * Classe contenente le proprietà della view del POI
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // View del ItemViewHolder
        public final View view;
        final TextView name;
        final TextView makerCode;
        final TextView maker;
        final TextView ilpraInfo;
        final ImageView picture;
        final ImageView docs;

        ViewHolderClicks listener;


        /**
         * Costruttore classe ItemViewHolder
         *
         * @param view             View da gestire col VH
         * @param viewHolderClicks Interface del listener dei click
         */
        public ViewHolder(View view, ViewHolderClicks viewHolderClicks) {
            super(view);
            this.view = view;
            this.listener = viewHolderClicks;

            this.name = view.findViewById(R.id.productName);
            this.makerCode = view.findViewById(R.id.makerCode);
            this.maker = view.findViewById(R.id.maker);
            this.ilpraInfo = view.findViewById(R.id.ilpraInfo);
            this.picture = view.findViewById(R.id.itemPic);
            this.docs = view.findViewById(R.id.imgDocs);

            this.picture.setOnClickListener(this);
        }

        /**
         * Metodo per gestire il click di una view del ItemViewHolder
         *
         * @param v View cliccata
         */
        @Override
        public void onClick(View v) {
            listener.onViewClick(getAdapterPosition());
        }

        /**
         * Interfaccia per gestire i click della
         */
        public interface ViewHolderClicks {
            void onViewClick(int position);
        }
    }
}