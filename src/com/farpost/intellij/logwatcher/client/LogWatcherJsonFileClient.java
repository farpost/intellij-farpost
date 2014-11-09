package com.farpost.intellij.logwatcher.client;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LogWatcherJsonFileClient extends LogWatcherJsonClient {
  @NotNull
  @Override
  protected InputStream createJsonStream(@NotNull String endpoint) throws IOException {
    return new FileInputStream(new File("/Users/zolotov/problem-methods.json"));
  }
}
