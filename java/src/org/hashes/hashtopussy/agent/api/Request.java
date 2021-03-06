package org.hashes.hashtopussy.agent.api;

import org.hashes.hashtopussy.agent.common.LogLevel;
import org.hashes.hashtopussy.agent.common.LoggerFactory;
import org.hashes.hashtopussy.agent.common.Setting;
import org.hashes.hashtopussy.agent.common.Settings;
import org.hashes.hashtopussy.agent.exceptions.InvalidQueryException;
import org.hashes.hashtopussy.agent.exceptions.InvalidUrlException;
import org.hashes.hashtopussy.agent.exceptions.WrongResponseCodeException;
import org.json.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;

public class Request {
  private String url;
  private JSONObject query;
  
  public Request() {
    this.url = (String) Settings.get(Setting.URL);
  }
  
  public Request(String url){
    this.url = url;
  }
  
  public void setQuery(JSONObject query) {
    this.query = query;
  }
  
  public JSONObject execute() throws WrongResponseCodeException, InvalidQueryException, InvalidUrlException, IOException {
    return execute(false, null);
  }
  
  public JSONObject execute(boolean logProgress, String downloadPath) throws InvalidQueryException, InvalidUrlException, IOException, WrongResponseCodeException {
    if (this.query == null) {
      throw new InvalidQueryException("Query arguments cannot be null!");
    } else if (this.url == null) {
      throw new InvalidUrlException("URL cannot be null!");
    }
    
    URL obj = new URL(this.url);
    HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
    
    // set request header
    con.setRequestMethod("POST");
    con.setRequestProperty("User-Agent", "HTP Client/1");
    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
    
    String urlParameters = "query=" + this.query.toString();
    
    // send post request
    con.setDoOutput(true);
    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
    wr.writeBytes(urlParameters);
    wr.flush();
    wr.close();
    
    int responseCode = con.getResponseCode();
    if (responseCode != 200) {
      throw new WrongResponseCodeException("Got response code: " + responseCode);
    }
    
    DataOutputStream outputWriter = null;
    if(downloadPath != null){
      File output = new File(downloadPath);
      outputWriter = new DataOutputStream(new FileOutputStream(output));
    }
    
    // read answer
    InputStream in = con.getInputStream();
    String inputLine;
    StringBuffer response = new StringBuffer();
    int count = 0;
    int lastProgress = 0;
    boolean json = false;
    String jsonBuffer = "";
    byte buffer[] = new byte[1024];
    int len;
    while ((len = in.read(buffer)) != -1) {
      inputLine = new String(buffer, 0, len);
      if(outputWriter != null) {
        if(inputLine.startsWith("{") && count == 0){
          json = true;
        }
        if(json){
          jsonBuffer += inputLine;
        }
        outputWriter.write(buffer, 0, len);
      }
      else {
        response.append(inputLine);
      }
      count += inputLine.length();
      if(logProgress && count - lastProgress > 500000){
        LoggerFactory.getLogger().log(LogLevel.INFO, "Progress: " + count);
        lastProgress = count;
      }
    }
    in.close();
    if(outputWriter != null){
      outputWriter.close();
      if(json){
        response.append(jsonBuffer);
      }
      else {
        response.append("{}");
      }
    }
    
    try {
      JSONObject ans = new JSONObject(response.toString());
      return ans;
    }
    catch (JSONException e){
      LoggerFactory.getLogger().log(LogLevel.FATAL, "Failed to parse message from server!");
      LoggerFactory.getLogger().log(LogLevel.DEBUG, response.toString());
      return new JSONObject();
    }
  }
}
