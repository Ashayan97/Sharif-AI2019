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


    public Sentry_AI(Hero hero, World world) {
        if (world == null)
            throw new RuntimeException("NULL WORLD");
        this.hero = hero;
        this.world = world;
        this.map = world.getMap();
        rangeFight = new Range_fight(world);
        heroes = world.getMyHeroes();
    }

    public boolean canAtk() {
        Hero[] heroes = rangeFight.InRangeAtk(hero, 7);
        for (Hero heroe : heroes) {

            return world.isInVision(heroe.getCurrentCell(), hero.getCurrentCell());
        }
        return false;
    }

    public void actionPhase() {

        Hero[] inVision = rangeFight.inVisionEnemy(hero);
        Hero[] heroes = rangeFight.InRangeAtk(hero, 7);
        if (hero.getAbility(AbilityName.SENTRY_ATTACK).getAPCost() <= world.getAP()) {
            for (int i = 0; i < heroes.length; i++) {
                if (heroes[i].getCurrentHP() - hero.getAbility(AbilityName.SENTRY_ATTACK).getPower() <= 0 && world.isInVision(hero.getCurrentCell(), heroes[i].getCurrentCell())) {
                    world.castAbility(hero, AbilityName.SENTRY_ATTACK, heroes[i].getCurrentCell());
                    return;
                }
            }
        }
        if (hero.getAbility(AbilityName.SENTRY_RAY).getAPCost() <= world.getAP()) {
            if (hero.getAbility(AbilityName.SENTRY_RAY).isReady())
                for (int i = 0; i < inVision.length; i++) {
                    if (inVision[i].getCurrentHP() - hero.getAbility(AbilityName.SENTRY_RAY).getPower() <= 0 && world.isInVision(hero.getCurrentCell(), heroes[i].getCurrentCell())) {
                        if (inVision[i].getName().equals(HeroName.GUARDIAN)) {
                            if (!inVision[i].getAbility(AbilityName.GUARDIAN_FORTIFY).isReady()) {
                                world.castAbility(hero, AbilityName.SENTRY_RAY, inVision[i].getCurrentCell());
                                return;
                            }
                        } else {
                            world.castAbility(hero, AbilityName.SENTRY_RAY, inVision[i].getCurrentCell());
                            return;
                        }
                    }
                }
        }
        if (hero.getAbility(AbilityName.SENTRY_DODGE).getAPCost() <= world.getAP()) {
            if (needToDodge() || !canAtk()) {
                Cell Des = rangeFight.bestDodge(hero.getCurrentCell(), 3, 6);
                if (!hero.getCurrentCell().equals(Des)) {
                    world.castAbility(hero, AbilityName.SENTRY_DODGE, Des);
                    return;
                }
            }
        }
        if (hero.getAbility(AbilityName.SENTRY_RAY).getAPCost() <= world.getAP()) {
            if (hero.getAbility(AbilityName.SENTRY_RAY).isReady()) {
                for (int i = 0; i < inVision.length; i++) {
                    if (inVision[i].getName().equals(HeroName.SENTRY) && world.isInVision(hero.getCurrentCell(), inVision[i].getCurrentCell())) {
                        world.castAbility(hero, AbilityName.SENTRY_RAY, inVision[i].getCurrentCell());
                        return;
                    }
                }
                for (int i = 0; i < inVision.length; i++) {
                    if (inVision[i].getName().equals(HeroName.BLASTER) && world.isInVision(hero.getCurrentCell(), inVision[i].getCurrentCell())) {
                        world.castAbility(hero, AbilityName.SENTRY_RAY, inVision[i].getCurrentCell());
                        return;
                    }
                }
                for (int i = 0; i < inVision.length; i++) {
                    if (inVision[i].getName().equals(HeroName.GUARDIAN))
                        if (!inVision[i].getAbility(AbilityName.GUARDIAN_FORTIFY).isReady() && world.isInVision(hero.getCurrentCell(), inVision[i].getCurrentCell())) {
                            world.castAbility(hero, AbilityName.SENTRY_RAY, inVision[i].getCurrentCell());
                            return;
                        }
                }
                for (int i = 0; i < inVision.length; i++) {
                    if (inVision[i].getName().equals(HeroName.HEALER) && world.isInVision(hero.getCurrentCell(), inVision[i].getCurrentCell())) {
                        world.castAbility(hero, AbilityName.SENTRY_RAY, inVision[i].getCurrentCell());
                        return;
                    }
                }
            }
        }
        if (hero.getAbility(AbilityName.SENTRY_ATTACK).getAPCost() <= world.getAP()) {
            Hero inAtk = null;
            for (int i = 0; i < heroes.length; i++) {
                if (inAtk == null && world.isInVision(hero.getCurrentCell(), heroes[i].getCurrentCell()))// && rangeFight.isInVision(hero, heroes[i]))
                    inAtk = heroes[i];
                else if (inAtk != null)
                    if (inAtk.getCurrentHP() >= heroes[i].getCurrentHP() && world.isInVision(hero.getCurrentCell(), heroes[i].getCurrentCell()))
                        inAtk = heroes[i];
            }
            if (inAtk != null)
                world.castAbility(hero, AbilityName.SENTRY_ATTACK, inAtk.getCurrentCell());
        }

    }


    public boolean isNeedToMove() {
        Hero[] heroes = rangeFight.InRangeAtk(hero, 6);
        if (heroes.length == 0)
            return true;
        else return false;
    }

    public boolean needToDodge() {
        Hero[] heroes = rangeFight.InRangeAtk(hero, 7);
        Hero[] inVision = rangeFight.inVisionEnemy(hero);
        for (int i = 0; i < heroes.length; i++) {
            if (heroes[i].getName().equals(HeroName.BLASTER))
                if (world.manhattanDistance(heroes[i].getCurrentCell(), hero.getCurrentCell()) <= Blaster_Danger_Range)
                    return true;
            if (heroes[i].getName().equals(HeroName.GUARDIAN))
                if (world.manhattanDistance(heroes[i].getCurrentCell(), hero.getCurrentCell()) <= Guardian_Danger_Range)
                    return true;
            if (heroes[i].getName().equals(HeroName.HEALER))
                if (world.manhattanDistance(heroes[i].getCurrentCell(), hero.getCurrentCell()) <= Healer_Danger_Range)
                    return true;
            if (heroes[i].getName().equals(HeroName.SENTRY))
                if (world.manhattanDistance(heroes[i].getCurrentCell(), hero.getCurrentCell()) <= Sentry_Danger_Range)
                    return true;
            for (int j = 0; j < inVision.length; j++) {
                if (inVision[i].getName().equals(HeroName.SENTRY) && inVision[i].getAbility(AbilityName.SENTRY_RAY).isReady())
                    return true;
            }

        }
        return false;
    }

    public void SentryMove() {
        Hero[] heroes = world.getOppHeroes();
        if (atkMode && heroes.length != 0) {
            Cell des = rangeFight.SingleToSingleAtkRange(hero, 7, rangeFight.NearstEnemy(hero.getCurrentCell(), heroes), 0)[0];
            Direction[] dir = world.getPathMoveDirections(hero.getCurrentCell(), des);
            world.moveHero(hero, dir[0]);

        } else {
            if (rangeFight.isSafe(hero, 6)) {
                if (!hero.getCurrentCell().isInObjectiveZone()) {
                    Direction dir = ObjectMove();
                    if (dir != null)
                        world.moveHero(hero, dir);
                    return;
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
            cells[i] = heroes[i].getCurrentCell();
        }
        Cell des = rangeFight.findNearestZoneCell(hero.getCurrentCell());
        Cell next = Utility.nextCell(world, hero.getCurrentCell(), des, cells);
        if (rangeFight.isSafe(next, 6)) {
            Direction[] dir = world.getPathMoveDirections(hero.getCurrentCell(), des, cells);
            return dir[0];
        } else
            return null;
    }

    private Direction EscapeDirection(Hero[] inRange) {
        Direction bestMove = null;
        float maxDistance = rangeFight.avgDistance(inRange, hero.getCurrentCell());
        int disOfZone = world.manhattanDistance(hero.getCurrentCell(), rangeFight.findNearestZoneCell(hero.getCurrentCell()));
        if (!map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()).isWall() &&
                rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())) > maxDistance &&
                map.isInMap(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()) &&
                world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()))) < disOfZone
        ) {
            bestMove = Direction.UP;
            maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()));
            disOfZone = world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())));
        }

        if (!map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()).isWall() &&
                rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())) > maxDistance &&
                map.isInMap(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()) &&
                world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()))) < disOfZone
        ) {
            bestMove = Direction.DOWN;
            maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()));
            disOfZone = world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())));

        }

        if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1).isWall() &&
                rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)) > maxDistance &&
                map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1) &&
                world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1))) < disOfZone
        ) {
            bestMove = Direction.LEFT;
            maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1));
            disOfZone = world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)));
        }
        if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1).isWall() &&
                rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1)) > maxDistance &&
                map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1) &&
                world.manhattanDistance(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1), rangeFight.findNearestZoneCell(map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1))) < disOfZone
        ) {
            bestMove = Direction.RIGHT;
        }
        return bestMove;
    }

    public Hero getHero() {
        return hero;
    }
}
