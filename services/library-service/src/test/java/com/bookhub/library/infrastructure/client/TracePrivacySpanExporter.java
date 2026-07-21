package com.bookhub.library.infrastructure.client;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

final class TracePrivacySpanExporter implements SpanExporter {

  private final List<SpanData> exportedSpans = new CopyOnWriteArrayList<>();

  @Override
  public CompletableResultCode export(final Collection<SpanData> spans) {
    exportedSpans.addAll(spans);
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }

  List<SpanData> exportedSpans() {
    return List.copyOf(exportedSpans);
  }

  List<String> exportedSpanAttributes() {
    return exportedSpans.stream()
        .flatMap(span -> span.getAttributes().asMap().entrySet().stream())
        .flatMap(
            entry ->
                java.util.stream.Stream.of(entry.getKey().getKey(), entry.getValue().toString()))
        .toList();
  }
}
