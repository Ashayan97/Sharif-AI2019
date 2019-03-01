package client;

import client.model.*;

import java.util.Arrays;
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

    public void move(boolean forceDupping) {
        if (blaster.getCurrentHP() == 0)
            return;
        Hero guardians[] = Utility.getGuardians(world, blaster);
        boolean safe = guardians.length == 0;
        if (safe)
            safeMove(forceDupping, blaster.getCurrentCell());
        else
            safeFromGuardians(blaster.getCurrentCell(), guardians);
    }

    private Hero[] getBelasters(World world, Hero blaster) {
        return getBlasters();
    }

    private Hero[] getBlasters() {
        Cell center = blaster.getCurrentCell();
        int blasterID = blaster.getId();
        Cell[] cl = Utility.availableCells(world.getMap(), 4, center);
        Vector<Hero> hs = new Vector<>();
        for (Cell aMh : cl) {
            if (world.getMyHero(aMh) != null &&
                    world.getMyHero(aMh).getName().equals(HeroName.BLASTER) &&
                    world.getMyHero(aMh).getId() != blasterID)
                hs.add(world.getMyHero(aMh));
        }
        return hs.toArray(new Hero[0]);
    }

    private void safeFromBlasters(Vector<Hero> dupRangeH) {
        Cell[] moveCells = moveCell(world, blaster);
        //obj && > 4
        float avgDis = Utility.avgDis(blaster, dupRangeH.toArray(new Hero[0]));
        //*********************************************
        Cell okCell = null, outofobjOkCell = null, objBadCell = null, badCell = null;
        boolean okFlag, outofFlag, objbadCellFlag, badCellFlag;
        for (int i = 0; i < moveCells.length; i++) {
            if (moveCells[i] == null || moveCells[i].isWall())
                continue;
            okFlag = outofFlag = objbadCellFlag = badCellFlag = true;
            for (int j = 0; j < dupRangeH.size(); j++) {
                if (!(moveCells[i].isInObjectiveZone() &&
                        Utility.distance(moveCells[i], currentCell(dupRangeH.get(j))) > 4)) {
                    okFlag = false;
                } else if (!(moveCells[i].isInObjectiveZone() &&
                        Utility.distance(moveCells[i], currentCell(dupRangeH.get(j))) > avgDis)) {
                    objbadCellFlag = false;
                } else if (!(Utility.distance(moveCells[i], currentCell(dupRangeH.get(j))) > 4)) {
                    outofFlag = false;
                } else if (!(Utility.distance(moveCells[i], currentCell(dupRangeH.get(j))) > 4)) {
                    badCellFlag = false;
                }
            }
            if (okFlag) {
                okCell = moveCells[i];
            } else if (objbadCellFlag) {
                objBadCell = moveCells[i];
            } else if (outofFlag) {
                outofobjOkCell = moveCells[i];
            } else if (badCellFlag) {
                badCell = moveCells[i];
            }
        }
        Direction[] dir;
        Cell blasterCurrentCell = blaster.getCurrentCell();
        if (okCell != null) {
            dir = world.getPathMoveDirections(blasterCurrentCell, okCell);
        } else if (objBadCell != null) {
            dir = world.getPathMoveDirections(blasterCurrentCell, badCell);
        } else if (outofobjOkCell != null) {
            dir = world.getPathMoveDirections(blasterCurrentCell, outofobjOkCell);
        } else if (badCell != null) {
            dir = world.getPathMoveDirections(blasterCurrentCell, badCell);
        } else {
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
                if (flag) {
                    world.moveHero(blaster.getId(), i == 0 ? Direction.UP :
                            i == 1 ? Direction.DOWN :
                                    i == 2 ? Direction.LEFT :
                                            Direction.RIGHT);
                    setCell(moveCells[i]);
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
            if (moveCell1 != null && flag) {
                Utility.move(world, blaster.getId(), blasterCurrentCell, moveCell1);
                setCell(moveCell1);
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
            if (moveCell != null && flag) {
                Utility.move(world, blaster.getId(), blasterCurrentCell, moveCell);
                setCell(moveCell);
                return;
            }
        }
    }

    private void safeMove(boolean foreDupping, Cell blasterCurrentCell) {
        if (blasterCurrentCell.isInObjectiveZone()) {
            Hero[] blasters = getBelasters(world, blaster);
            boolean dupRange = blasters.length != 0;
            Vector<Hero> dupRangeH = new Vector<>(Arrays.asList(blasters));
            if (foreDupping && dupRange) {
                safeFromBlasters(dupRangeH);
            } else if (!foreDupping) {
                gotoGoal(blasterCurrentCell);
            }
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

    private void gotoGoal(Cell blasterCurrentCell) {
        Cell g1 = world.getMap().getCells()[ai.getRowGoal()][ai.getColGoal1()];
        Cell g2 = world.getMap().getCells()[ai.getRowGoal()][ai.getColGoal2()];
        if (Utility.distance(blasterCurrentCell, g1) == 0 ||
                Utility.distance(blasterCurrentCell, g2) == 0)
            return;
        Cell goal;
        if (ai.isRezerevd()) {
            if (ai.getWhoRezereved() == ai.getInProcess()) {
                goal = ai.getRezerevedCell();
                Utility.move(world, blaster, blasterCurrentCell, ai.getRezerevedCell());
                setCell(Utility.nextCell(world, blasterCurrentCell, ai.getRezerevedCell()));
            } else {
                if (ai.getRezerevedCell().getColumn() != ai.getColGoal1()) {
                    goal = g1;
//                    Utility.move(world, blaster, blasterCurrentCell, g1);
//                    setCell(Utility.nextCell(world, blasterCurrentCell, g1));
                } else {
                    goal = g2;
//                    Utility.move(world, blaster, blasterCurrentCell, g2);
//                    setCell(Utility.nextCell(world, blasterCurrentCell, g2));
                }
            }
        } else {
            Cell minGoalFromMe = world.manhattanDistance(blasterCurrentCell, g1) < world.manhattanDistance(blasterCurrentCell, g2) ? g1 : g2;
            boolean nearestToMe = world.manhattanDistance(blasterCurrentCell, minGoalFromMe) <
                    world.manhattanDistance(currentCell(ai.getBlaster()), minGoalFromMe);
            if (nearestToMe) {
                goal = minGoalFromMe;
//                Utility.move(world, blaster, blasterCurrentCell, minGoalFromMe);
//                setCell(Utility.nextCell(world, blasterCurrentCell, minGoalFromMe));
//                ai.setRezereved(minGoalFromMe);
            } else {
                Cell g = minGoalFromMe.getColumn() == g1.getColumn() ? g2 : g1;
                goal = g;
//                Utility.move(world, blaster, blasterCurrentCell, g);
//                setCell(Utility.nextCell(world, blasterCurrentCell, g));
//                ai.setRezereved(g);
            }
        }
        Cell next = Utility.nextCell(world, blasterCurrentCell, goal);
        if (Utility.getGuardians(world, next).length != 0)
            return;
        Utility.move(world, blaster, blasterCurrentCell, next);
        setCell(next);
        ai.setRezereved(goal);
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

    private void dodgeToNearestEnemy() {
        Cell blasterCell = blaster.getCurrentCell();
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
        setCell(world.getMap().getCell(row, col));
    }


    /**
     * hero'ii k behesh attack mizane ro set mikone tu AI v ag dodge bzne cell ro set mikone mikone
     */
    public void attack(boolean forceDupping) {
        if (blaster.getCurrentHP() == 0)
            return;
        AbilityName abilityName = blaster.getAbility(AbilityName.BLASTER_BOMB).isReady() ?
                AbilityName.BLASTER_BOMB :
                AbilityName.BLASTER_ATTACK;
        Hero[] inMyAttckRange = Utility.getInAttackRange(world, blaster, abilityName);
        Cell blasterCell = blaster.getCurrentCell();
        if (inMyAttckRange.length == 0) {
            int row;
            int col;
            if (blaster.getCurrentCell().isInObjectiveZone() && forceDupping) {
                dodgeToNearestEnemy();
            } else if (blaster.getAbility(AbilityName.BLASTER_DODGE).isReady()
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
            if (Utility.distance(blasterCell, whereShouldIAttack) > (abilityName == AbilityName.BLASTER_BOMB ? 5 : 4)) {
                Logger.log(String.format("ID : %d, [%d,%d]-->[%d,%d], inRangeLen : %d ",
                        blaster.getId(), blasterCell.getRow(), blasterCell.getColumn(),
                        whereShouldIAttack.getRow(), whereShouldIAttack.getColumn(),
                        inMyAttckRange.length) + abilityName.name(),
                        Logger.FULL_SOFT_GREEN);
            } else {
                String msg = String.format(abilityName.name() + " [%d,%d] --> ", blasterCell.getRow(), blasterCell.getColumn());
                for (int i = 0; i < inMyAttckRange.length; i++) {
                    msg += String.format("[%d,%d] ", inMyAttckRange[i].getCurrentCell().getRow(),
                            inMyAttckRange[i].getCurrentCell().getColumn());
                }
                msg += String.format("res [%d,%d]", whereShouldIAttack.getRow(), whereShouldIAttack.getColumn());
                System.out.println(msg);
            }
            world.castAbility(blaster.getId(), abilityName, whereShouldIAttack);
            ai.setInAttack(blaster, world.getOppHero(whereShouldIAttack));
        }
    }

    /**
     * hero haii k tu rangesh hastan ro migire v tasmim migire kodum cdell baraye attack bhtre v un ro barmigardune
     */
    private Cell getBestForBlasterAttack(Hero[] inMyAttckRange, AbilityName abilityName) {
        Cell blasterCell = blaster.getCurrentCell();
        if (inMyAttckRange.length == 1)
            return minCellFrom(blasterCell, abilityName == AbilityName.BLASTER_BOMB ? 5 : 4, inMyAttckRange[0].getCurrentCell());
        if (abilityName == AbilityName.BLASTER_BOMB
                && inMyAttckRange.length == 2) {
            //2 ta hastan v ability = BOMB
            if (Utility.distance(inMyAttckRange[0].getCurrentCell(), inMyAttckRange[1].getCurrentCell())
                    <= 2 * Utility_Attack.radius_of_blaster_bomb) {
                // mishe zad beyeneshun
                return minCellFrom(blasterCell, 5, inMyAttckRange);
            } else { // nmishe zad beyneshun
                /// vase hamin mizanm be uni k junesh kamtre
                Utility.sortOnHP(inMyAttckRange);
                if (Utility.distance(inMyAttckRange[0].getCurrentCell(), blasterCell) <= 5)
                    //ag tu range bomb hast
                    return inMyAttckRange[0].getCurrentCell();
                else {
                    //tu range bomb nis va bayad bznm ru marz k effectsh bhsh berese
                    return minCellFrom(blasterCell, 5, inMyAttckRange[0].getCurrentCell());
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
                return minCellFrom(blasterCell, 5, inMyAttckRange);
            else {
                Utility.sortOnHP(inMyAttckRange);
                return minCellFrom(blasterCell, 5, inMyAttckRange[0].getCurrentCell());
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
                return minCellFrom(blasterCell, 5, inMyAttckRange[0].getCurrentCell());
            }
        } else if (abilityName == AbilityName.BLASTER_ATTACK) {
            // in vase attack zadane
            if (inMyAttckRange.length == 2) {
                if (Utility.distance(inMyAttckRange[1].getCurrentCell(), inMyAttckRange[0].getCurrentCell())
                        <= 2 * Utility.BLASTER_ATTACK_RADIUS) {
                    return minCellFrom(blasterCell, 4, inMyAttckRange);
                } else { // dota hastan ama faselashun bishtare 2 hast
                    Utility.sortOnHP(inMyAttckRange);
                    return minCellFrom(blasterCell, 4, inMyAttckRange[0].getCurrentCell());
                }
            } else if (inMyAttckRange.length == 3) { // 3 nfr hastan
                if (Utility.distance(inMyAttckRange[0].getCurrentCell(), inMyAttckRange[1].getCurrentCell())
                        <= 2 * Utility.BLASTER_ATTACK_RADIUS &&
                        Utility.distance(inMyAttckRange[0].getCurrentCell(), inMyAttckRange[2].getCurrentCell())
                                <= 2 * Utility.BLASTER_ATTACK_RADIUS &&
                        Utility.distance(inMyAttckRange[1].getCurrentCell(), inMyAttckRange[2].getCurrentCell())
                                <= 2 * Utility.BLASTER_ATTACK_RADIUS) { // fasele in seta 2 b 2 2 has, pas mishe zad vasateshun
                    return minCellFrom(blasterCell, 4, inMyAttckRange);
                } else {
                    Utility.sortOnHP(inMyAttckRange);
                    return minCellFrom(blasterCell, 4, inMyAttckRange[0].getCurrentCell());
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
                    return minCellFrom(blasterCell, 4, inMyAttckRange);
                } else {
                    Utility.sortOnHP(inMyAttckRange);
                    return minCellFrom(blasterCell, 4, inMyAttckRange[0].getCurrentCell());
                }
            }
        }
        Utility.sortOnDistance(blasterCell, inMyAttckRange);
        return inMyAttckRange[0].getCurrentCell();
    }

    private Cell vasateshun(World world, Cell start, Cell end) {
        System.out.println(String.format("[%d,%d] --> [%d,%d],[%d,%d]",
                blaster.getCurrentCell().getRow(), blaster.getCurrentCell().getColumn(),
                start.getRow(), start.getColumn(),
                end.getRow(), end.getColumn()));

        return vasateshun(world, start, end, end);
    }
    private Cell vasateshun(World world, Cell start, Cell mid, Cell end) {
        if (!mid.equals(end))
            System.out.println(String.format("[%d,%d] --> [%d,%d],[%d,%d],[%d,%d]",
                    blaster.getCurrentCell().getRow(), blaster.getCurrentCell().getColumn(),
                    start.getRow(), start.getColumn(),
                    mid.getRow(), mid.getColumn(),
                    end.getRow(), end.getColumn()));

        return vasateshun(world, start, mid, end, end);
    }
    private Cell vasateshun(World world, Cell start, Cell mid, Cell mid2, Cell end) {
        Cell DOWN = Utility.getDOWN(start, Utility.getDOWN(mid, Utility.getDOWN(mid2, end)));
        Cell UP = Utility.getUP(start, Utility.getUP(mid, Utility.getUP(mid2, end)));
        Cell LEFT = Utility.getLEFT(start, Utility.getLEFT(mid, Utility.getLEFT(mid2, end)));
        Cell RIGHT = Utility.getRIGHT(start, Utility.getRIGHT(mid, Utility.getRIGHT(mid2, end)));
        if (!end.equals(mid2))
            System.out.println(String.format("[%d,%d] --> [%d,%d],[%d,%d],[%d,%d]",
                    blaster.getCurrentCell().getRow(), blaster.getCurrentCell().getColumn(),
                    start.getRow(), start.getColumn(),
                    mid.getRow(), mid.getColumn(),
                    mid2.getRow(), mid2.getColumn(),
                    end.getRow(), end.getColumn()));
        return world.getMap().getCell((UP.getRow() + DOWN.getRow()) / 2, (LEFT.getColumn() + RIGHT.getColumn()) / 2);
    }

    private Cell minCellFrom(Cell from, int range, Cell to) {
        return minCellFrom(from, range, new Cell[]{to});
    }

    private Cell minCellFrom(Cell from, int radius, Cell... to) {
        Cell[] avi = Utility.availableCells(world.getMap(), radius, from);
        int minDis = Integer.MAX_VALUE;
        Cell min = avi[0];
        for (Cell cell : avi) {
            if (to.length == 1) {
                int tmp = Utility.distance(cell, to[0]);
                if (tmp < minDis) {
                    minDis = tmp;
                    min = cell;
                }
            } else if (to.length == 2) {
                int tmp1 = Utility.distance(cell, to[0]);
                int tmp2 = Utility.distance(cell, to[1]);
                if (tmp1 < minDis
                        && tmp2 < minDis) {
                    minDis = Math.max(tmp1, tmp2);
                    min = cell;
                }
            } else if (to.length == 3) {
                int tmp1 = Utility.distance(cell, to[0]);
                int tmp2 = Utility.distance(cell, to[1]);
                int tmp3 = Utility.distance(cell, to[2]); // fixed this
                if (tmp1 < minDis
                        && tmp2 < minDis
                        && tmp3 < minDis) {
                    minDis = Math.max(tmp1, Math.max(tmp2, tmp3));
                    min = cell;
                }
            }
        }
        return min;
    }

    private Cell minCellFrom(Cell from, int radius, Hero[] hs) {
        Cell[] cells = new Cell[hs.length];
        for (int i = 0; i < hs.length; i++) {
            cells[i] = hs[i].getCurrentCell();
        }
        return minCellFrom(from, radius, cells);
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
