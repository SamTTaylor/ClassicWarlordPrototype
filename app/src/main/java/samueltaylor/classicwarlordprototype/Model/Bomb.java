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
        for(Region r : threatzone.get(size-1)){
            for(Region re : r.getAdjacentregions()){
                if(regionList.contains(re)==false){
                    regionList.add(re);
                }
            }
        }
        threatzone.add(regionList);
    }

    public boolean fireBomb(Region r){
        List<Region> regionList;
        boolean inrange = false;
        if(location!=null){
            for(int i=0;i<threatzone.size();i++){
                if(threatzone.get(i).contains(r)){
                    location=r;
                    detonate();
                    inrange = true;
                }
            }
        }
        return inrange;
    }

    public void detonate(){
        switch (bombtype){
            case 0://ATOM
                for(Region r : location.getAdjacentregions()){
                    r.wipeOut();
                }
                location.Scorch();
                break;

            case 1://HYDROGEN
                for(Region r : location.getAdjacentregions()){
                    r.wipeOut();
                    r.detonateBomb();
                    r.Scorch();
                    for (Region re : r.getAdjacentregions()){
                        r.wipeOut();
                        r.detonateBomb();
                    }
                }
                location.Scorch();
                break;
        }
    }

    public int getBombtype(){
        return bombtype;
    }

}
