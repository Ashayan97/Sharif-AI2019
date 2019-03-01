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

    boolean canSeeAnyOne(){
        Hero[] enemy=world.getOppHeroes();
        for (int i = 0; i < enemy.length; i++) {
            if (enemy[i].getCurrentCell().getRow()!=-1)
                return true;
        }
        return false;
    }

    public Direction enemyTarget(Hero hero){
        Cell enemy=NearstEnemy(hero.getCurrentCell(),world.getOppHeroes());
        Cell[] cells=cellsOfArea(enemy,6);
        Direction des=null;
        Hero[] our=world.getMyHeroes();
        Cell[] ourCell=new Cell[our.length];
        for (int i = 0; i < our.length; i++) {
            ourCell[i]=our[i].getCurrentCell();
        }
        int dis=Integer.MAX_VALUE;
        for (Cell cell : cells) {
            if (world.isInVision(cell, enemy) && !cell.isWall() &&
                    world.manhattanDistance(cell, enemy) == 7 &&
//                    world.manhattanDistance(cell, enemy) <= 7 &&
                    world.manhattanDistance(hero.getCurrentCell(), cell) <= dis &&
                    !cell.isWall()&&
                    world.getPathMoveDirections(hero.getCurrentCell(),cell,ourCell).length!=0&&
                    world.getPathMoveDirections(hero.getCurrentCell(),cell,ourCell)!=null
            ) {
                des = world.getPathMoveDirections(hero.getCurrentCell(),cell,ourCell)[0];
                dis = world.manhattanDistance(hero.getCurrentCell(), cell);
            }
        }
        return des;
    }


    public Direction enemyRun(Hero hero){
        Cell enemy=NearstEnemy(hero.getCurrentCell(),world.getOppHeroes());
        Cell[] cells=cellsOfArea(enemy,8);
        Direction des=null;
        Hero[] our=world.getMyHeroes();
        Cell[] ourCell=new Cell[our.length];
        for (int i = 0; i < our.length; i++) {
            ourCell[i]=our[i].getCurrentCell();
        }
        int dis=Integer.MAX_VALUE;
        for (Cell cell : cells) {
            if (world.isInVision(cell, enemy) && !cell.isWall() &&
                    world.manhattanDistance(cell, enemy) == 8 &&
                    world.manhattanDistance(hero.getCurrentCell(), cell) <= dis &&
                    !cell.isWall()&&
                    world.getPathMoveDirections(hero.getCurrentCell(),cell,ourCell).length!=0&&
                    world.getPathMoveDirections(hero.getCurrentCell(),cell,ourCell)!=null
            ) {
                des = world.getPathMoveDirections(hero.getCurrentCell(),cell,ourCell)[0];
                dis = world.manhattanDistance(hero.getCurrentCell(), cell);
            }
        }
        return des;
    }


    public boolean isSafe(Hero hero, int range) {
        Hero[] OppHero = world.getOppHeroes();
        for (Hero aOppHero : OppHero) {
            if (world.manhattanDistance(hero.getCurrentCell(), aOppHero.getCurrentCell()) <= range)
                if (aOppHero.getName().equals(HeroName.GUARDIAN) && world.manhattanDistance(aOppHero.getCurrentCell(), hero.getCurrentCell()) > 5) {

                } else if (aOppHero.getName().equals(HeroName.SENTRY)) {

                } else if (!notInEnemyVision(hero, world.getOppHeroes()))
                    return false;
        }
        return true;
    }

    public boolean isSafe(Cell cell, int range) {
        Hero[] OppHero = world.getOppHeroes();
        for (Hero aOppHero : OppHero) {
            if (world.manhattanDistance(cell, aOppHero.getCurrentCell()) <= range)
                if (aOppHero.getName().equals(HeroName.GUARDIAN) && world.manhattanDistance(aOppHero.getCurrentCell(), cell) > 5) {

                } else if (aOppHero.getName().equals(HeroName.SENTRY)) {

                } else if (!notInEnemyVision(cell, world.getOppHeroes()))
                    return false;

        }
        return true;
    }

    public boolean notInEnemyVision(Hero hero, Hero[] oppHero) {
        for (Hero anOppHero : oppHero) {
            if (anOppHero.getCurrentCell().getRow() != -1)
                if (world.isInVision(anOppHero.getCurrentCell(), hero.getCurrentCell()))
                    return false;
        }
        return true;
    }

    public boolean notInEnemyVision(Cell hero, Hero[] oppHero) {
        for (Hero anOppHero : oppHero) {
            if (anOppHero.getCurrentCell().getRow() != -1)
                if (world.isInVision(anOppHero.getCurrentCell(), hero))
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
        ArrayList<Cell> ourLoc = new ArrayList<>();
        for (int i = 0; i < world.getMyHeroes().length; i++) {
            ourLoc.add(world.getMyHeroes()[i].getCurrentCell());
        }
        for (int i = 0; i < cells.length; i++) {
            if (min == null && world.getMyHero(cells[i]) == null && world.getPathMoveDirections(start, cells[i], ourLoc).length != 0) {
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
        ArrayList<Cell> ourLoc = new ArrayList<>();
        for (int i = 0; i < world.getMyHeroes().length; i++) {
            ourLoc.add(world.getMyHeroes()[i].getCurrentCell());
        }
        for (int i = 0; i < cells.length; i++) {
            if (world.getPathMoveDirections(start, cells[i], ourLoc).length == 0)
                continue;
            if (min == null && world.getMyHero(cells[i]) == null) {
                min = cells[i];
                minLen = world.manhattanDistance(start, min);
            } else if (world.getMyHero(cells[i]) == null
                    && world.manhattanDistance(cells[i], start) < minLen) {
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
        ArrayList<Cell> firstChoice = new ArrayList<>();
        ArrayList<Cell> lastChoice = new ArrayList<>();
        Cell Des = center;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (Cell cell : cells) {
            if (isSafe(cell, safeRange) && !cell.isWall() && world.getMyHero(cell) == null)
                lastChoice.add(cell);
        }

        for (int i = 0; i < lastChoice.size(); i++) {
            if (max < world.manhattanDistance(center, lastChoice.get(i)))
                max = world.manhattanDistance(center, lastChoice.get(i));
        }

        for (int i = 0; i < lastChoice.size(); i++) {
            if (world.manhattanDistance(center, lastChoice.get(i)) == max)
                firstChoice.add(cells[i]);
        }

        for (int i = 0; i < firstChoice.size(); i++) {
            if (world.manhattanDistance(firstChoice.get(i), findNearestZoneCell(firstChoice.get(i))) <= min) {
                min = world.manhattanDistance(firstChoice.get(i), findNearestZoneCell(firstChoice.get(i)));
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
        for (Hero heroe : heroes) {
            if (world.isInVision(hero.getCurrentCell(), heroe.getCurrentCell()) &&
                    world.manhattanDistance(heroe.getCurrentCell(), hero.getCurrentCell()) <= range)
                inVision.add(heroe);
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
