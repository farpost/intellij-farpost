package com.farpost.intellij;

import com.intellij.diagnostic.ITNReporter;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ErrorReporter extends ITNReporter {
  @Override
  public SubmittedReportInfo submit(IdeaLoggingEvent[] events, Component parentComponent) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean trySubmitAsync(IdeaLoggingEvent[] events,
                                String additionalInfo,
                                Component parentComponent,
                                Consumer<SubmittedReportInfo> consumer) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getReportActionText() {
    return "Report to FarPost";
  }
}