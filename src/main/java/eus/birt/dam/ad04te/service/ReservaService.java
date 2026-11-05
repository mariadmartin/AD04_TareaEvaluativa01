package eus.birt.dam.ad04te.service;

import java.time.LocalDateTime;

import org.hibernate.Session;
import org.hibernate.Transaction;

import eus.birt.dam.ad04te.config.HibernateUtil;
import eus.birt.dam.ad04te.entity.Actividad;
import eus.birt.dam.ad04te.entity.Cliente;
import eus.birt.dam.ad04te.entity.Reserva;

public class ReservaService {

	/**
	 * Crea una reserva de un cliente para una actividad, descontando las plazas
	 * reservadas de la actividad en la MISMA transaccion (todo o nada).
	 */
	public static Reserva crear(Long clienteId, Long actividadId, int numPlazas) {

		/*
		 * 0.- validar ANTES de abrir sesion: si esta mal, ni tocamos la BD.
		 */
		if (clienteId == null) {
			throw new IllegalArgumentException("Debes indicar el cliente de la reserva.");
		}
		if (actividadId == null) {
			throw new IllegalArgumentException("Debes indicar la actividad de la reserva.");
		}
		if (numPlazas <= 0) {
			throw new IllegalArgumentException("El numero de plazas debe ser mayor que cero.");
		}

		/*
		 * 1.- abrir sesion y transaccion: cliente, actividad y reserva se
		 * van a leer/escribir TODOS dentro de la misma transaccion, para
		 * que el conjunto sea todo o nada (si algo falla a mitad, el
		 * rollback deja la BD exactamente como estaba antes de empezar).
		 */
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {

			Transaction tx = session.beginTransaction();
			try {

				/*
				 * 2.- buscar cliente y actividad: find() devuelve null si
				 * no existe (no lanza excepcion), por eso hay que
				 * comprobarlo a mano, igual que con el monitor en
				 * ActividadService.
				 */
				Cliente cliente = session.find(Cliente.class, clienteId);
				if (cliente == null) {
					throw new IllegalArgumentException("No existe ningun cliente con id " + clienteId);
				}

				Actividad actividad = session.find(Actividad.class, actividadId);
				if (actividad == null) {
					throw new IllegalArgumentException("No existe ninguna actividad con id " + actividadId);
				}

				/*
				 * 3.- comprobar que quedan plazas suficientes ANTES de
				 * crear la reserva.
				 */
				if (actividad.getPlazasDisponibles() < numPlazas) {
					throw new IllegalArgumentException(
							"No hay suficientes plazas disponibles (quedan " + actividad.getPlazasDisponibles()
									+ ").");
				}

				/*
				 * 4.- crear y guardar la reserva.
				 */
				Reserva reserva = new Reserva(LocalDateTime.now(), numPlazas, actividad, cliente);
				session.persist(reserva);

				/*
				 * 5.- descontar las plazas: "actividad" sigue siendo un
				 * objeto gestionado por esta misma Session (se cargo con
				 * find() dentro de esta transaccion), asi que Hibernate
				 * detecta el cambio solo (dirty checking) y genera el
				 * UPDATE al hacer commit(), sin necesidad de llamar a
				 * persist() ni a merge() sobre la actividad.
				 */
				actividad.setPlazasDisponibles(actividad.getPlazasDisponibles() - numPlazas);

				/*
				 * 6.- confirmar: el INSERT de la reserva y el UPDATE de
				 * las plazas se confirman juntos, en el mismo commit.
				 */
				tx.commit();

				return reserva;

			} catch (RuntimeException e) {
				if (tx.isActive()) {
					tx.rollback();
				}
				throw e;
			}

		}
	}

	/**
	 * Cancela una reserva: devuelve sus plazas a la actividad y la borra, todo
	 * en la misma transaccion. Si la reserva ya no existe (por ejemplo, porque
	 * ya se cancelo antes), no hace nada y devuelve false.
	 */
	public static boolean cancelar(Long reservaId) {

		if (reservaId == null) {
			throw new IllegalArgumentException("Debes indicar la reserva a cancelar.");
		}

		/*
		 * 1.- abrir sesion y transaccion: buscar la reserva, devolver las
		 * plazas y borrarla tienen que ir todos juntos, todo o nada.
		 */
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {

			Transaction tx = session.beginTransaction();
			try {

				/*
				 * 2.- buscar la reserva. find() devuelve null si no
				 * existe.
				 */
				Reserva reserva = session.find(Reserva.class, reservaId);
				if (reserva == null) {
					/*
					 * no hay nada que cancelar: no se toca la BD. Esto
					 * hace que cancelar dos veces la misma reserva sea
					 * seguro, la segunda vez simplemente no la encuentra
					 * y no puede duplicar el abono de plazas.
					 */
					tx.commit();
					return false;
				}

				/*
				 * 3.- devolver las plazas a la actividad (dirty checking,
				 * igual que en crear(): basta con el setter).
				 */
				Actividad actividad = reserva.getActividad();
				actividad.setPlazasDisponibles(actividad.getPlazasDisponibles() + reserva.getNumeroPlazas());

				/*
				 * 4.- borrar la reserva.
				 */
				session.remove(reserva);

				/*
				 * 5.- confirmar: el UPDATE de las plazas y el DELETE de
				 * la reserva se confirman juntos, en el mismo commit.
				 */
				tx.commit();

				return true;

			} catch (RuntimeException e) {
				if (tx.isActive()) {
					tx.rollback();
				}
				throw e;
			}

		}
	}

}
