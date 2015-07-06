package samueltaylor.classicwarlordprototype.Model;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import samueltaylor.classicwarlordprototype.XMLParsing.SVGtoRegionParser;

/**
 * Created by Sam on 05/07/2015.
 */
public class GameModel {
    private List<Player> players;
    private List<Region> world;
    private int hostPlayer;
    private int currentPlayer;
    private List<float[]> colours;

    //PLAYER COLOURS: BLUE, RED, TAN, GREEN, ORANGE, PURPLE, PINK
    float cBlue[] = { 0.0f, 0.337f, 0.639f, 1.0f };
    float cRed[] = { 0.78f, 0.012f, 0.059f, 1.0f };
    float cGreen[] = { 0.09f, 0.643f, 0.09f, 1.0f };
    float cOrange[] = { 1.0f, 0.5f, 0.0f, 1.0f };
    float cPurple[] = { 0.482f, 0.031f, 0.71f, 1.0f };
    float cTan[] = { 0.71f, 0.545f, 0.251f, 1.0f };
    float cPink[] = { 1.0f, 0.133f, 0.569f, 1.0f };

    public GameModel(List<SVGtoRegionParser.Region> r, List<String> pids){
        initialiseColours();
        int i;
        float[] cTemp;
        players = new ArrayList<>();
        //Following for loop makes sure players are given the same set
        //of pseudo random colours across all the devices, without the need for communication
        java.util.Collections.sort(pids);
        for(String s : pids){
            i = (int)s.charAt(3);//Generate pseudo random number from ID
            while(i>=colours.size()){i-=colours.size();}//Get it into the colour array range
            cTemp = new float[]{colours.get(i)[0],colours.get(i)[1],colours.get(i)[2],1.0f};
            Player p = new Player(cTemp, s);//Pick that colour
            colours.remove(colours.get(i));
            players.add(p);
        }
        currentPlayer=0;
    }

    private void initialiseColours(){
        colours = new LinkedList<>(Arrays.asList(cBlue,cRed,cGreen,cOrange,cPurple,cPink,cTan));
    }

    public float[] getPlayerColour(String pid){
        for(Player p : players){
            if(p.getParticipantid().equals(pid)){
                return p.getColour();
            }
        }
        return null;
    }
}
