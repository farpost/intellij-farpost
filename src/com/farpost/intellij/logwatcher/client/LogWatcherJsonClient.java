package com.farpost.intellij.logwatcher.client;

import com.farpost.intellij.logwatcher.LogWatcherProjectComponent;
import com.google.gson.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class LogWatcherJsonClient extends LogWatcherClient {
  private static final Logger LOGGER = Logger.getInstance(LogWatcherProjectComponent.class);

  @Override
  public void processLogs(@NotNull String endpoint, @NotNull Consumer<LogEntryDescriptor> consumer) {
    InputStream is = null;
    try {
      is = createJsonStream(endpoint);

      Gson gson = new GsonBuilder().create();
      JsonArray obj = gson.fromJson(new InputStreamReader(is), JsonArray.class);
      for (JsonElement element : obj) {
        JsonObject o = (JsonObject)element;
        int line = o.getAsJsonPrimitive("line").getAsInt();
        String cluster = o.getAsJsonPrimitive("cluster").getAsString();
        String clazz = o.getAsJsonPrimitive("class").getAsString();
        String file = o.getAsJsonPrimitive("file").getAsString();
        String method = o.getAsJsonPrimitive("method").getAsString();

        consumer.consume(new LogEntryDescriptor(clazz, method, line, endpoint + "/entries/search/" + cluster));
      }
    }
    catch (IOException e) {
      LOGGER.error(e);
    }
    finally {
      if (is != null) {
        try {
          is.close();
        }
        catch (IOException e) {
          LOGGER.error(e);
        }
      }
    }
  }

  @NotNull
  protected abstract InputStream createJsonStream(@NotNull String endpoint) throws IOException;

  @Override
  @Nullable
  public String retrieveStacktrace(@NotNull String endpoint, @NotNull String id) {
    // todo: implement real stacktrace retrieving
    return DebugUtil.currentStackTrace();
  }
}
