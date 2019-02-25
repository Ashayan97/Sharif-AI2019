package client;

import client.model.*;

import java.util.ArrayList;

/**
 * @author : Amirhossein
 * guardian hero
 * */
public class Guardian_AI {
    private static int GUARDIAN_DOGE_RANGE = 2;
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
        // if guardian is outside of objective zone
        if(!isInObjectiveZone() && isDogeReady()){
            if(world.manhattanDistance(guardian.getCurrentCell(),findNearestCellOurSide())<=
                    guardian.getAbility(AbilityName.GUARDIAN_DODGE).getRange()) {
                world.castAbility(guardian, AbilityName.GUARDIAN_DODGE, findNearestCellOurSide());
            }else {
                world.castAbility(guardian, AbilityName.GUARDIAN_DODGE, nearOutsideOfObjective());
            }
            return;
        }
        // if guardian in objective Zone -->

    }

    private Cell nearOutsideOfObjective() {
        Cell[] path = directionPathToCell();
        if(path!=null) {
            for (Cell aPath : path) {
                if (world.manhattanDistance(guardian.getCurrentCell(), aPath) ==
                        guardian.getAbility(AbilityName.GUARDIAN_DODGE).getRange())
                    return aPath;
            }
        }
        return null;
    }

    private Cell[] directionPathToCell() {
        Direction[] path = world.getPathMoveDirections(guardian.getCurrentCell(),findNearestCellOurSide());
        ArrayList<Cell> result =  new ArrayList<>() ;
        Cell src = guardian.getCurrentCell();
        for (Direction dir : path) {
            src = Utility.nextCell(world,src,dir);
            result.add(src);
        }
        return (Cell[]) result.toArray();
    }

    public void movePhase(){
        //TODO -->
        //if guardian are not in Objective zone :
        if(!isInObjectiveZone()){
                world.moveHero(guardian,nearestObjectiveCell()[0]);
        }else {
            // guardian are in Objective zone
            // if can't see any one
            Cell cell = findNearestCellOurSide();
            if (!canSeeAnyOne()){
                // go to nearest row of objective Zone after Blaster
                world.moveHero(guardian,world.getPathMoveDirections(guardian.getCurrentCell(),findNearestCellOurSide())[0]);
            } else { // guardian an see enemyHeroes -->
                //TODO ==> guardian can see enemies
            }
        }

    }

    private Cell findNearestCellOurSide() {
        Cell[] myRespawnZone  = map.getMyRespawnZone();
        Cell[] objectiveZone =  map.getObjectiveZone();
        Cell result = null;
        int minDistance = Integer.MAX_VALUE;
        for (int i = 0; i < objectiveZone.length; i++) {
            for (int j = 0; j < myRespawnZone.length; j++) {
                if(world.manhattanDistance(objectiveZone[i],myRespawnZone[j])<minDistance){
                    minDistance = world.manhattanDistance(objectiveZone[i],myRespawnZone[j]);
                    result = objectiveZone[i];
                }
            }
        }
        return result;
    }

    private boolean isDogeReady() {
        return guardian.getAbility(AbilityName.GUARDIAN_DODGE).isReady();
    }

    private boolean canSeeAnyOne() {
        return new Range_fight(world).inVisionEnemy(guardian).length != 0;
    }

    private boolean isInObjectiveZone(){
        return guardian.getCurrentCell().isInObjectiveZone();
    }

    private Direction[] nearestObjectiveCell(){
        return world.getPathMoveDirections(guardian.getCurrentCell(),
                new Range_fight(world).findNearestZoneCell(guardian.getCurrentCell()));
    }
}
