package client;

import client.model.*;

import java.util.ArrayList;

public class Sentry_AI {
    static private int Blaster_Danger_Range = 6;
    static private int Guardian_Danger_Range = 4;
    static private int Healer_Danger_Range = 5;
    static private int Sentry_Danger_Range = 3;

    ///////////
    private Hero hero;
    private World world;
    private Map map;
    private Range_fight rangeFight;
    private Hero[] heroes;
    private int AtkPriority = 0;
    private boolean needToMove = false;
    private boolean atkMode = false;
    private Hero lastAtkTo = null;
    private Hero[] inVision;
    private Hero[] inRangeAtkHeroes;
    private Hero[] ourHeroes;
    private Hero commonEnemy;
//    private Cell[] bestOfZone;

    public Sentry_AI(Hero hero, World world) {
        if (world == null)
            throw new RuntimeException("NULL WORLD");
        this.hero = hero;
        this.world = world;
        this.map = world.getMap();
        rangeFight = new Range_fight(world);
        heroes = rangeFight.inVisionEnemy(hero, 7);
        inVision = rangeFight.inVisionEnemy(hero);
        inRangeAtkHeroes = rangeFight.InRangeAtk(hero, 7);
        ourHeroes = world.getMyHeroes();
//        this.bestOfZone = bestOfZone;

//        heroes = world.getOppHeroes();
    }


    public boolean canAtk() {
        for (Hero heroe : heroes) {
            return world.isInVision(heroe.getCurrentCell(), hero.getCurrentCell());
        }
        return false;
    }

    public void actionPhase() {
//        Hero[] heroes = rangeFight.InRangeAtk(hero, 7);
        if (hero.getAbility(AbilityName.SENTRY_ATTACK).getAPCost() <= world.getAP()) {
            for (int i = 0; i < heroes.length; i++) {
                if (heroes[i].getCurrentHP() - hero.getAbility(AbilityName.SENTRY_ATTACK).getPower() <= 0 && world.isInVision(hero.getCurrentCell(), heroes[i].getCurrentCell())) {
                    world.castAbility(hero, AbilityName.SENTRY_ATTACK, heroes[i].getCurrentCell());
                    return;
                }
            }
        }
        if (hero.getAbility(AbilityName.SENTRY_RAY).getAPCost() <= world.getAP()) {
            if (hero.getAbility(AbilityName.SENTRY_RAY).isReady()) {
                for (int i = 0; i < inVision.length; i++) {
                    if (inVision[i].getCurrentHP() - hero.getAbility(AbilityName.SENTRY_RAY).getPower() <= 0) {
                        world.castAbility(hero, AbilityName.SENTRY_RAY, inVision[i].getCurrentCell());
                        return;
                    }
                }

            }
        }
        if (hero.getAbility(AbilityName.SENTRY_DODGE).getAPCost() <= world.getAP()) {
            if (needToDodge()) {
                if (hero.getAbility(AbilityName.SENTRY_DODGE).isReady()) {
                    Cell Des = rangeFight.bestDodge(hero.getCurrentCell(), 3, 6);
                    if (!hero.getCurrentCell().equals(Des)) {
                        world.castAbility(hero, AbilityName.SENTRY_DODGE, Des);
                        return;
                    }
                }
            }
        }
        if (hero.getAbility(AbilityName.SENTRY_RAY).getAPCost() <= world.getAP()) {
            if (hero.getAbility(AbilityName.SENTRY_RAY).isReady()) {
                for (Hero anInVision3 : inVision) {
                    if (anInVision3.getName().equals(HeroName.SENTRY)) {
                        world.castAbility(hero, AbilityName.SENTRY_RAY, anInVision3.getCurrentCell());
                        return;
                    }
                }
                for (Hero anInVision2 : inVision) {
                    if (anInVision2.getName().equals(HeroName.BLASTER)) {
                        world.castAbility(hero, AbilityName.SENTRY_RAY, anInVision2.getCurrentCell());
                        return;
                    }
                }
                for (Hero anInVision1 : inVision) {
                    if (anInVision1.getName().equals(HeroName.GUARDIAN))
                        world.castAbility(hero, AbilityName.SENTRY_RAY, anInVision1.getCurrentCell());
                    return;

                }
                for (Hero anInVision : inVision) {
                    if (anInVision.getName().equals(HeroName.HEALER)) {
                        world.castAbility(hero, AbilityName.SENTRY_RAY, anInVision.getCurrentCell());
                        return;
                    }
                }
            }
        }
        if (hero.getAbility(AbilityName.SENTRY_ATTACK).getAPCost() <= world.getAP()) {
            Hero inAtk = null;
            for (Hero heroe : heroes) {
                if (inAtk == null)// && rangeFight.isInVision(hero, heroes[i]))
                    inAtk = heroe;
                else if (inAtk.getCurrentHP() >= heroe.getCurrentHP())
                    inAtk = heroe;
            }
            if (inAtk != null)
                world.castAbility(hero, AbilityName.SENTRY_ATTACK, inAtk.getCurrentCell());
        }
    }

