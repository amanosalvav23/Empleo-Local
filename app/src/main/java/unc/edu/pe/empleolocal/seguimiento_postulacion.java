package unc.edu.pe.empleolocal;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import java.text.Normalizer;

import unc.edu.pe.empleolocal.data.model.Postulacion;
import unc.edu.pe.empleolocal.databinding.ActivitySeguimientoPostulacionBinding;

public class seguimiento_postulacion extends AppCompatActivity {

    private ActivitySeguimientoPostulacionBinding binding;
    private Postulacion postulacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySeguimientoPostulacionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        postulacion = (Postulacion) getIntent().getSerializableExtra("postulacion");

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (postulacion != null) {
            binding.setPostulacion(postulacion);
            setupUI();
        }

        binding.btnWithdraw.setOnClickListener(v -> finish());
    }

    private void setupUI() {
        // Cargar logo
        Glide.with(this)
                .load(postulacion.getLogoUrl())
                .placeholder(R.drawable.ic_briefcase)
                .into(binding.ivCompanyLogo);

        // Configurar timeline vertical según el estado
        updateVerticalTimeline(postulacion.getEstado());
    }

    private void updateVerticalTimeline(String estado) {
        String e = normalize(estado);
        int blue = ContextCompat.getColor(this, R.color.login_blue_text);
        int gray = Color.parseColor("#E0E0E0");
        int darkGray = ContextCompat.getColor(this, R.color.login_subtitle);

        // Reset todos los pasos a gris
        setStepState(binding.dot1, binding.line1, binding.tvStatus1, binding.tvDesc1, gray, darkGray);
        setStepState(binding.dot2, binding.line2, binding.tvStatus2, binding.tvDesc2, gray, darkGray);
        setStepState(binding.dot3, binding.line3, binding.tvStatus3, binding.tvDesc3, gray, darkGray);
        setStepState(binding.dot4, null, binding.tvStatus4, binding.tvDesc4, gray, darkGray);

        if (e.contains("postulado")) {
            setStepState(binding.dot1, binding.line1, binding.tvStatus1, binding.tvDesc1, blue, blue);
            binding.tvStatusMessage.setText("Tu CV ha sido recibido. El reclutador lo revisará pronto.");
        } else if (e.contains("revision")) {
            setStepState(binding.dot1, binding.line1, binding.tvStatus1, binding.tvDesc1, blue, blue);
            setStepState(binding.dot2, binding.line2, binding.tvStatus2, binding.tvDesc2, blue, blue);
            binding.tvStatusMessage.setText("Tu perfil está siendo evaluado por el equipo de selección.");
        } else if (e.contains("entrevista")) {
            setStepState(binding.dot1, binding.line1, binding.tvStatus1, binding.tvDesc1, blue, blue);
            setStepState(binding.dot2, binding.line2, binding.tvStatus2, binding.tvDesc2, blue, blue);
            setStepState(binding.dot3, binding.line3, binding.tvStatus3, binding.tvDesc3, blue, blue);
            binding.tvStatusMessage.setText("¡Felicidades! Has sido seleccionado para una entrevista presencial.");
        } else if (e.contains("final") || e.contains("rechaz")) {
            setStepState(binding.dot1, binding.line1, binding.tvStatus1, binding.tvDesc1, blue, blue);
            setStepState(binding.dot2, binding.line2, binding.tvStatus2, binding.tvDesc2, blue, blue);
            setStepState(binding.dot3, binding.line3, binding.tvStatus3, binding.tvDesc3, blue, blue);
            setStepState(binding.dot4, null, binding.tvStatus4, binding.tvDesc4, blue, blue);
            
            if (e.contains("rechaz")) {
                binding.tvStatus4.setText("Postulación no seleccionada");
                binding.dot4.setColorFilter(Color.RED);
                binding.tvStatusMessage.setText("Gracias por participar. En esta ocasión no hemos podido avanzar con tu perfil.");
            } else {
                binding.tvStatusMessage.setText("El proceso ha finalizado. ¡Gracias por tu interés!");
            }
        }
    }

    private void setStepState(View dot, View line, View title, View desc, int dotColor, int textColor) {
        ((android.widget.ImageView) dot).setColorFilter(dotColor);
        if (line != null) line.setBackgroundColor(dotColor);
        ((android.widget.TextView) title).setTextColor(textColor);
        ((android.widget.TextView) desc).setTextColor(textColor);
    }

    private String normalize(String input) {
        if (input == null) return "";
        return Normalizer.normalize(input.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
