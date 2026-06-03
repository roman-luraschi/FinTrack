package com.fintrack.core.database.seed

import com.fintrack.core.database.entity.CategoryEntity
import com.fintrack.core.database.entity.ClassificationRuleEntity
import com.fintrack.core.domain.model.MatchType
import java.time.Instant

object DatabaseSeedData {
    fun categories(now: Instant = Instant.now()): List<CategoryEntity> = listOf(
        CategoryEntity(id = 1, name = "Comida", sortOrder = 1, isSystem = true),
        CategoryEntity(id = 2, name = "Supermercado", sortOrder = 2, isSystem = true),
        CategoryEntity(id = 3, name = "Transporte", sortOrder = 3, isSystem = true),
        CategoryEntity(id = 4, name = "Salud", sortOrder = 4, isSystem = true),
        CategoryEntity(id = 5, name = "Educación", sortOrder = 5, isSystem = true),
        CategoryEntity(id = 6, name = "Streaming", sortOrder = 6, isSystem = true),
        CategoryEntity(id = 7, name = "Servicios", sortOrder = 7, isSystem = true),
        CategoryEntity(id = 8, name = "Impuestos", sortOrder = 8, isSystem = true),
        CategoryEntity(id = 9, name = "Entretenimiento", sortOrder = 9, isSystem = true),
        CategoryEntity(id = 10, name = "Ropa", sortOrder = 10, isSystem = true),
        CategoryEntity(id = 11, name = "Hogar", sortOrder = 11, isSystem = true),
        CategoryEntity(id = 12, name = "Tecnología", sortOrder = 12, isSystem = true),
        CategoryEntity(id = 13, name = "Ingresos", sortOrder = 13, isSystem = true),
        CategoryEntity(id = 14, name = "Sin clasificar", sortOrder = 99, isSystem = true),
    )

    fun classificationRules(now: Instant = Instant.now()): List<ClassificationRuleEntity> = listOf(
        rule("MCDONALDS", MatchType.CONTAINS, 1, 100, now),
        rule("BURGER KING", MatchType.CONTAINS, 1, 100, now),
        rule("PEDIDOSYA", MatchType.CONTAINS, 1, 90, now),
        rule("RAPPI", MatchType.CONTAINS, 1, 90, now),
        rule("LA FAROLA", MatchType.CONTAINS, 1, 95, now),
        rule("CARREFOUR", MatchType.CONTAINS, 2, 100, now),
        rule("COTO", MatchType.CONTAINS, 2, 100, now),
        rule("JUMBO", MatchType.CONTAINS, 2, 100, now),
        rule("DISCO", MatchType.CONTAINS, 2, 90, now),
        rule("VEA", MatchType.CONTAINS, 2, 90, now),
        rule("DIA", MatchType.PREFIX, 2, 80, now),
        rule("UBER", MatchType.CONTAINS, 3, 100, now),
        rule("CABIFY", MatchType.CONTAINS, 3, 95, now),
        rule("SUBE", MatchType.CONTAINS, 3, 95, now),
        rule("YPF", MatchType.CONTAINS, 3, 90, now),
        rule("SHELL", MatchType.CONTAINS, 3, 90, now),
        rule("SPOTIFY", MatchType.CONTAINS, 6, 100, now),
        rule("NETFLIX", MatchType.CONTAINS, 6, 100, now),
        rule("DISNEY", MatchType.CONTAINS, 6, 90, now),
        rule("MERCADOLIBRE", MatchType.CONTAINS, 12, 100, now),
        rule("MERCADO LIBRE", MatchType.CONTAINS, 12, 100, now),
        rule("AMAZON", MatchType.CONTAINS, 12, 90, now),
        rule("FARMACITY", MatchType.CONTAINS, 4, 95, now),
        rule("OSDE", MatchType.CONTAINS, 4, 95, now),
        rule("SWISS MEDICAL", MatchType.CONTAINS, 4, 95, now),
        rule("EDESUR", MatchType.CONTAINS, 7, 90, now),
        rule("EDENOR", MatchType.CONTAINS, 7, 90, now),
        rule("METrogas", MatchType.CONTAINS, 7, 90, now),
        rule("PERSONAL", MatchType.CONTAINS, 7, 85, now),
        rule("MOVISTAR", MatchType.CONTAINS, 7, 85, now),
        rule("CLARO", MatchType.CONTAINS, 7, 85, now),
        rule("AFIP", MatchType.CONTAINS, 8, 100, now),
        rule("CINE", MatchType.CONTAINS, 9, 80, now),
        rule("ZARA", MatchType.CONTAINS, 10, 90, now),
        rule("FALABELLA", MatchType.CONTAINS, 10, 90, now),
    )

    private fun rule(
        pattern: String,
        matchType: MatchType,
        categoryId: Long,
        priority: Int,
        now: Instant,
    ): ClassificationRuleEntity = ClassificationRuleEntity(
        pattern = pattern,
        matchType = matchType,
        categoryId = categoryId,
        priority = priority,
        isActive = true,
        createdAt = now,
    )
}
