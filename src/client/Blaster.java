package client;

import client.model.*;

import java.util.Arrays;
import java.util.Random;
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
        if(blaster.getCurrentHP() == 0)
            return;
        Cell blasterCurrentCell = blaster.getCurrentCell();
        Hero guardians[] = Utility.getGuardians(world,blaster);
        boolean safe=guardians.length==0;
        if(safe) {
            safeMove(world, blaster, blasterCurrentCell, history);
        }else {
            safeFromGuardians(world, blaster, blasterCurrentCell, guardians);
        }
    }

    /**
     * hero'ii k behesh attack mizane ro set mikone tu AI v ag dodge bzne cell ro set mikone mikone
     * */
    static void blasterAttack(AI ai, World world, Hero blaster) {
        setAI(ai);
        if(blaster.getCurrentHP() == 0)
            return;
        AbilityName abilityName = blaster.getAbility(AbilityName.BLASTER_BOMB).isReady() && world.getAP() >= 25?
                AbilityName.BLASTER_BOMB :
                AbilityName.BLASTER_ATTACK;
        Hero[] inMyAttckRange = Utility.getInAttackRange(world, blaster, abilityName);
        Cell blasterCell = blaster.getCurrentCell();
        if (inMyAttckRange.length == 0) {
            int row;
            int col;
            if (blaster.getCurrentCell().isInObjectiveZone()) {
                dodgeToNearestEnemy(ai, world, blaster, blasterCell);
            } else if (blaster.getAbility(AbilityName.BLASTER_DODGE).isReady()) {
                // hichki tu range attack nis va kharej objzone hastim
                Cell[] avai = Utility.availableCells(world.getMap(), Utility.BLASTER_DODGE_RANGE, blasterCell);
                Cell minObjzone = objzoneCellMinDis(blasterCell,true);
                int min = Integer.MAX_VALUE;
                Cell shodDodge = avai[0];
                for (Cell anAvai : avai)
                    if (!anAvai.isWall() && Utility.distance(anAvai, minObjzone) < min) {
                        shodDodge = anAvai;
                        min = Utility.distance(anAvai, minObjzone);
                    }
                row = shodDodge.getRow();
                col = shodDodge.getColumn();
                world.castAbility(blaster.getId(), AbilityName.BLASTER_DODGE, row, col);
                ai.dodgeTo(blaster, world.getMap().getCell(row, col));
                setCell(blaster, world.getMap().getCell(row, col));
            }
        } else {
            Cell whereShouldIAttack = getBestForBlasterAttack(world, blaster, inMyAttckRange, abilityName);
            world.castAbility(blaster.getId(), abilityName, whereShouldIAttack);
            ai.setInAttack(blaster, world.getOppHero(whereShouldIAttack));
        }
    }

    private static void safeFromGuardians(World world, Hero blaster, Cell blasterCurrentCell, Hero[] guardians) {
        Cell[] moveCell = moveCell(world,blaster);
        Cell    down = moveCell[1],
                up = moveCell[0],
                left = moveCell[2],
                right = moveCell[3];
        boolean hasObjective = hasObjective(down, up, left, right);
        if (guardians.length > 1) {
            float avgDis = avgDis(blasterCurrentCell,guardians);
            Cell okCell =null, outofobjOkCell=null,objBadCell=null,badCell=null;
            for (int i = 0; i < moveCell.length; i++) {
                if(moveCell[i] == null || moveCell[i].isWall())
                    continue;// i++ if movecell[i] be null or wall
                boolean okFlag,outofFlag,objbadcellFlag,badcellFlag;
                okFlag = outofFlag = objbadcellFlag = badcellFlag = true;
                for (int j = 0; j < guardians.length; j++) {
                    Cell GCELL = guardians[j].getCurrentCell();
                    if((moveCell[i].isInObjectiveZone() && //be objective and dis > dangeres range
                            Utility.distance(moveCell[i],GCELL)>=GUARDIAN_DANGER_DISTANCE)){
                        okFlag = false;
                    }else if((Utility.distance(moveCell[i],GCELL)>=GUARDIAN_DANGER_DISTANCE)){ // dis > dangeres rrange
                        outofFlag = false;
                    }else if((moveCell[i].isInObjectiveZone() &&  // objective and dis > nowDis
                            Utility.distance(moveCell[i],GCELL) > avgDis )){
                        objbadcellFlag = false;
                    }else if(Utility.distance(moveCell[i],GCELL) > avgDis){ // dis > nowDis
                        badcellFlag = false;
                    }
                }
                if(okFlag){
                    okCell = moveCell[i];
                }else if(outofFlag){
                    outofobjOkCell = moveCell[i];
                }else if(objbadcellFlag){
                    objBadCell = moveCell[i];
                }else if(badcellFlag){
                    badCell = moveCell[i];
                }
            }
            Direction dir[];
            if(okCell != null){
                dir = world.getPathMoveDirections(blasterCurrentCell,okCell);
            }else if(outofobjOkCell != null){
                dir  = world.getPathMoveDirections(blasterCurrentCell,outofobjOkCell);
            }else if(objBadCell != null){
                dir  = world.getPathMoveDirections(blasterCurrentCell,objBadCell);
            }else if(badCell != null){
                dir  = world.getPathMoveDirections(blasterCurrentCell,badCell);
            }else{
                dir = world.getPathMoveDirections(blasterCurrentCell,moveCell[new Random().nextInt(4)]);
            }
            if(dir.length == 0)
                return;
            world.moveHero(blaster.getId(),dir[0]);
            setCell(blaster,Utility.nextCell(world,blasterCurrentCell,dir[0]));
        } else { // yeki guardian nazdikame
            Cell gCell = guardians[0].getCurrentCell();
            Cell okCell = null, outofobjOkCell= null,objBadCell= null,badCell= null;
            int disFromMe = Utility.distance(blasterCurrentCell,gCell);
            for (int i = 0; i < moveCell.length; i++) {
                if(moveCell[i] == null || moveCell[i].isWall())
                    continue;// i++ if movecell[i] be null or wall
                if(moveCell[i].isInObjectiveZone() && //be objective and dis > dangeres range
                    Utility.distance(moveCell[i],gCell)>=GUARDIAN_DANGER_DISTANCE){
                    okCell = moveCell[i];
                    break;
                }else if(Utility.distance(moveCell[i],gCell)>=GUARDIAN_DANGER_DISTANCE){ // dis > dangeres rrange
                    outofobjOkCell = moveCell[i];
                }else if(moveCell[i].isInObjectiveZone() &&  // objective and dis > nowDis
                        Utility.distance(moveCell[i],gCell) > disFromMe){
                    objBadCell = moveCell[i];
                }else if(Utility.distance(moveCell[i],gCell) > disFromMe){ // dis > nowDis
                    badCell = moveCell[i];
                }
            }
            Direction dir[];
            if(okCell != null){
                dir = world.getPathMoveDirections(blasterCurrentCell,okCell);
            }else if(outofobjOkCell != null){
                dir  = world.getPathMoveDirections(blasterCurrentCell,outofobjOkCell);
            }else if(objBadCell != null){
                dir  = world.getPathMoveDirections(blasterCurrentCell,objBadCell);
            }else if(badCell != null){
                dir  = world.getPathMoveDirections(blasterCurrentCell,badCell);
            }else{
                dir = world.getPathMoveDirections(blasterCurrentCell,moveCell[new Random().nextInt(4)]);
            }
            if(dir.length == 0)
                return;
            world.moveHero(blaster.getId(),dir[0]);
            setCell(blaster,Utility.nextCell(world,blasterCurrentCell,dir[0]));
        }
    }

    private static void dodgeToNearestEnemy(AI ai, World world, Hero blaster, Cell blasterCell) {
        int row;
        int col;
        Hero[] saw = Utility.getSawHero(world);
        if (saw.length == 0)
            return;
        Utility.sortOnHP(saw);
        Utility.sortOnDistance(blaster.getCurrentCell(), saw);
        row = (blasterCell.getRow() + saw[0].getCurrentCell().getRow()) / 2;
        col = (blasterCell.getColumn() + saw[0].getCurrentCell().getColumn()) / 2;
        world.castAbility(blaster, AbilityName.BLASTER_DODGE, world.getMap().getCells()[row][col]);
        ai.dodgeTo(blaster, world.getMap().getCells()[row][col]);
        setCell(blaster, world.getMap().getCell(row, col));
    }

    /**
     * hero haii k tu rangesh hastan ro migire v tasmim migire kodum cdell baraye attack bhtre v un ro barmigardune
     * */
    private static Cell getBestForBlasterAttack(World world, Hero mhero, Hero[] inMyAttckRange, AbilityName abilityName) {
        if(inMyAttckRange.length == 1)
            return inMyAttckRange[0].getCurrentCell();
        if(abilityName == AbilityName.BLASTER_BOMB && inMyAttckRange.length == 2){
            //2 ta hastan v ability = BOMB
            if(Utility.distance(inMyAttckRange[0].getCurrentCell(),inMyAttckRange[1].getCurrentCell()) <= 2*Utility_Attack.radius_of_blaster_bomb) {
                // mishe zad beyeneshun
                Cell[] effectiveCells = Utility.effectiveCells(world,inMyAttckRange[0].getCurrentCell(),inMyAttckRange[1].getCurrentCell());
                if(effectiveCells.length == 1)
                    return effectiveCells[0];
                return  Utility.distance(mhero.getCurrentCell(),effectiveCells[0]) <= Utility.distance(mhero.getCurrentCell(),effectiveCells[1])?
                        effectiveCells[0]:
                        effectiveCells[1];
            }else{ // nmishe zad beyneshun
                 /// vase hamin mizanm be uni k junesh kamtre
                Utility.sortOnHP(inMyAttckRange);
                if(Utility.distance(inMyAttckRange[0].getCurrentCell(),mhero.getCurrentCell())<=5)
                    //ag tu range bomb hast
                    return inMyAttckRange[0].getCurrentCell();
                else{
                    //tu range bomb nis va bayad bznm ru marz k effectsh bhsh berese
                    return minCellFrom(world,mhero.getCurrentCell(),5,inMyAttckRange[0].getCurrentCell());
                }
            }
        }else if(abilityName == AbilityName.BLASTER_BOMB && inMyAttckRange.length == 3){
            // 3 ta hastan v ability = BOMB
            if(Utility.distance(inMyAttckRange[0].getCurrentCell(),inMyAttckRange[1].getCurrentCell())
                <= 2*Utility_Attack.radius_of_blaster_bomb &&
                Utility.distance(inMyAttckRange[0].getCurrentCell(),inMyAttckRange[2].getCurrentCell())
                <= 2*Utility_Attack.radius_of_blaster_bomb &&
                Utility.distance(inMyAttckRange[1].getCurrentCell(),inMyAttckRange[2].getCurrentCell())
                <= 2*Utility_Attack.radius_of_blaster_bomb)
                // mishe zad vasateshun
                return vasateshun(world,inMyAttckRange[0].getCurrentCell(),inMyAttckRange[1].getCurrentCell(),inMyAttckRange[2].getCurrentCell());
        }else if(abilityName == AbilityName.BLASTER_BOMB && inMyAttckRange.length == 4){
            Utility.sortOnHP(inMyAttckRange);
            return minCellFrom(world,mhero.getCurrentCell(),5,inMyAttckRange[0].getCurrentCell());
        }else if(abilityName == AbilityName.BLASTER_ATTACK){
            // in vase attack zadane
            if(inMyAttckRange.length == 2) {
                if (Utility.distance(inMyAttckRange[1].getCurrentCell(), inMyAttckRange[0].getCurrentCell())
                        <= 2 * Utility.BLASTER_ATTACK_RADIUS){
                    Cell[] effective =
                            Utility.effectiveCells(world,inMyAttckRange[0].getCurrentCell(),inMyAttckRange[1].getCurrentCell());
                    if(effective.length == 1)
                        return effective[0];
                    return Utility.distance(effective[0],mhero.getCurrentCell())<=Utility.distance(effective[1],mhero.getCurrentCell())?
                            effective[0]:effective[1];
                }else { // dota hastan ama faselashun bishtare 2 hast
                    Utility.sortOnHP(inMyAttckRange);
                    return minCellFrom(world,mhero.getCurrentCell(),4,inMyAttckRange[0].getCurrentCell());
                }
            }else if(inMyAttckRange.length == 3){ // 3 nfr hastan
                if(Utility.distance(inMyAttckRange[0].getCurrentCell(),inMyAttckRange[1].getCurrentCell())
                    <= 2*Utility.BLASTER_ATTACK_RADIUS &&
                    Utility.distance(inMyAttckRange[0].getCurrentCell(),inMyAttckRange[2].getCurrentCell())
                    <= 2*Utility.BLASTER_ATTACK_RADIUS &&
                    Utility.distance(inMyAttckRange[1].getCurrentCell(),inMyAttckRange[2].getCurrentCell())
                    <= 2*Utility.BLASTER_ATTACK_RADIUS){ // fasele in seta 2 b 2 2 has, pas mishe zad vasateshun
                    return vasateshun(world,inMyAttckRange[0].getCurrentCell(),inMyAttckRange[1].getCurrentCell(),inMyAttckRange[2].getCurrentCell());
                }else{
                    Utility.sortOnHP(inMyAttckRange);
                    return minCellFrom(world,mhero.getCurrentCell(),4,inMyAttckRange[0].getCurrentCell());
                }
            }else{
                Utility.sortOnHP(inMyAttckRange);
                return minCellFrom(world,mhero.getCurrentCell(),4,inMyAttckRange[0].getCurrentCell());
            }
        }
        Utility.sortOnHP(inMyAttckRange);
        return minCellFrom(world,mhero.getCurrentCell(),4,inMyAttckRange[0].getCurrentCell());
    }

    private static Cell minCellFrom(World world,Cell from, int range, Cell to) {
        Cell avi[] = Utility.availableCells(world.getMap(),range,from);
        int minDis = Integer.MAX_VALUE;
        Cell min = avi[0];
        for (int i = 0; i < avi.length; i++) {
            int tmp = Utility.distance(avi[i],to);
            if(tmp < minDis){
                minDis = tmp;
                min = avi[i];
            }
        }
        return min;
    }

    /**
     * when blaster not see enemys
     * do this method
     */
    private static void safeMove(World world, Hero blaster, Cell blasterCurrentCell, History history) {
        if (blasterCurrentCell.isInObjectiveZone()/*true*/) {
            Hero[] blasters = getBelasters(world, blaster);
            boolean dupRange = blasters.length!=0;
            Vector<Hero> dupRangeH = new Vector<>(Arrays.asList(blasters));
            if (dupRange) {
                safeFromBlasters(world, blaster, blasterCurrentCell, dupRangeH);
            }
        } else {
            System.out.println("=======================\nin safemove and out of objzone\n"+blasterCurrentCell.getRow()+"-"+blasterCurrentCell.getColumn());

            Vector<Cell> blockcell = new Vector<>();
            Cell minDisCellFromObjzone;

            do{
                minDisCellFromObjzone = objzoneCellMinDis(blasterCurrentCell, blockcell);
                blockcell.add(minDisCellFromObjzone);
                System.out.println(blockcell.size()+":"+minDisCellFromObjzone.getRow()+"-"+minDisCellFromObjzone.getColumn());
                if(blockcell.size() == objectiveCells.length * objectiveCells.length)
                    return;
            } while (Utility.distance(blasterCurrentCell,minDisCellFromObjzone) == 1 &&
                    world.getMyHero(minDisCellFromObjzone) != null);

            Cell nextCell = Utility.nextCell(world,blasterCurrentCell,minDisCellFromObjzone);
            System.out.println("next cell : "+nextCell.getRow()+"-"+nextCell.getColumn());
            Hero[] perdict = Utility.getGuardians(world,nextCell);
            System.out.println("perdict len : "+perdict.length);
            if(perdict.length == 0) {
                move(world, blaster.getId(), blasterCurrentCell, nextCell, history);
                setCell(blaster, nextCell);
            }
            System.out.println("==========================");
        }
    }

    private static void safeFromBlasters(World world, Hero blaster, Cell blasterCurrentCell, Vector<Hero> dupRangeH) {
        Cell[] moveCells = moveCell(world, blaster);
        //obj && > 4
        float avgDis = avgDis(blasterCurrentCell,dupRangeH.toArray(new Hero[0]));
        //*********************************************
        Cell okCell = null,outofobjOkCell=null,objBadCell=null,badCell=null;
        boolean okFlag,outofFlag,objbadCellFlag,badCellFlag;
        for (int i = 0; i < moveCells.length; i++) {
            if(moveCells[i] == null || moveCells[i].isWall())
                continue;
            okFlag = outofFlag = objbadCellFlag = badCellFlag = true;
            for (int j = 0; j < dupRangeH.size(); j++) {
                if(!(moveCells[i].isInObjectiveZone() &&
                    Utility.distance(moveCells[i],currentCell(dupRangeH.get(j)))>4 )){
                    okFlag = false;
                }else if(!(moveCells[i].isInObjectiveZone() &&
                    Utility.distance(moveCells[i],currentCell(dupRangeH.get(j)))>avgDis )){
                    objbadCellFlag = false;
                }else if(!(Utility.distance(moveCells[i],currentCell(dupRangeH.get(j)))>4 )){
                    outofFlag = false;
                }else if(!(Utility.distance(moveCells[i],currentCell(dupRangeH.get(j)))>4 )){
                    badCellFlag = false;
                }
            }
            if(okFlag){
                okCell = moveCells[i];
            }else if(objbadCellFlag){
                objBadCell = moveCells[i];
            }else if(outofFlag){
                outofobjOkCell = moveCells[i];
            }else if(badCellFlag){
                badCell = moveCells[i];
            }
        }
        Direction[] dir;
        if(okCell != null){
            dir = world.getPathMoveDirections(blasterCurrentCell,okCell);
        }else if(objBadCell != null){
            dir = world.getPathMoveDirections(blasterCurrentCell,badCell);
        }else if(outofobjOkCell != null){
            dir = world.getPathMoveDirections(blasterCurrentCell,outofobjOkCell);
        }else if(badCell != null){
            dir = world.getPathMoveDirections(blasterCurrentCell,badCell);
        }else{
            return;
        }
        //*********************************************
        for (int i = 0; i < moveCells.length; i++) {
            if (moveCells[i] != null && moveCells[i].isInObjectiveZone()) {
                boolean flag = true;
                for (Hero ourBlaster : dupRangeH) {
                    if (Utility.distance(moveCells[i], currentCell(ourBlaster)) <= avgDis) {
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
        // obj && > aln
        for (Cell moveCell1 : moveCells) {
            boolean flag = true;
            for (int j = 0; moveCell1 != null
                    && moveCell1.isInObjectiveZone()
                    && j < dupRangeH.size(); j++) {
                if (!(Utility.distance(moveCell1, currentCell(dupRangeH.get(j))) >= avgDis)) {
                    flag = false;
                    break;
                }
            }
            if (moveCell1!=null && flag) {
                Utility.move(world, blaster.getId(), blasterCurrentCell, moveCell1);
                setCell(blaster, moveCell1);
                return;
            }
        }
        // > aln
        for (Cell moveCell : moveCells) {
            boolean flag = true;
            for (int j = 0; moveCell != null
                    && j < dupRangeH.size(); j++) {
                if (!(Utility.distance(moveCell, currentCell(dupRangeH.get(j))) >= avgDis)) {
                    flag = false;
                    break;
                }
            }
            if (moveCell!=null && flag) {
                Utility.move(world, blaster.getId(), blasterCurrentCell, moveCell);
                setCell(blaster, moveCell);
                return;
            }
        }
    }

    //*************helper********************

    private static float avgDis(Cell blasterCurrentCell, Hero[] toArray) {
        if(toArray.length == 0)
            return Integer.MIN_VALUE;
        float sum = 0;
        for (int i = 0; i < toArray.length; i++) {
            sum+=Utility.distance(blasterCurrentCell,currentCell(toArray[i]));
        }
        return sum/toArray.length;
    }

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
    private static Cell objzoneCellMinDis(Cell blasterCurrentCell) {
        return objzoneCellMinDis(blasterCurrentCell,new Cell[0]);
    }
    private static Cell objzoneCellMinDis(Cell blastercc,Vector<Cell> blocks){
        return objzoneCellMinDis(blastercc,blocks.toArray(new Cell[0]));
    }
    private static Cell objzoneCellMinDis(Cell blastercc,Cell[] blocks){
        if(blastercc.isInObjectiveZone())
            return null;
        int rowIndex=0,colIndex=0,minDis = Integer.MAX_VALUE;
        for (int i = 0; i < blocks.length; i++) {
            System.out.println(blocks[i].getRow()+"-"+blocks[i].getColumn());
        }
        for (int i=0;i<objectiveCells.length;i++) {
            for (int j = 0; j < objectiveCells.length; j++) {
                boolean flag = false;
                for (Cell block : blocks) {
                    if (objectiveCells[i][j].getRow() == block.getRow()
                            &&objectiveCells[i][j].getColumn() == block.getColumn() ) {
                        flag = true;
                        break;
                    }
                }
                if (flag)
                    continue;
                int tmpDis = Utility.distance(blastercc, objectiveCells[i][j]);
                if (tmpDis < minDis) {
                    minDis = tmpDis;
                    rowIndex = i;
                    colIndex = j;
                }
            }
        }
        System.out.println(rowIndex+"-"+colIndex);
        return objectiveCells[rowIndex][colIndex];
    }
    private static Cell objzoneCellMinDis(Cell center,boolean forceEmpty){
        if(!forceEmpty)
            return objzoneCellMinDis(center);
        if(center.isInObjectiveZone())
            return null;
        int minDis = Integer.MAX_VALUE;
        Cell ans = objectiveCells[0][0];
        for (int i = 0; i < objectiveCells.length; i++) {
            for (int j = 0; j < objectiveCells.length; j++) {
                int tmpDis = Utility.distance(center,objectiveCells[i][j]);
                if(tmpDis < minDis){
                    tmpDis = minDis;
                    ans = objectiveCells[i][j];
                }
            }
        }
        return ans;
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
        if(map.isInMap(row-1,col) && !map.getCell(row-1,col).isWall())
            cs[0] = (map.getCell(row-1,col)); // add up
        if(map.isInMap(row+1,col)&& !map.getCell(row+1,col).isWall())
            cs[1]=(map.getCell(row+1,col)); // add down
        if(map.isInMap(row,col-1)&& !map.getCell(row,col-1).isWall()) // add left
            cs[2]=(map.getCell(row,col-1));
        if(map.isInMap(row,col+1)&& !map.getCell(row,col+1).isWall()) // add right
            cs[3]=(map.getCell(row,col+1));
        return cs;
    }

    /**
     * blaster haye khodi ro return mikone
     * */
    private static Hero[] getBelasters(World world, Hero blaster) {
        return getBlasters(world,blaster.getCurrentCell(),blaster.getId());
    }
    private static Hero[] getBlasters(World world,Cell center,int blasterID){
        Cell[] cl = Utility.availableCells(world.getMap(),4,center);
        Vector<Hero> hs = new Vector<>();
        for (Cell aMh : cl) {
            if (world.getMyHero(aMh) != null &&
                    world.getMyHero(aMh).getName().equals(HeroName.BLASTER) &&
                    world.getMyHero(aMh).getId()!=blasterID)
                hs.add(world.getMyHero(aMh));
        }
        return hs.toArray(new Hero[0]);
    }

    /**
     * check mikone bebine hichkodum azin ha objzone hastan ya na
     * */
    private static boolean hasObjective(Cell... cells) {
        for (Cell cell : cells) {
            if (cell != null && cell.isInObjectiveZone())
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