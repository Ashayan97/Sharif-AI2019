package client;

import client.model.*;

import java.util.ArrayList;
import java.util.Vector;

public class AI {

    private int PICK_PHASE_COUNTER = 0;
    private ArrayList<Hero> herosInVision;
    private Cell[] objectiveCells;
    private History[] histories;
    private Vector<Cell> wallsCell;

    //****************************************
    void preProcess(World world) {
        objectiveCells = world.getMap().getObjectiveZone();
    }

    void pickTurn(World world) {
        pickHeroInPhase(world);
    }

    void moveTurn(World world) {
        Utility.printMap(world);
        init(world);
        if (world.getMovePhaseNum() == 1)
            Utility.printMap(world);
        BlasterDO(world, world.getMyHeroes()[0]);
        BlasterDO(world, world.getMyHeroes()[1]);
        BlasterDO(world, world.getMyHeroes()[2]);
        BlasterDO(world, world.getMyHeroes()[3]);
    }

    void actionTurn(World world) {
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

    /**
     * this method initialize our need across the phase or turn
     */
    private void init(World world) {
        if (objectiveCells == null) {
            System.out.println("-->ObjectiveCells not assign in PreProccess and init in moveTurn method");
            objectiveCells = world.getMap().getObjectiveZone();
        }
        initWallCell(world);

        initHistorys(world.getMyHeroes());

        initHeroInVision(world);

    }

    private void initHeroInVision(World world) {
        herosInVision = new ArrayList<>();
        for (int i = 0; i < 4; i++)
            histories[i].cleareSawHeroes();
        Hero[] oppHeroes = world.getOppHeroes();
        for (Hero oppHeroe : oppHeroes)
            if (oppHeroe.getCurrentCell().getColumn() != -1) {
                herosInVision.add(oppHeroe);
                Hero[] sawThisHero = whoSeeThisHero(world, oppHeroe);
                for (Hero aSawThisHero : sawThisHero)
                    histories[indexOfHeroInHistory(aSawThisHero)].addHero(oppHeroe);
            }
    }

    private void initWallCell(World world) {
        if (wallsCell == null) {
            System.out.println("-->wallsCell not assign in PreProccess and init in moveTurn method");
            wallsCell = new Vector<>();
            for (Cell[] arryCell : world.getMap().getCells())
                for (Cell cell : arryCell)
                    if (cell.isWall())
                        wallsCell.add(cell);
        }
    }

    /**
     * in method check migkone ke ag histories ma init nashode
     * initesh kone
     */
    private void initHistorys(Hero[] heroes) {
        if (histories == null) {
            histories = new History[4];
            for (int i = 0; i < 4; i++)
                histories[i] = new History(heroes[i].getId());
        }
    }

    /**
     * in method mige kodum az hero haye ma(@myHeroes), hero'ye doshman(oppHero) ro didan
     **/
    private Hero[] whoSeeThisHero(World world, Hero oppHeroe) {
        Vector<Hero> heroes = new Vector<>();
        Hero[] myHeroes = world.getMyHeroes();
        Cell oppHeroCurrentCell = oppHeroe.getCurrentCell();
        for (Hero myHero : myHeroes) {
            if(world.isInVision(myHero.getCurrentCell(),oppHeroCurrentCell))
                heroes.add(myHero);
//            Line line = Line.CREATOR(myHero.getCurrentCell(), oppHeroCurrentCell);
//            boolean isCollision = false;
//            // this scope get up down right and left to consider only wall in this range [ up,down ][ left,right]
//            Cell up = oppHeroCurrentCell.getRow() >= myHero.getCurrentCell().getRow() ?
//                    myHero.getCurrentCell() : oppHeroCurrentCell;
//            Cell down = oppHeroCurrentCell.getRow() <= myHero.getCurrentCell().getRow() ?
//                    myHero.getCurrentCell() : oppHeroCurrentCell;
//            Cell right = oppHeroCurrentCell.getColumn() >= myHero.getCurrentCell().getColumn() ?
//                    oppHeroCurrentCell : myHero.getCurrentCell();
//            Cell left = oppHeroCurrentCell.getColumn() <= myHero.getCurrentCell().getColumn() ?
//                    oppHeroCurrentCell : myHero.getCurrentCell();
//            //***end scope******
//            for (Cell aWallsCell : wallsCell) {
//                if (aWallsCell.getColumn() <= right.getColumn() && aWallsCell.getColumn() >= left.getColumn()
//                        && aWallsCell.getRow() >= up.getRow() && aWallsCell.getRow() <= down.getRow()) {
//                    if (line.isCollisionToWall(aWallsCell)) {
//                        isCollision = true;
//                        break;
//                    }
//                }
//            }
//            if (!isCollision)
//                heroes.add(myHero);
        }
        return heroes.toArray(new Hero[]{});
    }

    /**
     * we pick our hero for game in this method
     */
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

    /**
     * @Blaster charecter do this method across the game
     */
    private void BlasterDO(World world, Hero blaster) {
        Cell blasterCurrentCell = blaster.getCurrentCell();
        int historyIndex = indexOfHeroInHistory(blaster);
        if (historyIndex == -1) {
            System.out.println("History Index was -1");
            return;
        }
        History history = histories[historyIndex];

        if (herosInVision == null ||
                herosInVision.size() == 0 ||
                history.getSawHeroes().size() == 0) { // check to bezani bomb
            BlasterNotSeeAnyOne(world, blaster, blasterCurrentCell, history);
        } else if (herosInVision.size() == 1) {
            blasterSawAMotherFucker(world, blasterCurrentCell, history);
        }
    }

    /**
     * when blaster not see enemys
     * do this method
     */
    private void BlasterNotSeeAnyOne(World world, Hero blaster, Cell blasterCurrentCell, History history) {
        int indexOfMinDisFromObjectiveZoneCell = getIndexOfMinDisFromObjectiveZoneCell(blasterCurrentCell);
        if (indexOfMinDisFromObjectiveZoneCell == -1)
            return;
        Utility.move(world, blaster.getId(), blasterCurrentCell, objectiveCells[indexOfMinDisFromObjectiveZoneCell]);
        history.move(blasterCurrentCell);
        printCell("Blaster Cell ",blasterCurrentCell);
        printCell("ObjectiveCell detected",objectiveCells[indexOfMinDisFromObjectiveZoneCell]);
    }

    /**
     * when blaster see the an opp_hero do this metod
     */
    private void blasterSawAMotherFucker(World world, Cell blasterCurrentCell, History history) {
        System.out.println("=======================start of saw that motherFucker "+history.getHeroID()+" ========================");
        Hero mHero = world.getHero(history.getHeroID());
        Hero enemy = history.getSawHeroes().lastElement();
        Cell enemyCurrentCell = enemy.getCurrentCell();
        int mHeroID = history.getHeroID();

        System.out.println("I Saw those MotherFuckers");
        if(Utility.distance(blasterCurrentCell,enemyCurrentCell) == 1
                && mHero.getCurrentCell().isInObjectiveZone())
            return;
        ATTACK_STATE state = Utility.canAttack(mHero, enemy);
        if (state == ATTACK_STATE.DORADOR
                || state == ATTACK_STATE.TANBETAN
                || state == ATTACK_STATE.CANTATTACK) {
//            int distanceFromEnemy = Utility.distance(blasterCurrentCell, enemyCurrentCell);
//            if ((distanceFromEnemy < Utility_Attack.range_of_blaster_bomb)){
//                world.castAbility(mHeroID, AbilityName.BLASTER_BOMB, enemyCurrentCell);
//            }else {
//                Cell[] availavleCells = Utility.availableCells(world.getMap(),
//                        Utility_Attack.range_of_blaster_bomb,
//                        blasterCurrentCell);
//                for (Cell availavleCell : availavleCells)
//                    if (Utility.distance(availavleCell,
//                                        enemyCurrentCell) <= Utility_Attack.range_of_bomb)
//                        world.castAbility(mHeroID, AbilityName.BLASTER_BOMB, enemyCurrentCell);
//            }
            int cellIndexMinDisToObjctibve=getIndexOfMinDisFromObjectiveZoneCell(blasterCurrentCell);
            Cell nextToObjective = Utility.nextCell(world,blasterCurrentCell,objectiveCells[cellIndexMinDisToObjctibve]);
            int dis1 = Utility.distance(nextToObjective,enemyCurrentCell);
            int dis2 = Utility.distance(blasterCurrentCell,enemyCurrentCell);
            if(dis1 <= dis2)
                Utility.move(world,mHeroID,blasterCurrentCell,nextToObjective);
            else
                Utility.move(world, mHeroID, blasterCurrentCell, enemyCurrentCell);
            history.addLastStep(blasterCurrentCell);
        } else if (state == ATTACK_STATE.SCAPE) {
            Cell lastCell = history.getLastStep();
            if (!world.isInVision(lastCell, enemyCurrentCell)) {
                Utility.move(world, mHeroID, blasterCurrentCell, lastCell);
                history.addLastStep(blasterCurrentCell);
            }else {
                Cell[] cells = Utility.availableCells(world.getMap(), 2, blasterCurrentCell);
                int flag = 0;
                for (Cell cell : cells)
                    if (!world.isInVision(blasterCurrentCell, cell)) {
                        Utility.move(world, mHeroID, blasterCurrentCell, cell);
                        history.addLastStep(blasterCurrentCell);
                        flag = 1;
                        break;
                    }
                if (flag == 0) {
                        Cell[] around = Utility.availableCells(world.getMap(),4,blasterCurrentCell);
                        int max = Utility.distance(enemyCurrentCell,around[0]);
                        int indexOfMax = 0;
                    for (int i = 1; i < around.length; i++) {
                        int tmp = Utility.distance(enemyCurrentCell,around[i]);
                        if(tmp>max){
                            indexOfMax = i;
                            max = tmp;
                        }
                    }
                    Utility.move(world,mHeroID,blasterCurrentCell,around[indexOfMax]);
                    history.addLastStep(blasterCurrentCell);
                }
            }
        }
    }

    /**
     * this method an cell and return the nearest objective cell
     */
    private int getIndexOfMinDisFromObjectiveZoneCell(Cell blasterCurrentCell) {
        int indexOfMinDisFromObjectiveZoneCell = 0;
        int minDis = Utility.distance(blasterCurrentCell, objectiveCells[0]);
        for (int i = 1; i < objectiveCells.length; i++) {
            int tmpDis = Utility.distance(blasterCurrentCell, objectiveCells[i]);
            if (minDis < tmpDis) {
                indexOfMinDisFromObjectiveZoneCell = i;
                minDis = tmpDis;
            }
        }
        if (minDis == 0)
            return -1;
        return indexOfMinDisFromObjectiveZoneCell;
    }

    /**
     * this method get an hero and return the index of he in histories array
     */
    private int indexOfHeroInHistory(Hero hero) {
        for (int i = 0; i < 4; i++) {
            if (histories[i].getHeroID() == hero.getId()) {
                return i;
            }
        }
        System.out.println(String.format("i:%d , HEREID:%d", -1, hero.getId()));
        return -1;
    }

    private void printCell(String str, Cell cell) {
        System.out.println(str + cell.getRow() + "-" + cell.getColumn());
    }
}
