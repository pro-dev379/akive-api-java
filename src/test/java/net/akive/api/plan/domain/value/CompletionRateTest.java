package net.akive.api.plan.domain.value;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CompletionRateTest {

    @Test
    void 세션_총량별_달성률을_동일한_비중으로_두고_평균을_구한다() {
        List<QuantityCompletionAmount> completionAmounts = List.of(
                new QuantityCompletionAmount(100, 100),
                new QuantityCompletionAmount(900, 450)
        );

        CompletionRate completionRate = CompletionRate.calculate(completionAmounts);

        assertThat(completionRate.value())
                .as("계획량으로 가중한 55%가 아니라 총 계획별 달성률 100%와 50%의 평균을 계산한다")
                .isEqualByComparingTo("75.0");
    }

    @Test
    void 세션_총량별_달성률은_백_퍼센트를_초과하지_않는다() {
        List<QuantityCompletionAmount> completionAmounts = List.of(
                new QuantityCompletionAmount(100, 150),
                new QuantityCompletionAmount(100, 50)
        );

        CompletionRate completionRate = CompletionRate.calculate(completionAmounts);

        assertThat(completionRate.value()).isEqualByComparingTo("75.0");
    }

    @Test
    void 달성률은_소수점_둘째_자리에서_반올림한다() {
        CompletionRate completionRate = CompletionRate.calculate(List.of(
                new QuantityCompletionAmount(3, 2)
        ));

        assertThat(completionRate.value()).isEqualByComparingTo("66.7");
    }

    @Test
    void 조회_기간에_계획량이_없으면_달성률은_영_퍼센트다() {
        CompletionRate completionRate = CompletionRate.calculate(List.of());

        assertThat(completionRate.value()).isEqualByComparingTo("0.0");
    }
}
