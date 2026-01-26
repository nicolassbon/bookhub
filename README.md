# 📚 Social Books Platform (Legacy Monolith)

> ⚠️ **Versión Académica Archivada** - Proyecto original con Spring MVC tradicional (2024)  
> **👉 Ver versión moderna:** [Spring Boot REST API en branch main](link)

Plataforma social interactiva para amantes de la lectura. Permite a los usuarios descubrir libros, publicar reseñas, seguir amigos, desbloquear logros y suscribirse a planes premium mediante MercadoPago.

---

## 🚀 Características del Negocio

### 👤 Gestión de Usuarios
- Registro e inicio de sesión
- Recuperación de contraseña vía Email
- Perfil de usuario personalizable
- Sistema de **Onboarding** para nuevos usuarios

### 📖 Biblioteca y Contenido
- Catálogo de **Libros** filtrable por género y autor
- Sistema de **Reseñas** y **Comentarios**
- **Publicaciones** en muro (Social Feed)
- Interacción mediante **Likes/Dislikes**

### 🤝 Aspecto Social
- Sistema de **Amistad** (Seguir/Dejar de seguir)
- Listado de amigos y actividad reciente
- Notificaciones de actividad

### 🏆 Gamificación y Monetización
- Sistema de **Logros** y **Desafíos** (Badges)
- Suscripción a **Planes Premium** (Bronce/Plata/Oro)
- Integración de pagos con **MercadoPago SDK** (webhooks)

---

## 🛠 Tech Stack (Legacy)

- **Lenguaje:** Java 11
- **Framework:** Spring MVC 5.2.22
- **ORM:** Hibernate 5.4 (Session Management manual)
- **Base de Datos:** MySQL 8 (Prod) / HSQLDB (Dev)
- **Vistas:** Thymeleaf 3 + Bootstrap 5
- **Testing:** JUnit 5, Mockito, Playwright
- **Build:** Maven 3.8

---

## 🎯 Highlights Técnicos

- ✅ **22 entidades JPA** con relaciones complejas (many-to-many con atributos, auto-referencias, polimorfismo)
- ✅ **Arquitectura DDD** de 3 capas desacopladas (Presentación, Dominio, Infraestructura)
- ✅ **187 archivos de test** (Unitarios + Integración + E2E)
- ✅ **Feature Flags** para control de acceso granular según plan
- ✅ **Webhooks** de MercadoPago para confirmación automática de pagos
- ✅ **Sistema de notificaciones** tipado con estados (leída/no leída)
- ✅ **Patrones de diseño:** Repository, Service Layer, DTO, Interceptors

---

## 📌 Estado del Proyecto

**Este branch se mantiene como referencia histórica del proyecto académico original.**

- ❌ No está configurado para ejecutarse (dependencias deprecadas, BD no incluida)
- ✅ Código disponible para revisión de arquitectura y patrones
- ✅ Demuestra conocimiento de Spring MVC tradicional
- ✅ Punto de partida antes de la migración a Spring Boot

**Para ver la aplicación funcionando → [versión moderna en `main`](link)**

---

## 🔧 Configuración Original (Referencia)
```bash
# Comando original para levantar
mvn clean jetty:run
```

**Requisitos originales:**
- MySQL 8 con schema personalizado
- Java 11
- Maven 3.8+

> ⚠️ **Nota:** Esta configuración no está mantenida activamente.

---

## 📂 Estructura de Paquetes
```
com.tallerwebi/
├── dominio/          # Entidades JPA, Excepciones, Interfaces de Repositorio
├── infraestructura/  # Implementaciones DAO, Servicios externos, Configuración
└── presentacion/     # Controllers MVC, DTOs, Helpers
```

---

## 🔄 Migración a Spring Boot

Este proyecto fue migrado a arquitectura moderna REST API.

| Aspecto | Legacy (esta versión) | Moderno (main) |
|---------|----------------------|----------------|
| Framework | Spring MVC 5.2 | Spring Boot 3.x |
| Arquitectura | Monolito MVC | REST API |
| Vistas | Thymeleaf | JSON (Swagger docs) |
| Autenticación | Session-based | JWT + Spring Security |
| Base de Datos | MySQL | PostgreSQL |
| Deploy | WAR tradicional | Docker + Railway |

**[👉 Ver versión migrada](link-a-main)**

---

## 📝 Licencia

Proyecto académico - Universidad Nacional de La Matanza (UNLaM)