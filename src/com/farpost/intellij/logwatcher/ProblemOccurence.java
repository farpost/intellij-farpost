package com.farpost.intellij.logwatcher;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ProblemOccurence {

  @Nullable private final PsiElement myPsiElement;
  private TextRange myTextRange;
  private List<String> urls;

  public ProblemOccurence(@Nullable PsiElement psiElement, TextRange textRange, List<String> urls) {
    myPsiElement = psiElement;
    myTextRange = textRange;
    this.urls = urls;
  }

  @Nullable
  public PsiElement getPsiElement() {
    return myPsiElement;
  }

  public TextRange getTextRange() {
    return myTextRange;
  }

  public List<String> getUrls() {
    return urls;
  }
}
