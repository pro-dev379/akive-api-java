package net.akive.api.learner.application.required;

import net.akive.api.learner.application.required.data.TextEmbeddingRequestData;

import java.util.List;

public interface TextEmbeddingModel {

    List<Float> embed(TextEmbeddingRequestData request);
}
