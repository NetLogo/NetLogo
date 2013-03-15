// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

import javax.swing.SwingUtilities;
import java.io.IOException;

public abstract strictfp class Request {

  protected HttpPost request;
  private HttpClient http;

  public Request(HttpClient http, String url) {
    this.http = http;
    request = new HttpPost(url);
    request.addHeader("Accept", "application/json");
  }

  protected abstract void onReturn(String response);

  public void execute() {
    Thread thread = new Thread(new Runnable() {

      @Override
      public void run() {
        String responseStr;
        try {
          HttpResponse response = http.execute(request);
          HttpEntity entity = response.getEntity();
          responseStr = EntityUtils.toString(response.getEntity());
          EntityUtils.consume(entity);
        } catch(IOException e) {
          responseStr = null;
        }
        final String finalResponseStr = responseStr;
        SwingUtilities.invokeLater(new Runnable() {

          @Override
          public void run() {
            onReturn(finalResponseStr);
          }

        });
      }

    });
   thread.start();
  }

  public void abort() {
    request.abort();
  }

}

