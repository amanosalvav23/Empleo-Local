package unc.edu.pe.empleolocal.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import unc.edu.pe.empleolocal.data.model.Usuario;
import unc.edu.pe.empleolocal.databinding.ItemReviewBinding;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.ViewHolder> {

    private List<Usuario> usuarios = new ArrayList<>();
    private OnUsuarioClickListener listener;

    public interface OnUsuarioClickListener {
        void onUsuarioClick(Usuario usuario);
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
        notifyDataSetChanged();
    }

    public void setOnUsuarioClickListener(OnUsuarioClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReviewBinding binding = ItemReviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Usuario u = usuarios.get(position);
        holder.bind(u, listener);
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemReviewBinding b;

        public ViewHolder(ItemReviewBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        public void bind(Usuario u, OnUsuarioClickListener listener) {
            b.setUsuario(u);
            // Execute pending bindings to ensure data is applied immediately
            b.executePendingBindings();
            
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onUsuarioClick(u);
            });
        }
    }
}
