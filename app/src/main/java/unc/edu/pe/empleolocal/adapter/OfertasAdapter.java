package unc.edu.pe.empleolocal.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import unc.edu.pe.empleolocal.R;
import unc.edu.pe.empleolocal.data.model.Oferta;
import unc.edu.pe.empleolocal.databinding.ItemOfertaBinding;

public class OfertasAdapter extends RecyclerView.Adapter<OfertasAdapter.ViewHolder> {

    private List<Oferta> ofertas = new ArrayList<>();
    private List<String> savedIds = new ArrayList<>();
    private OnOfertaClickListener listener;

    public interface OnOfertaClickListener {
        void onOfertaClick(Oferta oferta);
        void onSaveClick(Oferta oferta);
        void onAplicarClick(Oferta oferta);
    }

    public void setOfertas(List<Oferta> ofertas) {
        this.ofertas = ofertas;
        notifyDataSetChanged();
    }

    public void setSavedIds(List<String> savedIds) {
        this.savedIds = savedIds;
        notifyDataSetChanged();
    }

    public void setOnOfertaClickListener(OnOfertaClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOfertaBinding binding = ItemOfertaBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Oferta o = ofertas.get(position);
        boolean isSaved = savedIds != null && savedIds.contains(o.getId());
        holder.bind(o, isSaved, listener);
    }

    @Override
    public int getItemCount() {
        return ofertas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemOfertaBinding b;

        public ViewHolder(ItemOfertaBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        public void bind(Oferta o, boolean isSaved, OnOfertaClickListener listener) {
            b.tvJobTitle.setText(o.getTitulo());
            b.tvCompanyName.setText(o.getEmpresa());
            b.tvAddress.setText(o.getDireccion() != null ? o.getDireccion() : "");
            b.tvSalary.setText("S/. " + String.format(Locale.getDefault(), "%,.0f", o.getSalario()) + "/mes");
            
            Glide.with(itemView.getContext())
                    .load(o.getLogoUrl())
                    .placeholder(R.drawable.ic_briefcase)
                    .into(b.ivCompanyLogo);

            b.ivSaveItem.setImageResource(isSaved ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_outline);
            b.ivSaveItem.setColorFilter(isSaved ? Color.BLACK : Color.GRAY);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onOfertaClick(o);
            });

            b.ivSaveItem.setOnClickListener(v -> {
                if (listener != null) listener.onSaveClick(o);
            });

            b.btnAplicar.setOnClickListener(v -> {
                if (listener != null) listener.onAplicarClick(o);
            });
        }
    }
}
