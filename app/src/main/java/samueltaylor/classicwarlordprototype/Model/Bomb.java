package samueltaylor.classicwarlordprototype.Model;

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

    public Bomb(Region r, int type){
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
        detonate(affectedEmpires);
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
        switch (bombtype){
            case 0://ATOM
                for(Region r : location.getAdjacentregions()){
                    if(r.getEmpire()!=null && !affectedEmpires.contains(r.getEmpire())){
                        affectedEmpires.add(r.getEmpire());
                    }
                    r.wipeOut();
                    r.detonateBomb(affectedEmpires);
                }
                location.detonateBomb(affectedEmpires);
                location.Scorch();
                break;

            case 1://HYDROGEN
                for(Region r : location.getAdjacentregions()){
                    r.wipeOut();
                    r.detonateBomb(affectedEmpires);
                    r.Scorch();
                    for (Region re : r.getAdjacentregions()){
                        r.wipeOut();
                        r.detonateBomb(affectedEmpires);
                    }
                }
                location.detonateBomb(affectedEmpires);
                location.Scorch();
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

}
