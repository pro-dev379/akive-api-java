package net.akive.api.dailylog.adapter.webapi.dto.response;

import net.akive.api.dailylog.domain.DailyLogType;
import net.akive.api.dailylog.domain.TimeUsageRates;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public record TimeUsageRatesFindResponse(List<TypeRate> timeUsageRates) {

    public static TimeUsageRatesFindResponse from(TimeUsageRates rates) {
        List<TypeRate> recordedRates = Arrays.stream(DailyLogType.values())
                .map(type -> new TypeRate(type.name(), type.getLabel(), rates.ratesByType().get(type)))
                .toList();
        List<TypeRate> allRates = Stream.concat(
                recordedRates.stream(),
                Stream.of(new TypeRate("UNRECORDED", "미기록", rates.unrecordedRate()))
        ).toList();

        return new TimeUsageRatesFindResponse(allRates);
    }

    public record TypeRate(String code, String label, BigDecimal usageRate) {
    }
}
