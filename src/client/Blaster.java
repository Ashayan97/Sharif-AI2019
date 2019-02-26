package client;

import client.model.*;
import java.util.Vector;
import java.util.concurrent.Callable;

/**
 * created by Omidekz
 * */
public class Blaster {
    private static Cell[][] objectiveCells;
    private static final int GUARDIAN_DANGER_DISTANCE = 3;
    private static AI AI;
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
    //**************main methods*****************

    /**
     * #Blaster charecter do this method across the game
     */
    static void blasterMove(AI ai,World world, Hero blaster, History history) {
        setAI(ai);
        Cell blasterCurrentCell = blaster.getCurrentCell();
        Hero guardians[] = Utility.getGuardians(world);
        boolean safe;
        safe = isSafe(blasterCurrentCell, guardians);
        if(safe) {
            Blaster.BlasterNotSeeAnyOne(world, blaster, blasterCurrentCell, history);
        }else {
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
//                int upDis   = Utility.distance(up,fGCell);
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
                setCell(blaster,Utility.nextCell(world,blasterCurrentCell,dir));
            } else { // yeki guardian nazdikame
                Direction dir;
                Cell gCell = guardians[0].getCurrentCell();
                if (hasObjective) { // objzone tuye movecellsam has
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
                } else { // ojzone tu movecellsam nis
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
                setCell(blaster,Utility.nextCell(world,blasterCurrentCell,dir));
            }
        }
    }

