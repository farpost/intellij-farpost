package com.farpost.intellij;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class Icons {
  private static Icon load(String path) {
    return IconLoader.getIcon(path, Icons.class);
  }

  public static final Icon LogWatcher = load("/com/farpost/intellij/icons/logwatcher.png");
  public static final Icon ToolWindowLogWatcher = load("/com/farpost/intellij/icons/toolWindowLogWatcher.png"); // 13x13
}
