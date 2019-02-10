package client;

import client.model.*;

import java.util.ArrayList;

public class AI {

    private int PICK_PhASE_COUNTER = 0;
    private ArrayList<Hero>  herosInVision;
    private Cell[] objectiveCells;
    private History[] histories = new History[4];

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
        if(world.getMovePhaseNum()==1)
            Utility.printMap(world);
        BlasterDO(world,world.getMyHeroes()[0]);
        BlasterDO(world,world.getMyHeroes()[1]);
        BlasterDO(world,world.getMyHeroes()[2]);
        BlasterDO(world,world.getMyHeroes()[3]);
    }

    void actionTurn(World world) {
//        Hero[] heros = world.getMyHeroes();

//        Cell[] cells = Utility.AvailableCells(world.getMap(), 4, heros[0].getCurrentCell());

//        world.castAbility(heros[0].getId(), heros[0].getDodgeAbilities()[0], objectiveCells[0]);

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

        Utility.printMap(world);

        herosInVision = new ArrayList<>();
        for (int i = 0; i < world.getOppHeroes().length; i++)
            if(world.getOppHeroes()[i].getCurrentCell().getColumn()!=-1)
                herosInVision.add(world.getOppHeroes()[i]);
    }

    private void pickHeroInPhase(World world) {
        switch (PICK_PhASE_COUNTER) {
            case 0:
                world.pickHero(HeroName.BLASTER);
                histories[0] = new History(world.getMyHeroes()[0].getId());
                break;
            case 1:
                world.pickHero(HeroName.BLASTER);
                histories[1] = new History(world.getMyHeroes()[1].getId());
                break;
            case 2:
                world.pickHero(HeroName.BLASTER);
                histories[2] = new History(world.getMyHeroes()[2].getId());
                break;
            case 3:
                world.pickHero(HeroName.BLASTER);
                histories[3] = new History(world.getMyHeroes()[3].getId());
                break;
        }
    }

    private void BlasterDO(World world,Hero blaster) {
        Cell blasterCurrentCell = blaster.getCurrentCell();
        History history =histories[indexOfHeroInHistory(blaster)];
        if (herosInVision == null || herosInVision.size() == 0) {
            int indexOfMinDisFromObjectiveZoneCell = getIndexOfMinDisFromObjectiveZoneCell(blasterCurrentCell);
            if (indexOfMinDisFromObjectiveZoneCell == -1)
                return;
            world.moveHero(blaster//this hero
                    , world.getPathMoveDirections(blasterCurrentCell /*from here*/
                            , objectiveCells[indexOfMinDisFromObjectiveZoneCell] /*to here*/)[0]/*get first suggest of paths*/);
            history.move(blaster.getCurrentCell());
        } else {
            System.out.println("=======================saw that motherFucker========================");
            Cell lastStep = history.getLastStep();
            world.moveHero(history.getHeroID(),world.getPathMoveDirections(blasterCurrentCell,lastStep)[0]);
        }
    }

    private int getIndexOfMinDisFromObjectiveZoneCell(Cell blasterCurrentCell) {
        int indexOfMinDisFromObjectiveZoneCell = 0;
        int minDis = Utility.Distance(blasterCurrentCell,objectiveCells[0]);
        for (int i = 1; i < objectiveCells.length; i++) {
            int tmpDis = Utility.Distance(blasterCurrentCell, objectiveCells[i]);
            if (minDis < tmpDis) {
                indexOfMinDisFromObjectiveZoneCell = i;
                minDis = tmpDis;
            }
        }
        if(minDis == 0)
            return -1;
        return indexOfMinDisFromObjectiveZoneCell;
    }
    private int indexOfHeroInHistory(Hero hero){
        for (int i = 0; i <4 ; i++) {
            if(histories[i].getHeroID() == hero.getId()){
                return i;
            }
        }
        return -1;
    }
}
