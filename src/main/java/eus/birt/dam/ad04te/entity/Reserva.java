package eus.birt.dam.ad04te.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "reserva")
public class Reserva {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "fecha_reserva", nullable = false)
	private LocalDateTime fechaReserva;

	@Column(name = "numero_plazas")
	private int numeroPlazas;

	@ManyToOne
	@JoinColumn(name = "actividad_id", nullable = false)
	private Actividad actividad;

	@ManyToOne
	@JoinColumn(name = "cliente_id", nullable = false)
	private Cliente cliente;

	// Constructores

	protected Reserva() {
	}

	public Reserva(LocalDateTime fechaReserva, int numeroPlazas, Actividad actividad, Cliente cliente) {
		super();
		this.fechaReserva = fechaReserva;
		this.numeroPlazas = numeroPlazas;
		this.actividad = actividad;
		this.cliente = cliente;
	}

	public LocalDateTime getFechaReserva() {
		return fechaReserva;
	}

	public void setFechaReserva(LocalDateTime fechaReserva) {
		this.fechaReserva = fechaReserva;
	}

	public int getNumeroPlazas() {
		return numeroPlazas;
	}

	public void setNumeroPlazas(int numeroPlazas) {
		this.numeroPlazas = numeroPlazas;
	}

	public Actividad getActividad() {
		return actividad;
	}

	public void setActividad(Actividad actividad) {
		this.actividad = actividad;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Reserva [id=" + id + ", fechaReserva=" + fechaReserva + ", numeroPlazas=" + numeroPlazas
				+ ", actividad=" + actividad + ", cliente=" + cliente + "]";
	}

}
