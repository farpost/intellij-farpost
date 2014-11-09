package com.farpost.intellij.logwatcher.client;

import com.intellij.util.io.URLUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class LogWatcherRemoteJsonClient extends LogWatcherJsonClient {
  @NotNull
  @Override
  protected InputStream createJsonStream(@NotNull String endpoint) throws IOException {
    String url = endpoint + "/search/problem-methods.json";
    return URLUtil.openStream(new URL(url));
  }
}
