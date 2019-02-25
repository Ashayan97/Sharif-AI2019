package client;

import client.model.*;

import java.util.Random;
import java.util.Vector;

public class Blaster {
    private static Cell[][] objectiveCells;
    private static final int GUARDIAN_DANGER_DISTANCE = 3;
    //*******************************
    static void set(World world){
        Map map = world.getMap();
        int row ,col;
        row = col = (int)Math.sqrt(map.getObjectiveZone().length);
        objectiveCells = new Cell[row][col];
        for (int i = 0; i < row; i++)
            for (int j = 0; j < col; j++)
                objectiveCells[i][j] = map.getObjectiveZone()[i*col + j];
    }
    //*******************************
    /**
     * this method an cell and return the nearest objective cell
     * if blaster was in objzone this method return -1
     */
    private static Cell getIndexOfMinDisFromObjectiveZoneCell(Cell blasterCurrentCell) {
        int rowIndex = 0;
        int colIndex=0;
        int minDis = Integer.MAX_VALUE;
        for (int i = 1; i < objectiveCells.length; i++) {
            for (int j = 0; j < objectiveCells[i].length; j++) {
                int tmpDis = Utility.distance(blasterCurrentCell, objectiveCells[i][j]);
                if (tmpDis < minDis) {
                    rowIndex = i;
                    colIndex = j;
                    minDis = tmpDis;
                }
            }
        }
        if (minDis == 0)
            return null;
        return objectiveCells[rowIndex][colIndex];
    }

    /**
     * #Blaster charecter do this method across the game
     */
    static void blasterMove(World world, Hero blaster, History history) {
        Cell blasterCurrentCell = blaster.getCurrentCell();
        Hero guardians[] = Utility.getGuardians(world);
        boolean safe = true;
        for (Hero guardian : guardians)
            if (Utility.distance(blasterCurrentCell, guardian.getCurrentCell()) <= GUARDIAN_DANGER_DISTANCE) {
                safe = false;
                break;
            }

        if(safe)
            Blaster.BlasterNotSeeAnyOne(world,blaster,blasterCurrentCell,history);
        else {
            Cell[] moveCell = moveCell(world,blaster);
            Cell    down = moveCell[1],
                    up = moveCell[0],
                    left = moveCell[2],
                    right = moveCell[3];
            boolean hasObjective = hasObjective(down, up, left, right);
            if (guardians.length > 1) {
                Utility.sortOnDistance(blasterCurrentCell, guardians);
                Cell fGCell = guardians[0].getCurrentCell();
                Cell sGCell = guardians[1].getCurrentCell();
                Direction dir;
                int leftDis = Utility.distance(left,fGCell);
                int rightDis= Utility.distance(right,fGCell);
                int upDis   = Utility.distance(up,fGCell);
                int downDis = Utility.distance(down,fGCell);

                if(leftDis >= GUARDIAN_DANGER_DISTANCE
                        && Utility.distance(left,sGCell)>=GUARDIAN_DANGER_DISTANCE)
                    dir = Direction.LEFT;
                else if(rightDis >= GUARDIAN_DANGER_DISTANCE
                        && Utility.distance(right,sGCell)>=GUARDIAN_DANGER_DISTANCE)
                    dir = Direction.RIGHT;
                else if(downDis >= GUARDIAN_DANGER_DISTANCE
                        && Utility.distance(down,sGCell)>=GUARDIAN_DANGER_DISTANCE)
                    dir = Direction.DOWN;
                else
                    dir = Direction.UP;
                world.moveHero(blaster.getId(),dir);
            } else {
                Direction dir;
                Cell gCell = guardians[0].getCurrentCell();
                if (hasObjective) {
                    if (down.isInObjectiveZone()
                            && Utility.distance(down, gCell) >= GUARDIAN_DANGER_DISTANCE)
                        dir = Direction.DOWN;
                    else if (up.isInObjectiveZone()
                            && Utility.distance(up, gCell) >= GUARDIAN_DANGER_DISTANCE)
                        dir = Direction.UP;
                    else if (left.isInObjectiveZone()
                            && Utility.distance(left, gCell) >= GUARDIAN_DANGER_DISTANCE)
                        dir = Direction.LEFT;
                    else
                        dir = Direction.RIGHT;
                } else {
                    if (Utility.distance(down, guardians[0].getCurrentCell()) >= GUARDIAN_DANGER_DISTANCE)
                        dir = Direction.DOWN;
                    else if (Utility.distance(up, guardians[0].getCurrentCell()) >= GUARDIAN_DANGER_DISTANCE)
                        dir = Direction.UP;
                    else if (Utility.distance(left, guardians[0].getCurrentCell()) >= GUARDIAN_DANGER_DISTANCE)
                        dir = Direction.LEFT;
                    else
                        dir = Direction.RIGHT;
                }
                world.moveHero(blaster.getId(), dir);
            }
        }
    }

    private static Cell[] moveCell(World world, Hero blaster) {
        Map map=world.getMap();
        int row = blaster.getCurrentCell().getRow();
        int col = blaster.getCurrentCell().getColumn();
        Cell[] cs = new Cell[4];
        if(map.isInMap(row-1,col))
            cs[0] = (map.getCell(row-1,col)); // add up
        if(map.isInMap(row+1,col))
            cs[1]=(map.getCell(row+1,col)); // add down
        if(map.isInMap(row,col-1)) // add left
            cs[2]=(map.getCell(row,col-1));
        if(map.isInMap(row,col+1)) // add right
            cs[3]=(map.getCell(row,col+1));
        return cs;
    }

