package com.fintrack.core.domain.util

import com.fintrack.core.domain.model.DashboardPeriod
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class PeriodRangeResolverTest {

    @Test
    fun monthRange_coversFullCalendarMonth() {
        val reference = LocalDate.of(2025, 6, 15)
            .atStartOfDay(PeriodRangeResolver.argentinaZone)
            .toInstant()
        val (start, end) = PeriodRangeResolver.resolve(DashboardPeriod.MONTH, reference)

        assertTrue(start <= reference)
        assertTrue(end >= reference)
        val startLocal = start.atZone(PeriodRangeResolver.argentinaZone).toLocalDate()
        val endLocal = end.atZone(PeriodRangeResolver.argentinaZone).toLocalDate()
        assertTrue(startLocal.dayOfMonth == 1)
        assertTrue(endLocal.dayOfMonth >= 15)
    }

    @Test
    fun dayRange_coversFullCalendarDay() {
        val reference = LocalDate.of(2025, 6, 15)
            .atTime(14, 30)
            .atZone(PeriodRangeResolver.argentinaZone)
            .toInstant()
        val start = PeriodRangeResolver.startOfDay(reference)
        val end = PeriodRangeResolver.endOfDay(reference)

        assertTrue(start <= reference)
        assertTrue(end >= reference)
        val startLocal = start.atZone(PeriodRangeResolver.argentinaZone).toLocalDate()
        val endLocal = end.atZone(PeriodRangeResolver.argentinaZone).toLocalDate()
        assertTrue(startLocal.dayOfMonth == 15)
        assertTrue(endLocal.dayOfMonth == 15)
    }

    @Test
    fun weekRange_startsOnMonday() {
        val wednesday = LocalDate.of(2025, 6, 4)
            .atTime(12, 0)
            .toInstant(ZoneOffset.UTC)
        val (start, _) = PeriodRangeResolver.resolve(DashboardPeriod.WEEK, wednesday)
        val startDay = start.atZone(PeriodRangeResolver.argentinaZone).dayOfWeek.value
        assertTrue(startDay == 1)
    }
}
