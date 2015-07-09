package samueltaylor.classicwarlordprototype.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import samueltaylor.classicwarlordprototype.XMLParsing.SVGtoRegionParser;

/**
 * Created by Sam on 05/07/2015.
 */
public class GameModel {

    //Game Management
    private List<Player> players;
    private List<Region> world;
    private List<String> phases;
    private int currentplayerindex;
    private int currentphase=0;
    private boolean nextphase=false;

    //Players
    private Player currentplayer;
    private List<float[]> colours;
    private List<String> colournames;

    //PLAYER COLOURS: BLUE, RED, TAN, GREEN, ORANGE, PURPLE, PINK
    float cBlue[] = { 0.0f, 0.35f, 0.6f, 1.0f};
    float cRed[] = { 0.7f, 0.1f, 0.1f, 1.0f };
    float cGreen[] = { 0.1f, 0.6f, 0.1f, 1.0f };
    float cOrange[] = { 1.0f, 0.5f, 0.0f, 1.0f };
    float cPurple[] = { 0.5f, 0.0f, 0.71f, 1.0f };
    float cTan[] = { 0.71f, 0.5f, 0.25f, 1.0f };
    float cPink[] = { 1.0f, 0.1f, 0.5f, 1.0f };

    //Counters
    int remainingmountaincount =0;

    public GameModel(List<SVGtoRegionParser.Region> r, List<String> pids){
        initialiseColours();
        currentplayerindex=-1;
        int i;
        float[] cTemp;
        players = new ArrayList<>();
        //Following for loop makes sure players are given the same set
        //of pseudo random colours across all the devices, without the need for communication
        java.util.Collections.sort(pids);
        for(String s : pids){
            i = (int)s.charAt(3)%colours.size();//Generate pseudo random number from ID
            cTemp = new float[]{colours.get(i)[0],colours.get(i)[1],colours.get(i)[2],1.0f};
            Player p = new Player(cTemp, colournames.get(i), s);//Pick that colour
            colours.remove(colours.get(i));
            players.add(p);
        }

        world = new ArrayList<>();
        //Load world into Model
        for(SVGtoRegionParser.Region re : r){
            Region tmpRegion = new Region(re.name, re.type);
            world.add(tmpRegion);
            if(re.type.equals("mountain")){
                remainingmountaincount++;
            }
        }

        phases = new LinkedList<>(Arrays.asList("Mountain", "Reinforcement", "Bombing", "Attack"));
    }

    public void nextPlayer(){//Move to next player's turn
        currentplayerindex++;
        if(currentplayerindex>=players.size()){
            currentplayerindex=0;
            if(currentphase!=0){//Mountain phase (0) precedes usual phase cycle
                nextphase=true;
            }
        }
        currentplayer = players.get(currentplayerindex);
    }

    public void nextPhase(){
        currentphase++;
        if(currentphase>phases.size()){
            currentphase=1;//Reset to reinforcement phase
        }
    }

    private void initialiseColours(){
        colours = new LinkedList<>(Arrays.asList(cBlue,cRed,cGreen,cOrange,cPurple,cPink,cTan));
        colournames = new LinkedList<>(Arrays.asList("Blue", "Red", "Green", "Orange", "Purple", "Pink", "Tan"));
    }

    public boolean mountainsAvailable(){
        if((double) remainingmountaincount /players.size()<1){
            return false;
        } else {
            return true;
        }
    }



    //Get/set

    public float[] getParticipantColour(String pid){
        for(Player p : players){
            if(p.getParticipantid().equals(pid)){
                return p.getColour();
            }
        }
        return null;
    }

    public Player getPlayer(String pid){
        for(Player p : players){
            if(p.getParticipantid().equals(pid)){
                return p;
            }
        }
        return null;
    }
    public List<Player> getPlayers(){return players;}
    public Player getCurrentplayer(){return currentplayer;}

    public String getCurrentphase() {return phases.get(currentphase);}
    public boolean getNextphase(){return nextphase;}

    public Region getRegion(int id){
        return world.get(id);
    }
    public Region getRegionByName(String name){
        for(Region r : world){
            if(name.equals(r.getName())){
                return r;
            }
        }
        return null;
    }
    public int getRegionIDByName(String name){
        for(int i=0;i<world.size();i++){
            if(name.equals(getRegion(i).getName())){
                return i;
            }
        }
        return 0;
    }


    public void setRemainingmountaincount(int i){
        remainingmountaincount +=i;}
    public int getRemainingmountaincount(){ return remainingmountaincount;}

}

