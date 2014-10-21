package com.farpost.intellij.logwatcher;

import com.farpost.intellij.Icons;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.Function;
import com.intellij.util.FunctionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.farpost.intellij.logwatcher.LogWatcherExternalAnnotator.putOrAppendUrl;

public class LogWatcherLineMarkerProvider implements LineMarkerProvider {

  @Nullable
  @Override
  public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
    if (!(element instanceof PsiIdentifier)) {
      return null;
    }

    final PsiElement parent = element.getParent();
    if (!(parent instanceof PsiMethod)) {
      return null;
    }

    Project project = element.getProject();
    final LogWatcherProjectComponent logWatcherProjectComponent = LogWatcherProjectComponent.getInstance(project);
    if (logWatcherProjectComponent == null) {
      return null;
    }

    Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
    if (document == null) return null;

    PsiMethod method = ((PsiMethod)parent);
    PsiClass aClass = method.getContainingClass();
    if (aClass == null) {
      return null;
    }

    final Map<Integer, ProblemOccurence> result = new HashMap<Integer, ProblemOccurence>();
    final String qualifiedName = aClass.getQualifiedName();

    final Collection<LogEntryDescriptor> logDescriptors = logWatcherProjectComponent.getDescriptorsForClass(qualifiedName);
    for (LogEntryDescriptor logDescriptor : logDescriptors) {
      if (logDescriptor.methodName.equals(method.getName())) {
        int lineStartOffset = logDescriptor.lineNumber > document.getLineCount() ? -2 : document.getLineStartOffset(logDescriptor.lineNumber - 1);

        PsiCodeBlock methodBody = method.getBody();
        if (methodBody != null && methodBody.getTextRange().contains(lineStartOffset)) {
          putOrAppendUrl(result, logDescriptor.lineNumber - 1, logDescriptor.logUrl, TextRange.create(lineStartOffset, lineStartOffset + 1),
                         null);
        }
        else {
          PsiIdentifier nameIdentifier = method.getNameIdentifier();
          if (nameIdentifier != null) {
            final TextRange nameIdentifierTextRange = nameIdentifier.getTextRange();
            final int lineNumber = document.getLineNumber(nameIdentifierTextRange.getStartOffset());
            putOrAppendUrl(result, lineNumber, logDescriptor.logUrl, nameIdentifierTextRange, nameIdentifier);
          }
        }

      }
    }

    for (Map.Entry<Integer, ProblemOccurence> problem : result.entrySet()) {
      ProblemOccurence value = problem.getValue();
      PsiElement psiElement = value.getPsiElement();
      return new LogWatcherLineMarker(method.getNameIdentifier(), value.getTextRange(), Icons.LogWatcher,
                                      Pass.UPDATE_ALL, FunctionUtil.constant("foo"), null, GutterIconRenderer.Alignment.LEFT);
    }
    return null;
  }

  @Override
  public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
    /*for (PsiElement element : elements) {
      if (element instanceof PsiMethod) {
        PsiMethod method = (PsiMethod)element;
        result.add(new LogWatcherLineMarker(method.getNameIdentifier(), method.getNameIdentifier().getTextRange(), Icons.LogWatcher, Pass.UPDATE_OVERRIDEN_MARKERS,
                                            FunctionUtil.constant("foo"), null, GutterIconRenderer.Alignment.LEFT));
      }
    }*/
  }

  public static class LogWatcherLineMarker extends LineMarkerInfo {
    public LogWatcherLineMarker(@NotNull PsiElement element,
                                @NotNull TextRange range,
                                Icon icon,
                                int updatePass,
                                @Nullable Function tooltipProvider,
                                @Nullable GutterIconNavigationHandler navHandler,
                                GutterIconRenderer.Alignment alignment) {
      super(element, range, icon, updatePass, tooltipProvider, navHandler, alignment);
    }
  }
}
