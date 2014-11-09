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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProblemOccurence)) return false;

    ProblemOccurence that = (ProblemOccurence)o;

    if (!myTextRange.equals(that.myTextRange)) return false;
    if (!urls.equals(that.urls)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myTextRange.hashCode();
    result = 31 * result + urls.hashCode();
    return result;
  }
}
