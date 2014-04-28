import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.spi.*; 
import ddf.minim.signals.*; 
import ddf.minim.*; 
import ddf.minim.analysis.*; 
import ddf.minim.ugens.*; 
import ddf.minim.effects.*; 
import controlP5.*; 
import java.io.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ItunesLibrary_test extends PApplet {











File folder;
JSONArray json;
ArrayList MusicList;

int iSelectedPos;
int iSelectedToPlay;

ControlP5 cp5;
ListBox lb;

Minim minim;
AudioPlayer ap;
AudioMetaData metaDataPlayingSong;

PImage photoPlayingSong;

boolean start;

public void setup(){
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

public void draw(){
  /*if(start){
    lb.scroll(0.5);
    lb.setValue(0.5);
  } */
  for (int i=0;i<MusicList.size();i++) {
    lb.getItem(i).setColorBackground(0xa99e4600);
  }
    lb.getItem(iSelectedPos).setColorBackground(0xffff9900); 
    try{
    image(photoPlayingSong, 100,400);
    }
    catch(Exception e)
    {
      //println("e: "+e);
    }
    finally{}

  if(start){
      lb.scroll(PApplet.parseFloat(mouseY) / PApplet.parseFloat(720));
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
  lb = cp5.addListBox("Afspeellijst", 20, 100, 900, 200);
  lb.setId(1);
  
  iSelectedPos = 0;
  
  drawInit();
  
  minim = new Minim(this);
  //println(MusicList.get(1));
  ap = minim.loadFile("" + MusicList.get(iSelectedPos));

  //ap.play();

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
  lb.setColorActive(0xffff9900);
  
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
  
  if(key == 'u'){
      println(iSelectedPos);
      println("up key pressed: ");
    if(iSelectedPos > 0){
        iSelectedPos = iSelectedPos - 1;        
        lb.scroll(iSelectedPos / MusicList.size());   
      }
    }
  else if(key == 'd'){
      println(iSelectedPos);
      if(iSelectedPos < MusicList.size()){
        iSelectedPos = iSelectedPos + 1;
        lb.scroll(iSelectedPos / MusicList.size());  

      }
    }    
  if(key == 'p'){

    if(ap.isPlaying())
      ap.pause();
    else
      ap.play();

  }
  if (key == 'l')
  {
    iSelectedToPlay = iSelectedPos;
    loadNewMusicFile("" + MusicList.get(iSelectedToPlay));
    metaDataPlayingSong = ap.getMetaData();
    ap.play();
    updateImageSong();
  }
  if(key == 'n'){
    if(iSelectedToPlay < MusicList.size()){
      iSelectedToPlay++;
      iSelectedPos ++;
      loadNewMusicFile("" + MusicList.get(iSelectedToPlay));
    }
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
  //loadNewMusicFile("" + MusicList.get(iSelectedPos));
}

public void loadNewMusicFile(String filename){
  ap.pause();
  ap = minim.loadFile(filename);
  // ap.play();
}

public void updateImageSong()
{
  
      try{
  String songNameB = metaDataPlayingSong.title();
  String[] songNamePieces = split(songNameB, " ");
  String songName = "";
  for(String s: songNamePieces)
  {
    songName += s;
    songName += '+';
  }
  println("songName: "+songName);
  // ophalen json van itunes api
  String urlToAPI = "https://ws.spotify.com/search/1/track.json?q=";
  urlToAPI += songName;
  JSONObject json = loadJSONObject(urlToAPI);
  println("urlToAPI: "+urlToAPI);
  JSONArray array = json.getJSONArray("tracks");
  JSONObject first = array.getJSONObject(0);
  println("json: "+json);
  String url = first.getString("href");
  println("url: "+url);

  urlToAPI = "https://embed.spotify.com/oembed/?url=";
  urlToAPI += url;
  JSONObject json2 = loadJSONObject(urlToAPI);
  String url2 = json2.getString("thumbnail_url");

  photoPlayingSong = loadImage(url2,"jpg");

  }
  catch (Exception e){
    println("e: "+e);
    photoPlayingSong = loadImage("data\\NoImage.jpg");
  }

}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ItunesLibrary_test" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
