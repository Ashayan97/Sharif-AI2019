package client;

import client.model.*;

import java.util.ArrayList;

public class Range_fight {
    ///////////////////////////////////////////////////////////
    private World world;
    private Map map;
    private Hero[] OurHero;
    private Hero[] OppHero;
    ////////////////////////////////////////////////////////////

    static public Cell[] cellsOfArea(Cell center, int Range, World world) {
        return Utility.availableCells(world.getMap(), Range, center);
    }


    //constructor to make Class
    public Range_fight(World world) {
        map = world.getMap();
        this.OppHero = world.getOppHeroes();
        this.OurHero = world.getMyHeroes();
        this.world = world;
    }

    public Cell NearstEnemy(Cell us, Hero[] heroes) {
        Cell min = null;
        for (int i = 0; i < heroes.length; i++) {
            if (min == null)
                min = heroes[i].getCurrentCell();
            else if (world.manhattanDistance(us, heroes[i].getCurrentCell()) < world.manhattanDistance(us, min))
                min = heroes[i].getCurrentCell();

        }
        return min;
    }

    public boolean isSafe(Hero hero, int range) {
        Hero[] OppHero = world.getOppHeroes();
        for (int i = 0; i < OppHero.length; i++) {
            if (world.manhattanDistance(hero.getCurrentCell(), OppHero[i].getCurrentCell()) <= range)
                if (OppHero[i].getName().equals(HeroName.GUARDIAN) && world.manhattanDistance(hero.getCurrentCell(), OppHero[i].getCurrentCell()) >= 5) {

                } else
                    return false;
        }
        return true;
    }

    public boolean isSafe(Cell cell, int range) {
        Hero[] OppHero = world.getOppHeroes();
        for (int i = 0; i < OppHero.length; i++) {
            if (world.manhattanDistance(cell, OppHero[i].getCurrentCell()) <= range)
                if (OppHero[i].getName().equals(HeroName.GUARDIAN) && world.manhattanDistance(cell, OppHero[i].getCurrentCell()) > 5) {
                } else if (OppHero[i].getName().equals(HeroName.HEALER) && world.manhattanDistance(cell, OppHero[i].getCurrentCell()) > 5) {
                } else
                    return false;

        }
        return true;
    }

    public boolean notInEnemyVision(Hero hero, Hero[] oppHero) {
        for (Hero anOppHero : oppHero) {
            if (world.isInVision(anOppHero.getCurrentCell(), hero.getCurrentCell()))
                return false;
        }
        return true;
    }

    public Hero[] InRangeAtk(Hero hero, int range) {
        ArrayList<Hero> heroes = new ArrayList<>();
        Hero[] Opp = world.getOppHeroes();
        for (int i = 0; i < Opp.length; i++) {
            if (Opp[i].getCurrentCell().getColumn() != -1 && world.manhattanDistance(hero.getCurrentCell(), Opp[i].getCurrentCell()) <= range)
                heroes.add(Opp[i]);
        }
        return heroes.toArray(new Hero[0]);
    }

    public Hero[] InRangeAtk(Cell hero, int range) {
        ArrayList<Hero> heroes = new ArrayList<>();
        Hero[] Opp = world.getOppHeroes();
        for (int i = 0; i < Opp.length; i++) {
            if (Opp[i].getCurrentCell().getColumn() != -1 && world.manhattanDistance(hero, Opp[i].getCurrentCell()) <= range)
                heroes.add(Opp[i]);
        }
        return heroes.toArray(new Hero[0]);
    }

    public Hero[] InRangeAtk(Hero hero, int range, Hero[] Opp) {
        ArrayList<Hero> heroes = new ArrayList<>();
//        Hero[] Opp = world.getOppHeroes();
        for (int i = 0; i < Opp.length; i++) {
            if (world.manhattanDistance(hero.getCurrentCell(), Opp[i].getCurrentCell()) <= range && !Opp[i].equals(hero))
                heroes.add(Opp[i]);
        }
        return heroes.toArray(new Hero[0]);
    }

    public Cell findNearestZoneCell(Cell start) {
        Cell[] cells = map.getObjectiveZone();
        Cell min = cells[0];
        int minLen = Integer.MAX_VALUE;
        ArrayList<Cell> ourLoc=new ArrayList<>();
        for (int i = 0; i < world.getMyHeroes().length; i++) {
            ourLoc.add(world.getMyHeroes()[i].getCurrentCell());
        }
        for (int i = 0; i < cells.length; i++) {
            if (min == null && world.getMyHero(cells[i]) == null && world.getPathMoveDirections(start,cells[i],ourLoc).length!=0) {
                min = cells[i];
                minLen = world.manhattanDistance(start, min);
            } else if (world.getMyHero(cells[i]) == null && world.getOppHero(cells[i]) == null && world.manhattanDistance(cells[i], start) < minLen) {
                min = cells[i];
                minLen = world.manhattanDistance(start, min);
            }
        }
        return min;
    }

