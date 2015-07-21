package samueltaylor.classicwarlordprototype.Model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sam on 05/07/2015.
 */
public class Bomb extends Object{

    private int bombtype;
    private int size;
    private Region location;
    private List<List<Region>> threatzone;
    private boolean detonated;

    public Bomb(Region r, int type){
        detonated=false;
        threatzone = new ArrayList<>();
        size = 1;
        location = r;
        bombtype = type;
        threatzone.add(r.getAdjacentregions());
    }

    public void increaseSize(){
        size++;
        List<Region> regionList = new ArrayList<>();
        for(Region r : threatzone.get(threatzone.size()-1)){
            for(Region re : r.getAdjacentregions()){
                if(regionList.contains(re)==false){
                    regionList.add(re);
                }
            }
        }
        threatzone.add(regionList);
    }

    public void fireBomb(Region r, List<Empire> affectedEmpires){
        location=r;
        location.detonateBomb(affectedEmpires);
    }

    public boolean checkRange(Region r){
        boolean inrange = false;
        if(location!=null){
            for(int i=0;i<threatzone.size();i++){
                if(threatzone.get(i).contains(r)){
                    inrange = true;
                }
            }
        }
        return inrange;
    }

    public void detonate(List<Empire> affectedEmpires){
        detonated=true;
        switch (bombtype){
            case 0://ATOM
                for(Region r : location.getAdjacentregions()){
                    if(r.getEmpire()!=null && !affectedEmpires.contains(r.getEmpire())){
                        affectedEmpires.add(r.getEmpire());
                    }
                    r.wipeOut();
                    if(r.getBomb()!=null && !r.getBomb().detonated){
                        r.detonateBomb(affectedEmpires);
                    }
                }
                location.Scorch();
                break;

            case 1://HYDROGEN
                for(Region r : location.getAdjacentregions()){
                    r.wipeOut();
                    if(r.getEmpire()!=null && !affectedEmpires.contains(r.getEmpire())){
                        affectedEmpires.add(r.getEmpire());
                    }
                    if(r.getBomb()!=null && !r.getBomb().detonated){
                        r.detonateBomb(affectedEmpires);
                    }
                    r.Scorch();
                    for (Region re : r.getAdjacentregions()){
                        re.wipeOut();
                        if(re.getEmpire()!=null && !affectedEmpires.contains(re.getEmpire())){
                            affectedEmpires.add(r.getEmpire());
                        }
                        if(re.getBomb()!=null && !re.getBomb().detonated){
                            re.detonateBomb(affectedEmpires);
                        }
                    }
                }
                location.Scorch();
                break;
        }

    }

    public boolean willDestroyRegions(List<Region> checkregions, Region target, Region source){
        int affectedamount=0;
        List<Region> affectedRegions = new ArrayList<>();


        projectDetonationRecursively(affectedRegions, checkregions, target, source);


        for(Region r : checkregions){
            if(affectedRegions.contains(r)){
                Log.e("checkregions", r.getName());
                affectedamount++;
            }
        }
        for(Region re : affectedRegions){
            Log.e("affectedRegions", re.getName());
        }
        if(affectedamount==checkregions.size()){
            return true;
        } else {
            return false;
        }
    }

    public void projectDetonationRecursively(List<Region> affectedRegions, List<Region> checkregions, Region target, Region source){
        if(!affectedRegions.contains(target)){
            affectedRegions.add(target);
        }
        switch (bombtype){
            case 0://ATOM
                for(Region r : target.getAdjacentregions()){
                    if(!affectedRegions.contains(r)){
                        if(checkregions.contains(r)){
                            affectedRegions.add(r);
                        }
                        if(r.getBomb()!=null && r!=source){
                            r.getBomb().projectDetonationRecursively(affectedRegions, checkregions,r, source);
                        }
                    }
                }
                break;

            case 1://HYDROGEN
                for(Region r : target.getAdjacentregions()){
                    if(!affectedRegions.contains(r)){
                        if(checkregions.contains(r)){
                            affectedRegions.add(r);
                        }
                        if(r.getBomb()!=null && r!=source){
                            r.getBomb().projectDetonationRecursively(affectedRegions, checkregions,r, source);
                        }
                    }
                    for (Region re : r.getAdjacentregions()){
                        if(!affectedRegions.contains(r)){
                            if(checkregions.contains(r)){
                                affectedRegions.add(r);
                            }
                            if(r.getBomb()!=null && r!=source){
                                r.getBomb().projectDetonationRecursively(affectedRegions, checkregions,r, source);
                            }
                        }
                    }
                }
                break;
        }
    }

    public int getBombtype(){
        return bombtype;
    }
    public String getTypeString(){
        switch (bombtype){
            case 0:
                return "A";
            case 1:
                return "H";
            default:
                return "";
        }
    }
    public int getSize(){return size;}

    public boolean getDetonated(){return detonated;}
    public void setDetonated(boolean b){detonated=b;}

}