    public void setCommonEnemy(Hero commonEnemy) {
        this.commonEnemy = commonEnemy;
    }

    public boolean isNeedToMove() {
        return inRangeAtkHeroes.length == 0;
    }

    public boolean needToDodge() {
        int counter = 0;
//        Hero[] inRangeAtkHeroes = rangeFight.InRangeAtk(hero, 7);
        //Hero[] inVision = rangeFight.inVisionEnemy(hero);
        for (Hero inRangeAtkHeroe : inRangeAtkHeroes) {
            if (counter == 2)
                return true;
            if (inRangeAtkHeroe.getName().equals(HeroName.BLASTER)) {
                counter++;
                if (world.manhattanDistance(inRangeAtkHeroe.getCurrentCell(), hero.getCurrentCell()) <= Blaster_Danger_Range)
                    return true;
            }
            if (inRangeAtkHeroe.getName().equals(HeroName.GUARDIAN))
                if (world.manhattanDistance(inRangeAtkHeroe.getCurrentCell(), hero.getCurrentCell()) <= Guardian_Danger_Range)
                    return true;
            if (inRangeAtkHeroe.getName().equals(HeroName.HEALER))
                if (world.manhattanDistance(inRangeAtkHeroe.getCurrentCell(), hero.getCurrentCell()) <= Healer_Danger_Range)
                    return true;
            if (inRangeAtkHeroe.getName().equals(HeroName.SENTRY))
                if (world.manhattanDistance(inRangeAtkHeroe.getCurrentCell(), hero.getCurrentCell()) <= Sentry_Danger_Range)
                    return true;
//            for (int j = 0; j < inVision.length; j++) {
//                if (inVision[i].getName().equals(HeroName.SENTRY) && inVision[i].getAbility(AbilityName.SENTRY_RAY).isReady())
//                    return true;
//            }

        }
        return false;
    }

    public void SentryMove() {
        if (hero.getCurrentHP() == 0)
            return;
        if (atkMode || (heroes.length == 0 && hero.getCurrentCell().isInObjectiveZone())) {
            Cell des = rangeFight.NearstEnemy(hero.getCurrentCell(), world.getOppHeroes());

            if (!atkMode) {
                Hero[] enemyHero = world.getOppHeroes();
                //Cell[] blockCell=rangeFight.cellsOfArea(rangeFight.NearstEnemy(hero.getCurrentCell(),enemyHero),6);
                if (des.isInObjectiveZone()) {
                    Direction[] dir = world.getPathMoveDirections(hero.getCurrentCell(), des);
                    world.moveHero(hero, dir[0]);
                }
            } else {
                Hero[] enemyHero = world.getOppHeroes();
                Cell[] blockCell = rangeFight.cellsOfArea(rangeFight.NearstEnemy(hero.getCurrentCell(), enemyHero), 6);
                Direction[] dir = world.getPathMoveDirections(hero.getCurrentCell(), commonEnemy.getCurrentCell(), blockCell);
                world.moveHero(hero, dir[0]);
            }

        } else if (heroes.length == 0 && inRangeAtkHeroes.length != 0 && !hero.getCurrentCell().isInObjectiveZone()) {
            Direction[] dir = world.getPathMoveDirections(hero.getCurrentCell(), rangeFight.bestInVision(hero, rangeFight.NearstEnemy(hero.getCurrentCell(), inRangeAtkHeroes), 7));
            world.moveHero(hero, dir[0]);
        } else {
            if (rangeFight.isSafe(hero, 6)||rangeFight.notInEnemyVision(hero,world.getOppHeroes())) {
                if (!hero.getCurrentCell().isInObjectiveZone()) {
                    Direction dir = ObjectMove();
                    if (dir != null)
                        world.moveHero(hero, dir);
//                    return;
                }
            } else {
//                if (!isNeedToMove())
//                    if (!hero.getCurrentCell().isInObjectiveZone()) {
//                        Direction dir = ObjectMove();
//                        if (dir != null)
//                            world.moveHero(hero, dir);
//                        return;
//                    }
                world.moveHero(hero, EscapeDirection(rangeFight.InRangeAtk(hero, 7)));
            }
        }

    }

