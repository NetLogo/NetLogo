package org.nlogo.modelingcommons;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.AbortableHttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.nlogo.app.App;
import org.nlogo.app.ModelSaver;
import org.nlogo.headless.HeadlessWorkspace;
import org.nlogo.nvm.Procedure;
import org.nlogo.swing.MessageDialog;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Ben
 * Date: 10/25/12
 * Time: 1:19 AM
 * To change this template use File | Settings | File Templates.
 */

public class ModelingCommons {
  //private static final String HOST = "http://localhost:3000";
  private static final String HOST = "http://modelingcommons.org";
  private static HttpClient http = new DefaultHttpClient();
  private JSONParser json = new JSONParser();
  private static Person person = null;
  private String uploadedModelURL;
  private String uploadedModelName;
  private String newUserAgreement;
  private List<String> priorityCountries;
  private List<String> unpriorityCountries;

  private ModelSaver modelSaver;
  private Frame frame;
  private App app;
  private List<Group> groups = null;

  public ModelingCommons(ModelSaver modelSaver, Frame frame, App app) {
    this.modelSaver = modelSaver;
    this.frame = frame;
    this.app = app;
  }
  public abstract class LoginRequest extends PostRequest {
    private JDialog loadingDialog;
    public LoginRequest(String email, String password) {
      super(http, HOST + "/account/login_action");
      addStringParam("email_address", email);
      addStringParam("password", password);
    }
    @Override
    protected void onReturn(String response) {
      loadingDialog.dispose();
      if(response == null) {
        onLogin("CONNECTION_ERROR");
        return;
      }
      try {
        JSONObject obj = (JSONObject)(json.parse(response));
        String status = (String)(obj.get("status"));
        if(status.equals("SUCCESS")) {
          JSONObject personObj = (JSONObject)(obj.get("person"));
          person = new Person(
              (String)(personObj.get("first_name")),
              (String)(personObj.get("last_name")),
              ((Number)(personObj.get("id"))).intValue(),
              (String)(personObj.get("avatar")),
              (String)(personObj.get("email_address"))
          );
        }
        onLogin(status);
        return;
      } catch(ParseException e) {
        onLogin("INVALID_RESPONSE_FROM_SERVER");
        return;
      }
    }

    @Override
    public void execute() {
      loadingDialog = new LoadingDialog(frame, "Logging in to Modeling Commons");
      super.execute();
      loadingDialog.setVisible(true);
    }

    protected abstract void onLogin(String status);
  }

  public abstract class LogoutRequest extends PostRequest {
    private JDialog loadingDialog;
    public LogoutRequest() {
      super(http, HOST + "/account/logout");
    }

    @Override
    protected void onReturn(String response) {
      loadingDialog.dispose();
      if(response == null) {
        onLogout("CONNECTION_ERROR");
        return;
      }

      try {
        JSONObject obj = (JSONObject)(json.parse(response));
        String status = (String)(obj.get("status"));
        if(status.equals("SUCCESS") || status.equals("NOT_LOGGED_IN")) {
          person = null;
        }
        onLogout(status);
      } catch(ParseException e) {
        onLogout("INVALID_RESPONSE_FROM_SERVER");
      }
    }

    @Override
    public void execute() {
      loadingDialog = new LoadingDialog(frame, "Logging out of Modeling Commons");
      super.execute();
      loadingDialog.setVisible(true);
    }

    protected abstract void onLogout(String status);
  }

