package samueltaylor.classicwarlordprototype.Model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import samueltaylor.classicwarlordprototype.GameController;

/**
 * Created by Sam on 05/07/2015.
 */
public class Region extends Object{

    private int allocatedforces=0;
    private Army army=null;
    private Bomb bomb;
    private Empire empire=null;
    private List<Region> adjacentregions;
    private boolean scorched;
    private String type; //Types are mountain, city, dense, rural, sea and light
    private String name;

    private boolean counted=false;

    public Region(String n, String t){

        name=n;
        type=t;
        scorched=false;
    }

    public void wipeOut(){
        if(army!=null){
            army.destroy();
            army=null;
        }
        if(empire!=null){
            empire.removeRegion(this);
            if(empire.getRegions().size()<=0){
                empire.getPlayer().removeEmpire(empire);
            }
            empire=null;
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
        if(adjacentregions==null){
            return null;
        } else {
            return adjacentregions;
        }
    }

    public void addAdjacentRegion(Region r){
        if(adjacentregions==null){
            adjacentregions = new ArrayList<>();
        }
        adjacentregions.add(r);
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

    //Echos outward through all connected regions in the same empire and compiles
    //them into a list
    public List<Region> getAllLinkedRegions(Empire e, List<Region> lstRegions){
        lstRegions.add(this);
        for(Region r : getAdjacentregions()){
            if(!lstRegions.contains(r)){
                if(r.getEmpire()!=null && r.getEmpire()==e){
                    r.getAllLinkedRegions(e, lstRegions);
                }
            }

        }
        return lstRegions;
    }

    public String getName(){return name;}
    public String getType(){return type;}
    public boolean isOwned() {if(empire==null){return false;}else{return true;}}

    public void setEmpire(Empire e){empire=e;}//Should be set only through the Empire.addRegion(); method
    public Empire getEmpire(){return empire;}

    public void setCounted(boolean f){counted=f;}
    public boolean getCounted(){return counted;}

    public Bomb getBomb(){return bomb;}
    public Army getArmy(){return army;}
    public void setArmy(Army a){army = a;}

    public int getAllocatedforces(){return allocatedforces;}
    public void adjustAllocatedforces(int i){allocatedforces+=i;}
    public void resetAllocatedforces(){allocatedforces=0;}
}
