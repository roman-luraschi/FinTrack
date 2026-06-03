package com.fintrack.core.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    val argentinaZone: ZoneId = ZoneId.of("America/Argentina/Buenos_Aires")

    private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es-AR"))
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", Locale.forLanguageTag("es-AR"))

    fun formatDate(instant: Instant): String =
        instant.atZone(argentinaZone).format(dateFormatter)

    fun formatDateTime(instant: Instant): String =
        instant.atZone(argentinaZone).format(dateTimeFormatter)

    fun startOfMonth(instant: Instant = Instant.now()): Instant {
        val localDate = instant.atZone(argentinaZone).toLocalDate()
        return localDate.withDayOfMonth(1).atStartOfDay(argentinaZone).toInstant()
    }

    fun endOfMonth(instant: Instant = Instant.now()): Instant {
        val localDate = instant.atZone(argentinaZone).toLocalDate()
        return localDate
            .withDayOfMonth(localDate.lengthOfMonth())
            .plusDays(1)
            .atStartOfDay(argentinaZone)
            .toInstant()
            .minusMillis(1)
    }

    fun startOfWeek(instant: Instant = Instant.now()): Instant {
        val localDate = instant.atZone(argentinaZone).toLocalDate()
        val monday = localDate.minusDays((localDate.dayOfWeek.value - 1).toLong())
        return monday.atStartOfDay(argentinaZone).toInstant()
    }

    fun endOfWeek(instant: Instant = Instant.now()): Instant {
        val start = startOfWeek(instant).atZone(argentinaZone).toLocalDate()
        return start.plusDays(7).atStartOfDay(argentinaZone).toInstant().minusMillis(1)
    }

    fun localDateToInstant(date: LocalDate): Instant =
        date.atStartOfDay(argentinaZone).toInstant()

    fun instantToLocalDate(instant: Instant): LocalDate =
        instant.atZone(argentinaZone).toLocalDate()
}