    public void setAtkMode(boolean atkMode) {
        this.atkMode = atkMode;
    }

    private Direction ObjectMove() {
        Cell[] cells = new Cell[4];
        for (int i = 0; i < 4; i++) {
            cells[i] = ourHeroes[i].getCurrentCell();
        }
        Cell des = rangeFight.findNearestZoneCell(hero.getCurrentCell());
        Cell next = Utility.nextCell(world, hero.getCurrentCell(), des, cells);
        if (rangeFight.isSafe(next, 6)) {
            Direction[] dir = world.getPathMoveDirections(hero.getCurrentCell(), des, cells);
            return dir[0];
        } else
            return null;
    }

    private boolean isBlaster() {
        for (Hero inRangeAtkHeroe : inRangeAtkHeroes) {
            if (inRangeAtkHeroe.getName().equals(HeroName.BLASTER))
                return true;
        }

        return false;
    }

    private Direction EscapeDirection(Hero[] inRange) {
        Direction bestMove = null;
        Cell nearest = rangeFight.NearstEnemy(hero.getCurrentCell(), inRangeAtkHeroes);
        float maxDistance = rangeFight.avgDistance(inRange, hero.getCurrentCell());
        Hero[] ourInRange = rangeFight.InRangeAtk(hero, 4, ourHeroes);
        float ourInrangeAvg = rangeFight.avgDistance(ourInRange, hero.getCurrentCell());
        if (isBlaster() && ourInrangeAvg > 0) {
            int disOfZone = world.manhattanDistance(hero.getCurrentCell(), rangeFight.findNearestZoneCell(hero.getCurrentCell()));
            if (!map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()).isWall() &&
                    world.isInVision(map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()), nearest) &&
                    rangeFight.avgDistance(ourHeroes, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())) > ourInrangeAvg &&
                    rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())) > maxDistance &&
                    map.isInMap(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()) &&
                    world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()))) <= disOfZone
            ) {
                bestMove = Direction.UP;
                maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()));
                disOfZone = world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())));
                ourInrangeAvg = rangeFight.avgDistance(ourHeroes, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()));
            }

            if (!map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()).isWall() &&
                    rangeFight.avgDistance(ourHeroes, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())) > ourInrangeAvg &&
                    world.isInVision(map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()), nearest) &&
                    rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())) > maxDistance &&
                    map.isInMap(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()) &&
                    world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()))) <= disOfZone
            ) {
                bestMove = Direction.DOWN;
                maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()));
                disOfZone = world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())));
                ourInrangeAvg = rangeFight.avgDistance(ourHeroes, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()));

            }

            if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1).isWall() &&
                    world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), nearest) &&
                    rangeFight.avgDistance(ourHeroes, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)) > ourInrangeAvg &&
                    rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)) > maxDistance &&
                    map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1) &&
                    world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1))) <= disOfZone
            ) {
                bestMove = Direction.LEFT;
                maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1));
                disOfZone = world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)));
                ourInrangeAvg = rangeFight.avgDistance(ourHeroes, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1));

            }
            if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1).isWall() &&
                    world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1), nearest) &&
                    rangeFight.avgDistance(ourHeroes, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1)) > ourInrangeAvg &&
                    rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1)) > maxDistance &&
                    map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1) &&
                    world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1))) <= disOfZone
            ) {
                bestMove = Direction.RIGHT;
            }
            if (bestMove != null)
                return bestMove;
            else {
                if (!map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()).isWall() &&
                        world.isInVision(map.getCell(hero.getCurrentCell().getRow()-1, hero.getCurrentCell().getColumn() ), nearest) &&
                        rangeFight.avgDistance(ourHeroes, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())) > ourInrangeAvg &&
                        rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())) > maxDistance &&
                        map.isInMap(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())
                ) {
                    bestMove = Direction.UP;
                    maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()));
                    ourInrangeAvg = rangeFight.avgDistance(ourHeroes, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()));

                }

                if (!map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()).isWall() &&
                        world.isInVision(map.getCell(hero.getCurrentCell().getRow()+1, hero.getCurrentCell().getColumn() ), nearest) &&
                        rangeFight.avgDistance(ourHeroes, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())) > ourInrangeAvg &&
                        rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())) > maxDistance &&
                        map.isInMap(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())
                ) {
                    bestMove = Direction.DOWN;
                    maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()));
                    ourInrangeAvg = rangeFight.avgDistance(ourHeroes, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()));
                }

                if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1).isWall() &&
                        world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), nearest) &&
                        rangeFight.avgDistance(ourHeroes, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)) > ourInrangeAvg &&
                        rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)) > maxDistance &&
                        map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)
                ) {
                    bestMove = Direction.LEFT;
                    maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1));
                    ourInrangeAvg = rangeFight.avgDistance(ourHeroes, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1));

                }
                if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1).isWall() &&
                        world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1), nearest) &&
                        rangeFight.avgDistance(ourHeroes, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())) > ourInrangeAvg &&
                        rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1)) > maxDistance &&
                        map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1)
                ) {
                    bestMove = Direction.RIGHT;
                }
                if (bestMove != null)
                    return bestMove;
                else {
                    if (!map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()).isWall() &&
                            rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())) > maxDistance &&
                            world.isInVision(map.getCell(hero.getCurrentCell().getRow()-1, hero.getCurrentCell().getColumn() ), nearest) &&
                            map.isInMap(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()) &&
                            world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()))) <= disOfZone
                    ) {
                        bestMove = Direction.UP;
                        maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()));
                        disOfZone = world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())));
                    }

                    if (!map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()).isWall() &&
                            world.isInVision(map.getCell(hero.getCurrentCell().getRow()+1, hero.getCurrentCell().getColumn() ), nearest) &&
                            rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())) > maxDistance &&
                            map.isInMap(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()) &&
                            world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()))) <= disOfZone
                    ) {
                        bestMove = Direction.DOWN;
                        maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()));
                        disOfZone = world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())));

                    }

                    if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1).isWall() &&
