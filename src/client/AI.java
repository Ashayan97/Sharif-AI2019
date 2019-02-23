package client;

import client.model.*;
import javafx.beans.value.WritableObjectValue;
import javafx.scene.effect.BlurType;

import java.time.chrono.MinguoChronology;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.Vector;

public class AI {

    private int PICK_PHASE_COUNTER = 0;
    private ArrayList<Hero> herosInVision;
    private Cell[] objectiveCells;
    private History[] histories;
    private Vector<Cell> wallsCell;
    private Vector<Hero> atttackTo;
    private int whoAttackID;
    private boolean inAttack=false;

    //****************************************
    void preProcess(World world) {
        objectiveCells = world.getMap().getObjectiveZone();
        initWallCell(world);
    }

    void pickTurn(World world) {
        pickHeroInPhase(world);
    }

    void moveTurn(World world) {
        Utility.printMap(world);
        init(world);
        BlasterDO(world, world.getMyHeroes()[0]);
        BlasterDO(world, world.getMyHeroes()[1]);
        BlasterDO(world, world.getMyHeroes()[2]);
        BlasterDO(world, world.getMyHeroes()[3]);
    }

    void actionTurn(World world) {
        init(world);
        blasterAttack(world,world.getMyHeroes()[0]);
        blasterAttack(world,world.getMyHeroes()[1]);
        blasterAttack(world,world.getMyHeroes()[2]);
        blasterAttack(world,world.getMyHeroes()[3]);
    }

    private void blasterAttack(World world, Hero myHero) {
        AbilityName abilityName = myHero.getAbility(AbilityName.BLASTER_BOMB).isReady()?
                AbilityName.BLASTER_BOMB:
                AbilityName.BLASTER_ATTACK;
        Hero[] inMyAttckRange = getInAttackRange(world,myHero,abilityName);
        if(inMyAttckRange.length == 0){
            world.castAbility(myHero,AbilityName.BLASTER_DODGE,objectiveCells[new Random().nextInt(objectiveCells.length)]);
            return;
        }
        Cell whereShouldIAttcka = getBestForBlasterAttack(world,myHero,inMyAttckRange,abilityName); // todo :(
        world.castAbility(myHero.getId(),abilityName,whereShouldIAttcka);
    }

    private Cell getBestForBlasterAttack(World world,Hero mhero,Hero[] inMyAttckRange,AbilityName abilityName) {
        if(inMyAttckRange.length == 1)
            return inMyAttckRange[0].getCurrentCell();
        if(abilityName == AbilityName.BLASTER_BOMB
        && inMyAttckRange.length == 2){
            if(Utility.distance(inMyAttckRange[0].getCurrentCell(),inMyAttckRange[1].getCurrentCell())
                    <=
                    2*Utility_Attack.radius_of_blaster_bomb) {
                Cell[] effectiveCells = Utility.effectiveCells(world,inMyAttckRange[0].getCurrentCell(),inMyAttckRange[1].getCurrentCell());
                if(effectiveCells.length == 1)
                    return effectiveCells[0];
                return Utility.distance(mhero.getCurrentCell(),effectiveCells[0])
                        <=
                        Utility.distance(mhero.getCurrentCell(),effectiveCells[1])?
                        effectiveCells[0]:
                        effectiveCells[1];
            }
        }
        Utility.sortOnHP(inMyAttckRange);
        return inMyAttckRange[0].getCurrentCell();
    }


    private Hero[] getInAttackRange(World world,Hero hero,AbilityName... abilityNames) {
        int radius = hero.getAbility(abilityNames[0]).getRange();
        Cell[] available = Utility.availableCells(world.getMap(),radius,hero.getCurrentCell());
        Vector<Hero> heroes = new Vector<>();
        Cell heroCell = hero.getCurrentCell();
        for (Cell anAvailable : available) {
            if (!hero.getAbility(abilityNames[0]).isLobbing() &&
                    world.isInVision(heroCell, anAvailable) &&
                    world.getOppHero(anAvailable) != null)
                heroes.add(world.getOppHero(anAvailable));
            else if (hero.getAbility(abilityNames[0]).isLobbing() &&
                    world.getOppHero(anAvailable) != null)
                heroes.add(world.getOppHero(anAvailable));
        }
        return heroes.toArray(new Hero[]{});
    }

    //****************************************

