// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.modelingcommons;

import org.apache.http.client.HttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

//Calls onSearchResults on a successful, but possibly 0-sized request
//Calls onSearchAborted if the request is aborted, or if the request returns an error
public abstract class SearchForModelsRequest extends PostRequest {

  public SearchForModelsRequest(HttpClient http, String queryString, int count, boolean changeability) {
    super(http, ModelingCommons.HOST + "/account/models");
    addStringParam("query", queryString);
    addStringParam("count", "" + count);
    if(changeability) {
      addStringParam("changeability", "changeability");
    }
  }

  @Override
  protected void onReturn(String response) {
    if(response == null) {
      onSearchAborted();
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
        onSearchResults(out);
      } catch(ParseException e) {
        //If the response is invalid JSON (server error), then do nothing for now
        //Not sure if we should do nothing (show no models) or display an error
      }
    }
  }

  protected abstract void onSearchResults(List<Model> models);
  protected abstract void onSearchAborted();

}