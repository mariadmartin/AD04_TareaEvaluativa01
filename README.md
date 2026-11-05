# AD04 - Tarea Evaluativa UD4 - Activa

Aplicacion de consola en Java para gestionar monitores, actividades, clientes y reservas de un
centro de actividades guiadas ("Activa"), usando Hibernate nativo (`Session`/`SessionFactory`)
sobre un esquema MySQL ya existente (`dbactiva`).

## Tecnologias y versiones

- Java 21 (JDK)
- Maven
- Hibernate ORM 7.4.12.Final (`org.hibernate.orm:hibernate-core`)
- MySQL Connector/J 9.7.0 (`com.mysql:mysql-connector-j`)
- MySQL 8.0 (en Docker)

## Base de datos

El esquema `dbactiva` (tablas `monitor`, `actividad`, `cliente`, `reserva`, con sus datos
iniciales) se asume ya creado de antemano; la aplicacion usa `hibernate.hbm2ddl.auto=validate`
(nunca `create`/`update`), nunca modifica la estructura.

Para levantarlo con Docker localmente, un `docker-compose.yml` como este (puerto `3307` para no
chocar con otros proyectos del mismo workspace):

```yaml
name: ad04_te
services:
  mysql:
    image: mysql:8.0
    container_name: ad04-te-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: dbactiva
      MYSQL_USER: ad04
      MYSQL_PASSWORD: ad04
    ports:
      - "3307:3306"
    volumes:
      - ./mysql_data:/var/lib/mysql
      - ./dbactiva.sql:/docker-entrypoint-initdb.d/00_dbactiva.sql:ro
```

```
docker compose up -d
```

(`dbactiva.sql` crea el esquema y los datos de partida; solo se ejecuta la primera vez, con el
volumen de datos vacio).

Si se usa una URL/puerto distintos, ajustar `src/main/resources/hibernate.cfg.xml`
(`connection.url`, `connection.username`).

## Contrasena de la base de datos

La contrasena **nunca** se escribe en `hibernate.cfg.xml`. Se lee en tiempo de ejecucion desde la
variable de entorno `DB_PASSWORD`:

- En Eclipse: boton derecho sobre `MainApp` -> **Run As -> Run Configurations... -> Environment**
  -> anadir `DB_PASSWORD` con el valor correspondiente (`ad04` para el `docker-compose.yml` de
  arriba).
- Por terminal: `DB_PASSWORD=ad04 mvn exec:java -Dexec.mainClass=eus.birt.dam.ad04te.main.MainApp`

## Ejecucion

Clase principal: `eus.birt.dam.ad04te.main.MainApp`. Menu de consola con estas opciones:

1. Alta de monitor
2. Alta de actividad
3. Alta de cliente
4. Buscar actividades (por localidad, dia y plazas necesarias)
5. Reservar plazas
6. Cancelar reserva
8. Salir

## Estructura del proyecto

```
src/main/java/eus/birt/dam/ad04te/
  entity/   Monitor, Actividad, Cliente, Reserva (JPA/Hibernate)
  service/  MonitorService, ActividadService, ClienteService, ReservaService
  config/   HibernateUtil (singleton de SessionFactory)
  main/     MainApp, Menu
```
