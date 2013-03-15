// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public abstract strictfp class PostRequest extends Request {

  private List<NameValuePair> params;

  public PostRequest(HttpClient http, String url) {
    super(http, url);
    params = new ArrayList<NameValuePair>();
  }

  public void addStringParam(String name, String value) {
    params.add(new BasicNameValuePair(name, value));
  }

  @Override
  public void execute() {
    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
    request.setEntity(entity);
    super.execute();
  }

}

