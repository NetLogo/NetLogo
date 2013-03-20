// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import org.apache.http.client.HttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

//Calls onSearchResults on a successful, but possibly 0-sized request
//Calls onSearchAborted if the request is aborted, or if the request returns an error
public abstract strictfp class SearchForModelsRequest extends PostRequest {

  public SearchForModelsRequest(HttpClient http, String queryString, int count, boolean changeability) {
    super(http, ModelingCommons.HOST/*"localhost:3000"*/ + "/account/models");
    addStringParam("query", queryString);
    addStringParam("count", "" + count);
    if(changeability) {
      addStringParam("changeability", "changeability");
    }
  }

  @Override
  protected void onReturn(String response) {
    if(response == null) {
      onSearchResults("ABORTED_OR_CONNECTION_ERROR", null);
    } else {
      JSONParser json = new JSONParser();
      try {
        List<Model> out = new ArrayList<Model>();
        JSONObject obj = (JSONObject) json.parse(response);
        JSONArray models = (JSONArray) obj.get("models");
        for(Object modelObj : models) {
          JSONObject model = (JSONObject) modelObj;
          String modelName = (String) model.get("name");
          int modelId = ((Number) model.get("id")).intValue();
          out.add(new Model(modelId, modelName));
        }
        onSearchResults("SUCCESS", out);
      } catch(ParseException e) {
        onSearchResults("INVALID_RESPONSE_FROM_SERVER", null);
      }
    }
  }

  protected abstract void onSearchResults(String status, List<Model> models);

}

