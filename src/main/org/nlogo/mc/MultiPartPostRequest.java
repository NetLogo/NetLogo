// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import org.apache.http.Consts;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;

import java.io.UnsupportedEncodingException;

public abstract strictfp class MultiPartPostRequest extends Request {

  private MultipartEntity entity;

  public MultiPartPostRequest(HttpClient http, String url) {
    super(http, url);
    entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
  }

  public void addStringParam(String name, String value) {
    try {
      entity.addPart(name, new StringBody(value, "text/plain", Consts.UTF_8));
    } catch (UnsupportedEncodingException unsupportedEncodingException) {
      //UTF8 is required to be supported by Java
      //This will never be thrown, but in case it is, don't suppress it
      throw new RuntimeException("UTF-8 is not supported on this system; UTF-8 is required by the Java standard.", unsupportedEncodingException);
    }
  }

  public void addFileParam(String name, String fileContents, final String fileName) {
    try {
      entity.addPart(name, new StringBody(fileContents, "text/plain", Consts.UTF_8) {

        public String getFilename() {
          return fileName;
        }

      });
    } catch (UnsupportedEncodingException unsupportedEncodingException) {
      //UTF8 is required to be supported by Java
      //This will never be thrown, but in case it is, don't suppress it
      throw new RuntimeException("UTF-8 is not supported on this system; UTF-8 is required by the Java standard.", unsupportedEncodingException);
    }
  }

  public void addFileParam(String name, byte[] fileContents, final String fileName) {
    entity.addPart(name, new ByteArrayBody(fileContents, fileName));
  }

  @Override
  public void execute() {
    request.setEntity(entity);
    super.execute();
  }

}

