import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import com.onformative.leap.LeapMotionP5; 
import ddf.minim.spi.*; 
import ddf.minim.signals.*; 
import ddf.minim.*; 
import ddf.minim.analysis.*; 
import ddf.minim.ugens.*; 
import ddf.minim.effects.*; 
import controlP5.*; 
import java.io.*; 

import com.onformative.leap.*; 
import com.leapmotion.leap.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Leap_test1 extends PApplet {













File folder;
JSONArray json;
ArrayList MusicList;

int iSelectedPos;
float currScroll;

ControlP5 cp5;
ListBox lb;

Minim minim;
AudioPlayer ap;

LeapMotionP5 leap;

boolean start;

public void setup(){
  size(1024,720);

  json = new JSONArray();
  start = false;
  currScroll = 0;
  
  leap = new LeapMotionP5(this);
  
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

public void draw(){
  if(start){
      //lb.scroll(float(mouseY) / float(720));
    try{
      if(leap.getFingerList().size() <= 1){
        Finger f = leap.getFingerList().get(0);

        PVector snelheid = leap.getVelocity(f);
        if(snelheid.y > 30 || snelheid.y < -30){

          float speed = snelheid.y / 50000;
          currScroll = currScroll + speed;

          if(currScroll > 1)
            currScroll = 1;
          else if(currScroll < 0)
            currScroll = 0;

          lb.scroll(currScroll);
          println("snelheid: " + snelheid);
        }

        /*
        PVector currPos = leap.getTip(f);      
        lb.scroll(currPos.y / 600);*/
      }
    }catch(Exception e){}
  }  
}

public void folderSelected(File selection){
  if (selection == null){
    println("Window was closed or the user hit cancel.");
  } else {
    folder = new File(dataPath(selection.getAbsolutePath()));    
    LoadMp3();
    setupAfterFolderSelection();
  }
}

public void setupAfterFolderSelection(){
  MusicList = ConvertJSONToList(json);
  
  cp5 = new ControlP5(this);
  lb = cp5.addListBox("myList", 40, 100, 900, 500);
  lb.setItemHeight(95);
  lb.setId(1);
  
  iSelectedPos = 0;
  
  drawInit();
  
  minim = new Minim(this);
  //println(MusicList.get(1));
  ap = minim.loadFile("" + MusicList.get(iSelectedPos));
  ap.play();
  
  start = true;
}

public void LoadMp3(){  
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

public void drawInit(){  
  lb.addItems(MusicList);
  lb.isScrollable();
  
  println(lb);  
}

public ArrayList<String> ConvertJSONToList(JSONArray myArr){
  ArrayList<String> arrMusic = new ArrayList<String>();
  
  for(int i = 0; i < myArr.size(); i++)
  {
     arrMusic.add(myArr.getJSONObject(i).getString("Filename"));
  }
  
  return arrMusic;
}

public void keyPressed(){
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
  if(key == 'd'){
    if(iSelectedPos < MusicList.size()){
      iSelectedPos++;
      loadNewMusicFile("" + MusicList.get(iSelectedPos));
    }
  }
  if(key == 'q'){
    if(iSelectedPos > 0){
       iSelectedPos--;
       loadNewMusicFile("" + MusicList.get(iSelectedPos));
    }
  }
}

public void controlEvent(ControlEvent e){
  println("Excecuting control command...");
  iSelectedPos = (int)e.group().value();       
  loadNewMusicFile("" + MusicList.get(iSelectedPos));
}

public void loadNewMusicFile(String filename){
  ap.pause();
  ap = minim.loadFile(filename);
  ap.play();
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Leap_test1" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