//                            world.isInVision(map.getCell(hero.getCurrentCell().getRow()-1, hero.getCurrentCell().getColumn() ), nearest) &&
//                            world.isInVision(map.getCell(hero.getCurrentCell().getRow()+1, hero.getCurrentCell().getColumn() ), nearest) &&
                            world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), nearest) &&
//                            world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1), nearest) &&
                            rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)) > maxDistance &&
                            map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1) &&
                            world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1))) <= disOfZone
                    ) {
                        bestMove = Direction.LEFT;
                        maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1));
                        disOfZone = world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)));
                    }
                    if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1).isWall() &&
//                            world.isInVision(map.getCell(hero.getCurrentCell().getRow()-1, hero.getCurrentCell().getColumn() ), nearest) &&
//                            world.isInVision(map.getCell(hero.getCurrentCell().getRow()+1, hero.getCurrentCell().getColumn() ), nearest) &&
//                            world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), nearest) &&
                            world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1), nearest) &&
                            rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1)) > maxDistance &&
                            map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1) &&
                            world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1))) <= disOfZone
                    ) {
                        bestMove = Direction.RIGHT;
                    }
                    if (bestMove != null)
                        return bestMove;
                    else {
                        if (!map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()).isWall() &&
                                world.isInVision(map.getCell(hero.getCurrentCell().getRow()-1, hero.getCurrentCell().getColumn() ), nearest) &&
//                                world.isInVision(map.getCell(hero.getCurrentCell().getRow()+1, hero.getCurrentCell().getColumn() ), nearest) &&
//                                world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), nearest) &&
//                                world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1), nearest) &&
                                rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())) > maxDistance &&
                                map.isInMap(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())
                        ) {
                            bestMove = Direction.UP;
                            maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()));

                        }

                        if (!map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()).isWall() &&
