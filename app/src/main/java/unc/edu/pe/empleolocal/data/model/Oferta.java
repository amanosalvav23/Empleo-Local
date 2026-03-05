package unc.edu.pe.empleolocal.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "ofertas")
public class Oferta implements Serializable {
    @PrimaryKey
    @NonNull
    private String id;
    private String titulo;
    private String empresa;
    private String logoUrl;
    private String direccion;
    private double latitud;
    private double longitud;
    private double salario;
    private String tipoContrato;
    private String modalidad; // Presencial, Remoto, Híbrido
    private String descripcion;
    private String responsabilidades;
    private String beneficios;
    private String sector;
    private long fechaPublicacion;
    private boolean verificado;

    // Campos de Empresa para el Perfil
    private String ruc;
    private String tamanoEmpresa;
    private String sedePrincipal;
    private float puntuacion;
    private int numResenas;
    private String anioFundacion;

    public Oferta() {}

    @NonNull public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
    public double getSalario() { return salario; }
    public void setSalario(double salario) { this.salario = salario; }
    public String getTipoContrato() { return tipoContrato; }
    public void setTipoContrato(String tipoContrato) { this.tipoContrato = tipoContrato; }
    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getResponsabilidades() { return responsabilidades; }
    public void setResponsabilidades(String responsabilidades) { this.responsabilidades = responsabilidades; }
    public String getBeneficios() { return beneficios; }
    public void setBeneficios(String beneficios) { this.beneficios = beneficios; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public long getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(long fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
    public boolean isVerificado() { return verificado; }
    public void setVerificado(boolean verificado) { this.verificado = verificado; }
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public String getTamanoEmpresa() { return tamanoEmpresa; }
    public void setTamanoEmpresa(String tamanoEmpresa) { this.tamanoEmpresa = tamanoEmpresa; }
    public String getSedePrincipal() { return sedePrincipal; }
    public void setSedePrincipal(String sedePrincipal) { this.sedePrincipal = sedePrincipal; }
    public float getPuntuacion() { return puntuacion; }
    public void setPuntuacion(float puntuacion) { this.puntuacion = puntuacion; }
    public int getNumResenas() { return numResenas; }
    public void setNumResenas(int numResenas) { this.numResenas = numResenas; }
    public String getAnioFundacion() { return anioFundacion; }
    public void setAnioFundacion(String anioFundacion) { this.anioFundacion = anioFundacion; }
}