    private static boolean hasObjective(Cell down, Cell up, Cell left, Cell right) {
        return down.isInObjectiveZone()||
                up.isInObjectiveZone()||
                left.isInObjectiveZone()||
                right.isInObjectiveZone();
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
            Cell cellIndexMinDisToObjective=getIndexOfMinDisFromObjectiveZoneCell(blasterCurrentCell);
            if(cellIndexMinDisToObjective == null){
                if(!Utility.nextCell(world,blasterCurrentCell,enemyCurrentCell).isInObjectiveZone())
                    return;
                move(world,mHeroID,blasterCurrentCell,enemyCurrentCell,history);
                return;
            }
            Cell nextToObjective = Utility.nextCell(world,blasterCurrentCell,cellIndexMinDisToObjective);
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
                Cell[] cells = Utility.availableCells(world.getMap(), Utility.BLASTER_DODGE_RANGE, blasterCurrentCell); // tamame khune ha be radius'e BLASTER_DODGE_RANGE  = 4
                int maxDistance = Utility.distance(enemyCurrentCell,cells[0]);
                Cell maxDistanceCell = cells[0];
                for(Cell i:cells){
                    if(!world.isInVision(i,enemyCurrentCell)){
                        // ag y khune az didesh khareje mirim unja
                        world.castAbility(mHeroID, AbilityName.BLASTER_DODGE,i);
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


    static Cell getBestForBlasterAttack(World world,Hero mhero,Hero[] inMyAttckRange,AbilityName abilityName) {
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

    /**
     * hero'ii k behesh attack mizane ro set mikone tu AI v ag dodge bzne cell ro set mikone mikone
     * */
    static void blasterAttack(AI ai, World world, Hero myHero) {
        AbilityName abilityName = myHero.getAbility(AbilityName.BLASTER_BOMB).isReady()?
                AbilityName.BLASTER_BOMB:
                AbilityName.BLASTER_ATTACK;
        Hero[] inMyAttckRange = Utility.getInAttackRange(world,myHero,abilityName);
        Cell blasterCell = myHero.getCurrentCell();
        if(inMyAttckRange.length == 0){
            int row;
            int col;
            if(myHero.getCurrentCell().isInObjectiveZone()){
                Hero[] saw=Utility.getSawHero(world);
                Utility.sortOnDistance(myHero.getCurrentCell(),saw);
                Utility.sortOnHP(saw);
                row = (blasterCell.getRow() + saw[0].getCurrentCell().getRow()) / 2;
                col = (blasterCell.getColumn() + saw[0].getCurrentCell().getColumn()) / 2;
            }else{
                Cell[] avai = Utility.availableCells(world.getMap(),Utility.BLASTER_DODGE_RANGE,blasterCell);
                Cell minObjzone = getIndexOfMinDisFromObjectiveZoneCell(blasterCell);
                int min = Integer.MAX_VALUE;
                Cell shodDodge = avai[0];
                for (Cell anAvai : avai)
                    if (Utility.distance(anAvai, minObjzone) < min)
                        shodDodge = anAvai;
                row = shodDodge.getRow();
                col = shodDodge.getColumn();
            }
            world.castAbility(myHero,AbilityName.BLASTER_DODGE,objectiveCells[row][col]);
            ai.dodgeTo(myHero,objectiveCells[row][col]);
            return;
        }
        Cell whereShouldIAttack = getBestForBlasterAttack(world,myHero,inMyAttckRange,abilityName);
        world.castAbility(myHero.getId(),abilityName,whereShouldIAttack);
        ai.setInAttack(myHero,world.getOppHero(whereShouldIAttack));
    }


    /**
     * when blaster not see enemys
     * do this method
     */
    static void BlasterNotSeeAnyOne(World world, Hero blaster, Cell blasterCurrentCell, History history) {
        Cell minDisCellFromObjzone = // if blaster was in objzone this method return -1
                getIndexOfMinDisFromObjectiveZoneCell(blasterCurrentCell);
        if (minDisCellFromObjzone == null) {
            // it is in objective zone
            Hero[] blasters=getBelasters(world,blaster.getId());
            boolean dupRange = false;
            Vector<Hero> dupRangeH = new Vector<>();
            for (Hero blaster1 : blasters) {
                if (Utility.distance(blasterCurrentCell, blaster1.getCurrentCell()) <= 4) {
                    dupRange = true;
                    dupRangeH.add(blaster1);
                }
            }
            if(dupRange){
                Cell[] moveCells = moveCell(world,blaster);
                for (int i = 0; i < moveCells.length; i++) {
                    if(moveCells[i]!=null){
                        boolean flag = true;
                        for (Hero aDupRangeH : dupRangeH) {
                            if (Utility.distance(moveCells[i], aDupRangeH.getCurrentCell()) <= 4) {
                                flag = false;
                                break;
                            }
                        }
                        if(flag){
                            world.moveHero(blaster.getId(), i==0?Direction.UP:
                                                                i==1?Direction.DOWN:
                                                                    i==2?Direction.LEFT:
                                                                            Direction.RIGHT);
                        }
                    }
                }
            }
            return;
        }
        move(world, blaster.getId(),blasterCurrentCell,minDisCellFromObjzone,history);
    }

    private static Hero[] getBelasters(World world, int excpIt) {
        Hero[] mh = world.getMyHeroes();
        Vector<Hero> hs = new Vector<>();
        for (Hero aMh : mh) {
            if (aMh.getName().equals(HeroName.BLASTER) && aMh.getId() != excpIt)
                hs.add(aMh);
        }
        return hs.toArray(new Hero[0]);
    }

    private static void move(World world,int HEROID, Cell src,Cell dst, History history,boolean saveCell){
        Utility.move(world,HEROID,src,dst);
        if(saveCell)
            history.addLastStep(src);
    }
    private static void move(World world,int HERODID,Cell src,Cell dest,History history){
        move(world,HERODID,src,dest,history,true);
    }

}
