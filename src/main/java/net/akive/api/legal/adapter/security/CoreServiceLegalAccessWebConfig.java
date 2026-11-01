package net.akive.api.legal.adapter.security;

import lombok.RequiredArgsConstructor;
import net.akive.api.legal.application.provided.LegalRequirementChecker;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
class CoreServiceLegalAccessWebConfig implements WebMvcConfigurer {

    private final LegalRequirementChecker legalRequirementChecker;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CoreServiceLegalAccessInterceptor(legalRequirementChecker))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/legal-documents/**",
                        "/members/me/withdraw",
                        "/members/me/withdrawal-reasons",
                        "/actuator/**",
                        "/error"
        );
    }
}
