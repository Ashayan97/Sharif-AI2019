package client;

import client.model.*;

import javax.swing.*;
import java.util.Random;
import java.util.Vector;

public class Blaster_AI {
    private static final int GUARDIAN_DANGER_DISTANCE = 3;
    private World world;
    private Hero blaster;
    private AI ai;
    private History history;
    private Cell[][] objectiveCells;

    public Blaster_AI(World world, AI ai, Hero blaster, History history, Cell[][] objectiveCells) {
        this.world = world;
        this.blaster = blaster;
        this.ai = ai;
        this.objectiveCells = objectiveCells;
    }

    public void move() {
        if (blaster.getCurrentHP() == 0)
            return;
        Hero guardians[] = Utility.getGuardians(world, blaster);
        boolean safe = guardians.length == 0;
        if (safe)
            safeMove(blaster.getCurrentCell());
        else
            safeFromGuardians(blaster.getCurrentCell(), guardians);
    }

    private void safeMove(Cell blasterCurrentCell) {
        if (blasterCurrentCell.isInObjectiveZone()) {
//            Hero[] blasters = getBelasters(world, blaster);
//            boolean dupRange = blasters.length!=0;
//            Vector<Hero> dupRangeH = new Vector<>(Arrays.asList(blasters));
//            if (dupRange) {
//                safeFromBlasters(world, blaster, blasterCurrentCell, dupRangeH);
//            }else{
            Cell g1 = world.getMap().getCells()[ai.getRowGoal()][ai.getColGoal1()];
            Cell g2 = world.getMap().getCells()[ai.getRowGoal()][ai.getColGoal2()];
            if (Utility.distance(blasterCurrentCell, g1) == 0 ||
                    Utility.distance(blasterCurrentCell, g2) == 0)
                return;
            Cell goal;
            if (ai.isRezerevd()) {
                if (ai.getWhoRezereved() == ai.getInProcess()) {
                    goal = ai.getRezerevedCell();
//                    Utility.move(world,blaster,blasterCurrentCell,ai.getRezerevedCell());
//                    setCell(Utility.nextCell(world,blasterCurrentCell,ai.getRezerevedCell()));
                } else {
                    if (ai.getRezerevedCell().getColumn() != ai.getColGoal1()) {
                        goal = g1;
//                        Utility.move(world,blaster,blasterCurrentCell,g1);
//                        setCell(Utility.nextCell(world,blasterCurrentCell,g1));
                    } else {
                        goal = g2;
//                        Utility.move(world,blaster,blasterCurrentCell,g2);
//                        setCell(Utility.nextCell(world,blasterCurrentCell,g2));
                    }
                }
            } else {
                Cell minGoalFromMe = world.manhattanDistance(blasterCurrentCell, g1) < world.manhattanDistance(blasterCurrentCell, g2) ? g1 : g2;
                boolean nearestToMe = world.manhattanDistance(blasterCurrentCell, minGoalFromMe) <
                        world.manhattanDistance(currentCell(ai.getBlaster()), minGoalFromMe);
                if (nearestToMe) {
                    goal = minGoalFromMe;
//                    Utility.move(world, blaster, blasterCurrentCell, minGoalFromMe);
//                    setCell(Utility.nextCell(world, blasterCurrentCell, minGoalFromMe));
                    ai.setRezereved(minGoalFromMe);
                } else {
                    Cell g = minGoalFromMe.getColumn() == g1.getColumn() ? g2 : g1;
                    goal = g;
//                    Utility.move(world, blaster, blasterCurrentCell, g);
//                    setCell(Utility.nextCell(world,blasterCurrentCell,g));
                    ai.setRezereved(g);
                }
            }
            Cell next = Utility.nextCell(world, blasterCurrentCell, goal);
            if (Utility.getGuardians(world, next).length != 0)
                return;
            Utility.move(world, blaster, blasterCurrentCell, next);
            setCell(next);
        } else {
            Vector<Cell> blockcell = new Vector<>();
            Cell minDisCellFromObjzone = objzoneCellMinDis(world, blasterCurrentCell, true);

//            do{
//                minDisCellFromObjzone = objzoneCellMinDis(blasterCurrentCell, blockcell);
//                blockcell.add(minDisCellFromObjzone);
//                if(blockcell.size() == objectiveCells.length * objectiveCells.length)
//                    return;
//            } while (Utility.distance(blasterCurrentCell,minDisCellFromObjzone) == 1 &&
//                    world.getMyHero(minDisCellFromObjzone) != null);

            Cell nextCell = Utility.nextCell(world, blasterCurrentCell, minDisCellFromObjzone);
            Hero[] perdict = Utility.getGuardians(world, nextCell);
            if (perdict.length == 0) {
                Utility.move(world, blaster.getId(), blasterCurrentCell, nextCell);
                setCell(nextCell);
            }
        }
    }

