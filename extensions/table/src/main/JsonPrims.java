package org.nlogo.extensions.table;

import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Reporter;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.nvm.ExtensionContext;
import org.nlogo.nvm.FileManager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

public class JsonPrims {
  public static class FromJsonFile implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax
          (new int[]{Syntax.StringType()},
              Syntax.WildcardType());
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public Object report(Argument args[], Context context)
        throws ExtensionException, LogoException {

      FileManager fm = ((ExtensionContext) context).workspace().fileManager();

      try {
        String path = fm.attachPrefix(args[0].getString());
        File file = new File(path.toString());
        if (!file.exists()) {
          throw new ExtensionException(args[0].get().toString() + " does not exist.");
        }
        Gson gson = new Gson();

        try {
          Map<?,?> map = gson.fromJson(new FileReader(path), Map.class);
          Table t = Table.fromMap(map);
          TableExtension.tables.put(t, t.id);
          return t;
        } catch (JsonSyntaxException e) {
          throw new ExtensionException("Error trying to read the JSON file. It is probably missing a colon or comma. See the line number on the next line: " + e.getMessage());
        }

      } catch (IOException e) {
        throw new ExtensionException(e.getMessage());
      }
    }
  }

  public static class FromJson implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(new int[]{Syntax.StringType()}, Syntax.WildcardType());
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public Object report(Argument args[], Context context)
      throws ExtensionException, LogoException
    {
      String json = args[0].getString();
      Gson gson = new Gson();

      try {
        Map<?,?> map = gson.fromJson(json, Map.class);
        Table t = Table.fromMap(map);
        TableExtension.tables.put(t, t.id);
        return t;
      } catch (JsonSyntaxException e) {
        throw new ExtensionException("The string given to FROM-JSON was not valid. " + e.getMessage());
      }
    }
  }

  public static class LogoListSerializer implements JsonSerializer<LogoList> {
    public JsonElement serialize(LogoList list, Type typeOfId, JsonSerializationContext context) {
      JsonArray array = new JsonArray();
      for (Iterator<Object> it = list.javaIterator(); it.hasNext();) {
        Object o = it.next();
        JsonElement el = context.serialize(o);
        array.add(el);
      }
      return array;
    }
  }

  public static class ToJson implements Reporter {
    public Syntax getSyntax() {
      return SyntaxJ.reporterSyntax(new int[]{Syntax.WildcardType()}, Syntax.StringType());
    }

    public String getAgentClassString() {
      return "OTPL";
    }

    public Object report(Argument args[], Context context)
      throws ExtensionException, LogoException
    {
      Table t = TableExtension.getTable(args[0]);
      Gson gson = new GsonBuilder().registerTypeAdapter(LogoList.class, new LogoListSerializer()).create();
      String json = gson.toJson(t);
      return json;
    }
  }

}
