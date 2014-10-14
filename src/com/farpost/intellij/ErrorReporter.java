package com.farpost.intellij;

import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ErrorReporter extends ErrorReportSubmitter {
  @Override
  public boolean submit(@NotNull IdeaLoggingEvent[] events,
                        String additionalInfo,
                        @NotNull Component parentComponent,
                        @NotNull Consumer<SubmittedReportInfo> consumer) {
    // todo implement reporter
    return super.submit(events, additionalInfo, parentComponent, consumer);
  }

  @Override
  public String getReportActionText() {
    return "Report to FarPost";
  }
}