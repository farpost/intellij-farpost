package com.farpost.intellij.logwatcher.annotator;

import com.intellij.openapi.util.TextRange;

import java.util.List;

public final class ProblemOccurence {

  private TextRange myTextRange;
  private List<String> urls;

  public ProblemOccurence(TextRange textRange, List<String> urls) {
    myTextRange = textRange;
    this.urls = urls;
  }

  public TextRange getTextRange() {
    return myTextRange;
  }

  public List<String> getUrls() {
    return urls;
  }
}
