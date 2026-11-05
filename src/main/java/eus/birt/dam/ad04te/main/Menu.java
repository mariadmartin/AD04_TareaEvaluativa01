package eus.birt.dam.ad04te.main;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import eus.birt.dam.ad04te.entity.Actividad;
import eus.birt.dam.ad04te.entity.Cliente;
import eus.birt.dam.ad04te.entity.Monitor;
import eus.birt.dam.ad04te.entity.Reserva;
import eus.birt.dam.ad04te.service.ActividadService;
import eus.birt.dam.ad04te.service.ClienteService;
import eus.birt.dam.ad04te.service.MonitorService;
import eus.birt.dam.ad04te.service.ReservaService;

public class Menu {

	/**
	 * Bucle principal del menu: muestra las opciones, lee la eleccion del
	 * usuario y llama al metodo correspondiente, hasta que se elige Salir.
	 */
	public static void iniciar() {
		Scanner sc = new Scanner(System.in);
		boolean salir = false;

		while (!salir) {
			System.out.println();
			System.out.println("===== ACTIVA =====");
			System.out.println("1. Alta de monitor");
			System.out.println("2. Alta de actividad");
			System.out.println("3. Alta de cliente");
			System.out.println("4. Buscar actividades");
			System.out.println("5. Reservar plazas");
			System.out.println("6. Cancelar reserva");
			System.out.println("8. Salir");
			System.out.print("Elige una opcion: ");

			int opcion;
			try {
				opcion = sc.nextInt();
			} catch (InputMismatchException e) {
				System.out.println("Eso no es un numero. Intentalo de nuevo.");
				/*
				 * descarta la entrada no numerica para no quedarse atascado
				 * en un bucle infinito leyendo siempre lo mismo.
				 */
				sc.nextLine();
				continue;
			}
			/*
			 * descarta el salto de linea que deja pendiente nextInt(), para
			 * que el siguiente nextLine() no lea una linea vacia.
			 */
			sc.nextLine();

			switch (opcion) {
			case 1:
				altaMonitor(sc);
				break;
			case 2:
				altaActividad(sc);
				break;
			case 3:
				altaCliente(sc);
				break;
			case 4:
				buscarActividades(sc);
				break;
			case 5:
				reservarPlazas(sc);
				break;
			case 6:
				cancelarReserva(sc);
				break;
			case 8:
				salir = true;
				System.out.println("Hasta luego.");
				break;
			default:
				System.out.println("Opcion no implementada todavia.");
			}
		}

		sc.close();
	}

	/**
	 * Pide los datos de un monitor por consola y lo crea a traves de
	 * MonitorService.
	 */
	private static void altaMonitor(Scanner sc) {
		System.out.print("Nombre: ");
		String nombre = sc.nextLine();
		System.out.print("Especialidad: ");
		String especialidad = sc.nextLine();

		try {
			Monitor monitor = MonitorService.crear(nombre, especialidad);
			System.out.println("Monitor creado: " + monitor);
		} catch (RuntimeException e) {
			System.out.println("No se pudo crear el monitor: " + e.getMessage());
		}
	}

	/**
	 * Pide los datos de un cliente por consola y lo crea a traves de
	 * ClienteService.
	 */
	private static void altaCliente(Scanner sc) {
		System.out.print("Nombre: ");
		String nombre = sc.nextLine();
		System.out.print("Email: ");
		String email = sc.nextLine();

		try {
			Cliente cliente = ClienteService.crear(nombre, email);
			System.out.println("Cliente creado: " + cliente);
		} catch (RuntimeException e) {
			System.out.println("No se pudo crear el cliente: " + e.getMessage());
		}
	}

