package unc.edu.pe.empleolocal.data.model;

import java.io.Serializable;

public class Notificacion implements Serializable {
    private String id;
    private String titulo;
    private String mensaje;
    private String tipo; // "PROXIMIDAD" o "POSTULACION"
    private long timestamp;
    private boolean leida;
    private String usuarioId;

    public Notificacion() {}

    public Notificacion(String titulo, String mensaje, String tipo, String usuarioId) {
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.usuarioId = usuarioId;
        this.timestamp = System.currentTimeMillis();
        this.leida = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
}
