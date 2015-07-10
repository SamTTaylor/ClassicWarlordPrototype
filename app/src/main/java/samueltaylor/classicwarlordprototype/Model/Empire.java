package samueltaylor.classicwarlordprototype.Model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sam on 05/07/2015.
 */
public class Empire extends Object{

    private List<Region> regions;
    private int unallocatedforces;
    private int unallocatedbombs;

    public Empire(Region r){
        regions = new ArrayList<>();
        regions.add(r);
    }

    public Region getRegion(String name){
//        for(Region r : regions){
//            if(r.name.equals(name)){
//                return r;
//            }
//        }
        return null;
    }

    public void addRegion(Region r){
        if(regions.contains(r)==false){
            regions.add(r);
        } else {
            Log.e("EMPIRE", "Region already in Empire");
        }
    }

    public void removeRegion(Region r){
        if(regions.contains(r)==true){
            regions.remove(r);
        } else {
            Log.e("EMPIRE", "Region not in Empire");
        }
    }

    public List<Region> getRegions() {
        return regions;
    }

    public void joinEmpire(Empire e){
        for(Region r : e.getRegions()){
            addRegion(r);
            e.removeRegion(r);
        }
        try {
            e.finalize();
        } catch (Throwable throwable) {
            Log.e("EMPIRE", "Failed to finalize empire: "+throwable.toString());
        }
    }

    int countReinforcements(){
        int reinforcements=0;
        //Count empires, for each empire, tally regions' worth
        return reinforcements;
    }

    public void Reinforce(){
        unallocatedforces+=countReinforcements();
    }
}
