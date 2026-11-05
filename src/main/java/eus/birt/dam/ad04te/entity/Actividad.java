package eus.birt.dam.ad04te.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "actividad")
public class Actividad {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nombre", nullable = false)
	private String nombre;

	@Column(name = "localidad", nullable = false)
	private String localidad;

	@Column(name = "fecha_hora", nullable = false)
	private LocalDateTime fechaHora;

	@Column(name = "plazas_totales", nullable = false)
	private Integer plazasTotales;

	@Column(name = "plazas_disponibles", nullable = false)
	private Integer plazasDisponibles;

	@ManyToOne
	@JoinColumn(name = "monitor_id", nullable = false)
	private Monitor monitor;

	// Constructor

	protected Actividad() {
	}

	public Actividad(String nombre, String localidad, LocalDateTime fechaHora, Integer plazasTotales,
			Integer plazasDisponibles, Monitor monitor) {
		this.nombre = nombre;
		this.localidad = localidad;
		this.fechaHora = fechaHora;
		this.plazasTotales = plazasTotales;
		this.plazasDisponibles = plazasDisponibles;
		this.monitor = monitor;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getLocalidad() {
		return localidad;
	}

	public void setLocalidad(String localidad) {
		this.localidad = localidad;
	}

	public LocalDateTime getFechaHora() {
		return fechaHora;
	}

	public void setFechaHora(LocalDateTime fechaHora) {
		this.fechaHora = fechaHora;
	}

	public Integer getPlazasTotales() {
		return plazasTotales;
	}

	public void setPlazasTotales(Integer plazasTotales) {
		this.plazasTotales = plazasTotales;
	}

	public Integer getPlazasDisponibles() {
		return plazasDisponibles;
	}

	public void setPlazasDisponibles(Integer plazasDisponibles) {
		this.plazasDisponibles = plazasDisponibles;
	}

	public Long getId() {
		return id;
	}

	public Monitor getMonitor() {
		return monitor;
	}

	public void setMonitor(Monitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public String toString() {
		return "Actividad [id=" + id + ", nombre=" + nombre + ", localidad=" + localidad + ", fechaHora=" + fechaHora
				+ ", plazasTotales=" + plazasTotales + ", plazasDisponibles=" + plazasDisponibles + ", monitor="
				+ monitor + "]";
	}

}
