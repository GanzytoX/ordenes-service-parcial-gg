# Ordenes Service

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
| Ordenes Service | `8082` |

## Endpoints

| Método | Ruta                           | Descripción                    |
| ------ | ------------------------------ | ------------------------------ |
| `POST` | `/ordenes`                     | Crear una orden                |
| `GET`  | `/ordenes/{id}`                | Obtener orden por ID           |
| `GET`  | `/ordenes/usuario/{usuarioId}` | Listar órdenes de un usuario   |
| `PUT`  | `/ordenes/{id}/status`         | Actualizar estado de una orden |

### Estados posibles

`PENDIENTE` · `PROCESADA` · `CANCELADA`

### Ejemplo de body (POST)

```json
{
  "usuarioId": "user-001",
  "items": [
    {
      "productoId": "abc123",
      "cantidad": 2,
      "precio": 1500.0
    }
  ]
}
```

## Variables de entorno

| Variable                | Descripción                    | Default                             |
| ----------------------- | ------------------------------ | ----------------------------------- |
| `MONGODB_URI`           | URI de conexión a MongoDB      | `mongodb://localhost:27030/ordenes` |
| `EUREKA_URI`            | URL del servidor Eureka        | `http://localhost:8761/eureka`      |
| `AWS_ACCESS_KEY_ID`     | Credencial AWS                 | `test`                              |
| `AWS_SECRET_ACCESS_KEY` | Credencial AWS                 | `test`                              |
| `AWS_DEFAULT_REGION`    | Región AWS                     | `us-east-1`                         |
| `AWS_ENDPOINT_URL`      | Endpoint override (LocalStack) | —                                   |

## CloudWatch

Los logs se envían al log group `ordenes-log-group`.

```bash
aws --endpoint-url=http://localhost:4566 logs describe-log-streams \
  --log-group-name ordenes-log-group --region us-east-1
```

## Ejecución local

```bash
mvn spring-boot:run
```

## Ejecución con Docker Compose

```bash
docker compose up --build ordenes-service
```
