Proyecto - Aplicación de consola (Literatura) - Entrega final
Generado: 2025-08-11T20:24:57.350798Z

Configuración:
- Java 17
- Maven
- Spring Boot 3.x

Instrucciones:
1) Importá el proyecto en IntelliJ como Maven project.
2) Desde la terminal del proyecto ejecutá:
   mvn clean package -DskipTests
   mvn spring-boot:run

El menú en consola dispone de 10 opciones (buscar, listar, top10, formatos por ID, etc.).
La opción 9 ("Ver formatos de un libro por ID") consulta el endpoint /books/<built-in function id> y muestra los formatos disponibles.
