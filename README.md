# Post-contenido — Unidad 7: Patrones Arquitectónicos I

## Descripción
Repositorio del post-contenido de la Unidad 7 de Patrones de Diseño
de Software. Un unico proyecto Spring Boot (multas-biblioteca-api)
para la gestion de multas de biblioteca, con dos partes: una API REST
en capas (Model, Repository, Service, Controller) sobre H2, y pago en
linea de multas con dos pasarelas intercambiables.

## Parte 1 — Arquitectura en Capas
MultaRepository extiende JpaRepository y agrega una consulta
agregada (countByEstudianteIdAndEstado). MultaService concentra las
reglas de negocio (limite de multas pendientes, generacion); el
calculo del monto vive en la propia entidad Multa
(Multa.calcularMonto). MultaController expone /api/multas. Ver
paquetes model/, repository/, service/ y controller/.

## Parte 2 — Pago en Linea con Dos Pasarelas
[Documentar aqui la opcion elegida entre A (rama condicional), B
(Strategy en service/) o C (puerto de dominio con adaptadores) y por
que. Si se eligio C: domain/port/PasarelaPagoPort y domain/ResultadoPago
son Java puro; infrastructure/pago/PagosUdesAdapter e
infrastructure/pago/WompiAdapter traducen cada formato HTTP externo
al mismo ResultadoPago; se seleccionan por app.pagos.proveedor sin
tocar MultaController ni el resto de MultaService.]

## Cómo ejecutar
```
$ cd multas-biblioteca-api && mvn spring-boot:run
```

## Herramientas utilizadas
- Java 17, Spring Boot 3.x, Spring Data JPA, H2, RestTemplate
- Apache Maven, Postman/curl, Git, GitHub

## Decisiones de diseño

### Punto de decisión 1 — Cálculo del monto: ¿entidad o Service?
Se decidió ubicar calcularMonto en la entidad Multa porque la regla depende exclusivamente de información propia del dominio: los días de atraso, el valor cobrado por día y el tope máximo permitido. No requiere consultar un repositorio, consumir un servicio externo ni coordinar otros componentes.

Ubicar esta lógica en MultaService sería técnicamente posible, pero reduciría progresivamente la entidad a una estructura pasiva de datos. El resultado sería un modelo anémico, en el que las reglas relacionadas conceptualmente con una multa estarían dispersas en servicios externos a la propia entidad.

Mantener la operación en Multa mejora la cohesión: el objeto que representa el concepto del negocio también conoce una de sus reglas fundamentales. Además, cualquier otro caso de uso que necesite calcular el monto puede reutilizar la regla sin depender artificialmente de MultaService.

### Punto de decisión 2 — Conteo de multas pendientes: ¿consulta o filtrado en memoria?
La restricción que impide que un estudiante acumule más de tres multas pendientes necesita información persistida. Por esta razón, la decisión de negocio se encuentra en MultaService, pero el dato requerido se obtiene mediante: **countByEstudianteIdAndEstado(...)** en MultaRepository.

Se descartó recuperar todas las multas del estudiante y posteriormente filtrarlas mediante Streams de Java porque ese enfoque trasladaría innecesariamente datos desde la base de datos hacia la memoria de la aplicación. Una operación COUNT ejecutada por el motor de base de datos solamente devuelve el valor que necesita la regla de negocio. Esta diferencia sería especialmente importante si el historial de un estudiante contuviera cientos o miles de multas.

Por tanto, la separación adoptada es deliberada: el Repository obtiene eficientemente el dato; el Service toma la decisión de negocio usando ese dato.

## Estructura de paquetes — Parte 1

La primera parte del proyecto se organiza siguiendo arquitectura en capas. 
El paquete `controller` concentra la capa de presentación y expone los endpoints REST. 
El paquete `service` implementa la lógica de aplicación y coordina los casos de uso. 
El paquete `model` contiene la entidad del dominio y las excepciones de negocio. 
El paquete `repository` encapsula el acceso a datos mediante Spring Data JPA.

## Diagrama de estructura de paquetes — Parte 1

![Diagrama de paquetes Parte 1](evidencias/parte-1/Diagrama-paquetes.png)