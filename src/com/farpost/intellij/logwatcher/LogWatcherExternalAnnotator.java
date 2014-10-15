package com.farpost.intellij.logwatcher;

import com.intellij.ide.BrowserUtil;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.*;
import com.intellij.tools.SimpleActionGroup;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.NotNullFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

import static com.farpost.intellij.Icons.LogWatcher;
import static com.intellij.openapi.ui.popup.JBPopupFactory.ActionSelectionAid.NUMBERING;
import static com.intellij.openapi.ui.popup.JBPopupFactory.getInstance;
import static com.intellij.util.containers.ContainerUtil.createMaybeSingletonList;
import static com.intellij.util.containers.ContainerUtil.getFirstItem;
import static java.util.Arrays.asList;

public class LogWatcherExternalAnnotator extends ExternalAnnotator<Object, Object> {

  @Nullable
  @Override
  public Object collectInformation(@NotNull PsiFile file) {
    return super.collectInformation(file);
  }

  @Nullable
  @Override
  public Object collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
    return 1;
  }

  @Nullable
  @Override
  public Object doAnnotate(Object collectedInfo) {
    return 1;
  }

  @Override
  public void apply(@NotNull PsiFile file, Object annotationResult, @NotNull final AnnotationHolder holder) {
    final String cl = "com.farpost.search.web.RestControllerV13";
    final String m = "searchDocuments";
    String location = "PerformSearchCallable.java:36";
    PsiElementVisitor v = new JavaRecursiveElementWalkingVisitor() {
      @Override
      public void visitClass(PsiClass aClass) {
        if (cl.equalsIgnoreCase(aClass.getQualifiedName())) {
          PsiMethod[] candidates = aClass.findMethodsByName(m, false);
          for (PsiMethod candidate : candidates) {
            PsiIdentifier nameIdentifier = candidate.getNameIdentifier();
            if (nameIdentifier != null) {
              Annotation a = holder.createInfoAnnotation(nameIdentifier, null);
              a.setGutterIconRenderer(new MyGutterIconRenderer(cl, m, asList("http://logwatcher.srv.loc/", "http://logwatcher2.srv.loc/")));
            }
          }
        }
      }
    };

    file.accept(v);
  }

  final static class MyGutterIconRenderer extends GutterIconRenderer {


    private final String myCl;
    private final String myM;
    private final List<String> myUrls;

    public MyGutterIconRenderer(String cl, String m, List<String> urls) {
      myCl = cl;
      myM = m;
      myUrls = urls;
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
          SimpleActionGroup g = new SimpleActionGroup();
          for (String url : myUrls) {
            g.add(new GotoUrlAction(url));
          }
          if (e.getInputEvent() instanceof MouseEvent) {
            MouseEvent me = (MouseEvent)e.getInputEvent();
            getInstance().createActionGroupPopup("LogWatcher", g, e.getDataContext(), NUMBERING, true, null, 10)
              .show(new RelativePoint(me));
          }

        }
      };
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof MyGutterIconRenderer)) return false;

      MyGutterIconRenderer that = (MyGutterIconRenderer)o;

      if (!myCl.equals(that.myCl)) return false;
      if (!myM.equals(that.myM)) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = myCl.hashCode();
      result = 31 * result + myM.hashCode();
      return result;
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

  private static class ToListConverted implements NotNullFunction<String, Collection<? extends PsiElement>> {

    private final PsiElement element;

    public ToListConverted(PsiElement element) {
      this.element = element;
    }

    @NotNull
    @Override
    public Collection<? extends PsiElement> fun(String dom) {
      return createMaybeSingletonList(element);
    }
  }

  private static class MyItemProvider implements NotNullFunction<String, Collection<? extends GotoRelatedItem>> {

    private final PsiElement myPsiElement;
    private final String url;

    public MyItemProvider(PsiElement psiElement, String url) {
      myPsiElement = psiElement;
      this.url = url;
    }

    @NotNull
    @Override
    public Collection<? extends GotoRelatedItem> fun(String dom) {
      GotoRelatedItem item = new GotoRelatedItem(myPsiElement, "LogWatcher") {
        @Override
        public void navigate() {
          System.out.println("Going to: " + url);
        }
      };
      return createMaybeSingletonList(item);
    }
  }
}