    private void safeFromGuardians(Cell blasterCurrentCell, Hero[] guardians) {
        Cell[] moveCell = moveCell(world, blaster);
        Cell down = moveCell[1],
                up = moveCell[0],
                left = moveCell[2],
                right = moveCell[3];
        boolean hasObjective = hasObjective(down, up, left, right);
        if (guardians.length > 1) {
            float avgDis = Utility.avgDis(blasterCurrentCell, guardians);
            Cell okCell = null, outofobjOkCell = null, objBadCell = null, badCell = null;
            for (int i = 0; i < moveCell.length; i++) {
                if (moveCell[i] == null || moveCell[i].isWall())
                    continue;// i++ if movecell[i] be null or wall
                boolean okFlag, outofFlag, objbadcellFlag, badcellFlag;
                okFlag = outofFlag = objbadcellFlag = badcellFlag = true;
                for (int j = 0; j < guardians.length; j++) {
                    Cell GCELL = guardians[j].getCurrentCell();
                    if ((moveCell[i].isInObjectiveZone() && //be objective and dis > dangeres range
                            Utility.distance(moveCell[i], GCELL) >= GUARDIAN_DANGER_DISTANCE)) {
                        okFlag = false;
                    } else if ((Utility.distance(moveCell[i], GCELL) >= GUARDIAN_DANGER_DISTANCE)) { // dis > dangeres rrange
                        outofFlag = false;
                    } else if ((moveCell[i].isInObjectiveZone() &&  // objective and dis > nowDis
                            Utility.distance(moveCell[i], GCELL) > avgDis)) {
                        objbadcellFlag = false;
                    } else if (Utility.distance(moveCell[i], GCELL) > avgDis) { // dis > nowDis
                        badcellFlag = false;
                    }
                }
                if (okFlag) {
                    okCell = moveCell[i];
                } else if (outofFlag) {
                    outofobjOkCell = moveCell[i];
                } else if (objbadcellFlag) {
                    objBadCell = moveCell[i];
                } else if (badcellFlag) {
                    badCell = moveCell[i];
                }
            }
            Direction dir[];
            if (okCell != null) {
                dir = world.getPathMoveDirections(blasterCurrentCell, okCell);
            } else if (outofobjOkCell != null) {
                dir = world.getPathMoveDirections(blasterCurrentCell, outofobjOkCell);
            } else if (objBadCell != null) {
                dir = world.getPathMoveDirections(blasterCurrentCell, objBadCell);
            } else if (badCell != null) {
                dir = world.getPathMoveDirections(blasterCurrentCell, badCell);
            } else {
                dir = world.getPathMoveDirections(blasterCurrentCell, moveCell[new Random().nextInt(4)]);
            }
            if (dir.length == 0)
                return;
            world.moveHero(blaster.getId(), dir[0]);
            setCell(Utility.nextCell(world, blasterCurrentCell, dir[0]));
        } else { // yeki guardian nazdikame
            Cell gCell = guardians[0].getCurrentCell();
            Cell okCell = null, outofobjOkCell = null, objBadCell = null, badCell = null;
            int disFromMe = Utility.distance(blasterCurrentCell, gCell);
            for (int i = 0; i < moveCell.length; i++) {
                if (moveCell[i] == null || moveCell[i].isWall())
                    continue;// i++ if movecell[i] be null or wall
                if (moveCell[i].isInObjectiveZone() && //be objective and dis > dangeres range
                        Utility.distance(moveCell[i], gCell) >= GUARDIAN_DANGER_DISTANCE) {
                    okCell = moveCell[i];
                    break;
                } else if (Utility.distance(moveCell[i], gCell) >= GUARDIAN_DANGER_DISTANCE) { // dis > dangeres rrange
                    outofobjOkCell = moveCell[i];
                } else if (moveCell[i].isInObjectiveZone() &&  // objective and dis > nowDis
                        Utility.distance(moveCell[i], gCell) > disFromMe) {
                    objBadCell = moveCell[i];
                } else if (Utility.distance(moveCell[i], gCell) > disFromMe) { // dis > nowDis
                    badCell = moveCell[i];
                }
            }
            Direction dir[];
            if (okCell != null) {
                dir = world.getPathMoveDirections(blasterCurrentCell, okCell);
            } else if (outofobjOkCell != null) {
                dir = world.getPathMoveDirections(blasterCurrentCell, outofobjOkCell);
            } else if (objBadCell != null) {
                dir = world.getPathMoveDirections(blasterCurrentCell, objBadCell);
            } else if (badCell != null) {
                dir = world.getPathMoveDirections(blasterCurrentCell, badCell);
            } else {
                dir = world.getPathMoveDirections(blasterCurrentCell, moveCell[new Random().nextInt(4)]);
            }
            if (dir.length == 0)
                return;
            world.moveHero(blaster.getId(), dir[0]);
            setCell(Utility.nextCell(world, blasterCurrentCell, dir[0]));
        }
    }

