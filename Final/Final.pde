import com.onformative.leap.LeapMotionP5;
import controlP5.*;
import java.io.*;

import ddf.minim.spi.*;
import ddf.minim.signals.*;
import ddf.minim.*;
import ddf.minim.analysis.*;
import ddf.minim.ugens.*;
import ddf.minim.effects.*;


File folder;
JSONArray json;
ArrayList MusicList;

int iSelectedPos;
float currScroll;

ControlP5 cp5;
ListBox lb;

Minim minim;
AudioPlayer ap;
AudioMetaData metaDataPlayingSong;

PImage photoPlayingSong;

LeapMotionP5 leap;

boolean start;
boolean isPaused;

int screenWidth;
int screenHeight;

void setup(){
	size(displayWidth, displayHeight);
  screenWidth = displayWidth;
  screenHeight = displayHeight;
	setInitValues();
	loadJSONFile();
}

void draw(){
  background(255);
  
	if(start){

    if(isPaused){
      drawPaused();
    }      
    else if(!isPaused){
      if(!ap.isPlaying())
          playNext();

      drawPlaying();      
    }

		evaluateLeapMovement();
	}
  try{
    image(photoPlayingSong, 40 ,height - 150,100,100);
    //text("text",X,Y);
    text(metaDataPlayingSong.title, 170, height - 150)
    }
  catch(Exception e)
  {  }
}

// --- init-functions ---

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
  
  initListBox();  
  initDraw();
  initLoadMinim();  
  
  start = true;
}

void initListBox(){
	cp5 = new ControlP5(this);
  	lb = cp5.addListBox("myList", 40, 100, width - 100, height - 200);
  	lb.setItemHeight(95);
  	lb.setId(1);
}
void initImageSong(){

}

void initLoadMinim(){
	//println(MusicList.get(1));

	minim = new Minim(this);  	
  	ap = minim.loadFile("" + MusicList.get(iSelectedPos));
  	//ap.play();
}

void initDraw(){  
  lb.addItems(MusicList);
  lb.isScrollable();
}

void setInitValues(){
	iSelectedPos = 0;
	json = new JSONArray();
  	start = false;
  	currScroll = 0;
  
  	leap = new LeapMotionP5(this);
}

void loadJSONFile(){
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
    temp.setString("Titel", filenames[i]);
    json.setJSONObject(i, temp);
  }
  
  //println(json);
  saveJSONArray(json, "data/mlist.json");     
}

// --- Main-functions ---

ArrayList<String> ConvertJSONToList(JSONArray myArr){
  ArrayList<String> arrMusic = new ArrayList<String>();
  
  for(int i = 0; i < myArr.size(); i++)
  {
     arrMusic.add(myArr.getJSONObject(i).getString("Filename"));
  }
  
  return arrMusic;
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
  metaDataPlayingSong = ap.getMetaData();
  updateImageSong();
}

void playNext(){
  if(iSelectedPos < MusicList.size()){
    iSelectedPos++;
    loadNewMusicFile("" + MusicList.get(iSelectedPos));
  }
}

void playPrev(){
  if(iSelectedPos > 0){
    iSelectedPos--;
    loadNewMusicFile("" + MusicList.get(iSelectedPos));
  }
}

// --- Input-functions ---

void keyPressed(){
  if(key == 'p'){
    if(ap.isPlaying()){
      ap.pause();
      isPaused = true;
    }
    else{
      ap.play();
      isPaused = false;
    }
  }
  if(key == 'd'){    
    playNext();    
  }
  if(key == 'q'){    
    playPrev();    
  }
}

void evaluateLeapMovement(){
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

// --- Draw-functions ---
void drawPlaying(){
  triangle(500, 650, 500, 700, 525, 675);
}

void drawPaused(){
  rect(500, 650, 10, 50);
  rect(515, 650, 10, 50);
}
// --- api access functies voor image song ---
void updateImageSong()
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
  String urlToAPI = "https://itunes.apple.com/search?term=";
  urlToAPI += songName;
  JSONObject json = loadJSONObject(urlToAPI);
  println("urlToAPI: "+urlToAPI);
  JSONArray array = json.getJSONArray("results");
  JSONObject first = array.getJSONObject(0);
  println("json: "+json);
  String url = first.getString("artworkUrl100");
  println("url: "+url);
  photoPlayingSong = loadImage(url);

  }
  catch (Exception e){
    println("e: "+e);
    photoPlayingSong = loadImage("data\\NoImage.jpg");
  }

}