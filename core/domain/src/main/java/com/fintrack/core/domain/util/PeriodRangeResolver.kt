package com.fintrack.core.domain.util

import com.fintrack.core.domain.model.DashboardPeriod
import java.time.Instant
import java.time.ZoneId

object PeriodRangeResolver {
    val argentinaZone: ZoneId = ZoneId.of("America/Argentina/Buenos_Aires")

    fun resolve(period: DashboardPeriod, reference: Instant = Instant.now()): Pair<Instant, Instant> =
        when (period) {
            DashboardPeriod.WEEK -> startOfWeek(reference) to endOfWeek(reference)
            DashboardPeriod.MONTH -> startOfMonth(reference) to endOfMonth(reference)
        }

    fun startOfMonth(instant: Instant): Instant {
        val localDate = instant.atZone(argentinaZone).toLocalDate()
        return localDate.withDayOfMonth(1).atStartOfDay(argentinaZone).toInstant()
    }

    fun endOfMonth(instant: Instant): Instant {
        val localDate = instant.atZone(argentinaZone).toLocalDate()
        return localDate
            .withDayOfMonth(localDate.lengthOfMonth())
            .plusDays(1)
            .atStartOfDay(argentinaZone)
            .toInstant()
            .minusMillis(1)
    }

    fun startOfWeek(instant: Instant): Instant {
        val localDate = instant.atZone(argentinaZone).toLocalDate()
        val monday = localDate.minusDays((localDate.dayOfWeek.value - 1).toLong())
        return monday.atStartOfDay(argentinaZone).toInstant()
    }

    fun endOfWeek(instant: Instant): Instant {
        val start = startOfWeek(instant).atZone(argentinaZone).toLocalDate()
        return start.plusDays(7).atStartOfDay(argentinaZone).toInstant().minusMillis(1)
    }

    fun startOfDay(instant: Instant): Instant {
        val localDate = instant.atZone(argentinaZone).toLocalDate()
        return localDate.atStartOfDay(argentinaZone).toInstant()
    }

    fun endOfDay(instant: Instant): Instant {
        val localDate = instant.atZone(argentinaZone).toLocalDate()
        return localDate.plusDays(1).atStartOfDay(argentinaZone).toInstant().minusMillis(1)
    }
}
