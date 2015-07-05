package samueltaylor.classicwarlordprototype.Model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sam on 05/07/2015.
 */
public class Empire extends Object{

    private List<Region> regions;

    public Empire(){
        regions = new ArrayList<>();
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
}