  public abstract class CreateUserRequest extends MultiPartPostRequest {
    private JDialog loadingDialog;
    private String invalid;
    public CreateUserRequest(String firstName, String lastName, String emailAddress, Sex sex, String country, Integer birthdayYear, Month birthdayMonth, Integer birthdayDay, String password, Image profilePicture) {
      super(http, HOST + "/account/create");
      addStringParam("new_person[first_name]", firstName);
      addStringParam("new_person[last_name]", lastName);
      addStringParam("new_person[email_address]", emailAddress);
      addStringParam("new_person[sex]", sex.toString());
      addStringParam("new_person[country_name]", country);
      String birthdayYearString = "";
      if(birthdayYear != null) {
        birthdayYearString = birthdayYear.toString();
      }
      String birthdayMonthString = "";
      if(birthdayMonth != null) {
        birthdayMonthString = "" + birthdayMonth.getMonthNum();
      }
      String birthdayDayString = "";
      if(birthdayDay != null) {
        birthdayDayString = birthdayDay.toString();
      }
      addStringParam("new_person[birthdate(1i)]", birthdayYearString);
      addStringParam("new_person[birthdate(2i)]", birthdayMonthString);
      addStringParam("new_person[birthdate(3i)]", birthdayDayString);
      addStringParam("new_person[password]", password);
      addStringParam("new_person[registration_consent]","1");

      if(profilePicture != null) {
        try {
          ByteArrayOutputStream profilePictureStream = new ByteArrayOutputStream();
          BufferedImage image = profilePicture.getImage();
          if(image == null) {
            invalid = "INVALID_PROFILE_PICTURE";
          } else {
            ImageIO.write(image, "png", profilePictureStream);
            addFileParam("new_person[avatar]", profilePictureStream.toByteArray(), firstName + "_" + lastName + ".png");
          }
        } catch(ImageException e) {
          invalid = "INVALID_PROFILE_PICTURE";
        } catch(IOException e) {
          invalid = "INVALID_PROFILE_PICTURE";
        }
      }

    }

    @Override
    protected void onReturn(String response) {
      loadingDialog.dispose();
      if(response == null) {
        onCreateUser("CONNECTION_ERROR");
        return;
      }
      try {
        JSONObject obj = (JSONObject)(json.parse(response));
        String status = (String)(obj.get("status"));
        if(status.equals("SUCCESS")) {
          JSONObject personObj = (JSONObject)(obj.get("person"));
          person = new Person(
              (String)(personObj.get("first_name")),
              (String)(personObj.get("last_name")),
              ((Number)(personObj.get("id"))).intValue(),
              (String)(personObj.get("avatar")),
              (String)(personObj.get("email_address"))
          );
        }
        onCreateUser(status);
      } catch(ParseException e) {
        onCreateUser("INVALID_RESPONSE_FROM_SERVER");
      }
    }

    @Override
    public void execute() {
      if(invalid != null) {
        onCreateUser(invalid);
        return;
      }
      loadingDialog = new LoadingDialog(frame, "Creating account on Modeling Commons");
      super.execute();
      loadingDialog.setVisible(true);
    }

    protected abstract void onCreateUser(String status);
  }


  public abstract class UploadModelRequest extends MultiPartPostRequest {
    private JDialog loadingDialog;
    private String invalid;
    public UploadModelRequest(String modelName, Group group, Permission visibility, Permission changeability, Image previewImage) {
      super(http, HOST + "/upload/create_model");

      addStringParam("new_model[name]", modelName);
      addStringParam("read_permission", visibility.getId().toString());
      addStringParam("write_permission", changeability.getId().toString());
      if(group != null) {
        addStringParam("group_id", "" + group.getId());
      }
      addFileParam("new_model[uploaded_body]", modelSaver.save(), modelName + ".nlogo");
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
        onUploaded("CONNECTION_ERROR");
        return;
      }
      try {
        JSONObject obj = (JSONObject)(json.parse(response));
        String status = (String)(obj.get("status"));
        if(status.equals("SUCCESS")) {
          JSONObject model = ((JSONObject)(obj.get("model")));
          uploadedModelURL = (String)(model.get("url"));
          uploadedModelName = (String)(model.get("name"));
        }
        onUploaded(status);
      } catch(ParseException e) {
        onUploaded("INVALID_RESPONSE_FROM_SERVER");
      }
    }

    @Override
    public void execute() {
      if(invalid != null) {
        onUploaded(invalid);
        return;
      }
      loadingDialog = new LoadingDialog(frame, "Uploading model to Modeling Commons");
      super.execute();
      loadingDialog.setVisible(true);
    }

