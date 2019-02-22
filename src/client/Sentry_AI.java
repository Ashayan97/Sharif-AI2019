package client;

import client.model.*;

import java.util.ArrayList;

public class Sentry_AI {
    private Hero hero;
    private World world;
    private Map map;
    private Range_fight rangeFight;
    private Hero[] heroes;
    private int AtkPriority=0;
    public Sentry_AI(Hero hero, World world) {
        this.hero = hero;
        this.world = world;
        this.map = world.getMap();
        rangeFight = new Range_fight(world);
        heroes = world.getMyHeroes();
    }


    public Direction SentryMove() {
        if (rangeFight.isSafe(hero, 7)) {
            return ObjectMove();
        } else {

            Hero[] AtkInRange=rangeFight.InRangeAtk(hero,7);
            for (int i = 0; i < AtkInRange.length; i++) {
                //TODO
            }

        }
        return null;
    }


    private Direction ObjectMove() {
        Cell[] cells = new Cell[4];
        for (int i = 0; i < 4; i++) {
            cells[i] = heroes[i].getCurrentCell();
        }
        Direction[] dir = world.getPathMoveDirections(hero.getCurrentCell(), rangeFight.findNearestZoneCell(hero.getCurrentCell()), cells);
        return dir[0];
    }

    private Direction EscapeDirection(Hero[] inRange){


        return null;
    }


}