	/**
	 * Pide los datos de una actividad por consola (incluyendo el id del
	 * monitor) y la crea a traves de ActividadService.
	 */
	private static void altaActividad(Scanner sc) {
		System.out.print("Nombre: ");
		String nombre = sc.nextLine();
		System.out.print("Localidad: ");
		String localidad = sc.nextLine();
		System.out.print("Fecha y hora (dd/MM/yyyy HH:mm): ");
		String textoFecha = sc.nextLine();
		System.out.print("Plazas totales: ");
		String textoPlazas = sc.nextLine();
		System.out.print("Id del monitor: ");
		String textoMonitorId = sc.nextLine();

		/*
		 * 1.- convertir los textos leidos a los tipos que pide
		 * ActividadService.crear(...). Si cualquiera de las conversiones
		 * falla (fecha mal escrita, letras en vez de numero...) se lanza una
		 * excepcion DISTINTA en cada caso (DateTimeParseException,
		 * NumberFormatException), pero las dos son RuntimeException, asi que
		 * basta un unico catch para avisar sin reventar el programa.
		 */
		try {
			DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
			LocalDateTime fechaHora = LocalDateTime.parse(textoFecha, formato);
			int plazasTotales = Integer.parseInt(textoPlazas);
			Long monitorId = Long.parseLong(textoMonitorId);

			/*
			 * 2.- con los datos ya convertidos, se llama al servicio.
			 */
			Actividad actividad = ActividadService.crear(nombre, localidad, fechaHora, plazasTotales, monitorId);
			System.out.println("Actividad creada: " + actividad);

		} catch (RuntimeException e) {
			System.out.println("No se pudo crear la actividad: " + e.getMessage());
		}
	}

	/**
	 * Pide localidad, dia y plazas necesarias por consola, y muestra las
	 * actividades disponibles que cumplan esos criterios.
	 */
	private static void buscarActividades(Scanner sc) {
		System.out.print("Localidad: ");
		String localidad = sc.nextLine();
		System.out.print("Dia (dd/MM/yyyy): ");
		String textoDia = sc.nextLine();
		System.out.print("Plazas necesarias: ");
		String textoPlazas = sc.nextLine();

		try {
			DateTimeFormatter formatoDia = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			LocalDate dia = LocalDate.parse(textoDia, formatoDia);
			int plazasNecesarias = Integer.parseInt(textoPlazas);

			List<Actividad> actividades = ActividadService.buscarDisponibles(localidad, dia, plazasNecesarias);

			if (actividades.isEmpty()) {
				System.out.println("No hay actividades disponibles con esos criterios.");
			} else {
				for (Actividad actividad : actividades) {
					System.out.println(actividad);
				}
			}

		} catch (RuntimeException e) {
			System.out.println("No se pudo realizar la busqueda: " + e.getMessage());
		}
	}

	/**
	 * Pide el id del cliente, el id de la actividad y el numero de plazas, y
	 * crea la reserva a traves de ReservaService.
	 */
	private static void reservarPlazas(Scanner sc) {
		System.out.print("Id del cliente: ");
		String textoClienteId = sc.nextLine();
		System.out.print("Id de la actividad: ");
		String textoActividadId = sc.nextLine();
		System.out.print("Numero de plazas: ");
		String textoPlazas = sc.nextLine();

		try {
			Long clienteId = Long.parseLong(textoClienteId);
			Long actividadId = Long.parseLong(textoActividadId);
			int numPlazas = Integer.parseInt(textoPlazas);

			Reserva reserva = ReservaService.crear(clienteId, actividadId, numPlazas);
			System.out.println("Reserva creada: " + reserva);

		} catch (RuntimeException e) {
			System.out.println("No se pudo crear la reserva: " + e.getMessage());
		}
	}

	/**
	 * Pide el id de la reserva y la cancela a traves de ReservaService,
	 * avisando si esa reserva ya no existia.
	 */
	private static void cancelarReserva(Scanner sc) {
		System.out.print("Id de la reserva: ");
		String textoReservaId = sc.nextLine();

		try {
			Long reservaId = Long.parseLong(textoReservaId);

			boolean cancelada = ReservaService.cancelar(reservaId);
			if (cancelada) {
				System.out.println("Reserva cancelada.");
			} else {
				System.out.println("No existe ninguna reserva con id " + reservaId + ".");
			}

		} catch (RuntimeException e) {
			System.out.println("No se pudo cancelar la reserva: " + e.getMessage());
		}
	}

}
