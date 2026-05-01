package net.akive.api.common.email;

import lombok.Builder;

import java.util.List;

@Builder
public record EmailMessage(String from, List<String> to, String subject, String content) {
}