    protected abstract void onUploaded(String status);
  }


  String uploadModel(final String modelName, final Group group, final Permission visibility, final Permission changeability, final Image previewImage) {
    try {

      HttpPost post = new HttpPost(HOST + "/upload/create_model");
      post.addHeader("Accept", "application/json");
      MultipartEntity requestEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
      requestEntity.addPart("new_model[name]", new StringBody(modelName, "text/plain", Consts.UTF_8));
      requestEntity.addPart("read_permission", new StringBody(visibility.getId().toString(), "text/plain", Consts.UTF_8));
      requestEntity.addPart("write_permission", new StringBody(changeability.getId().toString(), "text/plain", Consts.UTF_8));
      if(group != null) {
        requestEntity.addPart("group_id", new StringBody("" + group.getId(), "text/plain", Consts.UTF_8));
      }
      requestEntity.addPart("new_model[uploaded_body]", new StringBody(modelSaver.save(), "text/plain", Consts.UTF_8) {
        public String getFilename() {
          return modelName + ".nlogo";
        }
      });
      if(previewImage != null) {
        try {
          ByteArrayOutputStream previewImageStream = new ByteArrayOutputStream();
          BufferedImage image = previewImage.getImage();
          if(image == null) {
            return "INVALID_PREVIEW_IMAGE";
          }
          ImageIO.write(image, "png", previewImageStream);
          requestEntity.addPart("new_model[uploaded_preview]", new ByteArrayBody(previewImageStream.toByteArray(), modelName + ".png"));
        } catch(ImageException e) {
          return "INVALID_PREVIEW_IMAGE";
        }
      }

      post.setEntity(requestEntity);

      HttpResponse response =  http.execute(post);
      HttpEntity entity = response.getEntity();
      String responseStr = EntityUtils.toString(response.getEntity());
      EntityUtils.consume(entity);
      try {
        JSONObject obj = (JSONObject)(json.parse(responseStr));
        String status = (String)(obj.get("status"));
        if(status.equals("SUCCESS")) {
          JSONObject model = ((JSONObject)(obj.get("model")));
          this.uploadedModelURL = (String)(model.get("url"));
          this.uploadedModelName = (String)(model.get("name"));
        }
        return status;
      } catch(ParseException e) {
        return "INVALID_RESPONSE_FROM_SERVER";
      }


    } catch(IOException e) {
      return "CONNECTION_ERROR";
    }
  }
  String forkChildModel(final int existingModelId, final String newModelName, final String description) {
    return updateModel(existingModelId, newModelName, description, NewModelType.CHILD);
  }

  String makeNewVersionOfModel(final int existingModelId, final String description) {
    return updateModel(existingModelId, "", description, NewModelType.NEW_VERSION);
  }

  public abstract class UpdateModelRequest extends MultiPartPostRequest {
    private JDialog loadingDialog;
    private String invalid;
    public UpdateModelRequest(int existingModelId, String newModelName, String description, NewModelType newModelType) {
      super(http, HOST + "/upload/update_model");

      if(!(newModelType == NewModelType.NEW_VERSION || newModelType == NewModelType.CHILD)) {
        throw new IllegalArgumentException("Invalid upload type - must be child or new version");
      }
      addStringParam("new_version[name]", newModelName);
      addStringParam("new_version[description]", description);
      addStringParam("new_version[node_id]", "" + existingModelId);
      addStringParam("fork", newModelType.toString());
      String modelName;
      if(newModelType == NewModelType.NEW_VERSION) {
        modelName = "" + existingModelId + "_new";
      } else {
        modelName = newModelName;
      }
      addFileParam("new_version[uploaded_body]", modelSaver.save(), modelName + ".nlogo");

    }

    @Override
    protected void onReturn(String response) {
      loadingDialog.dispose();
      if(response == null) {
        onUploaded("CONNECTION_ERROR");
        return;
      }
      try {
        JSONObject obj = (JSONObject)(json.parse(response));
        String status = (String)(obj.get("status"));
        if(status.equals("SUCCESS")) {
          JSONObject model = ((JSONObject)(obj.get("model")));
          uploadedModelURL = (String)(model.get("url"));
          uploadedModelName = (String)(model.get("name"));
        }
        onUploaded(status);
      } catch(ParseException e) {
        onUploaded("INVALID_RESPONSE_FROM_SERVER");
      }
    }

    @Override
    public void execute() {
      if(invalid != null) {
        onUploaded(invalid);
        return;
      }
      loadingDialog = new LoadingDialog(frame, "Uploading model to Modeling Commons");
      super.execute();
      loadingDialog.setVisible(true);
    }

    protected abstract void onUploaded(String status);
  }



  String updateModel(final int existingModelId, final String newModelName, final String description, final NewModelType newModelType) {
    if(!(newModelType == NewModelType.NEW_VERSION || newModelType == NewModelType.CHILD)) {
      throw new IllegalArgumentException("Invalid upload type - must be child or new version");
    }

    try {
      HttpPost post = new HttpPost(HOST + "/upload/update_model");
      post.addHeader("Accept", "application/json");
      MultipartEntity requestEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
      requestEntity.addPart("new_version[name]", new StringBody(newModelName, "text/plain", Consts.UTF_8));
      requestEntity.addPart("new_version[description]", new StringBody(description, "text/plain", Consts.UTF_8));
      requestEntity.addPart("new_version[node_id]", new StringBody("" + existingModelId, "text/plain", Consts.UTF_8));
      requestEntity.addPart("fork", new StringBody("" + newModelType, "text/plain", Consts.UTF_8));
      requestEntity.addPart("new_version[uploaded_body]", new StringBody(modelSaver.save(), "text/plain", Consts.UTF_8) {

        public String getFilename() {
          String modelName;
          if(newModelType == NewModelType.NEW_VERSION) {
            modelName = "" + existingModelId + "_new";
          } else {
            modelName = newModelName;
          }
          return modelName + ".nlogo";
        }
      });

      post.setEntity(requestEntity);

      HttpResponse response =  http.execute(post);
      HttpEntity entity = response.getEntity();
      String responseStr = EntityUtils.toString(response.getEntity());
      EntityUtils.consume(entity);
      try {
        JSONObject obj = (JSONObject)(json.parse(responseStr));
        String status = (String)(obj.get("status"));
        if(status.equals("SUCCESS")) {
          JSONObject model = ((JSONObject)(obj.get("model")));
          this.uploadedModelURL = (String)(model.get("url"));
          this.uploadedModelName = (String)(model.get("name"));
        }
        return status;
      } catch(ParseException e) {
        return "INVALID_RESPONSE_FROM_SERVER";
      }


    } catch(IOException e) {
      return "CONNECTION_ERROR";
    }
  }


  void downloadGroups() throws ParseException, IOException {
    List<Group> newGroups = new ArrayList<Group>();
    HttpGet get = new HttpGet(HOST + "/account/list_groups");
    get.addHeader("Accept", "application/json");
    HttpResponse response =  http.execute(get);
    HttpEntity entity = response.getEntity();
    String responseStr = EntityUtils.toString(response.getEntity());
    EntityUtils.consume(entity);
    JSONObject obj = (JSONObject)(json.parse(responseStr));
    JSONArray groups = (JSONArray)(obj.get("groups"));
    Iterator iterator = groups.iterator();
    while(iterator.hasNext()) {
      JSONObject group = (JSONObject)(iterator.next());
      int id = ((Number)(group.get("id"))).intValue();
      String name = (String)(group.get("name"));
      newGroups.add(new Group(id, name));
    }
    this.groups = newGroups;
  }

  public abstract class DownloadGroupsRequest extends PostRequest {
    private JDialog loadingDialog;
    public DownloadGroupsRequest() {
      super(http, HOST + "/account/list_groups");
    }

    @Override
    protected void onReturn(String response) {
      loadingDialog.dispose();
      if(response == null) {
        onDownloaded("CONNECTION_ERROR", null);
        return;
      }
      try {
        List<Group> newGroups = new ArrayList<Group>();
        JSONObject obj = (JSONObject)(json.parse(response));
        JSONArray groupsArr = (JSONArray)(obj.get("groups"));

        Iterator iterator = groupsArr.iterator();
        while(iterator.hasNext()) {
          JSONObject group = (JSONObject)(iterator.next());
          int id = ((Number)(group.get("id"))).intValue();
          String name = (String)(group.get("name"));
          newGroups.add(new Group(id, name));
        }
        onDownloaded("SUCCESS", newGroups);
      } catch(ParseException e) {
        onDownloaded("INVALID_RESPONSE_FROM_SERVER", null);
      }
    }

    @Override
    public void execute() {
      loadingDialog = new LoadingDialog(frame, "Loading groups you belong to");
      super.execute();
      loadingDialog.setVisible(true);
    }

    protected abstract void onDownloaded(String status, List<Group> groups);
  }

  public abstract class DownloadNewUserParametersRequest extends PostRequest {
    private JDialog loadingDialog;
    public DownloadNewUserParametersRequest() {
      super(http, HOST + "/account/new");
    }

    @Override
    protected void onReturn(String response) {
      loadingDialog.dispose();
      if(response == null) {
        onDownloaded("CONNECTION_ERROR", null, null, null);
        return;
      }
      try {
        List<String> priorityCountries = new ArrayList<String>();
        List<String> unpriorityCountries = new ArrayList<String>();
        JSONObject obj = (JSONObject)(json.parse(response));
        JSONArray countries = (JSONArray)(obj.get("countries"));
        Iterator iterator = countries.iterator();
        while(iterator.hasNext()) {
          JSONObject country = (JSONObject)(iterator.next());
          String countryName = (String)(country.get("name"));
          Boolean isPriority = (Boolean)(country.get("priority"));
          if(isPriority) {
            priorityCountries.add(countryName);
          } else {
            unpriorityCountries.add(countryName);
          }
        }
        String userAgreement = (String)(obj.get("user_agreement"));
        onDownloaded("SUCCESS", userAgreement, priorityCountries, unpriorityCountries);
      } catch(ParseException e) {
        onDownloaded("INVALID_RESPONSE_FROM_SERVER", null, null, null);
      }
    }

    @Override
    public void execute() {
      loadingDialog = new LoadingDialog(frame, "Loading new user information");
      super.execute();
      loadingDialog.setVisible(true);
    }

    protected abstract void onDownloaded(String status, String newUserAgreement, List<String> priorityCountries, List<String> unpriorityCountries);
  }


  void downloadNewUserParameters() throws ParseException, IOException{
    List<String> priorityCountries = new ArrayList<String>();
    List<String> unpriorityCountries = new ArrayList<String>();
    HttpGet get = new HttpGet(HOST + "/account/new");
    get.addHeader("Accept", "application/json");
    HttpResponse response =  http.execute(get);
    HttpEntity entity = response.getEntity();
    String responseStr = EntityUtils.toString(response.getEntity());
    EntityUtils.consume(entity);

  }


  private AbortableHttpRequest searchForModelsRequest;

  void abortSearchForModels() {
    if(searchForModelsRequest != null) {
      searchForModelsRequest.abort();
    }
  }

  List<Model> searchForModels(String queryString, int count, boolean changeability) throws ParseException, IOException {
    List<Model> out = new ArrayList<Model>();
    HttpPost post = new HttpPost(HOST + "/account/models");
    post.addHeader("Accept", "application/json");
    List<NameValuePair> credentials = new ArrayList<NameValuePair>(2);
    credentials.add(new BasicNameValuePair("query", queryString));
    credentials.add(new BasicNameValuePair("count", "" + count));
    if(changeability) {
      credentials.add(new BasicNameValuePair("changeability", "changeability"));
    }
    UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(credentials, Consts.UTF_8);
    post.setEntity(formEntity);

    abortSearchForModels();
    searchForModelsRequest = post;
    HttpResponse response =  http.execute(post);
    searchForModelsRequest = null;

    HttpEntity entity = response.getEntity();
    String responseStr = EntityUtils.toString(response.getEntity());
    EntityUtils.consume(entity);
    JSONObject obj = (JSONObject)(json.parse(responseStr));
    JSONArray models = (JSONArray)(obj.get("models"));
    Iterator iterator = models.iterator();
    while(iterator.hasNext()) {
      JSONObject model = (JSONObject)(iterator.next());
      String modelName = (String)(model.get("name"));
      int modelId = ((Number)(model.get("id"))).intValue();
      out.add(new Model(modelId, modelName));
    }
    return out;
  }

  //Calls onSearchResults on a successful, but possibly 0-sized request
  //Does not call onSearchResults on an aborted or failed request
  public abstract class SearchForModelsRequest extends PostRequest {
    public SearchForModelsRequest(String queryString, int count, boolean changeability) {
      super(http, HOST + "/account/models");
      addStringParam("query", queryString);
      addStringParam("count", "" + count);
      if(changeability) {
        addStringParam("changeability", "changeability");
      }
    }

    @Override
    protected void onReturn(String response) {
      if(response == null) {
        return;
      }
      try {
        List<Model> out = new ArrayList<Model>();
        JSONObject obj = (JSONObject)(json.parse(response));
        JSONArray models = (JSONArray)(obj.get("models"));
        Iterator iterator = models.iterator();
        while(iterator.hasNext()) {
          JSONObject model = (JSONObject)(iterator.next());
          String modelName = (String)(model.get("name"));
          int modelId = ((Number)(model.get("id"))).intValue();
          out.add(new Model(modelId, modelName));
        }
        onSearchResults(out);
      } catch(ParseException e) {
        return;
      }
    }

    protected abstract void onSearchResults(List<Model> models);
  }




  public boolean isLoggedIn() {
    return person != null;
  }

  public String getUploadedModelURL() {
    return uploadedModelURL;
  }

  public String getUploadedModelName() {
    return uploadedModelName;
  }

  public List<Group> getGroups() {
    return groups;
  }

  public List<String> getPriorityCountries() {
    return priorityCountries;
  }

  public List<String> getUnpriorityCountries() {
    return unpriorityCountries;
  }

  public String getNewUserAgreement() {
    return newUserAgreement;
  }

  public Person getPerson() {
    return person;
  }


  public void promptForLogin(final String error) {
    JDialog loginDialog = new LoginDialog(frame, this, error);
    loginDialog.setVisible(true);
  }
  public void promptForLogin() {
    promptForLogin(" ");
  }
  public void promptForUpload(final String error) {
    DownloadGroupsRequest request = new DownloadGroupsRequest() {
      @Override
      protected void onDownloaded(String status, List<Group> groups) {
        if(status.equals("SUCCESS")) {
          ModelingCommons.this.groups = groups;
          boolean enableAutoGeneratePreviewImage = app.workspace().getProcedures().get("SETUP") != null && app.workspace().getProcedures().get("GO") != null;
          JDialog uploadDialog = new UploadDialog(frame, ModelingCommons.this, error, enableAutoGeneratePreviewImage);
          uploadDialog.setVisible(true);
        } else if(status.equals("INVALID_RESPONSE_FROM_SERVER")) {
          MessageDialog.show("Error connecting to Modeling Commons", "Invalid response from Modeling Commons");
        } else if(status.equals("CONNECTION_ERROR")) {
          MessageDialog.show("Error connecting to Modeling Commons", "Could not connect to Modeling Commons");
        }
      }
    };
    request.execute();

  }
  public void promptForUpload() {
    promptForUpload(" ");
  }
  public void promptForSuccess(final String error) {
    JDialog successDialog = new UploadSuccessDialog(frame, this, error);
    successDialog.setVisible(true);
  }
  public void promptForSuccess() {
    promptForSuccess(" ");
  }
  public void promptForCreateAccount(final String error) {
    DownloadNewUserParametersRequest request = new DownloadNewUserParametersRequest() {
      @Override
      protected void onDownloaded(String status, String newUserAgreement, List<String> priorityCountries, List<String> unpriorityCountries) {
        if(status.equals("SUCCESS")) {
          ModelingCommons.this.newUserAgreement = newUserAgreement;
          ModelingCommons.this.priorityCountries = priorityCountries;
          ModelingCommons.this.unpriorityCountries = unpriorityCountries;
          JDialog createAccountDialog = new NewUserDialog(frame, ModelingCommons.this, error);
          createAccountDialog.setVisible(true);
        } else if(status.equals("INVALID_RESPONSE_FROM_SERVER")) {
          MessageDialog.show("Error connecting to Modeling Commons", "Invalid response from Modeling Commons");
        } else if(status.equals("CONNECTION_ERROR")) {
          MessageDialog.show("Error connecting to Modeling Commons", "Could not connect to Modeling Commons");
        }
      }
    };
    request.execute();
  }
  public void promptForCreateAccount() {
    promptForCreateAccount(" ");
  }
  public void saveToModelingCommons() {
    if(!isLoggedIn()) {
      promptForLogin();
    } else {
      promptForUpload();
    }

  }
  Image getAutoGeneratedModelImage() {
    return new Image() {
      @Override
      public BufferedImage getImage() throws ImageException {
        Map<String, Procedure> map = app.workspace().getProcedures();
        try {
          HeadlessWorkspace headless = HeadlessWorkspace.newInstance();
          headless.openString(modelSaver.save());

          /*Procedure procedure =
             headless.compileForRun
                 ("random-seed 0 " + headless.previewCommands() +
                     "\nprint \"GENERATED: " + headless + "\"",
                     context, false);
         JobOwner jobOwner = new SimpleJobOwner("Modeling Commons Preview Image", headless.mainRNG(), Observer.class);
         headless.runCompiledCommands(jobOwner, procedure);*/
          String command = "random-seed 0 " + headless.previewCommands();
          headless.command(command);
          BufferedImage image = headless.exportView();
          headless.dispose();
          return image;
        } catch(InterruptedException e) {
        } catch (Exception e) {
          throw new ImageException("Could not autogenerate preview image: " + e.getMessage());

        }
        return null;
      }
    };
  }

  Image getCurrentModelViewImage() {
    return new Image() {
      @Override
      public BufferedImage getImage() throws ImageException {
        return app.workspace().exportView();
      }
    };
  }
  HttpClient getHttpClient() {
    return http;
  }
}
