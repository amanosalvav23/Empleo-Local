package unc.edu.pe.empleolocal.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import unc.edu.pe.empleolocal.R;
import unc.edu.pe.empleolocal.data.model.Postulacion;
import unc.edu.pe.empleolocal.databinding.ItemPostulacionRevisionBinding;

public class PostulacionesAdapter extends RecyclerView.Adapter<PostulacionesAdapter.ViewHolder> {

    private List<Postulacion> postulaciones = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onVerDetalle(Postulacion postulacion);
    }

    public void setPostulaciones(List<Postulacion> postulaciones) {
        this.postulaciones = postulaciones;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostulacionRevisionBinding binding = ItemPostulacionRevisionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Postulacion p = postulaciones.get(position);
        holder.bind(p, listener);
    }

    @Override
    public int getItemCount() {
        return postulaciones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPostulacionRevisionBinding b;

        public ViewHolder(ItemPostulacionRevisionBinding binding) {
            super(binding.getRoot());
            this.b = binding;
        }

        public void bind(Postulacion p, OnItemClickListener listener) {
            b.setPostulacion(p);
            
            Glide.with(itemView.getContext())
                    .load(p.getLogoUrl())
                    .placeholder(R.drawable.ic_briefcase)
                    .into(b.ivLogo);

            String estado = p.getEstado() != null ? p.getEstado() : "Postulado";
            String estadoNormal = normalize(estado);
            
            resetSteps();
            
            // Lógica robusta para detectar estados (con o sin tildes)
            if (estadoNormal.contains("postulado")) {
                setStatusStyle(R.drawable.bg_status_blue, R.color.login_blue_text);
                highlightStep(b.dotApplied, b.tvStepApplied, null);
            } 
            else if (estadoNormal.contains("revision")) {
                setStatusStyle(R.drawable.bg_status_blue, R.color.login_blue_text);
                highlightStep(b.dotApplied, b.tvStepApplied, b.progressLine1);
                highlightStep(b.dotReview, b.tvStepReview, null);
            } 
            else if (estadoNormal.contains("entrevista")) {
                setStatusStyle(R.drawable.bg_status_green, android.R.color.white);
                highlightStep(b.dotApplied, b.tvStepApplied, b.progressLine1);
                highlightStep(b.dotReview, b.tvStepReview, b.progressLine2);
                highlightStep(b.dotInterview, b.tvStepInterview, null);
            } 
            else if (estadoNormal.contains("final") || estadoNormal.contains("completo")) {
                setStatusStyle(R.drawable.bg_status_green, android.R.color.white);
                highlightStep(b.dotApplied, b.tvStepApplied, b.progressLine1);
                highlightStep(b.dotReview, b.tvStepReview, b.progressLine2);
                highlightStep(b.dotInterview, b.tvStepInterview, null);
                highlightStep(b.dotFinal, b.tvStepFinal, null);
            } 
            else if (estadoNormal.contains("rechaz")) {
                setStatusStyle(R.drawable.bg_status_red, android.R.color.white);
                b.llSteps.setVisibility(View.GONE);
            }

            b.btnVerDetalle.setOnClickListener(v -> {
                if (listener != null) listener.onVerDetalle(p);
            });
            
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onVerDetalle(p);
            });
        }

        private String normalize(String input) {
            if (input == null) return "";
            return Normalizer.normalize(input.toLowerCase(), Normalizer.Form.NFD)
                    .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        }

        private void resetSteps() {
            b.llSteps.setVisibility(View.VISIBLE);
            int gray = ContextCompat.getColor(itemView.getContext(), R.color.login_subtitle);
            int lightGray = Color.parseColor("#E0E0E0");
            
            b.dotApplied.setBackgroundResource(R.drawable.bg_dot_gray);
            b.dotReview.setBackgroundResource(R.drawable.bg_dot_gray);
            b.dotInterview.setBackgroundResource(R.drawable.bg_dot_gray);
            b.dotFinal.setBackgroundResource(R.drawable.bg_dot_gray);
            
            b.tvStepApplied.setTextColor(gray);
            b.tvStepReview.setTextColor(gray);
            b.tvStepInterview.setTextColor(gray);
            b.tvStepFinal.setTextColor(gray);
            
            b.progressLine1.setBackgroundColor(lightGray);
            b.progressLine2.setBackgroundColor(lightGray);
        }

        private void highlightStep(View dot, android.widget.TextView text, View line) {
            dot.setBackgroundResource(R.drawable.bg_dot_blue);
            text.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.login_blue_text));
            if (line != null) {
                line.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.login_blue_text));
            }
        }

        private void setStatusStyle(int bgRes, int textColorRes) {
            b.tvStatusBadge.setBackgroundResource(bgRes);
            b.tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), textColorRes));
        }
    }
}
