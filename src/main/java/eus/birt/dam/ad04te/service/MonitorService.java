package eus.birt.dam.ad04te.service;

import org.hibernate.Session;
import org.hibernate.Transaction;

import eus.birt.dam.ad04te.config.HibernateUtil;
import eus.birt.dam.ad04te.entity.Monitor;

public class MonitorService {

	/**
	 * Crea y guarda un nuevo monitor, validando antes de tocar la base de datos.
	 */
	public static Monitor crear(String nombre, String especialidad) {

		/*
		 * 0.- validar ANTES de abrir sesion: si esta mal, ni tocamos la BD.
		 */
		if (nombre == null || nombre.isBlank()) {
			throw new IllegalArgumentException("El nombre del monitor no puede estar vacio.");
		}
		if (especialidad == null || especialidad.isBlank()) {
			throw new IllegalArgumentException("La especialidad del monitor no puede estar vacia.");
		}

		// crear un monitor en memoria (todavia sin id, Hibernate no lo conoce)
		Monitor monitor = new Monitor(nombre, especialidad);

		/*
		 * 1.- abrir sesion: ya no recibe el SessionFactory por parametro, lo
		 * pide directamente al singleton HibernateUtil.
		 */
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {

			// Abre la transaccion
			Transaction tx = session.beginTransaction();
			try {

				// guardar monitor en bbdd - confirmo con commit
				session.persist(monitor);

				tx.commit();

				/*
				 * return DESPUES del commit: aqui "monitor" ya tiene su id,
				 * porque con GenerationType.IDENTITY el INSERT se ejecuta en
				 * el propio persist(), no hace falta esperar al commit.
				 */
				return monitor;

			} catch (RuntimeException e) {
				if (tx.isActive()) {
					tx.rollback();
				}

				throw e;
			}

		}

	}

}
