package com.spasinnya.presentation.mapper

import com.spasinnya.domain.model.homework.HomeworkAnswer
import com.spasinnya.domain.model.homework.HomeworkValue
import com.spasinnya.presentation.model.HomeworkAnswerDto
import com.spasinnya.presentation.model.HomeworkRequest
import com.spasinnya.presentation.model.HomeworkResponse
import com.spasinnya.presentation.model.HomeworkValueDto

fun HomeworkRequest.toDomain(): List<HomeworkAnswer> =
    answers.map { answer ->
        HomeworkAnswer(
            questionId = answer.questionId,
            values = answer.values.map { HomeworkValue(id = it.id, answer = it.answer) }
        )
    }

fun List<HomeworkAnswer>.toHomeworkResponse(): HomeworkResponse =
    HomeworkResponse(
        answers = map { answer ->
            HomeworkAnswerDto(
                questionId = answer.questionId,
                values = answer.values.map { HomeworkValueDto(id = it.id, answer = it.answer) }
            )
        }
    )
