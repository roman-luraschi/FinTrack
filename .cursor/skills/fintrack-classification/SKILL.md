---
name: fintrack-classification
description: >-
  Defines FinTrack's fully offline three-tier expense classifier: Rules Engine,
  Learning Engine from user corrections, and Similarity Engine with normalization
  and fuzzy matching. Forbids external AI APIs. Use when categorizing transactions,
  merchant mapping, classification rules, or category learning in FinTrack.
---

# FinTrack — clasificación offline

## Motor de clasificación (texto del usuario)

La aplicación utiliza un motor de clasificación de gastos totalmente offline.

No utilizar:

- OpenAI
- Gemini
- Claude
- APIs externas

La clasificación se compone de tres niveles.

Nivel 1:

Rules Engine

Ejemplos:

MCDONALDS -> Comida
UBER -> Transporte
SPOTIFY -> Streaming

Nivel 2:

Learning Engine

Cuando el usuario corrige una categoría:

Guardar la relación.

Ejemplo:

LA FAROLA -> Comida

La próxima vez clasificar automáticamente.

Nivel 3:

Similarity Engine

Implementar:

- Normalización
- Eliminación de acentos
- Eliminación de caracteres especiales
- Fuzzy Matching

Objetivos:

- Funcionar offline.
- Aprender del usuario.
- Mejorar precisión con el tiempo.

Priorizar simplicidad y mantenibilidad sobre modelos complejos de Machine Learning.

---

## Pipeline de decisión

Ejecutar en orden; detener en el primer match con confianza suficiente:

```
descripción/merchant normalizado
  → Nivel 1: Rules (match exacto / prefijo / regex simple)
  → Nivel 2: Learning (lookup merchant → category aprendida)
  → Nivel 3: Similarity (fuzzy vs reglas + aprendizajes)
  → sin match: categoría por defecto o "Sin clasificar" + sugerencia UI
```

Registrar `classificationSource`: `RULE` | `LEARNED` | `SIMILARITY` | `DEFAULT` para trazabilidad (ver **`fintrack-financial-data`**).

---

## Capas sugeridas (Domain / Data)

| Componente | Responsabilidad |
|------------|-----------------|
| `MerchantNormalizer` | lower, trim, quitar acentos (NFD), alfanumérico, colapsar espacios |
| `RulesEngine` | Tabla Room `classification_rules` (pattern → category); seed con defaults AR |
| `LearningEngine` | `user_merchant_category` (merchantNormalized, categoryId, updatedAt) |
| `SimilarityEngine` | Levenshtein/ratio con umbral; candidatos solo contra reglas + aprendizajes |
| `ClassifyExpenseUseCase` | Orquesta pipeline; sin dependencias de red |

**Prohibido:** SDKs de LLM, HTTP a servicios de IA, embeddings en la nube.

---

## Learning Engine — reglas

- Corrección del usuario → upsert por `merchantNormalized` (no duplicar filas).
- Corrección explícita **siempre** gana sobre Similarity en la siguiente clasificación (Nivel 2 antes de 3).
- Permitir que el usuario revierta o edite un aprendizaje (soft delete en aprendizajes).

---

## Similarity Engine — límites

- Umbral configurable (ej. ≥ 0.85) para auto-asignar; debajo → sugerir, no auto-commit.
- Comparar contra conjunto acotado (reglas + learned), no contra todo el histórico de transacciones.
- Sin ML on-device pesado (TFLite, etc.) salvo petición explícita del producto.

---

## Persistencia

- Reglas y aprendizajes en **Room** (misma DB local, offline-first).
- WorkManager solo para tareas batch opcionales (reindexar), nunca para llamar APIs de clasificación.

---

## Checklist

- [ ] ¿Pipeline 1 → 2 → 3 sin llamadas externas?
- [ ] ¿Normalización compartida entre los tres niveles?
- [ ] ¿Corrección del usuario persiste y aplica en Nivel 2?
- [ ] ¿Fuzzy con umbral y sin auto-clasificar ambiguo?
- [ ] ¿Sin OpenAI/Gemini/Claude ni APIs externas de IA?
