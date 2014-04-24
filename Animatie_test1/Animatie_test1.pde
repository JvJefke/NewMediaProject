import ddf.minim.spi.*;
import ddf.minim.signals.*;
import ddf.minim.*;
import ddf.minim.analysis.*;
import ddf.minim.ugens.*;
import ddf.minim.effects.*;

import controlP5.*;
import java.io.*;

File folder;
JSONArray json;
ArrayList MusicList;

int iSelectedPos;

ControlP5 cp5;
ListBox lb;

Minim minim;
AudioPlayer ap;

boolean start;

void setup(){
  size(1024,720);  
  json = new JSONArray();
  start = false;
  
  try{
    JSONArray jsonLoaded = loadJSONArray("data\\mlist.json");
    json = jsonLoaded;    
    println(jsonLoaded);
    setupAfterFolderSelection();
  }catch(Exception ex){
    println("Failed to load json file");
    selectFolder("Select a musicfolder to add music", "folderSelected");
  }
  
}

void draw(){
  if(start){
    lb.scroll(0.5);
    lb.setValue(0.5);
  }  
}

void folderSelected(File selection){
  if (selection == null){
    println("Window was closed or the user hit cancel.");
  } else {
    folder = new File(dataPath(selection.getAbsolutePath()));    
    LoadMp3();
    setupAfterFolderSelection();
  }
}

void setupAfterFolderSelection(){
  MusicList = ConvertJSONToList(json);
  
  cp5 = new ControlP5(this);
  lb = cp5.addListBox("myList", 20, 100, 900, 200);
  lb.setId(1);
  
  iSelectedPos = 0;
  
  drawInit();
  
  minim = new Minim(this);
  //println(MusicList.get(1));
  ap = minim.loadFile("" + MusicList.get(iSelectedPos));
  ap.play();
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
  lb.addItems(MusicList);
  lb.isScrollable();
  
  println(lb);  
}

ArrayList<String> ConvertJSONToList(JSONArray myArr){
  ArrayList<String> arrMusic = new ArrayList<String>();
  
  for(int i = 0; i < myArr.size(); i++)
  {
     arrMusic.add(myArr.getJSONObject(i).getString("Filename"));
  }
  
  return arrMusic;
}

void keyPressed(){
  //println(key);
  println(keyCode);
  /*if(keyCode == UP)
      println(iSelectedPos);
      if(iSelectedPos > 0){
        iSelectedPos = iSelectedPos - 1;        
        lb.scroll(iSelectedPos / MusicList.size());   
      }
  else if(keyCode == DOWN)
      println(iSelectedPos);
      if(iSelectedPos < MusicList.size()){
        iSelectedPos = iSelectedPos + 1;
        lb.scroll(iSelectedPos / MusicList.size());  
      }*/
      
  if(key == 'p'){
    if(ap.isPlaying())
      ap.pause();
    else
      ap.play();
  }
  if(key == 'n'){
    if(iSelectedPos < MusicList.size()){
      iSelectedPos++;
      loadNewMusicFile("" + MusicList.get(iSelectedPos));
    }
  }
}

void controlEvent(ControlEvent e){
  println("Excecuting control command...");
  iSelectedPos = (int)e.group().value();       
  loadNewMusicFile("" + MusicList.get(iSelectedPos));
}

void loadNewMusicFile(String filename){
  ap.pause();
  ap = minim.loadFile(filename);
  ap.play();
}
