package samueltaylor.classicwarlordprototype.Model;

import android.util.Log;

/**
 * Created by Sam on 05/07/2015.
 */
public class Army extends Object {

    private int size;
    private Region location;
    private Player player;


    public Army (Player p, int s){
        player = p;
        size = s;
    }

    public void destroy(){
        try {
            this.finalize();
        } catch (Throwable throwable) {
            Log.e("ARMY", throwable.toString());
        }
    }

    void splitArmy(Region r, int x){

    }

    public int getSize(){return size;}
}
