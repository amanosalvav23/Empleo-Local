package unc.edu.pe.empleolocal.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import unc.edu.pe.empleolocal.R;
import unc.edu.pe.empleolocal.data.model.Notificacion;
import unc.edu.pe.empleolocal.databinding.ItemNotificacionBinding;

public class NotificacionesAdapter extends RecyclerView.Adapter<NotificacionesAdapter.ViewHolder> {

    private List<Notificacion> notificaciones = new ArrayList<>();
    private OnNotificacionClickListener listener;

    public interface OnNotificacionClickListener {
        void onNotificacionClick(Notificacion notificacion);
    }

    public void setNotificaciones(List<Notificacion> notificaciones) {
        this.notificaciones = notificaciones;
        notifyDataSetChanged();
    }

    public void setOnNotificacionClickListener(OnNotificacionClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificacionBinding binding = ItemNotificacionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notificacion n = notificaciones.get(position);
        holder.bind(n, listener);
    }

    @Override
    public int getItemCount() {
        return notificaciones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificacionBinding b;

        public ViewHolder(ItemNotificacionBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        public void bind(Notificacion n, OnNotificacionClickListener listener) {
            b.setNotificacion(n);
            
            if ("PROXIMIDAD".equals(n.getTipo())) {
                b.ivNotifIcon.setImageResource(R.drawable.ic_location);
            } else {
                b.ivNotifIcon.setImageResource(R.drawable.ic_briefcase);
            }

            b.cardNotif.setOnClickListener(v -> {
                if (listener != null) listener.onNotificacionClick(n);
            });
        }
    }
}