//                                world.isInVision(map.getCell(hero.getCurrentCell().getRow()-1, hero.getCurrentCell().getColumn() ), nearest) &&
                                world.isInVision(map.getCell(hero.getCurrentCell().getRow()+1, hero.getCurrentCell().getColumn() ), nearest) &&
//                                world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), nearest) &&
//                                world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1), nearest) &&
                                rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())) > maxDistance &&
                                map.isInMap(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())
                        ) {
                            bestMove = Direction.DOWN;
                            maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()));
                        }

                        if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1).isWall() &&
//                                world.isInVision(map.getCell(hero.getCurrentCell().getRow()-1, hero.getCurrentCell().getColumn() ), nearest) &&
//                                world.isInVision(map.getCell(hero.getCurrentCell().getRow()+1, hero.getCurrentCell().getColumn() ), nearest) &&
                                world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), nearest) &&
//                                world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1), nearest) &&
                                rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)) > maxDistance &&
                                map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)
                        ) {
                            bestMove = Direction.LEFT;
                            maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1));

                        }
                        if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1).isWall() &&
//                                world.isInVision(map.getCell(hero.getCurrentCell().getRow()-1, hero.getCurrentCell().getColumn() ), nearest) &&
//                                world.isInVision(map.getCell(hero.getCurrentCell().getRow()+1, hero.getCurrentCell().getColumn() ), nearest) &&
//                                world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), nearest) &&
                                world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1), nearest) &&
                                rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1)) > maxDistance &&
                                map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1)
                        ) {
                            bestMove = Direction.RIGHT;
                        }
                        if (bestMove != null)
                            return bestMove;
                        else {

                            if (!map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()).isWall() &&
                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow()-1, hero.getCurrentCell().getColumn() ), nearest) &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow()+1, hero.getCurrentCell().getColumn() ), nearest) &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), nearest) &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1), nearest) &&
                                    rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())) == maxDistance &&
                                    map.isInMap(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())
                            ) {
                                bestMove = Direction.UP;
                                maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()));
                            }

                            if (!map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()).isWall() &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow()-1, hero.getCurrentCell().getColumn() ), nearest) &&
                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow()+1, hero.getCurrentCell().getColumn() ), nearest) &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), nearest) &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1), nearest) &&
                                    rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())) == maxDistance &&
                                    map.isInMap(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())
                            ) {
                                bestMove = Direction.DOWN;
                                maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()));
                            }

                            if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1).isWall() &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow()-1, hero.getCurrentCell().getColumn() ), nearest) &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow()+1, hero.getCurrentCell().getColumn() ), nearest) &&
                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), nearest) &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1), nearest) &&
                                    rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)) == maxDistance &&
                                    map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)
                            ) {
                                bestMove = Direction.LEFT;
                                maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1));
                            }
                            if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1).isWall() &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow()-1, hero.getCurrentCell().getColumn() ), nearest) &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow()+1, hero.getCurrentCell().getColumn() ), nearest) &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), nearest) &&
                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1), nearest) &&
                                    rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1)) == maxDistance &&
                                    map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1)
                            ) {
                                bestMove = Direction.RIGHT;
                            }
                        }
                        if (bestMove != null)
                            return bestMove;
                        else {

                            if (!map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()).isWall() &&
                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow()-1, hero.getCurrentCell().getColumn() ), nearest) &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow()+1, hero.getCurrentCell().getColumn() ), nearest) &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), nearest) &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1), nearest) &&
                                    rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())) > maxDistance &&
                                    map.isInMap(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())
                            ) {
                                bestMove = Direction.UP;
                                maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()));
                            }

                            if (!map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()).isWall() &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow()-1, hero.getCurrentCell().getColumn() ), nearest) &&
                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow()+1, hero.getCurrentCell().getColumn() ), nearest) &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), nearest) &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1), nearest) &&
                                    rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())) > maxDistance &&
                                    map.isInMap(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())
                            ) {
                                bestMove = Direction.DOWN;
                                maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()));
                            }

                            if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1).isWall() &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow()-1, hero.getCurrentCell().getColumn() ), nearest) &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow()+1, hero.getCurrentCell().getColumn() ), nearest) &&
                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), nearest) &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1), nearest) &&
                                    rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)) > maxDistance &&
                                    map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)
                            ) {
                                bestMove = Direction.LEFT;
                                maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1));
                            }
                            if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1).isWall() &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow()-1, hero.getCurrentCell().getColumn() ), nearest) &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow()+1, hero.getCurrentCell().getColumn() ), nearest) &&