    /**
     * hero'ii k behesh attack mizane ro set mikone tu AI v ag dodge bzne cell ro set mikone mikone
     * */
    static void blasterAttack(AI ai, World world, Hero myHero) {
        setAI(ai);
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
                if(saw. length == 0)
                    return;
                Utility.sortOnDistance(myHero.getCurrentCell(),saw);
                Utility.sortOnHP(saw);
                row = (blasterCell.getRow() + saw[0].getCurrentCell().getRow()) / 2;
                col = (blasterCell.getColumn() + saw[0].getCurrentCell().getColumn()) / 2;
                world.castAbility(myHero,AbilityName.BLASTER_DODGE,world.getMap().getCells()[row][col]);
                ai.dodgeTo(myHero,world.getMap().getCells()[row][col]);
                setCell(myHero,world.getMap().getCell(row,col));
            }else{
                Cell[] avai = Utility.availableCells(world.getMap(),Utility.BLASTER_DODGE_RANGE,blasterCell);
                Cell minObjzone = getIndexOfMinDisFromObjectiveZoneCell(blasterCell);
                if(minObjzone == null)
                    world.castAbility(myHero.getId(),AbilityName.BLASTER_DODGE,avai[0]);
                int min = Integer.MAX_VALUE;
                Cell shodDodge = avai[0];
                for (Cell anAvai : avai)
                    if (!anAvai.isWall() && Utility.distance(anAvai, minObjzone) < min)
                        shodDodge = anAvai;
                row = shodDodge.getRow();
                col = shodDodge.getColumn();
                world.castAbility(myHero.getId(),AbilityName.BLASTER_DODGE,row,col);
                ai.dodgeTo(myHero,world.getMap().getCell(row,col));
                setCell(myHero,world.getMap().getCell(row,col));
            }
            return;
        }
        Cell whereShouldIAttack = getBestForBlasterAttack(world,myHero,inMyAttckRange,abilityName);
        world.castAbility(myHero.getId(),abilityName,whereShouldIAttack);
        ai.setInAttack(myHero,world.getOppHero(whereShouldIAttack));
    }

    /**
     * hero haii k tu rangesh hastan ro migire v tasmim migire kodum cdell baraye attack bhtre v un ro barmigardune
     * */
    private static Cell getBestForBlasterAttack(World world, Hero mhero, Hero[] inMyAttckRange, AbilityName abilityName) {
        if(inMyAttckRange.length == 1)
            return inMyAttckRange[0].getCurrentCell();
        if(abilityName == AbilityName.BLASTER_BOMB
                && inMyAttckRange.length == 2){//2 ta hastan
            if(Utility.distance(inMyAttckRange[0].getCurrentCell(),inMyAttckRange[1].getCurrentCell())
                    <= 2*Utility_Attack.radius_of_blaster_bomb) { // mishe zad beyeneshun
                Cell[] effectiveCells = Utility.effectiveCells(world,inMyAttckRange[0].getCurrentCell(),inMyAttckRange[1].getCurrentCell());
                if(effectiveCells.length == 1)
                    return effectiveCells[0];
                return  Utility.distance(mhero.getCurrentCell(),effectiveCells[0])
                        <=
                        Utility.distance(mhero.getCurrentCell(),effectiveCells[1])?
                        effectiveCells[0]:
                        effectiveCells[1];
            }
        }else if(abilityName == AbilityName.BLASTER_BOMB
                && inMyAttckRange.length == 3){ // 3 ta hastan
            if(Utility.distance(inMyAttckRange[0].getCurrentCell(),inMyAttckRange[1].getCurrentCell())
                <= 2*Utility_Attack.radius_of_blaster_bomb &&
                Utility.distance(inMyAttckRange[0].getCurrentCell(),inMyAttckRange[2].getCurrentCell())
                <= 2*Utility_Attack.radius_of_blaster_bomb &&
                Utility.distance(inMyAttckRange[1].getCurrentCell(),inMyAttckRange[2].getCurrentCell())
                <= 2*Utility_Attack.radius_of_blaster_bomb)
                return vasateshun(world,inMyAttckRange[0].getCurrentCell(),inMyAttckRange[1].getCurrentCell(),inMyAttckRange[2].getCurrentCell());
        }else if(abilityName != AbilityName.BLASTER_ATTACK){ // in vase attack zadane
            if(inMyAttckRange.length == 2) {
                if (Utility.distance(inMyAttckRange[1].getCurrentCell(), inMyAttckRange[0].getCurrentCell())
                        <= 2 * Utility.BLASTER_ATTACK_RADIUS){
                    Cell[] effective =
                            Utility.effectiveCells(world,inMyAttckRange[0].getCurrentCell(),inMyAttckRange[1].getCurrentCell());
                    if(effective.length == 1)
                        return effective[0];
                    return Utility.distance(effective[0],mhero.getCurrentCell())<=Utility.distance(effective[1],mhero.getCurrentCell())?
                            effective[0]:effective[1];
                }else // dota hastan ama faselashun bishtare 2 hast
                    return inMyAttckRange[0].getCurrentHP()<inMyAttckRange[1].getCurrentHP()?
                            inMyAttckRange[0].getCurrentCell():inMyAttckRange[1].getCurrentCell();
            }else if(inMyAttckRange.length == 3){ // 3 nfr hastan
                if(Utility.distance(inMyAttckRange[0].getCurrentCell(),inMyAttckRange[1].getCurrentCell())
                    <= 2*Utility.BLASTER_ATTACK_RADIUS &&
                    Utility.distance(inMyAttckRange[0].getCurrentCell(),inMyAttckRange[2].getCurrentCell())
                    <= 2*Utility.BLASTER_ATTACK_RADIUS &&
                    Utility.distance(inMyAttckRange[1].getCurrentCell(),inMyAttckRange[2].getCurrentCell())
                    <= 2*Utility.BLASTER_ATTACK_RADIUS){ // fasele in seta 2 b 2 2 has, pas mishe zad vasateshun
                    return vasateshun(world,inMyAttckRange[0].getCurrentCell(),inMyAttckRange[1].getCurrentCell(),inMyAttckRange[2].getCurrentCell());
                }
            }
        }
        Utility.sortOnHP(inMyAttckRange);
        return inMyAttckRange[0].getCurrentCell();
    }

    /**
     * when blaster not see enemys
     * do this method
     */
    private static void BlasterNotSeeAnyOne(World world, Hero blaster, Cell blasterCurrentCell, History history) {
        if (blasterCurrentCell.isInObjectiveZone()) {
            // it is in objective zone
            Hero[] blasters = getBelasters(world, blaster.getId());
            /// own blaster
            boolean dupRange = false;
            Vector<Hero> dupRangeH = new Vector<>();
            for (Hero blaster1 : blasters) {
                if (Utility.distance(blasterCurrentCell, currentCell(blaster1)) <= 4) {
                    dupRange = true;
                    dupRangeH.add(blaster1);
                }
            }
            if (dupRange) {
                Cell[] moveCells = moveCell(world, blaster);
                for (int i = 0; i < moveCells.length; i++) {
                    if (moveCells[i] != null && moveCells[i].isInObjectiveZone()) {
                        boolean flag = true;
                        for (Hero ourBlaster : dupRangeH) {
                            if (Utility.distance(moveCells[i], currentCell(ourBlaster)) <= 4) {
                                flag = false;
                                break;
                            }
                        }
                        if(flag){
                            world.moveHero(blaster.getId(), i== 0 ? Direction.UP :
                                                            i== 1 ? Direction.DOWN :
                                                            i== 2 ? Direction.LEFT :
                                                            Direction.RIGHT);
                            setCell(blaster,moveCells[i]);
                            return;
                        }
                    }
                }
                if(blaster.getAbility(AbilityName.BLASTER_DODGE).isReady()){
                    Cell[] avi = Utility.availableCells(world.getMap(),Utility.BLASTER_DODGE_RANGE,blasterCurrentCell);
                    Cell badCell=avi[0];
                    for (int i = 0; i < avi.length; i++) {
                        boolean flag = true;
                        boolean badFlag = true;
                        for (int j = 0; j < dupRangeH.size(); j++) {
                            if(avi[i].isWall()||
                                    !avi[i].isInObjectiveZone()||
                                    Utility.distance(avi[i],dupRangeH.get(j).getCurrentCell())<=4){
                                flag = false;
                                break;
                            }
                            if(Utility.distance(avi[i],dupRangeH.get(j).getCurrentCell())<=4)
                                badFlag = false;
                        }
                        if(flag){
                            world.castAbility(blaster,AbilityName.BLASTER_DODGE,avi[i]);
                            setCell(blaster,avi[i]);
                            return;
                        }else if(badFlag)
                            badCell = avi[i];
                    }
                    if(badCell != null){
                        world.castAbility(blaster,AbilityName.BLASTER_DODGE,badCell);
                        setCell(blaster,badCell);
                    }
                    return;
                }
                for (int i = 0; i <moveCells.length; i++) {
                    boolean flag = true;
                    for (int j = 0; moveCells[i] != null && moveCells[i].isInObjectiveZone() && j < dupRangeH.size(); j++) {
                        if (!(Utility.distance(moveCells[i], currentCell(dupRangeH.get(j))) >
                                Utility.distance(blasterCurrentCell, currentCell(dupRangeH.get(j))))) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        Utility.move(world, blaster.getId(), blasterCurrentCell, moveCells[i]);
                        setCell(blaster,moveCells[i]);
                    }
                }
            }
        } else {
            Cell minDisCellFromObjzone = // if blaster was in objzone this method return -1
                    getIndexOfMinDisFromObjectiveZoneCell(blasterCurrentCell);
            move(world, blaster.getId(), blasterCurrentCell, minDisCellFromObjzone, history);
            setCell(blaster,minDisCellFromObjzone);
        }
    }

    //*************helper********************
    private static void setCell(Hero blaster, Cell moveCell) {
        AI.addCell(blaster,moveCell);
    }
    private static void setAI(AI ai) {
        AI = ai;
    }
    private static boolean checkAI(){
        return !(AI==null);
    }

    private static boolean isSafe(Cell blasterCurrentCell, Hero[] guardians) {
        for (Hero guardian : guardians)
            if (Utility.distance(blasterCurrentCell, guardian.getCurrentCell()) <= GUARDIAN_DANGER_DISTANCE) {
                return false;
            }
        return true;
    }

    /**
     * this method an cell and return the nearest objective cell
     * if blaster was in objzone this method return -1
     */
    private static Cell getIndexOfMinDisFromObjectiveZoneCell(Cell blasterCurrentCell) {
        int rowIndex = 0;
        int colIndex=0;
        int minDis = Integer.MAX_VALUE;
        for (int i = 0 ; i < objectiveCells.length; i++) {
            for (int j = 0; j < objectiveCells.length; j++) {
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
     * 3 cell migire v beyneshun ro return mikone msln:
     * (1,1) (2,2) (1 3) => return mikone (1,2)
     * */
    private static Cell vasateshun(World world,Cell start, Cell mid, Cell end) {
        Cell left = Utility.getLEFT(Utility.getLEFT(start,mid),end);
        Cell right = Utility.getRIGHT(Utility.getRIGHT(start,mid),end);
        Cell up = Utility.getUP(Utility.getUP(start,mid),end);
        Cell down = Utility.getDOWN(Utility.getDOWN(start,mid),end);
        return world.getMap().getCell((up.getRow() + down.getRow())/2,(left.getColumn()+right.getColumn())/2);
    }

    /**
     * cell haii k mitune bere ro return mikone
     * 0=up, 1=down, left=2, right=3
     * */
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

    /**
     * blaster haye khodi ro return mikone
     * */
    private static Hero[] getBelasters(World world, int excpIt) {
        Hero[] mh = world.getMyHeroes();
        Vector<Hero> hs = new Vector<>();
        for (Hero aMh : mh) {
            if (aMh.getName().equals(HeroName.BLASTER) && aMh.getId() != excpIt)
                hs.add(aMh);
        }
        return hs.toArray(new Hero[0]);
    }

    /**
     * check mikone bebine hichkodum azin ha objzone hastan ya na
     * */
    private static boolean hasObjective(Cell... cells) {
        for (Cell cell : cells) {
            if (cell.isInObjectiveZone())
                return true;
        }
        return false;
    }

    private static Cell currentCell(Hero h){
        return AI.getNextCell(h) == null?h.getCurrentCell():AI.getNextCell(h);
    }

    private static void move(World world,int HEROID, Cell src,Cell dst, History history,boolean saveCell){
        Utility.move(world,HEROID,src,dst);
        if(saveCell)
            history.addLastStep(src);
    }
    private static void move(World world,int HERODID,Cell src,Cell dest,History history){
        move(world,HERODID,src,dest,history,true);
    }

    //    private static boolean isObjzoneEmpty(World world) {
//        Hero[] opp = Utility.getSawHero(world);
//        if(opp.length == 0)
//            return true;
//        for (int i = 0; i < opp.length; i++) {
//            if(opp[i].getCurrentCell().isInObjectiveZone())
//                return false;
//        }
//        return true;
//    }

}