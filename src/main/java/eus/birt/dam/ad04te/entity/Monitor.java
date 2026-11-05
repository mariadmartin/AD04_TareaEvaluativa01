package eus.birt.dam.ad04te.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "monitor")
public class Monitor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nombre", length = 100, nullable = false)
	private String nombre;

	@Column(name = "especialidad", length = 100, nullable = false)
	private String especialidad;

	@OneToMany(mappedBy = "monitor")
	private List<Actividad> actividades = new ArrayList<>();

	// Constructor
	protected Monitor() {
	}

	public Monitor(String nombre, String especialidad) {
		super();
		this.nombre = nombre;
		this.especialidad = especialidad;
	}

	// Getters - Setters
	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEspecialidad() {
		return especialidad;
	}

	public void setEspecialidad(String especialidad) {
		this.especialidad = especialidad;
	}

	public Long getId() {
		return id;
	}

	public List<Actividad> getActividades() {
		return actividades;
	}

	@Override
	public String toString() {
		return "Monitor [id=" + id + ", nombre=" + nombre + ", especialidad=" + especialidad + "]";
	}

	public void addActividad(Actividad actividad) {
		actividades.add(actividad);
		actividad.setMonitor(this);
	}

}
