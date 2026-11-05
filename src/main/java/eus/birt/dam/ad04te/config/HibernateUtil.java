package eus.birt.dam.ad04te.config;

import eus.birt.dam.ad04te.entity.Actividad;
import eus.birt.dam.ad04te.entity.Cliente;
import eus.birt.dam.ad04te.entity.Monitor;
import eus.birt.dam.ad04te.entity.Reserva;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;

import java.util.HashMap;
import java.util.Map;

/**
 * Crea una unica SessionFactory por aplicacion (mismo patron que
 * ud4_ta2-ud4_ta5).
 */
public final class HibernateUtil {

	/**
	 * Campo static final: la JVM garantiza que esta inicializacion (la llamada a
	 * construirSessionFactory()) se ejecuta UNA SOLA VEZ, la primera vez que se usa
	 * esta clase, de forma segura aunque varios hilos la usen a la vez. Asi se
	 * consigue el singleton sin escribir a mano un "if (instancia == null)".
	 */
	private static final SessionFactory SESSION_FACTORY = construirSessionFactory();

	/**
	 * Constructor privado: nadie puede hacer "new HibernateUtil()" desde fuera. No
	 * tiene sentido instanciar esta clase, todo es static.
	 */
	private HibernateUtil() {
	}

	/**
	 * Construye la SessionFactory leyendo hibernate.cfg.xml, aplicando la
	 * contrasena desde la variable de entorno DB_PASSWORD, y registrando las 4
	 * entidades del modelo.
	 */
	private static SessionFactory construirSessionFactory() {
		/*
		 * La contrasena NUNCA se escribe en hibernate.cfg.xml (ese fichero se sube al
		 * repositorio). Se lee en tiempo de ejecucion desde una variable de entorno que
		 * cada quien configura en su maquina (Run Configurations -> Environment en
		 * Eclipse).
		 */
		String clave = System.getenv("DB_PASSWORD");
		if (clave == null || clave.isBlank()) {
			throw new IllegalStateException("Define DB_PASSWORD antes de ejecutar.");
		}

		/*
		 * Map<String, Object>: un "diccionario" clave->valor (como un objeto JSON). La
		 * clave siempre es texto (String); el valor puede ser cualquier tipo de objeto
		 * (Object), porque esta misma estructura la usa applySettings(...) para recibir
		 * CUALQUIER propiedad de configuracion, no solo texto. Aqui solo metemos un
		 * String (la contrasena), pero el tipo declarado tiene que coincidir con lo que
		 * pide ese metodo. HashMap es la implementacion concreta de Map mas habitual
		 * cuando solo hace falta buscar por clave, sin importar el orden.
		 */
		Map<String, Object> overrides = new HashMap<>();
		overrides.put(AvailableSettings.JAKARTA_JDBC_PASSWORD, clave);

		/*
		 * registry = la configuracion ya "cerrada": lee hibernate.cfg.xml (URL,
		 * usuario, dialecto, hbm2ddl.auto=validate...) y le suma el mapa de arriba (la
		 * contrasena). Equivale, en espiritu, al pool de conexiones de JDBC/UD03.
		 */
		StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure("hibernate.cfg.xml")
				.applySettings(overrides).build();

		try {
			/*
			 * metadata = el mapeo objeto<->tabla ya compilado: Hibernate lee las
			 * anotaciones (@Entity, @Column, @ManyToOne...) de las 4 entidades registradas
			 * aqui con addAnnotatedClass(...).
			 */
			Metadata metadata = new MetadataSources(registry).addAnnotatedClass(Monitor.class)
					.addAnnotatedClass(Actividad.class).addAnnotatedClass(Cliente.class)
					.addAnnotatedClass(Reserva.class).getMetadataBuilder().build();

			/*
			 * Con configuracion + mapeo ya listos, se construye el objeto: la
			 * SessionFactory final. Esta llamada es costosa, por eso solo se hace una vez
			 * (arriba).
			 */
			return metadata.getSessionFactoryBuilder().build();
		} catch (RuntimeException ex) {
			/*
			 * Si algo falla construyendo el Metadata/SessionFactory (p. ej.
			 * hbm2ddl.auto=validate detecta un desajuste con dbactiva real), el registry ya
			 * tiene recursos abiertos (como un mini pool de conexiones). Como la
			 * SessionFactory nunca llego a existir, nadie los cerraria solo -> se destruyen
			 * a mano antes de relanzar la excepcion.
			 */
			StandardServiceRegistryBuilder.destroy(registry);
			throw ex;
		}
	}

	/**
	 * Unico metodo publico. Siempre devuelve el mismo objeto SESSION_FACTORY ya
	 * construido; nunca vuelve a llamar a construirSessionFactory().
	 */
	public static SessionFactory getSessionFactory() {
		return SESSION_FACTORY;
	}
}
