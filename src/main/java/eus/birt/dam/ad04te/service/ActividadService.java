package eus.birt.dam.ad04te.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import eus.birt.dam.ad04te.config.HibernateUtil;
import eus.birt.dam.ad04te.entity.Actividad;
import eus.birt.dam.ad04te.entity.Monitor;

public class ActividadService {

	/**
	 * Crea y guarda una nueva actividad, validando antes de tocar la base de
	 * datos y comprobando que el monitor indicado existe de verdad.
	 */
	public static Actividad crear(String nombre, String localidad, LocalDateTime fechaHora, int plazasTotales,
			Long monitorId) {

		/*
		 * 0.- validar ANTES de abrir sesion: si esta mal, ni tocamos la BD.
		 */
		if (nombre == null || nombre.isBlank()) {
			throw new IllegalArgumentException("El nombre de la actividad no puede estar vacio.");
		}
		if (localidad == null || localidad.isBlank()) {
			throw new IllegalArgumentException("La localidad no puede estar vacia.");
		}
		if (fechaHora == null) {
			throw new IllegalArgumentException("La fecha y hora de la actividad no puede ser nula.");
		}
		if (fechaHora.isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("No se puede crear una actividad en una fecha pasada.");
		}
		if (plazasTotales <= 0) {
			throw new IllegalArgumentException("Las plazas totales deben ser mayores que cero.");
		}
		if (monitorId == null) {
			throw new IllegalArgumentException("Debes indicar el monitor de la actividad.");
		}

		// 1.- abrir sesion
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {

			Transaction tx = session.beginTransaction();
			try {

				/*
				 * buscar el monitor: find() devuelve null si no existe (no
				 * lanza excepcion), por eso hay que comprobarlo a mano.
				 */
				Monitor monitor = session.find(Monitor.class, monitorId);
				if (monitor == null) {
					throw new IllegalArgumentException("No existe ningun monitor con id " + monitorId);
				}

				/*
				 * al crear la actividad, las plazas disponibles arrancan
				 * siendo las mismas que las plazas totales.
				 */
				Actividad actividad = new Actividad(nombre, localidad, fechaHora, plazasTotales, plazasTotales,
						monitor);

				session.persist(actividad);

				tx.commit();

				return actividad;

			} catch (RuntimeException e) {
				if (tx.isActive()) {
					tx.rollback();
				}
				throw e;
			}

		}
	}

	/**
	 * Busca actividades en una localidad y dia concretos que tengan al menos las
	 * plazas disponibles indicadas, ordenadas por fecha.
	 */
	public static List<Actividad> buscarDisponibles(String localidad, LocalDate dia, int plazasNecesarias) {

		if (localidad == null || localidad.isBlank()) {
			throw new IllegalArgumentException("La localidad no puede estar vacia.");
		}
		if (dia == null) {
			throw new IllegalArgumentException("El dia no puede ser nulo.");
		}
		if (plazasNecesarias <= 0) {
			throw new IllegalArgumentException("Las plazas necesarias deben ser mayores que cero.");
		}

		/*
		 * el dia completo se expresa como un rango: desde las 00:00 de ese
		 * dia (inclusive) hasta las 00:00 del dia siguiente (exclusive).
		 */
		LocalDateTime inicioDia = dia.atStartOfDay();
		LocalDateTime finDia = dia.plusDays(1).atStartOfDay();

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {

			/*
			 * join fetch: trae el monitor en la MISMA consulta (un unico
			 * SELECT con JOIN), en vez de que Hibernate lance una consulta
			 * aparte a "monitor" por cada actividad cuando se imprime
			 * (problema N+1).
			 */
			Query<Actividad> query = session.createQuery(
					"FROM Actividad a JOIN FETCH a.monitor " + "WHERE lower(a.localidad) = lower(:localidad) "
							+ "AND a.fechaHora >= :inicio AND a.fechaHora < :fin "
							+ "AND a.plazasDisponibles >= :plazas " + "ORDER BY a.fechaHora",
					Actividad.class);

			query.setParameter("localidad", localidad);
			query.setParameter("inicio", inicioDia);
			query.setParameter("fin", finDia);
			query.setParameter("plazas", plazasNecesarias);

			return query.getResultList();
		}
	}

	/**
	 * Devuelve todas las actividades (incluidas las completas), ordenadas por
	 * fecha.
	 */
	public static List<Actividad> listarTodas() {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			/*
			 * Query<Actividad> es la consulta ya preparada (como un
			 * PreparedStatement de JDBC), todavia sin ejecutar: el "JOIN
			 * FETCH" es solo texto HQL dentro del String, no cambia el
			 * codigo Java de alrededor. getResultList() es quien la
			 * ejecuta de verdad y devuelve la List<Actividad> final.
			 */
			Query<Actividad> query = session.createQuery("FROM Actividad a JOIN FETCH a.monitor ORDER BY a.fechaHora",
					Actividad.class);
			return query.getResultList();
		}
	}

}
