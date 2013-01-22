package org.nlogo.modelingcommons;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Ben
 * Date: 1/16/13
 * Time: 12:54 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class PostRequest extends Request  {
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