    private Cell currentCell(Hero th) {
        return ai.getNextCell(th) == null ? th.getCurrentCell() : ai.getNextCell(th);
    }

    /**
     * hero'ii k behesh attack mizane ro set mikone tu AI v ag dodge bzne cell ro set mikone mikone
     */
    public void attack() {
        if (blaster.getCurrentHP() == 0 || world.getAP() < 15)
            return;
        AbilityName abilityName = blaster.getAbility(AbilityName.BLASTER_BOMB).isReady() ?
                AbilityName.BLASTER_BOMB :
                AbilityName.BLASTER_ATTACK;
        Hero[] inMyAttckRange = Utility.getInAttackRange(world, blaster, abilityName);
        Cell blasterCell = blaster.getCurrentCell();
        if (inMyAttckRange.length == 0) {
            int row;
            int col;
//            if (blaster.getCurrentCell().isInObjectiveZone()) {
//                dodgeToNearestEnemy(ai, world, blaster, blasterCell);
            /*}else */
            if (blaster.getAbility(AbilityName.BLASTER_DODGE).isReady()
                    && !blasterCell.isInObjectiveZone()) {
                // hichki tu range attack nis va kharej objzone hastim
                Cell[] avai = Utility.availableCells(world.getMap(), Utility.BLASTER_DODGE_RANGE, blasterCell);
                Cell minObjzone = objzoneCellMinDis(world, blasterCell, true);
                int min = Integer.MAX_VALUE;
                Cell shodDodge = avai[0];
                for (Cell anAvai : avai)
                    if (!anAvai.isWall()
                            && world.isInVision(minObjzone, anAvai)
                            && Utility.distance(anAvai, minObjzone) < min) {
                        shodDodge = anAvai;
                        min = Utility.distance(anAvai, minObjzone);
                    }
                row = shodDodge.getRow();
                col = shodDodge.getColumn();
                world.castAbility(blaster.getId(), AbilityName.BLASTER_DODGE, row, col);
                ai.dodgeTo(blaster, world.getMap().getCell(row, col));
                setCell(world.getMap().getCell(row, col));
            }
        } else {
            Cell whereShouldIAttack = getBestForBlasterAttack(inMyAttckRange, abilityName);
            if (Utility.distance(blasterCell, whereShouldIAttack) > (abilityName == AbilityName.BLASTER_BOMB ? 7 : 5))
                Logger.log(String.format("ID : %d, [%d,%d]-->[%d,%d], inRangeLen : %d", blaster.getId(), blasterCell.getRow(), blasterCell.getColumn(), whereShouldIAttack.getRow(), whereShouldIAttack.getColumn(), inMyAttckRange.length) + abilityName.name(), Logger.FULL_SOFT_GREEN);
            world.castAbility(blaster.getId(), abilityName, whereShouldIAttack);
            ai.setInAttack(blaster, world.getOppHero(whereShouldIAttack));
        }
    }

