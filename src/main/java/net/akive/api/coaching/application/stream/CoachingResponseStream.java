package net.akive.api.coaching.application.stream;

import net.akive.api.coaching.domain.MessageCompletionStatus;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import static net.akive.api.coaching.domain.MessageCompletionStatus.COMPLETED;
import static net.akive.api.coaching.domain.MessageCompletionStatus.TRUNCATED;

public interface CoachingResponseStream {

    void subscribe(Listener downstreamListener);

    void cancel();

    default CoachingResponseStream onResponse(BiConsumer<String, MessageCompletionStatus> responseConsumer) {
        CoachingResponseStream upstream = this;

        return new CoachingResponseStream() {

            private final StringBuilder content = new StringBuilder();
            private final AtomicBoolean completed = new AtomicBoolean();

            @Override
            public void subscribe(Listener downstreamListener) {
                upstream.subscribe(new Listener() {
                    @Override
                    public void onDelta(String delta) {
                        content.append(delta);
                        downstreamListener.onDelta(delta);
                    }

                    @Override
                    public void onComplete() {
                        if (!completed.compareAndSet(false, true)) {
                            return;
                        }

                        try {
                            responseConsumer.accept(content.toString(), COMPLETED);
                            downstreamListener.onComplete();
                        } catch (RuntimeException e) {
                            downstreamListener.onError(e);
                        }
                    }

                    @Override
                    public void onIncomplete(IncompleteReason reason) {
                        if (!completed.compareAndSet(false, true)) {
                            return;
                        }

                        try {
                            responseConsumer.accept(content.toString(), TRUNCATED);
                            downstreamListener.onIncomplete(reason);
                        } catch (RuntimeException e) {
                            downstreamListener.onError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        if (completed.compareAndSet(false, true)) {
                            downstreamListener.onError(error);
                        }
                    }
                });
            }

            @Override
            public void cancel() {
                if (completed.compareAndSet(false, true)) {
                    upstream.cancel();
                }
            }
        };
    }

    default CoachingResponseStream onTermination(Runnable terminationAction) {
        CoachingResponseStream upstream = this;

        return new CoachingResponseStream() {

            private final AtomicBoolean terminated = new AtomicBoolean();

            @Override
            public void subscribe(Listener downstreamListener) {
                try {
                    upstream.subscribe(new Listener() {
                        @Override
                        public void onDelta(String delta) {
                            if (!terminated.get()) {
                                downstreamListener.onDelta(delta);
                            }
                        }

                        @Override
                        public void onComplete() {
                            if (!terminated.compareAndSet(false, true)) {
                                return;
                            }

                            try {
                                terminationAction.run();
                                downstreamListener.onComplete();
                            } catch (RuntimeException e) {
                                downstreamListener.onError(e);
                            }
                        }

                        @Override
                        public void onIncomplete(IncompleteReason reason) {
                            if (!terminated.compareAndSet(false, true)) {
                                return;
                            }

                            try {
                                terminationAction.run();
                                downstreamListener.onIncomplete(reason);
                            } catch (RuntimeException e) {
                                downstreamListener.onError(e);
                            }
                        }

                        @Override
                        public void onError(Throwable error) {
                            if (!terminated.compareAndSet(false, true)) {
                                return;
                            }

                            try {
                                terminationAction.run();
                            } catch (RuntimeException e) {
                                error.addSuppressed(e);
                            }

                            downstreamListener.onError(error);
                        }
                    });
                } catch (RuntimeException subscribeException) {
                    if (!terminated.compareAndSet(false, true)) {
                        throw subscribeException;
                    }

                    try {
                        terminationAction.run();
                    } catch (RuntimeException terminationException) {
                        subscribeException.addSuppressed(terminationException);
                    }

                    throw subscribeException;
                }
            }

            @Override
            public void cancel() {
                if (!terminated.compareAndSet(false, true)) {
                    return;
                }

                try {
                    upstream.cancel();
                } finally {
                    terminationAction.run();
                }
            }
        };
    }

    interface Listener {

        void onDelta(String delta);

        void onComplete();

        void onIncomplete(IncompleteReason reason);

        void onError(Throwable error);
    }

    enum IncompleteReason {

        MAX_OUTPUT_TOKENS("max_output_tokens");

        private final String code;

        IncompleteReason(String code) {
            this.code = code;
        }

        public String code() {
            return this.code;
        }
    }
}