    /**
     * this method initialize our need across the phase or turn
     */
    private void init(World world) {
        initHistorys(world.getMyHeroes());
        initHeroInVision(world);
    }
//    ina tuye init budan
//        if (objectiveCells == null) {
//            System.out.println("-->ObjectiveCells not assign in PreProccess and init in moveTurn method");
//            objectiveCells = world.getMap().getObjectiveZone();
//        }
//        initWallCell(world);
//

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
    private void initHistorys(Hero[] myHero) {
        if (histories == null) {
            histories = new History[4];
            for (int i = 0; i < 4; i++)
                histories[i] = new History(myHero[i].getId());
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
     * #Blaster charecter do this method across the game
     */
    private void BlasterDO(World world, Hero blaster) {
        Cell blasterCurrentCell = blaster.getCurrentCell();
        int historyIndex = indexOfHeroInHistory(blaster); // if heroID not be in history this method return -1
        if (historyIndex == -1) {
            System.out.println("History Index was -1");
            return;
        }
        History history = histories[historyIndex];

        BlasterNotSeeAnyOne(world,blaster,blasterCurrentCell,history);

//        if(inAttack){
//            int distance = Utility.distance(blasterCurrentCell,)
//            if() {
//                int remMovePhase = world.getMovePhaseNum();
//            }
//        }
//
//        if (herosInVision == null ||
//                herosInVision.size() == 0 ||
//                history.getSawHeroes().size() == 0) {
//            BlasterNotSeeAnyOne(world, blaster, blasterCurrentCell, history);
//        } else if (herosInVision.size() == 1) {
//            blasterSawAMotherFucker(world, blasterCurrentCell, history);
//        }else {
//            Hero[] heros = atttackTo.toArray(new Hero[]{});
//            Utility.sortOnHP(heros);
//            Utility.sortOnDistance(blasterCurrentCell, heros);
//            int distance = Utility.distance(blasterCurrentCell,heros[0].getCurrentCell());
//            if(distance<=Utility_Attack.range_of_blaster_attack||
//                    (blaster.getAbilities()[0].isReady() &&
//                            distance<=Utility_Attack.range_of_blaster_bomb+Utility_Attack.radius_of_blaster_bomb)) {
//                setGpAttack(blaster, history.getSawHeroes());
//            } else
//                BlasterNotSeeAnyOne(world,blaster,blasterCurrentCell,history);
//        }
    }

    /**
     * when blaster not see enemys
     * do this method
     */
    private void BlasterNotSeeAnyOne(World world, Hero blaster, Cell blasterCurrentCell, History history) {
        int indexOfMinDisFromObjectiveZoneCell = // if blaster was in objzone this method return -1
                getIndexOfMinDisFromObjectiveZoneCell(blasterCurrentCell);
        if (indexOfMinDisFromObjectiveZoneCell == -1) {
            return;
        }
        move(world, blaster.getId(),blasterCurrentCell,objectiveCells[indexOfMinDisFromObjectiveZoneCell],history);
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

        //ag tuye objzone bud va distance'sh ba enemy <= range attackesh bud lazem nist kari bokone
        if(Utility.distance(blasterCurrentCell,enemyCurrentCell) <= Utility_Attack.range_of_blaster_attack-2
                && mHero.getCurrentCell().isInObjectiveZone())
            return;
        ATTACK_STATE state = Utility.canAttack(mHero, enemy);
        if (state == ATTACK_STATE.DORADOR
                || state == ATTACK_STATE.TANBETAN
                || state == ATTACK_STATE.CANTATTACK) {
            // check mikone k ag mishe bere be samt objective ama distance'esh ba enemy taqiir nakone ya kamtar beshe
            // ag halat'e bala rokh nadad mire be samte enemy
            int cellIndexMinDisToObjective=getIndexOfMinDisFromObjectiveZoneCell(blasterCurrentCell);
            if(cellIndexMinDisToObjective == -1){
                if(!Utility.nextCell(world,blasterCurrentCell,enemyCurrentCell).isInObjectiveZone())
                    return;
                move(world,mHeroID,blasterCurrentCell,enemyCurrentCell,history);
                return;
            }
            Cell nextToObjective = Utility.nextCell(world,blasterCurrentCell,objectiveCells[cellIndexMinDisToObjective]);
            int disToObjzone = Utility.distance(nextToObjective,enemyCurrentCell);
            int disToEnemy = Utility.distance(blasterCurrentCell,enemyCurrentCell);
            if(disToObjzone <= disToEnemy)
                move(world,mHeroID,blasterCurrentCell,nextToObjective,history);
            else
                move(world, mHeroID, blasterCurrentCell, enemyCurrentCell,history);
        } else if (state == ATTACK_STATE.SCAPE) {
            Cell lastCell = history.getLastStep();
            if (!world.isInVision(lastCell, enemyCurrentCell)) {
                // ag last step az did enemy kharej bashe mirim unja
                move(world, mHeroID, blasterCurrentCell, lastCell,history);
            }else {
                Cell[] cells = Utility.availableCells(world.getMap(), Utility.DODGE_RANGE, blasterCurrentCell); // tamame khune ha be radius'e DODGE_RANGE  = 4
                int maxDistance = Utility.distance(enemyCurrentCell,cells[0]);
                Cell maxDistanceCell = cells[0];
                for(Cell i:cells){
                    if(!world.isInVision(i,enemyCurrentCell)){
                        // ag y khune az didesh khareje mirim unja
                        world.castAbility(mHeroID,AbilityName.BLASTER_DODGE,i);
                        history.addLastStep(blasterCurrentCell);
                        return;
                    }
                    // dar qeyre in surat say mikonim khuneii ro peyda konim k faselash max bashe
                    int tmp = Utility.distance(i,enemyCurrentCell);
                    if(tmp>maxDistance){
                        maxDistance = tmp;
                        maxDistanceCell = i;
                    }
                }
                world.castAbility(mHeroID,AbilityName.BLASTER_DODGE,maxDistanceCell);
                history.addLastStep(blasterCurrentCell);
            }
        }
    }

    /**
     * this method an cell and return the nearest objective cell
     * if blaster was in objzone this method return -1
     */
    private int getIndexOfMinDisFromObjectiveZoneCell(Cell blasterCurrentCell) {
        int indexOfMinDisFromObjectiveZoneCell = 0;
        int minDis = Utility.distance(blasterCurrentCell, objectiveCells[0]);
        for (int i = 1; i < objectiveCells.length; i++) {
            int tmpDis = Utility.distance(blasterCurrentCell, objectiveCells[i]);
            if (tmpDis < minDis) {
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
     * if heroID not be in history this method return -1
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

    private void move(World world,int HEROID, Cell src,Cell dst, History history,boolean saveCell,boolean forceMove){
        if(forceMove){
            Cell nextCell = Utility.nextCell(world,src,dst);
            if(world.getMyHero(nextCell) != null){
                Cell firstCell=null;
                Cell secondCell=null;
                if(nextCell.getColumn() == src.getColumn()){
                    // tuye ye sotun hastan v niaz dare az chap ya raste un bere

                    if(world.getMap().isInMap(nextCell.getRow(),nextCell.getColumn()+1))
                        secondCell = world.getMap().getCell(nextCell.getRow(),nextCell.getColumn()+1);

                    if(world.getMap().isInMap(nextCell.getRow(),nextCell.getColumn()-1))
                        firstCell = world.getMap().getCell(nextCell.getRow(),nextCell.getColumn()-1);
                }else{
                    // tuye ye radif hastan v niaz dre az bala ya paein bere
                    if(world.getMap().isInMap(nextCell.getRow()-1,nextCell.getColumn()))
                        firstCell = world.getMap().getCell(nextCell.getRow()-1,nextCell.getColumn());
                    if(world.getMap().isInMap(nextCell.getRow()+1,nextCell.getColumn()))
                        secondCell = world.getMap().getCell(nextCell.getRow()+1,nextCell.getColumn());
                }
                dst = Utility.distance(secondCell,dst)<=Utility.distance(firstCell,dst)?secondCell:firstCell;
            }
        }
        Utility.move(world,HEROID,src,dst);
        if(saveCell)
            history.addLastStep(src);
    }
    private void move(World world,int HERODID,Cell src,Cell dest,History history){
        move(world,HERODID,src,dest,history,true,false);
    }

    private void setGpAttack(Hero whoAttack, Collection<Hero> toAttack){
        this.atttackTo.addAll(toAttack);
        this.whoAttackID = whoAttack.getId();
        inAttack = true;
    }
    private void clearGpAttack(){
        whoAttackID = -1;
        atttackTo = null;
        inAttack = false;
    }

    private void printCell(String str, Cell cell) {
        System.out.println(str + cell.getRow() + "-" + cell.getColumn());
    }
}
