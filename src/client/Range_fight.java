package client;

import client.model.Cell;
import client.model.Hero;
import client.model.Map;
import client.model.World;

import java.util.ArrayList;

public class Range_fight {

    ///////////////////////////////////////////////////////////
    private World world;
    private Map map;
    private Hero[] OurHero;
    private Hero[] OppHero;
    ////////////////////////////////////////////////////////////


    //constructor to make Class
    public Range_fight(World world) {
        map = world.getMap();
        this.OppHero = world.getOppHeroes();
        this.OurHero = world.getMyHeroes();
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

    // Atk with best distance from single to single enemy
    public Cell[] SingleToSingleAtkRange(Hero hero, int throwRange, Cell center, int effRange) {
        int shotRange = effRange + throwRange;
        ArrayList<Cell> cells = new ArrayList<>();
        Cell[] ThrowCells ;
        for (int j = 0; j <= 2 * shotRange; j++) {
            if (j < shotRange + 1)
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                if (!map.getCell(center.getRow() - j, center.getColumn() - shotRange + j).isWall() &&
                        map.isInMap(center.getRow() - j, center.getColumn() - shotRange + j) &&
                        world.getMyHero(center.getRow() - j, center.getColumn() - shotRange + j) == null
                        && world.getOppHero(center.getRow() - j, center.getColumn() - shotRange + j) == null &&
                        !map.getCell(center.getRow() + j, center.getColumn() - shotRange + j).isWall() &&
                        map.isInMap(center.getRow() + j, center.getColumn() - shotRange + j) &&
                        world.getMyHero(center.getRow() + j, center.getColumn() - shotRange + j) == null
                        && world.getOppHero(center.getRow() + j, center.getColumn() - shotRange + j) == null) {
                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    cells.add(map.getCell(center.getRow() + j, center.getColumn() - shotRange + j));
                    cells.add(map.getCell(center.getRow() - j, center.getColumn() - shotRange + j));
                } else {
                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    if (!map.getCell(center.getRow() - 2 * shotRange + j, center.getColumn() - shotRange + j).isWall() &&
                            map.isInMap(center.getRow() - 2 * shotRange + j, center.getColumn() - shotRange + j) &&
                            world.getMyHero(center.getRow() - 2 * shotRange + j, center.getColumn() - shotRange + j) == null
                            && world.getOppHero(center.getRow() - 2 * shotRange + j, center.getColumn() - shotRange + j) == null &&
                            !map.getCell(center.getRow() + 2 * shotRange - j, center.getColumn() - shotRange + j).isWall() &&
                            map.isInMap(center.getRow() + 2 * shotRange - j, center.getColumn() - shotRange + j) &&
                            world.getMyHero(center.getRow() + 2 * shotRange - j, center.getColumn() - shotRange + j) == null
                            && world.getOppHero(center.getRow() + 2 * shotRange - j, center.getColumn() - shotRange + j) == null) {
                        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                        cells.add(map.getCell(center.getRow() + 2 * shotRange - j, center.getColumn() - shotRange + j));
                        cells.add(map.getCell(center.getRow() - 2 * shotRange + j, center.getColumn() - shotRange + j));
                    }
                }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        for (int k = 0; k < 2; k++)
//            for (int i = 0; i <= effRange; i++)
//                for (int j = 0; j <= 2 * i; j++)
//                    if (k == 0)
//                        if (!map.getCell(center.getRow() - effRange + i, center.getColumn() - i + j).isWall() &&
//                                map.isInMap(center.getRow() - effRange + i, center.getColumn() - i + j)) {
//                            ThrowCells.add(map.getCell(center.getRow() - effRange + i, center.getColumn() - i + j));
//                        } else {
//                            if (!map.getCell(center.getRow() + effRange - i, center.getColumn() - i + j).isWall() &&
//                                    map.isInMap(center.getRow() + effRange - i, center.getColumn() - i + j)) {
//                                ThrowCells.add(map.getCell(center.getRow() + effRange - i, center.getColumn() - i + j));
//                            }
//                        }
        ThrowCells=cellsOfArea(center,effRange);
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        Cell[] res = new Cell[2];
        Cell LocalMinThrow = null;
        Cell min = null;
        Cell minThrow = null;

        /////////////////////////////////////////////
        for (int i = 0; i < cells.size(); i++) {
            if (min == null) {
                for (int j = 0; j < ThrowCells.length; j++) {
                    if (LocalMinThrow == null)
                        LocalMinThrow = ThrowCells[i];
                    else if (world.manhattanDistance(ThrowCells[i], cells.get(i)) < world.manhattanDistance(LocalMinThrow, cells.get(i)))
                        LocalMinThrow = ThrowCells[i];
                    min = cells.get(i);
                    minThrow = LocalMinThrow;
                }
                LocalMinThrow = null;
            } else
                for (int j = 0; j < ThrowCells.length; j++) {
                    if (LocalMinThrow == null)
                        LocalMinThrow = ThrowCells[i];
                    else if (world.manhattanDistance(ThrowCells[i], cells.get(i)) < world.manhattanDistance(LocalMinThrow, cells.get(i)))
                        LocalMinThrow = ThrowCells[i];
                }
            if (world.manhattanDistance(hero.getCurrentCell(), cells.get(i)) < world.manhattanDistance(hero.getCurrentCell(), min)) {
                min = cells.get(i);
                minThrow = LocalMinThrow;
            }
            LocalMinThrow = null;
        }
        /////////////////////////////////////////////////////////
        res[0] = min;
        res[1] = minThrow;
        return res;
    }


    public boolean isSafe(Hero hero, int range) {
        Hero[] OppHero = world.getOppHeroes();
        for (int i = 0; i < OppHero.length; i++) {
            if (world.manhattanDistance(hero.getCurrentCell(), OppHero[i].getCurrentCell()) < range)
                return false;
        }
        return true;
    }


    public boolean isSafe(Cell cell, int range) {
        Hero[] OppHero = world.getOppHeroes();
        for (int i = 0; i < OppHero.length; i++) {
            if (world.manhattanDistance(cell, OppHero[i].getCurrentCell()) < range)
                return false;
        }
        return true;
    }

    public Hero[] InRangeAtk(Hero hero, int range) {
        ArrayList<Hero> heroes = new ArrayList<>();
        Hero[] Opp = world.getOppHeroes();
        for (int i = 0; i < Opp.length; i++) {
            if (world.manhattanDistance(hero.getCurrentCell(), OppHero[i].getCurrentCell()) <= range)
                heroes.add(Opp[i]);
        }
        return heroes.toArray(new Hero[0]);
    }

    public Cell findNearestZoneCell(Cell start) {
        Cell[] cells = map.getObjectiveZone();
        Cell min = null;
        int minLen = Integer.MAX_VALUE;
        for (int i = 0; i < cells.length; i++) {
            if (min == null && world.getMyHero(cells[i]) == null && world.getOppHero(cells[i]) == null) {
                min = cells[i];
                minLen = world.manhattanDistance(start, min);
            } else if (world.getMyHero(cells[i]) == null && world.getOppHero(cells[i]) == null && world.manhattanDistance(cells[i], start) < minLen) {
                min = cells[i];
                minLen = world.manhattanDistance(start, min);
            }
        }
        return min;
    }

    public float avgDistance(Hero[] inRange, Cell cell) {
        float avg = 0;
        for (Hero anInRange : inRange) {
            avg += world.manhattanDistance(cell, anInRange.getCurrentCell());
        }

        return avg / (float) inRange.length;
    }

    public Cell[] cellsOfArea(Cell center, int Range) {
        return Utility.availableCells(world.getMap(),Range,center);
    }

    public Cell bestDodge(Cell center,int Range,int safeRange){
        Cell[] cells=cellsOfArea(center,Range);
        Cell Des=center;
        for (int i = 0; i < cells.length; i++) {
            if (isSafe(cells[i],safeRange))
                Des=cells[i];
        }
        for (int i = 0; i < cells.length; i++) {
            if (isSafe(cells[i],safeRange)&&cells[i].isInObjectiveZone())
                Des=cells[i];
        }

        return Des;
    }

    public Hero[] inVisionEnemy(Hero hero){
        Hero[] heroes=world.getOppHeroes();
        ArrayList<Hero> inVision=new ArrayList<>();
        for (int i = 0; i <heroes.length; i++) {
            if (world.isInVision(hero.getCurrentCell(),heroes[i].getCurrentCell()))
                inVision.add(heroes[i]);
        }
        return inVision.toArray(new Hero[inVision.size()]);
    }

}
