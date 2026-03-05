package unc.edu.pe.empleolocal.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "postulaciones")
public class Postulacion implements Serializable {
    @PrimaryKey
    @NonNull
    private String id;
    private String usuarioId;
    private String ofertaId;
    private String tituloOferta;
    private String empresaOferta;
    private String logoUrl;
    private String direccionOferta;
    private double salarioOferta;
    private long fechaPostulacion;
    private String estado; // "Postulado", "En Revisión", "Entrevista", "Final", "Rechazada"

    public Postulacion() {
        this.id = ""; // Default empty string for Room PrimaryKey
    }

    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getOfertaId() { return ofertaId; }
    public void setOfertaId(String ofertaId) { this.ofertaId = ofertaId; }

    public String getTituloOferta() { return tituloOferta; }
    public void setTituloOferta(String tituloOferta) { this.tituloOferta = tituloOferta; }

    public String getEmpresaOferta() { return empresaOferta; }
    public void setEmpresaOferta(String empresaOferta) { this.empresaOferta = empresaOferta; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getDireccionOferta() { return direccionOferta; }
    public void setDireccionOferta(String direccionOferta) { this.direccionOferta = direccionOferta; }

    public double getSalarioOferta() { return salarioOferta; }
    public void setSalarioOferta(double salarioOferta) { this.salarioOferta = salarioOferta; }

    public long getFechaPostulacion() { return fechaPostulacion; }
    public void setFechaPostulacion(long fechaPostulacion) { this.fechaPostulacion = fechaPostulacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
