package com.farpost.intellij.logwatcher.annotator;

import com.farpost.intellij.logwatcher.LogWatcherProjectComponent;
import com.farpost.intellij.logwatcher.client.LogEntryDescriptor;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogWatcherExternalAnnotator extends ExternalAnnotator<List<ProblemOccurence>, List<ProblemOccurence>> {

  @Nullable
  @Override
  public List<ProblemOccurence> collectInformation(@NotNull PsiFile file) {
    return null;
  }

  @Nullable
  @Override
  public List<ProblemOccurence> collectInformation(@NotNull PsiFile file, @NotNull final Editor editor, boolean hasErrors) {
    final Map<Integer, ProblemOccurence> result = new HashMap<Integer, ProblemOccurence>();

    final LogWatcherProjectComponent logWatcherProjectComponent = LogWatcherProjectComponent.getInstance(file.getProject());
    if (logWatcherProjectComponent == null) {
      return null;
    }

    PsiElementVisitor v = new JavaRecursiveElementWalkingVisitor() {
      @Override
      public void visitClass(PsiClass aClass) {
        final String qualifiedName = aClass.getQualifiedName();

        final Collection<LogEntryDescriptor> logDescriptors = logWatcherProjectComponent.getDescriptorsForClass(qualifiedName);
        for (LogEntryDescriptor logDescriptor : logDescriptors) {
          PsiMethod[] candidates = aClass.findMethodsByName(logDescriptor.methodName, false);
          for (PsiMethod candidate : candidates) {
            final Document document = editor.getDocument();
            if (logDescriptor.lineNumber - 1 > document.getLineCount()) {
              continue;
            }
            int lineStartOffset = document.getLineStartOffset(logDescriptor.lineNumber - 1);

            PsiCodeBlock body = candidate.getBody();
            if (body != null && body.getTextRange().contains(lineStartOffset)) {
              putOrAppendUrl(result, logDescriptor.lineNumber - 1, logDescriptor.logUrl,
                             TextRange.create(lineStartOffset, lineStartOffset));
            }
            else {
              PsiIdentifier nameIdentifier = candidate.getNameIdentifier();
              if (nameIdentifier != null) {
                final TextRange nameIdentifierTextRange = nameIdentifier.getTextRange();
                final int lineNumber = document.getLineNumber(nameIdentifierTextRange.getStartOffset());
                putOrAppendUrl(result, lineNumber, logDescriptor.logUrl, nameIdentifierTextRange);
              }
            }
          }
        }
      }
    };

    file.accept(v);
    return ContainerUtil.newArrayList(result.values());
  }

  private static void putOrAppendUrl(Map<Integer, ProblemOccurence> result, int lineNumber, String url, TextRange textRange) {
    final ProblemOccurence existingOccurence = result.get(lineNumber);
    if (existingOccurence != null) {
      existingOccurence.getUrls().add(url);
    }
    else {
      result.put(lineNumber, new ProblemOccurence(textRange, ContainerUtil.newArrayList(url)));
    }
  }

  @Nullable
  @Override
  public List<ProblemOccurence> doAnnotate(List<ProblemOccurence> url) {
    return url;
  }

  @Override
  public void apply(@NotNull PsiFile file, final List<ProblemOccurence> problems, @NotNull final AnnotationHolder holder) {
    for (ProblemOccurence problem : problems) {
      Annotation a = holder.createInfoAnnotation(problem.getTextRange(), null);
      a.setGutterIconRenderer(new LogWatcherGutterRenderer(problem.getUrls()));
    }
  }

}
