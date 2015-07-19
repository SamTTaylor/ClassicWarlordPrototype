package samueltaylor.classicwarlordprototype.Model;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Sam on 05/07/2015.
 */
public class Empire extends Object{

    private List<Region> regions;
    private int unallocatedforces=0;
    private int unallocatedbombs=0;

    public Empire(Region r){
        regions = new ArrayList<>();
        regions.add(r);
    }

    public Region getRegion(String name){
        for(Region r : regions){
            if(r.getName().equals(name)){
                return r;
            }
        }
        return null;
    }

    public void addRegion(Region r){
        if(!regions.contains(r)){
            regions.add(r);
            r.setEmpire(this);
        } else {
            Log.e("EMPIRE", "Region " + r.getName() + " already in Empire of " + regions.get(0).getName());
        }
    }

    public void removeRegion(Region r){
        if(regions.contains(r)==true){
            regions.remove(r);
        } else {
            Log.e("EMPIRE", "Region " + r.getName() +" not in Empire");
        }
    }

    public List<Region> getRegions() {
        return regions;
    }

    public void joinEmpire(Empire e){
        for(Region r : e.getRegions()){//Take all regions from target empire
            addRegion(r);
        }
        try {
            e.finalize();//Attempt to destroy target empire
        } catch (Throwable throwable) {
            Log.e("EMPIRE", "Failed to finalize empire: "+throwable.toString());
        }
    }

    public int countReinforcements(){
        int reinforcements=1;//1 army for each empire
        int cities=0;
        int dense=0;
        int rural=0;
        //Count regions, for each region, add to count
        for(Region r : regions){
            switch (r.getType()){
                case"city":
                    cities++;
                    break;
                case"dense":
                    dense++;
                    break;
                case"rural":
                    rural++;
                    break;
            }
        }
        reinforcements+=cities;//1 army for each city
        reinforcements+=dense/2;//1 army for 2 dense
        reinforcements+=rural/3;//1 army for 3 rural
        return reinforcements;
    }

    public void checkSplitEmpire(Region r){
        //Checks if the empire has split into disconnected sections and if it has, creates new Empires for each of them
        //loop through each region
        boolean thisempireused=false;
        Player p = regions.get(0).getArmy().getPlayer();
        List<Region> lstAdj = new ArrayList<>(r.getAdjacentregions());

        List<Region> regionsHandled = new ArrayList<>();
        List<Region> linkedregions = new ArrayList<>();
        for(Region reg : lstAdj){
            if(!regionsHandled.contains(reg) && reg.getEmpire()!=null && reg.getEmpire()==this){
                reg.getAllLinkedRegions(reg.getEmpire(), linkedregions);
                regionsHandled.add(reg);
                for(Region re : lstAdj){//Only check regions that haven't been handled yet
                    if(!regionsHandled.contains(re) && linkedregions.contains(re)){
                        regionsHandled.add(re);
                    }
                }
                if(thisempireused){//For first iteration just leave them in current empire
                    Empire e = new Empire(reg);//Otherwise, create new empire and put all those regions in it
                    for(Region regi : linkedregions){
                        if(regi!=r){
                            e.addRegion(regi);
                            regi.setEmpire(e);
                            p.addEmpire(e);
                        }
                    }
                }
                thisempireused=true;
            }
        }




    }

    public void Reinforce(){
        unallocatedforces+=countReinforcements();
    }
    public int getUnallocatedforces(){return unallocatedforces;}
    public void adjustUnallocatedforces(int i){unallocatedforces+=i;}
    public void resetUnallocatedforces(){
        for(Region r : regions){
            r.resetAllocatedforces();
        }
        unallocatedforces=0;
    }
}
