package com.fintrack.core.data.notification.parser

import com.fintrack.core.data.dto.BankNotificationDto

internal object NotificationFixtureLoader {
    fun load(fileName: String): BankNotificationDto {
        val stream = NotificationFixtureLoader::class.java.classLoader
            ?.getResourceAsStream("notifications/$fileName")
            ?: error("Fixture no encontrado: $fileName")
        val json = stream.reader().readText()
        return parse(json)
    }

    private fun parse(json: String): BankNotificationDto = BankNotificationDto(
        packageName = json.requireField("packageName"),
        notificationId = json.requireField("notificationId"),
        postedAt = json.requireLongField("postedAt"),
        title = json.requireField("title"),
        text = json.requireField("text"),
        bigText = json.optionalField("bigText"),
        subText = json.optionalField("subText"),
        channelId = json.optionalField("channelId"),
    )

    private fun String.requireField(field: String): String =
        optionalField(field) ?: error("Campo requerido ausente: $field")

    private fun String.requireLongField(field: String): Long =
        optionalLongField(field) ?: error("Campo numérico requerido ausente: $field")

    private fun String.optionalField(field: String): String? {
        val pattern = Regex(""""$field"\s*:\s*(?:"([^"]*)"|null)""")
        return pattern.find(this)?.groupValues?.get(1)
    }

    private fun String.optionalLongField(field: String): Long? {
        val stringPattern = Regex(""""$field"\s*:\s*"(\d+)"""")
        stringPattern.find(this)?.groupValues?.get(1)?.toLongOrNull()?.let { return it }
        val numberPattern = Regex(""""$field"\s*:\s*(\d+)""")
        return numberPattern.find(this)?.groupValues?.get(1)?.toLongOrNull()
    }
}
