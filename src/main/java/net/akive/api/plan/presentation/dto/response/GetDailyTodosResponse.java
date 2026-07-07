package net.akive.api.plan.presentation.dto.response;

import net.akive.api.plan.application.dto.result.DailyTodoResult;

import java.util.List;

public record GetDailyTodosResponse(List<DailyTodoItemResponse> todos) {

    public static GetDailyTodosResponse from(List<DailyTodoResult> results) {
        return new GetDailyTodosResponse(
                results.stream().map(DailyTodoItemResponse::from).toList()
        );
    }
}