    /**
     * hero haii k tu rangesh hastan ro migire v tasmim migire kodum cdell baraye attack bhtre v un ro barmigardune
     */
    private Cell getBestForBlasterAttack(Hero[] inMyAttckRange, AbilityName abilityName) {
        if (inMyAttckRange.length == 1)
            return inMyAttckRange[0].getCurrentCell();
        if (abilityName == AbilityName.BLASTER_BOMB
                && inMyAttckRange.length == 2) {
            //2 ta hastan v ability = BOMB
            if (Utility.distance(inMyAttckRange[0].getCurrentCell(), inMyAttckRange[1].getCurrentCell())
                    <= 2 * Utility_Attack.radius_of_blaster_bomb) {
                // mishe zad beyeneshun
                return vasateshun(world, inMyAttckRange[0].getCurrentCell(),
                        inMyAttckRange[0].getCurrentCell());
            } else { // nmishe zad beyneshun
                /// vase hamin mizanm be uni k junesh kamtre
                Utility.sortOnHP(inMyAttckRange);
                if (Utility.distance(inMyAttckRange[0].getCurrentCell(), blaster.getCurrentCell()) <= 5)
                    //ag tu range bomb hast
                    return inMyAttckRange[0].getCurrentCell();
                else {
                    //tu range bomb nis va bayad bznm ru marz k effectsh bhsh berese
                    return minCellFrom(blaster.getCurrentCell(), 5, inMyAttckRange[0].getCurrentCell());
                }
            }
        } else if (abilityName == AbilityName.BLASTER_BOMB
                && inMyAttckRange.length == 3) {
            // 3 ta hastan v ability = BOMB
            if (Utility.distance(inMyAttckRange[0].getCurrentCell(), inMyAttckRange[1].getCurrentCell())
                    <= 2 * Utility_Attack.radius_of_blaster_bomb &&
                    Utility.distance(inMyAttckRange[0].getCurrentCell(), inMyAttckRange[2].getCurrentCell())
                            <= 2 * Utility_Attack.radius_of_blaster_bomb &&
                    Utility.distance(inMyAttckRange[1].getCurrentCell(), inMyAttckRange[2].getCurrentCell())
                            <= 2 * Utility_Attack.radius_of_blaster_bomb)
                // mishe zad vasateshun
                return vasateshun(world, inMyAttckRange[0].getCurrentCell(), inMyAttckRange[1].getCurrentCell(), inMyAttckRange[2].getCurrentCell());
            else {
                Utility.sortOnHP(inMyAttckRange);
                return minCellFrom(blaster.getCurrentCell(), 5, inMyAttckRange[0].getCurrentCell());
            }
        } else if (abilityName == AbilityName.BLASTER_BOMB
                && inMyAttckRange.length == 4) {
            int range = 2 * Utility_Attack.radius_of_blaster_bomb;
            if (Utility.distance(inMyAttckRange[0].getCurrentCell(), inMyAttckRange[1].getCurrentCell())
                    <= range
                    && Utility.distance(inMyAttckRange[0].getCurrentCell(), inMyAttckRange[2].getCurrentCell())
                    <= range
                    && Utility.distance(inMyAttckRange[0].getCurrentCell(), inMyAttckRange[3].getCurrentCell())
                    <= range
                    && Utility.distance(inMyAttckRange[1].getCurrentCell(), inMyAttckRange[2].getCurrentCell())
                    <= range
                    && Utility.distance(inMyAttckRange[1].getCurrentCell(), inMyAttckRange[3].getCurrentCell())
                    <= range
                    && Utility.distance(inMyAttckRange[2].getCurrentCell(), inMyAttckRange[3].getCurrentCell())
                    <= range) {
                //4 an v mishe zad vasateshun
                return vasateshun(world, inMyAttckRange[0].getCurrentCell(),
                        inMyAttckRange[1].getCurrentCell(),
                        inMyAttckRange[2].getCurrentCell(),
                        inMyAttckRange[3].getCurrentCell());
            } else {
                Utility.sortOnHP(inMyAttckRange);
                return minCellFrom(blaster.getCurrentCell(), 5, inMyAttckRange[0].getCurrentCell());
            }
        } else if (abilityName == AbilityName.BLASTER_ATTACK) {
            // in vase attack zadane
            if (inMyAttckRange.length == 2) {
                if (Utility.distance(inMyAttckRange[1].getCurrentCell(), inMyAttckRange[0].getCurrentCell())
                        <= 2 * Utility.BLASTER_ATTACK_RADIUS) {
                    return vasateshun(world, inMyAttckRange[0].getCurrentCell(), inMyAttckRange[1].getCurrentCell());
                } else { // dota hastan ama faselashun bishtare 2 hast
                    Utility.sortOnHP(inMyAttckRange);
                    return minCellFrom(blaster.getCurrentCell(), 4, inMyAttckRange[0].getCurrentCell());
                }
            } else if (inMyAttckRange.length == 3) { // 3 nfr hastan
                if (Utility.distance(inMyAttckRange[0].getCurrentCell(), inMyAttckRange[1].getCurrentCell())
                        <= 2 * Utility.BLASTER_ATTACK_RADIUS &&
                        Utility.distance(inMyAttckRange[0].getCurrentCell(), inMyAttckRange[2].getCurrentCell())
                                <= 2 * Utility.BLASTER_ATTACK_RADIUS &&
                        Utility.distance(inMyAttckRange[1].getCurrentCell(), inMyAttckRange[2].getCurrentCell())
                                <= 2 * Utility.BLASTER_ATTACK_RADIUS) { // fasele in seta 2 b 2 2 has, pas mishe zad vasateshun
                    return vasateshun(world, inMyAttckRange[0].getCurrentCell(), inMyAttckRange[1].getCurrentCell(), inMyAttckRange[2].getCurrentCell());
                } else {
                    Utility.sortOnHP(inMyAttckRange);
                    return minCellFrom(blaster.getCurrentCell(), 4, inMyAttckRange[0].getCurrentCell());
                }
            } else {
                int range = 2 * Utility.BLASTER_ATTACK_RADIUS;
                if (Utility.distance(inMyAttckRange[0].getCurrentCell(), inMyAttckRange[1].getCurrentCell())
                        <= range
                        && Utility.distance(inMyAttckRange[0].getCurrentCell(), inMyAttckRange[2].getCurrentCell())
                        <= range
                        && Utility.distance(inMyAttckRange[0].getCurrentCell(), inMyAttckRange[3].getCurrentCell())
                        <= range
                        && Utility.distance(inMyAttckRange[1].getCurrentCell(), inMyAttckRange[2].getCurrentCell())
                        <= range
                        && Utility.distance(inMyAttckRange[1].getCurrentCell(), inMyAttckRange[3].getCurrentCell())
                        <= range
                        && Utility.distance(inMyAttckRange[2].getCurrentCell(), inMyAttckRange[3].getCurrentCell())
                        <= range) {
                    //4 an v mishe zad vasateshun
                    return vasateshun(world, inMyAttckRange[0].getCurrentCell(),
                            inMyAttckRange[1].getCurrentCell(),
                            inMyAttckRange[2].getCurrentCell(),
                            inMyAttckRange[3].getCurrentCell());
                } else {
                    Utility.sortOnHP(inMyAttckRange);
                    return minCellFrom(blaster.getCurrentCell(), 4, inMyAttckRange[0].getCurrentCell());
                }
            }
        }
        Utility.sortOnDistance(blaster.getCurrentCell(), inMyAttckRange);
        return inMyAttckRange[0].getCurrentCell();
    }