//                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), nearest) &&
                                    world.isInVision(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1), nearest) &&
                                    rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1)) > maxDistance &&
                                    map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1)
                            ) {
                                bestMove = Direction.RIGHT;
                            }
                        }
                    }
                }
                return bestMove;
            }
        } else {
////////////////////////////////////////////////////////////////////////////////////////////////

            int disOfZone = world.manhattanDistance(hero.getCurrentCell(), rangeFight.findNearestZoneCell(hero.getCurrentCell()));
            if (!map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()).isWall() &&
                    rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())) > maxDistance &&
                    map.isInMap(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()) &&
                    world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()))) <= disOfZone
            ) {
                bestMove = Direction.UP;
                maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()));
                disOfZone = world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())));
            }

            if (!map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()).isWall() &&
                    rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())) > maxDistance &&
                    map.isInMap(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()) &&
                    world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()))) <= disOfZone
            ) {
                bestMove = Direction.DOWN;
                maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()));
                disOfZone = world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())));

            }

            if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1).isWall() &&
                    rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)) > maxDistance &&
                    map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1) &&
                    world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1))) <= disOfZone
            ) {
                bestMove = Direction.LEFT;
                maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1));
                disOfZone = world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)));
            }
            if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1).isWall() &&
                    rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1)) > maxDistance &&
                    map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1) &&
                    world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1))) <= disOfZone
            ) {
                bestMove = Direction.RIGHT;
            }
            if (bestMove != null)
                return bestMove;
            else {
                if (!map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()).isWall() &&
                        rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())) > maxDistance &&
                        map.isInMap(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())
                ) {
                    bestMove = Direction.UP;
                    maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()));
                }

                if (!map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()).isWall() &&
                        rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())) > maxDistance &&
                        map.isInMap(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())
                ) {
                    bestMove = Direction.DOWN;
                    maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()));
                }

                if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1).isWall() &&
                        rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)) > maxDistance &&
                        map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)
                ) {
                    bestMove = Direction.LEFT;
                    maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1));
                }
                if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1).isWall() &&
                        rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1)) > maxDistance &&
                        map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1)
                ) {
                    bestMove = Direction.RIGHT;
                }
                if (bestMove != null)
                    return bestMove;
                else {

                    if (!map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()).isWall() &&
                            rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())) == maxDistance &&
                            map.isInMap(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())
                    ) {
                        bestMove = Direction.UP;
                        maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()));
                    }

                    if (!map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()).isWall() &&
                            rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())) == maxDistance &&
                            map.isInMap(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())
                    ) {
                        bestMove = Direction.DOWN;
                        maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()));
                    }

                    if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1).isWall() &&
                            rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)) == maxDistance &&
                            map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)
                    ) {
                        bestMove = Direction.LEFT;
                        maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1));
                    }
                    if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1).isWall() &&
                            rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1)) == maxDistance &&
                            map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1)
                    ) {
                        bestMove = Direction.RIGHT;
                    }
                }
                return bestMove;
            }
        }

    }


    public Hero getHero() {
        return hero;
    }
}
