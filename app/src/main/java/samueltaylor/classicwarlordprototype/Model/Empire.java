package samueltaylor.classicwarlordprototype.Model;

import android.util.Log;

import java.util.ArrayList;
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

    public void Reinforce(){
        unallocatedforces+=countReinforcements();
    }
    public int getUnallocatedforces(){return unallocatedforces;}
}
