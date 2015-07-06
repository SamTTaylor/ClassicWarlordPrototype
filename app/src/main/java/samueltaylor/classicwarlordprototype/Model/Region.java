package samueltaylor.classicwarlordprototype.Model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import samueltaylor.classicwarlordprototype.GameController;

/**
 * Created by Sam on 05/07/2015.
 */
public class Region extends Object{

    private boolean selected;
    private Army army=null;
    private Bomb bomb;
    private Empire empire=null;
    private List<Region> adjacentregions;
    private boolean scorched;
    private String type;
    private String name;

    public Region(String n, String t){
        adjacentregions = new ArrayList<>();
        name=n;
        type=t;
        scorched=false;
    }

    public boolean allocateArmy(Army a){
        if (army==null){
            army = a;
            return true;
        } else {
            Log.e("REGION", "CANNOT ALLOCATE MORE THAN 1 ARMY");
            return false;
        }
    }

    public void wipeOut(){
        if(army!=null){
            army.destroy();
        }
        if(empire!=null){
            empire.removeRegion(this);
        }
    }

    public void detonateBomb(){
        if(bomb!=null){
            bomb.detonate();
        }
    }

    public void Scorch(){
        wipeOut();
        scorched=true;
    }

    public List<Region> getAdjacentregions(){
        return adjacentregions;
    }

    public boolean allocateBomb(int type){
        if(bomb!=null){
            bomb = new Bomb(this, type);
        } else if(bomb.getBombtype()==type){
            bomb.increaseSize();
        } else {
            return false;//BOMB ALLOCATION UNSUCCESSFUL
        }
        return true;
    }
}