    private Cell vasateshun(World world, Cell start, Cell end) {
        return vasateshun(world, start, end, end);
    }

    private Cell vasateshun(World world, Cell start, Cell mid, Cell end) {
        return vasateshun(world, start, mid, end, end);
    }

    private Cell vasateshun(World world, Cell start, Cell mid, Cell mid2, Cell end) {
        Cell DOWN = Utility.getDOWN(start, Utility.getDOWN(mid, Utility.getDOWN(mid2, end)));
        Cell UP = Utility.getUP(start, Utility.getUP(mid, Utility.getUP(mid2, end)));
        Cell LEFT = Utility.getLEFT(start, Utility.getLEFT(mid, Utility.getLEFT(mid2, end)));
        Cell RIGHT = Utility.getRIGHT(start, Utility.getRIGHT(mid, Utility.getRIGHT(mid2, end)));
        return world.getMap().getCell((UP.getRow() + DOWN.getRow()) / 2, (LEFT.getColumn() + RIGHT.getColumn()) / 2);
    }

    private Cell minCellFrom(Cell from, int range, Cell to) {
        Cell avi[] = Utility.availableCells(world.getMap(), range, from);
        int minDis = Integer.MAX_VALUE;
        Cell min = avi[0];
        for (int i = 0; i < avi.length; i++) {
            int tmp = Utility.distance(avi[i], to);
            if (tmp < minDis) {
                minDis = tmp;
                min = avi[i];
            }
        }
        return min;
    }

