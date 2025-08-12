Hecho por Tomás Solá para el desafío de literatura del curso de Alura Latam con Oracle.

LiteraturApp - Versión final corregida
=====================================

Cambios aplicados:
- Menú visual mejorado con bordes y colores ANSI.
- Opción 5: Buscar libros por idioma. Lista idiomas extraídos de los libros en cache, orden alfabético,
  permite selección por número o por código (ej: 'es' o '1').
- Tablas alineadas y estéticas para listados de libros y autores.
- Opción 'Salir' muestra 'Hasta luego 👋' y cierra inmediatamente.
- README adicional con instrucciones de ejecución.

Cómo usar:
1. Abrir el proyecto en IntelliJ (Java 17).
2. Ejecutar la aplicación principal (clase con @SpringBootApplication).
3. Interactuar por consola con el menú.

Notas importantes:
- Las mejoras se aplicaron sobre ConsoleService.java. Si deseas integrarlas de forma distinta,
  revisá src/main/java/com/literaturaapp/service/ConsoleService.java (se creó una copia .orig con el original).
- Si en tu entorno la consola no interpreta códigos ANSI, los colores no se verán; el funcionamiento seguirá igual.
- Verifica conexión a Internet para que la app pueda consultar la API Gutendex.

Posibles errores y soluciones:
- Error de conexión: comprobar que tienes acceso a internet y que gutendex.com no esté bloqueado.
- Compilación: asegurate de usar Java 17 y ejecutar 'mvn clean package' si es necesario.