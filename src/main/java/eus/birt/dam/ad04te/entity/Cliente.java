package eus.birt.dam.ad04te.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cliente")
public class Cliente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nombre", length = 100, nullable = false)
	private String nombre;

	@Column(name = "email", unique = true, nullable = false, length = 150)
	private String email;

	// Constructor
	protected Cliente() {
	}

	public Cliente(String nombre, String email) {
		super();
		this.nombre = nombre;
		this.email = email;
	}

	// Getters - Setters

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Cliente [id=" + id + ", nombre=" + nombre + ", email=" + email + "]";
	}

}
