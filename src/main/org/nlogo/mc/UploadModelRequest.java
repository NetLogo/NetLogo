// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import org.apache.http.client.HttpClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract strictfp class UploadModelRequest extends MultiPartPostRequest {

  private JDialog loadingDialog;
  private String invalid;
  private Frame frame;

  public UploadModelRequest(HttpClient http, Frame frame, String modelName, String modelBody, Group group, Permission visibility, Permission changeability, Image previewImage) {
    super(http, ModelingCommons.HOST + "/upload/create_model");
    this.frame = frame;
    addStringParam("new_model[name]", modelName);
    addStringParam("read_permission", visibility.getId());
    addStringParam("write_permission", changeability.getId());
    if(group != null) {
      addStringParam("group_id", "" + group.getId());
    }
    addFileParam("new_model[uploaded_body]", modelBody, modelName + ".nlogo");
    if(previewImage != null) {
      try {
        ByteArrayOutputStream previewImageStream = new ByteArrayOutputStream();
        BufferedImage image = previewImage.getImage();
        if(image == null) {
          invalid = "INVALID_PREVIEW_IMAGE";
        } else {
          ImageIO.write(image, "png", previewImageStream);
          addFileParam("new_model[uploaded_preview]", previewImageStream.toByteArray(), modelName + ".png");
        }
      } catch(ImageException e) {
        invalid = "INVALID_PREVIEW_IMAGE";
      } catch(IOException e) {
        invalid = "INVALID_PREVIEW_IMAGE";
      }
    }
  }

  @Override
  protected void onReturn(String response) {
    loadingDialog.dispose();
    if(response == null) {
      onUploaded("CONNECTION_ERROR", null, null);
      return;
    }
    JSONParser json = new JSONParser();
    try {
      JSONObject obj = (JSONObject)(json.parse(response));
      String status = (String)(obj.get("status"));
      String uploadedModelURL = null;
      String uploadedModelName = null;
      if(status.equals("SUCCESS")) {
        JSONObject model = ((JSONObject) obj.get("model"));
        uploadedModelURL = (String) model.get("url");
        uploadedModelName = (String) model.get("name");
      }
      onUploaded(status, uploadedModelURL, uploadedModelName);
    } catch(ParseException e) {
      //Thrown when JSON is invalid
      //Indicates an error on the modeling commons server
      onUploaded("INVALID_RESPONSE_FROM_SERVER", null, null);
    }
  }

  @Override
  public void execute() {
    if(invalid != null) {
      onUploaded(invalid, null, null);
      return;
    }
    loadingDialog = new LoadingDialog(frame, "Uploading model to Modeling Commons");
    super.execute();
    loadingDialog.setVisible(true);
  }

  protected abstract void onUploaded(String status, String uploadedModelURL, String uploadedModelName);

}

