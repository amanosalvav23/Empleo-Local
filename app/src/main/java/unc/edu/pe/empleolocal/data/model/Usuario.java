package unc.edu.pe.empleolocal.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.io.Serializable;
import java.util.List;

import unc.edu.pe.empleolocal.data.database.converter.Converters;

@Entity(tableName = "usuarios")
public class Usuario implements Serializable {
    @PrimaryKey
    @NonNull
    private String uid;
    private String nombre;
    private String apellido;
    private String telefono;
    private String correo;
    private double latitud;
    private double longitud;
    private String direccion;
    
    @TypeConverters(Converters.class)
    private List<String> sectores;
    
    private String cvUrl;
    private int radioBusqueda;

    public Usuario() {
        // Required for Firebase
    }

    public Usuario(@NonNull String uid, String nombre, String apellido, String telefono, String correo) {
        this.uid = uid;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.correo = correo;
    }

    // Getters and Setters
    @NonNull
    public String getUid() { return uid; }
    public void setUid(@NonNull String uid) { this.uid = uid; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public List<String> getSectores() { return sectores; }
    public void setSectores(List<String> sectores) { this.sectores = sectores; }
    public String getCvUrl() { return cvUrl; }
    public void setCvUrl(String cvUrl) { this.cvUrl = cvUrl; }
    public int getRadioBusqueda() { return radioBusqueda; }
    public void setRadioBusqueda(int radioBusqueda) { this.radioBusqueda = radioBusqueda; }
}
