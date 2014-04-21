import java.io.*;

File folder;
JSONArray json;

int iSelectedPos = 0;

void setup(){
  size(1024,720);
  json = new JSONArray();
  try{
    JSONArray jsonLoaded = loadJSONArray("data\\mlist.json");
    json = jsonLoaded;
    println(jsonLoaded);
  }catch(Exception ex){
    println("Failed to load json file");
    selectFolder("Select a musicfolder to add music", "folderSelected");
  }
  
  drawInit();
}
void loop(){
  
}

void folderSelected(File selection){
  if (selection == null){
    println("Window was closed or the user hit cancel.");
  } else {
    folder = new File(dataPath(selection.getAbsolutePath()));  
    
    LoadMp3();
  }
}

void LoadMp3(){  
  java.io.FilenameFilter mp3Filter = new java.io.FilenameFilter() {
    public boolean accept(File dir, String name) {
      return name.toLowerCase().endsWith(".mp3");
    }
  };
  
  String[] filenames = folder.list(mp3Filter);
  
  for (int i = 0; i < filenames.length; i++)
  {
    JSONObject temp = new JSONObject();
    temp.setString("Filename", folder.getAbsolutePath() + "\\" + filenames[i]);
    json.setJSONObject(i, temp);
  }
  
  //println(json);
  saveJSONArray(json, "data/mlist.json");
     
}

void drawInit(){
  
  
  //Mask  
  fill();
}
