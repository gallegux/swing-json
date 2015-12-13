package org.pablotron.swingjson;

import java.io.Reader;
import java.util.Map;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public final class ContextParser {
  private ContextParser() {}

  private static final JsonParser parser = new JsonParser();

  public static Context parse(
    final Reader in,
    final ContextErrorHandler handler
  ) throws Exception {
    final JsonObject root = parser.parse(in).getAsJsonObject();
    final Context r = new Context(handler);

    try {
      // parse text
      if (root.has("text")) {
        final JsonObject text = root.getAsJsonObject("text");

        for (final Map.Entry<String, JsonElement> e: text.entrySet())
          r.addText(e.getKey(), e.getValue().getAsString());
      }

      // set look and feel
      if (root.has("style")) {
        final String style = root.get("style").getAsString();

        for (final LookAndFeelInfo info: UIManager.getInstalledLookAndFeels()) {
          if (style.equals(info.getName())) {
            UIManager.setLookAndFeel(info.getClassName());
            break;
          }
        }
      }

      if (root.has("fonts")) {
        final JsonObject o = root.get("fonts").getAsJsonObject();

        // add fonts
        for (final Map.Entry<String, JsonElement> e: o.entrySet())
          r.addFont(e.getKey(), FontParser.parse(e.getValue()));
      }

      // parse and add roots
      if (root.has("kids")) {
        for (final JsonElement el: root.getAsJsonArray("kids"))
          r.addRoot(ComponentParsers.parse(r, el.getAsJsonObject()));
      }
    } catch (Exception e) {
      r.error(e);
    }

    // return context
    return r;
  }

  public static Context parse(final Reader in) throws Exception {
    return parse(in, null);
  }
};
