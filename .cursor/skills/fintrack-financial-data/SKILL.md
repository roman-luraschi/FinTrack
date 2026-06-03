---
name: fintrack-financial-data
description: >-
  Enforces sensitive handling of financial transactions in FinTrack: deduplication,
  traceability, source attribution, soft delete, idempotent sync, change history,
  auditability, and skepticism toward external APIs. Use when persisting Transaction
  data, sync, imports, Room migrations, or any financial write path in FinTrack.
---

# FinTrack — datos financieros sensibles

## Requisitos (texto del usuario)

Estás desarrollando software financiero.

Toda transacción debe ser tratada como información sensible.

Requisitos:

- Evitar duplicados.
- Mantener trazabilidad.
- Registrar origen de cada movimiento.
- Nunca eliminar datos automáticamente.
- Utilizar soft delete cuando sea apropiado.
- Diseñar procesos idempotentes.

Para sincronizaciones:

- Detectar duplicados mediante externalId.
- Si no existe externalId utilizar:
  - amount
  - merchant
  - timestamp

Mantener historial de cambios.

Toda operación financiera debe poder auditarse.

Priorizar consistencia de datos sobre velocidad.

Nunca asumir que una API externa devuelve datos perfectos.

---

## Reglas operativas

| Requisito | Implementación |
|-----------|----------------|
| Información sensible | Sin logs de montos/descripciones en producción; cifrado at-rest si el SO lo permite; permisos mínimos |
| Trazabilidad + origen | Campo `source` obligatorio; conservar payload crudo de import/sync en tabla de staging o `sync_metadata` |
| Sin borrado automático | Jobs de retención solo con acción explícita del usuario o política documentada y configurable |
| Soft delete | `deletedAt` / `isDeleted`; queries por defecto excluyen borrados; purga física solo manual |
| Idempotencia | Misma clave de deduplicación → mismo resultado sin segunda fila; workers reintentables |
| Historial | Tabla o eventos `TransactionChange` (quién/qué/cuándo/valores anteriores) en updates críticos |
| Auditoría | Correlación por `operationId` en imports/sync; timestamps UTC en `createdAt`/`updatedAt` |
| Consistencia > velocidad | Transacciones Room/@Transaction; validar antes de commit; UI optimista solo con rollback claro |
| APIs imperfectas | Validar schema, rangos, fechas; normalizar en capa Data; marcar `needsReview` si ambiguo |

---

## Deduplicación

**Orden de matching (sync/import):**

1. `externalId` + `source` → match fuerte; actualizar si existe, insertar si no.
2. Sin `externalId` → clave débil: `amount` + `merchant` (o `description` normalizada) + `timestamp` (ventana configurable, ej. ±2 min).
3. Colisión débil → no fusionar automáticamente; flag `DUPLICATE_CANDIDATE` para revisión.

Nunca sobrescribir una transacción confirmada por el usuario sin registro en historial.

---

## Modelo alineado con FinTrack

Ver skill **`fintrack-android`**: entidad `Transaction` (`externalId`, `source`, `amount`, `description`, fechas).

- `merchant` puede mapearse a `description` normalizada o campo dedicado cuando exista.
- Imports CSV/XLSX/OCR/API: siempre asignar `source` y `externalId` cuando el origen lo provea.

---

## Checklist antes de merge

- [ ] ¿Write path idempotente y con deduplicación documentada?
- [ ] ¿Soft delete en lugar de `DELETE` físico?
- [ ] ¿Cambios auditables (historial o log estructurado)?
- [ ] ¿Validación de datos externos antes de persistir?
- [ ] ¿Ningún cleanup automático de transacciones?
