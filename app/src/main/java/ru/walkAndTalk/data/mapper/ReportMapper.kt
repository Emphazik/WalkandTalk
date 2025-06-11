package ru.walkAndTalk.data.mapper

import ru.walkAndTalk.data.model.ReportDto
import ru.walkAndTalk.domain.model.Report

fun ReportDto.toDomain(contentTypeName: String): Report {
    return Report(
        id = id,
        contentType = contentTypeName,
        contentId = contentId,
        userId = userId,
        reason = reason,
        createdAt = createdAt
    )
}

fun Report.toDto(contentTypeId: String): ReportDto {
    return ReportDto(
        id = id,
        contentTypeId = contentTypeId,
        contentId = contentId,
        userId = userId,
        reason = reason,
        createdAt = createdAt
    )
}