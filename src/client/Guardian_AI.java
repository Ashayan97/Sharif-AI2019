package client;

import client.model.Hero;
import client.model.Map;
import client.model.World;

/**
 * @author : Amirhossein
 * guardian hero
 * */
public class Guardian_AI {
    private Hero guardian;
    private World world;
    private Map map;
    private Hero[] enemyHeros;

    public Guardian_AI(Hero guardian, World world) {
        this.guardian = guardian;
        this.world = world;
        this.map = world.getMap();
    }

    public Hero getGuardian() {
        return guardian;
    }

    public void setGuardian(Hero guardian) {
        this.guardian = guardian;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public void actionPhase(){
        //TODO -->
    }

    public void movePhase(){
        //TODO -->
    }


}
