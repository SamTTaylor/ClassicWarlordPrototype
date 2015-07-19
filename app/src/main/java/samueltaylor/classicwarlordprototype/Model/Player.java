package samueltaylor.classicwarlordprototype.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sam on 05/07/2015.
 */


public class Player extends Object{

    private String participantid;
    private List<Army> armies;
    private List<Empire> empires;
    private float[] colour;
    private String colourstring;
    int selectedregionid=-1;
    int prevselectedregionid=-1;

    public Player(float[] c, String cs, String pid){
        armies = new ArrayList<>();
        empires = new ArrayList<>();
        participantid = pid;
        colour = c;
        colourstring=cs;
    }

    int countEmpires(){
        return empires.size();
    }

    int countRegions(){
        int regions=0;
        return regions;
    }



    void moveArmy(Region src, Region dst){

    }

    void attackRegion(Region src, Region dst, int pledge){

    }

    int defendRegion(Region r){
        int guess=0;
        return guess;
    }

    void allocateBomb(Region r){

    }

    void allocateArmy(Region r, int amount){
        Army a = new Army(this, amount);
        r.allocateArmy(a);
    }

    public void newEmpire(Region r){//Creates a new empire starting at specified region
        Empire empire = new Empire(r);
        empires.add(empire);
        r.setEmpire(empire);
        allocateArmy(r,1);
    }

    public List<Empire> getEmpires(){
        return empires;
    }
    public void addEmpire(Empire e) {empires.add(e);}

    public String getParticipantid(){
        return participantid;
    }

    public float[] getColour(){
        return colour;
    }
    public String getColourstring(){return colourstring;}

    public int getSelectedregionid(){return selectedregionid;}

    public void setSelectedregionid(int i)
    {
        prevselectedregionid=selectedregionid;
        if(selectedregionid==i){
            selectedregionid=-1;
        }else{
            selectedregionid = i;
        }
    }

    public int getPrevselectedregionid(){return prevselectedregionid;}
    public void setPrevselectedregionid(int i){prevselectedregionid=i;}
    public void resetPrevselectedregionid(){prevselectedregionid=-1;}

    public void deselectall(){selectedregionid=-1;prevselectedregionid=-1;}
}
