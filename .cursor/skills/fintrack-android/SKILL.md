---
name: fintrack-android
description: >-
  Guides development of FinTrack, an offline-first personal finance Android app for
  Argentina. Covers Transaction model, movement sources (Mercado Pago, notifications,
  OCR, CSV/XLSX, manual), Room, WorkManager, Hilt, Compose, and local-first privacy.
  Use when working in the FinTrack repo, financial transactions, expense automation,
  or Argentine fintech integrations.
---

# FinTrack — dominio y producto

## Contexto del producto (texto del usuario)

Estás desarrollando una aplicación Android llamada FinTrack.

FinTrack es un asistente financiero personal para Argentina.

Objetivo principal:

Reducir al máximo la carga manual de gastos.

Fuentes de movimientos:

- Mercado Pago API
- Notificaciones Android
- OCR de tickets
- Importación CSV
- Importación XLSX
- Carga manual

La aplicación es:

- Offline First
- Local First
- Privacy First

La base de datos local es la fuente principal de verdad.

Tecnologías:

- Room Database
- WorkManager
- Hilt
- Kotlin
- Jetpack Compose

Modelo principal:

Transaction

Campos:

- id
- externalId
- amount
- description
- category
- subcategory
- source
- accountId
- transactionDate
- notes
- createdAt
- updatedAt

Sources posibles:

- MANUAL
- MERCADO_PAGO_API
- BANK_NOTIFICATION
- CSV_IMPORT
- XLSX_IMPORT
- OCR

Siempre diseñar pensando en futuras integraciones sin implementarlas anticipadamente.

---

## Principios de implementación

| Principio | Implicación |
|-----------|-------------|
| **Local First** | Room es source of truth; red/sync es secundario |
| **Offline First** | UI y reglas de negocio funcionan sin red; WorkManager para sync diferido |
| **Privacy First** | Datos sensibles en dispositivo; minimizar telemetría y exposición |
| **Menos carga manual** | Priorizar ingestión automática (notificaciones, imports, API) sobre formularios largos |

**Integraciones futuras:** definir interfaces en Domain (`TransactionSource`, parsers, sync) y enums estables; no construir flujos completos de Mercado Pago/OCR/banco hasta que el feature esté en scope.

---

## Transaction — contrato

| Campo | Notas |
|-------|--------|
| `id` | PK local (Room) |
| `externalId` | ID del origen externo; nullable para `MANUAL`; clave de deduplicación |
| `amount` | Monto; convención de signo documentada en el módulo (gasto/ingreso) |
| `description` | Texto visible |
| `category` / `subcategory` | Taxonomía local; normalizar en Use Cases |
| `source` | Uno de los `Sources` listados arriba |
| `accountId` | FK a cuenta/wallet local |
| `transactionDate` | Fecha del movimiento (negocio) |
| `notes` | Opcional |
| `createdAt` / `updatedAt` | Auditoría local |

**Deduplicación:** mismo `externalId` + `source` → no duplicar; resolver conflictos en capa Data/Use Case.

---

## Fuentes de movimientos — alcance por capa

| Source | Ingestión típica | Estado inicial |
|--------|------------------|----------------|
| `MANUAL` | UI → Use Case → Repository | Implementar primero |
| `CSV_IMPORT` / `XLSX_IMPORT` | Parser en Data, job WorkManager | Contrato de columnas; sin UI pesada |
| `MERCADO_PAGO_API` | Remote datasource + sync | Interface + stub; sin OAuth hasta feature |
| `BANK_NOTIFICATION` | NotificationListener / parser | Interface; permisos y parsing por fases |
| `OCR` | Camera/galería → texto → mapper | Interface; ML/OCR como módulo opcional |

---

## Stack y arquitectura

Aplicar también la skill personal **`android-kotlin-architecture`** cuando haya que generar código: arquitectura y carpetas **antes** de implementar.

```
app/
feature/
  transactions/
    domain/     # Transaction, enums, Use Cases, repo interfaces
    data/       # Room entities/DAOs, mappers, import parsers
    presentation/
  sync/         # WorkManager workers, colas (cuando existan)
core/
  database/     # Room, migraciones
  di/           # Hilt modules
```

- **Room:** entidades, DAOs, migraciones versionadas; nunca lógica de negocio en DAOs.
- **WorkManager:** sync, imports pesados, reintentos; sin bloquear UI.
- **Argentina:** montos en ARS; formatos de fecha/locale `es-AR`; considerar impuestos/percepciones solo cuando el producto lo pida.

---

## Checklist por feature

- [ ] ¿Persiste primero en Room y la UI lee del repositorio local?
- [ ] ¿`source` y `externalId` permiten trazabilidad y deduplicación?
- [ ] ¿La integración nueva es interfaz + enum, sin acoplar Presentation al proveedor?
- [ ] ¿WorkManager para trabajo en background, no corrutinas fire-and-forget sueltas?
- [ ] ¿Arquitectura explicada antes del código (skill Android)?
