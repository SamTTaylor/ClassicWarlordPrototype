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
    private int unallocatedforces;
    private int unallocatedbombs;
    private float[] colour;
    int selectedregionid=-1;
    int prevselectedregionid=-1;

    public Player(float[] c, String pid){
        armies = new ArrayList<>();
        empires = new ArrayList<>();
        participantid = pid;
        unallocatedforces=0;
        unallocatedbombs=0;
        colour = c;
    }

    int countEmpires(){
        return empires.size();
    }

    int countRegions(){
        int regions=0;
        return regions;
    }

    int countReinforcements(){
        int reinforcements=0;
        //Count empires, for each empire, tally regions' worth
        return reinforcements;
    }

    public void Reinforce(){
        unallocatedforces+=countReinforcements();
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

    }

    public String getParticipantid(){
        return participantid;
    }

    public float[] getColour(){
        return colour;
    }
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
}
