# Order Service (Sales & Transactions)

Gestiona el proceso de compra, desde la creación de la orden hasta la confirmación definitiva del pedido.

## Especificaciones
* **Puerto:** `8084`
* **Base de Datos:** `order_db` (PostgreSQL)

## Estados de Orden
1. **PENDING:** Orden creada, esperando validación de pago.
2. **CONFIRMED:** Pago validado y orden lista para procesamiento.

## Endpoints Principales
| Método | Endpoint 			   | Descripción  						| Acceso       |
| :--- 	 | :--- 				   | :--- 				  				| :--- 		   |
| `POST` | `/api/orders` 		   | Crea una nueva orden 				| Usuario Auth |
| `GET`  | `/api/orders/user/{id}` | Historial de órdenes del usuario 	| Usuario Auth |

## Instalación
1. Crear base de datos `order_db`.
2. Configuraciones en `src/main/resources/application.yml`, `src/main/resources/application-dev.yml`, `src/main/resources/application-local.yml`.
3. Asegurarse de tener el `user-service` activo para validación de tokens.
4. Ejecutar: `mvn spring-boot:run`.

