package com.farpost.intellij.logwatcher;

import com.intellij.ide.BrowserUtil;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.farpost.intellij.Icons.LogWatcher;
import static com.intellij.openapi.ui.popup.JBPopupFactory.ActionSelectionAid.NUMBERING;
import static com.intellij.openapi.ui.popup.JBPopupFactory.getInstance;
import static com.intellij.util.containers.ContainerUtil.getFirstItem;

// todo since retrieving log data is fast now it should be reimplemented with Annotator or LineMarkerProvider
public class LogWatcherExternalAnnotator implements Annotator, DumbAware {

  private static final Logger log = Logger.getInstance(LogWatcherExternalAnnotator.class);

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (!(element instanceof PsiCallExpression)) {
      return;
    }

    Project project = element.getProject();
    final LogWatcherProjectComponent logWatcherProjectComponent = LogWatcherProjectComponent.getInstance(project);
    if (logWatcherProjectComponent == null) {
      return;
    }

    Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
    if (document == null) return;

    PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
    if (method == null) return;

    PsiClass aClass = method.getContainingClass();
    if (aClass == null) {
      return;
    }

    final Map<Integer, ProblemOccurence> result = new HashMap<Integer, ProblemOccurence>();
    final String qualifiedName = aClass.getQualifiedName();

    final Collection<LogEntryDescriptor> logDescriptors = logWatcherProjectComponent.getDescriptorsForClass(qualifiedName);
    for (LogEntryDescriptor logDescriptor : logDescriptors) {
      if (logDescriptor.methodName.equals(method.getName())) {
        int lineStartOffset =
          logDescriptor.lineNumber > document.getLineCount() ? -2 : document.getLineStartOffset(logDescriptor.lineNumber - 1);

        PsiCodeBlock methodBody = method.getBody();
        int ln = document.getLineNumber(element.getTextRange().getStartOffset());
        if (ln == logDescriptor.lineNumber - 1) {
          putOrAppendUrl(result, logDescriptor.lineNumber - 1, logDescriptor.logUrl, element.getTextRange(),
                         element);
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
      Annotation a =
        psiElement != null ? holder.createInfoAnnotation(psiElement, null) : holder.createInfoAnnotation(value.getTextRange(), null);
      a.setGutterIconRenderer(new MyGutterIconRenderer(value.getUrls(), problem.getKey()));
    }
  }

  public static void putOrAppendUrl(Map<Integer, ProblemOccurence> result,
                                    int lineNumber,
                                    String url,
                                    TextRange textRange,
                                    @Nullable PsiElement psiElement) {
    final ProblemOccurence existingOccurence = result.get(lineNumber);
    if (existingOccurence != null) {
      existingOccurence.getUrls().add(url);
    }
    else {
      result.put(lineNumber, new ProblemOccurence(psiElement, textRange, ContainerUtil.newArrayList(url)));
    }
  }

  final static class MyGutterIconRenderer extends GutterIconRenderer {

    private final List<String> myUrls;
    private final Integer myKey;

    public MyGutterIconRenderer(List<String> urls, Integer key) {
      myUrls = urls;
      myKey = key;
    }

    @NotNull
    @Override
    public Icon getIcon() {
      return LogWatcher;
    }

    @Nullable
    @Override
    public AnAction getClickAction() {
      if (myUrls.size() == 1) {
        return new GotoUrlAction(getFirstItem(myUrls));
      }
      return new AnAction() {
        @Override
        public void actionPerformed(AnActionEvent e) {
          DefaultActionGroup g = new DefaultActionGroup();
          for (String url : myUrls) {
            g.add(new GotoUrlAction(url));
          }
          if (e.getInputEvent() instanceof MouseEvent) {
            MouseEvent me = (MouseEvent)e.getInputEvent();
            getInstance().createActionGroupPopup(null, g, e.getDataContext(), NUMBERING, true, null, 10).show(new RelativePoint(me));
          }

        }
      };
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof MyGutterIconRenderer)) return false;

      MyGutterIconRenderer that = (MyGutterIconRenderer)o;

      if (!myUrls.equals(that.myUrls) || !myKey.equals(that.myKey)) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return myUrls.hashCode() + 31 * myKey.hashCode();
    }
  }

  private static class GotoUrlAction extends AnAction {
    private final String url;

    private GotoUrlAction(String url) {
      this.url = url;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      BrowserUtil.browse(url);
    }

    @Override
    public void update(AnActionEvent e) {
      e.getPresentation().setText(url);
    }
  }

}
