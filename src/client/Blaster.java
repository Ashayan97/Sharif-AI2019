package client;

import client.model.*;

import java.util.Random;

public class Blaster {
    private static Cell[][] objectiveCells;
    static void set(World world){
        Map map = world.getMap();
        int row ,col;
        row = col = (int)Math.sqrt(map.getObjectiveZone().length);
        objectiveCells = new Cell[row][col];
        for (int i = 0; i < row; i++)
            for (int j = 0; j < col; j++)
                objectiveCells[i][j] = map.getObjectiveZone()[i*col + j];
    }
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
        Blaster.BlasterNotSeeAnyOne(world,blaster,blasterCurrentCell,history);
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
                Cell[] cells = Utility.availableCells(world.getMap(), Utility.DODGE_RANGE, blasterCurrentCell); // tamame khune ha be radius'e DODGE_RANGE  = 4
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

    static void blasterAttack(World world, Hero myHero) {
        AbilityName abilityName = myHero.getAbility(AbilityName.BLASTER_BOMB).isReady()?
                AbilityName.BLASTER_BOMB:
                AbilityName.BLASTER_ATTACK;
        Hero[] inMyAttckRange = Utility.getInAttackRange(world,myHero,abilityName);
        if(inMyAttckRange.length == 0){
            // todo no is no in his range
            // todo so is better to go to enemys
            Random random = new Random();
            int row = random.nextInt(objectiveCells.length);
            int col = random.nextInt(objectiveCells.length);
            world.castAbility(myHero,AbilityName.BLASTER_DODGE,objectiveCells[row][col]);
            return;
        }
        Cell whereShouldIAttcka = getBestForBlasterAttack(world,myHero,inMyAttckRange,abilityName); // todo :(
        world.castAbility(myHero.getId(),abilityName,whereShouldIAttcka);
    }


    /**
     * when blaster not see enemys
     * do this method
     */
    static void BlasterNotSeeAnyOne(World world, Hero blaster, Cell blasterCurrentCell, History history) {
        Cell minDisCellFromObjzone = // if blaster was in objzone this method return -1
                getIndexOfMinDisFromObjectiveZoneCell(blasterCurrentCell);
        if (minDisCellFromObjzone == null) {
            return; // it is in objective zone
        }
        move(world, blaster.getId(),blasterCurrentCell,minDisCellFromObjzone,history);
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
