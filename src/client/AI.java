package client;

import client.model.*;

import java.util.ArrayList;
import java.util.Vector;

public class AI {

    private int PICK_PHASE_COUNTER = 0;
    private ArrayList<Hero> herosInVision;
    private Cell[] objectiveCells;
    private History[] histories;
    private Cell[] wallsCell;

    //****************************************
    void preProcess(World world) {
        objectiveCells = world.getMap().getObjectiveZone();
    }

    void pickTurn(World world) {
        pickHeroInPhase(world);
    }

    void moveTurn(World world) {
        init(world);
        if (world.getMovePhaseNum() == 1)
            Utility.printMap(world);
        BlasterDO(world, world.getMyHeroes()[0]);
        BlasterDO(world, world.getMyHeroes()[1]);
        BlasterDO(world, world.getMyHeroes()[2]);
        BlasterDO(world, world.getMyHeroes()[3]);
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
        if (objectiveCells == null) {
            System.out.println("-->ObjectiveCells not assign in PreProccess and init in moveTurn method");
            objectiveCells = world.getMap().getObjectiveZone();
        }
        Utility.printMap(world);

        herosInVision = new ArrayList<>();
        Hero[] oppHeroes = world.getOppHeroes();
        for (int i = 0; i < oppHeroes.length; i++)
            if (oppHeroes[i].getCurrentCell().getColumn() != -1) {
                herosInVision.add(oppHeroes[i]);
                Hero[] sawThisHero = whoSeeThisHero(world.getMyHeroes(),oppHeroes[i]);
            }

        initHistorys(world.getMyHeroes());
    }

    private Hero[] whoSeeThisHero(Hero[] myHeroes, Hero oppHeroe) {
        Vector<Hero> heroes = new Vector<>();
        for (int i = 0; i < myHeroes.length; i++) {

        }
        return null;
    }

    private void initHistorys(Hero[] heroes) {
        if (histories == null) {
            histories = new History[4];
            for (int i = 0; i < 4; i++)
                histories[i] = new History(heroes[i].getId());
        }
    }

    private void pickHeroInPhase(World world) {
        switch (PICK_PHASE_COUNTER) {
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
        PICK_PHASE_COUNTER++;

    }

    private void BlasterDO(World world, Hero blaster) {
        Cell blasterCurrentCell = blaster.getCurrentCell();
        int historyIndex = indexOfHeroInHistory(blaster);
        if(historyIndex == -1) {
            System.out.println("History Index was -1");
            return;
        }
        History history = histories[historyIndex];

        if (herosInVision == null || herosInVision.size() == 0) {
            BlasterNotSeeAnyOne(world, blaster, blasterCurrentCell, history);
        } else /*if (herosInVision.size() == 1)*/{
            blasterSawAMotherFucker(world, blasterCurrentCell, history);
        }
    }

    private void blasterSawAMotherFucker(World world, Cell blasterCurrentCell, History history) {
        System.out.println("=======================start of saw that motherFucker========================");
        Cell lastStep = history.getLastStep();
        if (lastStep == null) {
            System.out.println("=============LAST STEP WaS BE NULL====================");
            //todo fknm byd goriz bzne
        }
        else if(lastStep.equals(blasterCurrentCell)){
            //todo
            System.out.println("LAST STEP EQUALS WITH CUSTEP");
        }else {
            printCell("in cell ", blasterCurrentCell);
            printCell("last step ", lastStep);
            Direction direction;
            if(Math.abs(blasterCurrentCell.getColumn() - lastStep.getColumn()) == 1){
                    direction = lastStep.getColumn() > blasterCurrentCell.getColumn() ?
                            Direction.RIGHT :
                            Direction.LEFT;
                System.out.println("last step in first if , dir="+direction);
            }else if(Math.abs(blasterCurrentCell.getRow() - lastStep.getRow()) == 1){
                direction = lastStep.getRow() > blasterCurrentCell.getRow() ?
                        Direction.DOWN :
                        Direction.UP;
                System.out.println("last step in second if , dir="+direction);
            }else{
                direction=Utility.pathTo(world,blasterCurrentCell,lastStep);
                System.out.println("last step in second if , dir="+direction);
            }
            world.moveHero(history.getHeroID(),direction);
        }
        System.out.println("=======================end of saw that motherFucker========================");
    }

    private void BlasterNotSeeAnyOne(World world, Hero blaster, Cell blasterCurrentCell, History history) {
        int indexOfMinDisFromObjectiveZoneCell = getIndexOfMinDisFromObjectiveZoneCell(blasterCurrentCell);
        if (indexOfMinDisFromObjectiveZoneCell == -1)
            return;
        world.moveHero(blaster,
                Utility.pathTo(world,blasterCurrentCell,objectiveCells[indexOfMinDisFromObjectiveZoneCell]));
        history.move(blasterCurrentCell);
    }

    private void printCell(String str, Cell cell) {
        System.out.println(str+ cell.getRow()+"-"+ cell.getColumn());
    }

    private int getIndexOfMinDisFromObjectiveZoneCell(Cell blasterCurrentCell) {
        int indexOfMinDisFromObjectiveZoneCell = 0;
        int minDis = Utility.Distance(blasterCurrentCell, objectiveCells[0]);
        for (int i = 1; i < objectiveCells.length; i++) {
            int tmpDis = Utility.Distance(blasterCurrentCell, objectiveCells[i]);
            if (minDis < tmpDis) {
                indexOfMinDisFromObjectiveZoneCell = i;
                minDis = tmpDis;
            }
        }
        if (minDis == 0)
            return -1;
        return indexOfMinDisFromObjectiveZoneCell;
    }

    private int indexOfHeroInHistory(Hero hero) {
        for (int i = 0; i < 4; i++) {
            if (histories[i].getHeroID() == hero.getId()) {
                return i;
            }
        }
        System.out.println(String.format("i:%d , HEREID:%d", -1, hero.getId()));
        return -1;
    }
}