    /**
     * check mikone bebine hichkodum azin ha objzone hastan ya na
     */
    private boolean hasObjective(Cell... cells) {
        for (Cell cell : cells) {
            if (cell != null && cell.isInObjectiveZone())
                return true;
        }
        return false;
    }

    private Cell objzoneCellMinDis(Cell blasterCurrentCell) {
        return objzoneCellMinDis(blasterCurrentCell, new Cell[0]);
    }

    private Cell objzoneCellMinDis(Cell blastercc, Vector<Cell> blocks) {
        return objzoneCellMinDis(blastercc, blocks.toArray(new Cell[0]));
    }

    private Cell objzoneCellMinDis(Cell blastercc, Cell[] blocks) {
        if (blastercc.isInObjectiveZone())
            return null;
        int rowIndex = 0, colIndex = 0, minDis = Integer.MAX_VALUE;
        for (int i = 0; i < objectiveCells.length; i++) {
            for (int j = 0; j < objectiveCells.length; j++) {
                boolean flag = false;
                for (Cell block : blocks) {
                    if (objectiveCells[i][j].getRow() == block.getRow()
                            && objectiveCells[i][j].getColumn() == block.getColumn()) {
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
        return objectiveCells[rowIndex][colIndex];
    }

    private Cell objzoneCellMinDis(World world, Cell center, boolean forceEmpty) {
        if (!forceEmpty)
            return objzoneCellMinDis(center);
        if (center.isInObjectiveZone())
            return null;

        Cell first = objzoneCellMinDis(center);
        Vector<Cell> block = new Vector<>();
        while (world.getMyHero(first) != null) {
            block.add(first);
            first = objzoneCellMinDis(center, block);
            if (block.size() == objectiveCells.length * objectiveCells.length)
                return null;
        }
        return first;
    }

    private void setCell(Cell moveCell) {
        ai.addCell(blaster, moveCell);
    }

    /**
     * cell haii k mitune bere ro return mikone
     * 0=up, 1=down, left=2, right=3
     */
    private static Cell[] moveCell(World world, Hero blaster) {
        Map map = world.getMap();
        int row = blaster.getCurrentCell().getRow();
        int col = blaster.getCurrentCell().getColumn();
        Cell[] cs = new Cell[4];
        if (map.isInMap(row - 1, col) && !map.getCell(row - 1, col).isWall())
            cs[0] = (map.getCell(row - 1, col)); // add up
        if (map.isInMap(row + 1, col) && !map.getCell(row + 1, col).isWall())
            cs[1] = (map.getCell(row + 1, col)); // add down
        if (map.isInMap(row, col - 1) && !map.getCell(row, col - 1).isWall()) // add left
            cs[2] = (map.getCell(row, col - 1));
        if (map.isInMap(row, col + 1) && !map.getCell(row, col + 1).isWall()) // add right
            cs[3] = (map.getCell(row, col + 1));
        return cs;
    }
}
