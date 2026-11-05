package eus.birt.dam.ad04te.service;

import org.hibernate.Session;
import org.hibernate.Transaction;

import eus.birt.dam.ad04te.config.HibernateUtil;
import eus.birt.dam.ad04te.entity.Cliente;

public class ClienteService {

	/*
	 * Regex sencilla. No valida un email al 100% (eso no es realista con una sola
	 * regex), solo descarta los casos claramente mal escritos.
	 */
	private static final String REGEX_EMAIL = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";

	/**
	 * Crea y guarda un nuevo cliente, validando el formato del email y comprobando
	 * antes que no exista ya otro cliente con el mismo email.
	 */
	public static Cliente crear(String nombre, String email) {

		/*
		 * 0.- validar ANTES de abrir sesion: si esta mal, ni tocamos la BD.
		 */
		if (nombre == null || nombre.isBlank()) {
			throw new IllegalArgumentException("El nombre del cliente no puede estar vacio.");
		}
		if (email == null || !email.trim().matches(REGEX_EMAIL)) {
			throw new IllegalArgumentException("El email no tiene un formato valido.");
		}

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {

			Transaction tx = session.beginTransaction();
			try {

				/*
				 * comprobar duplicado ANTES de insertar: cuenta cuantos clientes tienen ese
				 * email ignorando mayusculas/minusculas y espacios al principio/final.
				 * getSingleResult() porque esperamos un UNICO valor (el conteo), no una lista.
				 */
				Long coincidencias = session
						.createQuery("SELECT COUNT(c) FROM Cliente c WHERE lower(trim(c.email)) = lower(trim(:email))",
								Long.class)
						.setParameter("email", email).getSingleResult();

				if (coincidencias > 0) {
					throw new IllegalArgumentException("Ya existe un cliente con ese email.");
				}

				Cliente cliente = new Cliente(nombre, email.trim());

				session.persist(cliente);

				tx.commit();

				return cliente;

			} catch (RuntimeException e) {
				if (tx.isActive()) {
					tx.rollback();
				}
				throw e;
			}

		}
	}

}
