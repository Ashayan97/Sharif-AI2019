package client;

import client.model.*;

import java.util.ArrayList;

public class Sentry_AI {
    static private int Blaster_Danger_Range = 5;
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
        if(world==null)
            throw new RuntimeException("NULL WORLD");
        this.hero = hero;
        this.world = world;
        this.map = world.getMap();
        rangeFight = new Range_fight(world);
        heroes = world.getMyHeroes();
    }

    public void actionPhase() {
        Hero[] inVision = rangeFight.inVisionEnemy(hero);
        Hero[] heroes = rangeFight.InRangeAtk(hero, 7);
        if (hero.getAbility(AbilityName.SENTRY_ATTACK).getAPCost() == world.getAP()) {
            for (int i = 0; i < heroes.length; i++) {
                if (heroes[i].getCurrentHP() - hero.getAbility(AbilityName.SENTRY_ATTACK).getPower() <= 0 && rangeFight.isInVision(hero,heroes[i])) {
                    world.castAbility(hero, AbilityName.SENTRY_ATTACK, heroes[i].getCurrentCell());
                    return;
                }
            }
        }
        if (hero.getAbility(AbilityName.SENTRY_RAY).getAPCost() == world.getAP()) {
            if (hero.getAbility(AbilityName.SENTRY_RAY).isReady())
                for (int i = 0; i < inVision.length; i++) {
                    if (inVision[i].getCurrentHP() - hero.getAbility(AbilityName.SENTRY_RAY).getPower() <= 0) {
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
        if (hero.getAbility(AbilityName.SENTRY_DODGE).getAPCost()==world.getAP()) {
            if (needToDodge()) {
                Cell Des = rangeFight.bestDodge(hero.getCurrentCell(), 3, 6);
                if (!hero.getCurrentCell().equals(Des)) {
                    world.castAbility(hero, AbilityName.SENTRY_DODGE, Des);
                    return;
                }
            }
        }
        if (hero.getAbility(AbilityName.SENTRY_RAY).getAPCost()==world.getAP()) {
            if (hero.getAbility(AbilityName.SENTRY_RAY).isReady()) {
                for (int i = 0; i < inVision.length; i++) {
                    if (inVision[i].getName().equals(HeroName.SENTRY)) {
                        world.castAbility(hero, AbilityName.SENTRY_RAY, inVision[i].getCurrentCell());
                        return;
                    }
                }
                for (int i = 0; i < inVision.length; i++) {
                    if (inVision[i].getName().equals(HeroName.BLASTER)) {
                        world.castAbility(hero, AbilityName.SENTRY_RAY, inVision[i].getCurrentCell());
                        return;
                    }
                }
                for (int i = 0; i < inVision.length; i++) {
                    if (inVision[i].getName().equals(HeroName.GUARDIAN))
                        if (!inVision[i].getAbility(AbilityName.GUARDIAN_FORTIFY).isReady()) {
                            world.castAbility(hero, AbilityName.SENTRY_RAY, inVision[i].getCurrentCell());
                            return;
                        }
                }
                for (int i = 0; i < inVision.length; i++) {
                    if (inVision[i].getName().equals(HeroName.HEALER)) {
                        world.castAbility(hero, AbilityName.SENTRY_RAY, inVision[i].getCurrentCell());
                        return;
                    }
                }
            }
        }
        if (hero.getAbility(AbilityName.SENTRY_ATTACK).getAPCost()==world.getAP()) {
            Hero inAtk = null;
            for (int i = 0; i < heroes.length; i++) {
                if (inAtk == null&&rangeFight.isInVision(hero,heroes[i]))
                    inAtk = heroes[i];
                else if (inAtk.getCurrentHP() > heroes[i].getCurrentHP()&&rangeFight.isInVision(hero,heroes[i]))
                    inAtk = heroes[i];
            }
            if (inAtk != null)
                world.castAbility(hero, AbilityName.SENTRY_ATTACK, inAtk.getCurrentCell());
        }
    }


    public boolean isNeedToMove() {
        Hero[] heroes = rangeFight.InRangeAtk(hero, 5);
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

    public Direction SentryMove() {
        Hero[] heroes = world.getOppHeroes();
        if (atkMode == true && heroes.length != 0) {
            Direction[] dir = world.getPathMoveDirections(hero.getCurrentCell(), rangeFight.SingleToSingleAtkRange(hero, 7, rangeFight.NearstEnemy(hero.getCurrentCell(), heroes), 0)[0]);
            return dir[0];

        } else {
            if (rangeFight.isSafe(hero, 7)) {
                return ObjectMove();
            } else {
                if (!isNeedToMove())
                    return ObjectMove();
                return EscapeDirection(rangeFight.InRangeAtk(hero, 7));
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
        Direction[] dir = world.getPathMoveDirections(hero.getCurrentCell(), rangeFight.findNearestZoneCell(hero.getCurrentCell()), cells);
        return dir[0];
    }

    private Direction EscapeDirection(Hero[] inRange) {
        Direction bestMove = null;
        float maxDistance = rangeFight.avgDistance(inRange, hero.getCurrentCell());
        if (!map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()).isWall() &&
                rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())) > maxDistance &&
                map.isInMap(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn())) {
            bestMove = Direction.UP;
            maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() - 1, hero.getCurrentCell().getColumn()));
        }

        if (!map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()).isWall() &&
                rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())) > maxDistance &&
                map.isInMap(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn())) {
            bestMove = Direction.DOWN;
            maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow() + 1, hero.getCurrentCell().getColumn()));
        }

        if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1).isWall() &&
                rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)) > maxDistance &&
                map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1)) {
            bestMove = Direction.LEFT;
            maxDistance = rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() - 1));
        }
        if (!map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1).isWall() &&
                rangeFight.avgDistance(inRange, map.getCell(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1)) > maxDistance &&
                map.isInMap(hero.getCurrentCell().getRow(), hero.getCurrentCell().getColumn() + 1)) {
            bestMove = Direction.RIGHT;

        }
        return bestMove;
    }

    public Hero getHero() {
        return hero;
    }
}
