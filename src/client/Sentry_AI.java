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


    public Sentry_AI(Hero hero, World world) {
        this.hero = hero;
        this.world = world;
        this.map = world.getMap();
        rangeFight = new Range_fight(world);
        heroes = world.getMyHeroes();
    }


    public boolean isNeedToMove() {
        Hero[] heroes = rangeFight.InRangeAtk(hero, 6);
        if (heroes.length == 0)
            return true;
        else return false;
    }

    public boolean needToDodge() {
        Hero[] heroes = rangeFight.InRangeAtk(hero, 7);
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
        }
        return false;
    }

    public Direction SentryMove() {
        Hero[] heroes = world.getOppHeroes();
        if (atkMode == true && heroes.length != 0) {
           // Direction[] dir=world.getPathMoveDirections(rangeFight.SingleToSingleAtkRange(hero,7)[0]);
        } else {
            if (rangeFight.isSafe(hero, 7)) {
                return ObjectMove();
            } else {

                Hero[] AtkInRange = rangeFight.InRangeAtk(hero, 7);
                for (int i = 0; i < AtkInRange.length; i++) {
                    //TODO
                }

            }
        }
        return null;
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


}
