package org.nlogo.modelingcommons;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

import javax.swing.SwingUtilities;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Ben
 * Date: 1/16/13
 * Time: 12:19 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Request {

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


        } catch (IOException e) {
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
    /*
    SwingWorker<String, Void> requestWorker = new SwingWorker<String, Void>() {
      @Override
      protected String doInBackground() throws Exception {
        try {
          HttpResponse response = http.execute(request);
          HttpEntity entity = response.getEntity();
          String responseStr = EntityUtils.toString(response.getEntity());
          EntityUtils.consume(entity);
          return responseStr;
        } catch (IOException e) {
          return null;
        }
      }

      @Override
      protected void done() {
        try {
          String responseStr = get();
          onReturn(responseStr);
        } catch (InterruptedException e) {

        } catch (ExecutionException e) {

        }

      }
    };

    requestWorker.execute();
*/
  }

  public void abort() {
    request.abort();
  }
}
