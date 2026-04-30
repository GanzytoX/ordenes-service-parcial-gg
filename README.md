# Orders Service

Microservicio para la gestión de órdenes de compra. Permite crear y consultar órdenes asociadas a un usuario. Los logs son enviados a **AWS CloudWatch** (o LocalStack en desarrollo).

## Tecnologías

- Java 17
- Spring Boot 3.5.11
- Spring Data MongoDB
- Spring Cloud Netflix Eureka Client
- AWS SDK v2 (CloudWatch Logs)
- Lombok

## Puerto

| Servicio        | Puerto |
| --------------- | ------ |
| Orders Service | `8082` |

## Endpoints

| Método | Ruta                           | Descripción                    |
| ------ | ------------------------------ | ------------------------------ |
| `POST` | `/orders`                     | Crear una order                |
| `GET`  | `/orders/{id}`                | Obtener order por ID           |
| `GET`  | `/orders/usuario/{userId}` | Listar órdenes de un usuario   |
| `PUT`  | `/orders/{id}/status`         | Actualizar estado de una order |

### Estados posibles

`PENDIENTE` · `PROCESADA` · `CANCELADA`

### Ejemplo de body (POST)

```json
{
  "userId": "user-001",
  "items": [
    {
      "productoId": "abc123",
      "cantidad": 2,
      "price": 1500.0
    }
  ]
}
```

## Variables de entorno

| Variable                | Descripción                    | Default                             |
| ----------------------- | ------------------------------ | ----------------------------------- |
| `MONGODB_URI`           | URI de conexión a MongoDB      | `mongodb://localhost:27030/orders` |
| `EUREKA_URI`            | URL del servidor Eureka        | `http://localhost:8761/eureka`      |
| `AWS_ACCESS_KEY_ID`     | Credencial AWS                 | `test`                              |
| `AWS_SECRET_ACCESS_KEY` | Credencial AWS                 | `test`                              |
| `AWS_DEFAULT_REGION`    | Región AWS                     | `us-east-1`                         |
| `AWS_ENDPOINT_URL`      | Endpoint override (LocalStack) | —                                   |

## CloudWatch

Los logs se envían al log group `orders-log-group`.

```bash
aws --endpoint-url=http://localhost:4566 logs describe-log-streams \
  --log-group-name orders-log-group --region us-east-1
```

## Ejecución local

```bash
mvn spring-boot:run
```

## Ejecución con Docker Compose

```bash
docker compose up --build ordenes-service
```
