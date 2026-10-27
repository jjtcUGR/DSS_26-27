# Carrito de la compra

Aplicación web de la Práctica 1, desarrollada con Java 21 y Spring Boot 4.1.2.

## Requisitos

- JDK 21.
- Conexión a Internet la primera vez que se use Maven Wrapper, para descargar Maven y las dependencias.

## Configuración y ejecución

La aplicación requiere una contraseña de administrador configurada en el entorno. En PowerShell:

```powershell
$env:CARRITO_ADMIN_PASSWORD = "elige-una-clave-local"
.\mvnw.cmd spring-boot:run
```

El usuario predeterminado es `admin`; puede cambiarse con `CARRITO_ADMIN_USERNAME`. No se guarda una contraseña predeterminada en el repositorio. La clave de ejemplo debe mantenerse local y no publicarse.

Abre <http://localhost:8080>. Para iniciar sesión, usa el usuario configurado y la contraseña que acabas de establecer.

## Funcionalidades

- Catálogo público en `/products`.
- Gestión de productos (alta, edición y eliminación) para el rol `ADMIN`.
- Carrito de compra asociado a la sesión.
- Exportación del catálogo en SQL desde `/admin/export`.
- Persistencia con H2 en memoria; los datos se reinician al detener la aplicación.

Para compilar y ejecutar las pruebas:

```powershell
.\mvnw.cmd clean verify
```

En Linux o macOS se puede usar `sh ./mvnw ...`.