    public Cell findNearestCell(Cell start, Cell[] cells) {
        Cell min = null;
        int minLen = Integer.MAX_VALUE;
        ArrayList<Cell> ourLoc=new ArrayList<>();
        for (int i = 0; i < world.getMyHeroes().length; i++) {
            ourLoc.add(world.getMyHeroes()[i].getCurrentCell());
        }
        for (int i = 0; i < cells.length; i++) {
            if (min == null && world.getMyHero(cells[i]) == null && world.getPathMoveDirections(start,cells[i],ourLoc).length!=0) {
                min = cells[i];
                minLen = world.manhattanDistance(start, min);
            } else if (world.getMyHero(cells[i]) == null && world.getOppHero(cells[i]) == null && world.manhattanDistance(cells[i], start) < minLen) {
                min = cells[i];
                minLen = world.manhattanDistance(start, min);
            }
        }
        return min;
    }

    static public float avgDistance(Hero[] inRange, Cell cell, World world) {
        float avg = 0;
        for (Hero anInRange : inRange) {
            avg += world.manhattanDistance(cell, anInRange.getCurrentCell());
        }
        return avg / (float) inRange.length;
    }

    public float avgDistance(Hero[] inRange, Cell cell) {
        float avg = 0;
        for (Hero anInRange : inRange) {
            avg += world.manhattanDistance(cell, anInRange.getCurrentCell());
        }
        return avg / (float) inRange.length;
    }

    public Cell[] cellsOfArea(Cell center, int Range) {
        return Utility.availableCells(world.getMap(), Range, center);
    }

    public Cell bestDodge(Cell center, int Range, int safeRange) {
        Cell[] cells = cellsOfArea(center, Range);
        ArrayList<Cell> lastChoice = new ArrayList<>();
        Cell Des = center;
        int min = Integer.MAX_VALUE;
        for (Cell cell : cells) {
            if (isSafe(cell, safeRange) && !cell.isWall() && world.getMyHero(cell)==null)
                lastChoice.add(cell);
        }
        for (int i = 0; i < lastChoice.size(); i++) {
            if (world.manhattanDistance(lastChoice.get(i), findNearestZoneCell(lastChoice.get(i))) <= min) {
                Des = lastChoice.get(i);
                min = world.manhattanDistance(lastChoice.get(i), findNearestZoneCell(lastChoice.get(i)));
            }
        }

        if (Des.equals(center))
            return bestSafe(cells, center);
        return Des;
    }

    public Hero[] inVisionEnemy(Hero hero) {
        Hero[] heroes = world.getOppHeroes();
        ArrayList<Hero> inVision = new ArrayList<>();
        for (int i = 0; i < heroes.length; i++) {
            if (world.isInVision(hero.getCurrentCell(), heroes[i].getCurrentCell()))
                inVision.add(heroes[i]);
        }
        return inVision.toArray(new Hero[inVision.size()]);
    }

    public Hero[] inVisionEnemy(Hero hero, int range) {
        Hero[] heroes = world.getOppHeroes();
        ArrayList<Hero> inVision = new ArrayList<>();
        for (int i = 0; i < heroes.length; i++) {
            if (world.isInVision(hero.getCurrentCell(), heroes[i].getCurrentCell()) && world.manhattanDistance(heroes[i].getCurrentCell(), hero.getCurrentCell()) <= range)
                inVision.add(heroes[i]);
        }
        return inVision.toArray(new Hero[inVision.size()]);
    }

    public Hero[] inVisionEnemy(Cell hero, int range) {
        Hero[] heroes = world.getOppHeroes();
        ArrayList<Hero> inVision = new ArrayList<>();
        for (int i = 0; i < heroes.length; i++) {
            if (world.isInVision(hero, heroes[i].getCurrentCell()) && world.manhattanDistance(heroes[i].getCurrentCell(), hero) <= range)
                inVision.add(heroes[i]);
        }
        return inVision.toArray(new Hero[inVision.size()]);
    }

    public Cell bestSafe(Cell[] cells, Cell center) {
        Cell bestCell = null;
        Hero[] inrange = InRangeAtk(world.getMyHero(center), 6);
        float Avg = avgDistance(inrange, center);
        for (int i = 0; i < cells.length; i++) {
            if (avgDistance(inrange, cells[i]) >= Avg && !cells[i].isWall())
                bestCell = cells[i];
        }
        return bestCell;

    }

    public Cell bestInVision(Hero hero, Cell enemy, int range) {
        Cell[] cells = cellsOfArea(enemy, range);
        Cell near = null;
        int dis = Integer.MAX_VALUE;
        for (int i = 0; i < cells.length; i++) {
            if (near == null && !cells[i].isWall() && world.getMyHero(cells[i].getRow(), cells[i].getColumn()) == null) {
                near = cells[i];
                dis = world.manhattanDistance(hero.getCurrentCell(), cells[i]);
            } else if (world.manhattanDistance(hero.getCurrentCell(), cells[i]) < dis &&
                    world.isInVision(enemy, cells[i]) &&
                    world.manhattanDistance(enemy, cells[i]) <= range &&
                    world.manhattanDistance(enemy, cells[i]) >= 5 &&
                    !cells[i].isWall() &&
                    world.getMyHero(cells[i].getRow(), cells[i].getColumn()) == null) {
                near = cells[i];
                dis = world.manhattanDistance(hero.getCurrentCell(), cells[i]);
            }
        }
        return near;
    }
}
