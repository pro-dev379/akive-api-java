package net.akive.api.legal.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.legal.adapter.webapi.dto.response.LegalDocumentsFindResponse;
import net.akive.api.legal.application.provided.LegalDocumentFinder;
import net.akive.api.legal.domain.LegalDocumentActionContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/legal-documents")
@RequiredArgsConstructor
public class LegalDocumentApi {

    private final LegalDocumentFinder legalDocumentFinder;

    @GetMapping
    public LegalDocumentsFindResponse findAllCurrent(@RequestParam LegalDocumentActionContext context) {
        return LegalDocumentsFindResponse.from(legalDocumentFinder.findAllCurrentByContext(context));
    }
}
