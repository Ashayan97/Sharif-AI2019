package client;

import client.model.*;

public class AI {

    private int PICK_PhASE_COUNTER = 0;
    private Hero[]  herosInVision;
    private Cell[] objectiveCells;

    //****************************************
    void preProcess(World world) {
        objectiveCells = world.getMap().getObjectiveZone();
    }

    void pickTurn(World world) {
        pickHeroInPhase(world);
        PICK_PhASE_COUNTER++;
    }

    void moveTurn(World world) {
        init(world);
    }

    void actionTurn(World world) {
        Hero[] heros = world.getMyHeroes();

        Cell[] cells = Utility.AvailableCells(world.getMap(), 4, heros[0].getCurrentCell());

        world.castAbility(heros[0].getId(), heros[0].getDodgeAbilities()[0], objectiveCells[0]);

    }

    //****************************************
    private void moveturn(World world) {
        if (world.getMovePhaseNum() == 1)
            Utility.printMap(world);
        Hero hero;
        Direction dir;
        System.out.println("score " + world.getMyScore());
        int n;
        switch (world.getMovePhaseNum()) {
            case 1:
                n = 0;
                hero = world.getMyHeroes()[n];
                dir = world.getPathMoveDirections(hero.getCurrentCell(), objectiveCells[n])[0];
//                printInfo(hero, dir);
                world.moveHero(hero, dir);
                break;
            case 2:
                n = 1;
                hero = world.getMyHeroes()[n];
                dir = world.getPathMoveDirections(hero.getCurrentCell(), objectiveCells[n])[0];
                world.moveHero(hero, dir);
                break;
            case 3:
                n = 2;
                hero = world.getMyHeroes()[n];
                dir = world.getPathMoveDirections(hero.getCurrentCell(), objectiveCells[n])[0];
                world.moveHero(hero, dir);
                break;
            case 4:
                n = 3;
                hero = world.getMyHeroes()[n];
                dir = world.getPathMoveDirections(hero.getCurrentCell(), objectiveCells[n])[0];
                world.moveHero(hero, dir);
                break;
            default:
                break;
        }
    }

    private void init(World world) {
        if (objectiveCells == null)
            objectiveCells = world.getMap().getObjectiveZone();
        if (world.getMovePhaseNum() == 1)
            Utility.printMap(world);
        herosInVision = world.getOppHeroes();
    }

    private void pickHeroInPhase(World world) {
        switch (PICK_PhASE_COUNTER) {
            case 0:
                world.pickHero(HeroName.BLASTER);
                break;
            case 1:
                world.pickHero(HeroName.BLASTER);
                break;
            case 2:
                world.pickHero(HeroName.BLASTER);
                break;
            case 3:
                world.pickHero(HeroName.BLASTER);
                break;
        }
    }

    private void BlasterDO(World world,Hero blaster){
        Cell blasterCurrentCell = blaster.getCurrentCell();
        if(herosInVision == null || herosInVision.length == 0){
            int minDis = Utility.Distance(blasterCurrentCell,objectiveCells[0]);
            int indexOfMinDisFromObjectiveZoneCell = 0;
            for (int i = 1; i < objectiveCells.length; i++) {
                int tmpDis = Utility.Distance(blasterCurrentCell, objectiveCells[i]);
                if (minDis < tmpDis) {
                    indexOfMinDisFromObjectiveZoneCell = i;
                    minDis = tmpDis;
                }
            }
            world.moveHero(blaster//this hero
                    , world.getPathMoveDirections(blasterCurrentCell /*from here*/
                            , objectiveCells[indexOfMinDisFromObjectiveZoneCell] /*to here*/)[0]/*get first suggest of paths*/);
        }
    }

}
