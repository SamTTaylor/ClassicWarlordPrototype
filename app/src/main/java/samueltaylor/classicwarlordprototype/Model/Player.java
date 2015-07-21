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
        for(Empire e : empires){
            for (Region r : e.getRegions()){
                regions++;
            }
        }
        return regions;
    }

    public void allocateBomb(Region r, int type){
        r.allocateBomb(type);
    }

    public void allocateArmy(Region r, int amount){
        Army a = new Army(this, amount);
        r.getEmpire().allocateArmy(r,a);
        armies.add(a);
    }

    public void newEmpire(Region r, int armysize){//Creates a new empire starting at specified region
        Empire empire = new Empire(r);
        empires.add(empire);
        allocateArmy(r,armysize);
        empire.setPlayer(this);
    }

    public List<Empire> getEmpires(){
        return empires;
    }
    public void addEmpire(Empire e) {empires.add(e);}
    public void removeEmpire(Empire e){empires.remove(e);}

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

    public void removeArmy(Army a){armies.remove(a);}
}
