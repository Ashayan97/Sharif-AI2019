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
    private Hero[] OurHero ;
    private Hero[] OppHero ;
    ////////////////////////////////////////////////////////////


    //constructor to make Class
    public Range_fight(World world) {
        map = world.getMap();
        this.OppHero = world.getOppHeroes();
        this.OurHero = world.getMyHeroes();
    }



    // Atk with best distance from single to single enemy
    public Cell[] SingleToSingleAtkRange(Hero hero, int throwRange, Cell center, int OurDistance, int effRange) {
        int shotRange = effRange + throwRange;
        ArrayList<Cell> cells = new ArrayList<>();
        ArrayList<Cell> ThrowCells = new ArrayList<>();
        for (int j = 0; j <= 2 * shotRange; j++) {
            if (j < shotRange + 1)
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                if (!map.getCell(center.getRow() - j, center.getColumn() - shotRange + j).isWall() &&
                        map.isInMap(center.getRow() - j, center.getColumn() - shotRange + j) &&
                        !world.getMyHero(center.getRow() - j, center.getColumn() - shotRange + j).equals(null)
                        && !world.getOppHero(center.getRow() - j, center.getColumn() - shotRange + j).equals(null) &&
                        !map.getCell(center.getRow() + j, center.getColumn() - shotRange + j).isWall() &&
                        map.isInMap(center.getRow() + j, center.getColumn() - shotRange + j) &&
                        !world.getMyHero(center.getRow() + j, center.getColumn() - shotRange + j).equals(null)
                        && !world.getOppHero(center.getRow() + j, center.getColumn() - shotRange + j).equals(null)) {
                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    cells.add(map.getCell(center.getRow() + j, center.getColumn() - shotRange + j));
                    cells.add(map.getCell(center.getRow() - j, center.getColumn() - shotRange + j));
                } else {
                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    if (!map.getCell(center.getRow() - 2 * shotRange + j, center.getColumn() - shotRange + j).isWall() &&
                            map.isInMap(center.getRow() - 2 * shotRange + j, center.getColumn() - shotRange + j) &&
                            !world.getMyHero(center.getRow() - 2 * shotRange + j, center.getColumn() - shotRange + j).equals(null)
                            && !world.getOppHero(center.getRow() - 2 * shotRange + j, center.getColumn() - shotRange + j).equals(null) &&
                            !map.getCell(center.getRow() + 2 * shotRange - j, center.getColumn() - shotRange + j).isWall() &&
                            map.isInMap(center.getRow() + 2 * shotRange - j, center.getColumn() - shotRange + j) &&
                            !world.getMyHero(center.getRow() + 2 * shotRange - j, center.getColumn() - shotRange + j).equals(null)
                            && !world.getOppHero(center.getRow() + 2 * shotRange - j, center.getColumn() - shotRange + j).equals(null)) {
                        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                        cells.add(map.getCell(center.getRow() + 2 * shotRange - j, center.getColumn() - shotRange + j));
                        cells.add(map.getCell(center.getRow() - 2 * shotRange + j, center.getColumn() - shotRange + j));
                    }
                }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        for (int k = 0; k < 2; k++)
            for (int i = 0; i <= effRange; i++)
                for (int j = 0; j <= 2 * i; j++)
                    if (k == 0)
                        if (!map.getCell(center.getRow() - effRange + i, center.getColumn() - i + j).isWall() &&
                                map.isInMap(center.getRow() - effRange + i, center.getColumn() - i + j)) {
                            ThrowCells.add(map.getCell(center.getRow() - effRange + i, center.getColumn() - i + j));
                        } else {
                            if (!map.getCell(center.getRow() + effRange - i, center.getColumn() - i + j).isWall() &&
                                    map.isInMap(center.getRow() + effRange - i, center.getColumn() - i + j)) {
                                ThrowCells.add(map.getCell(center.getRow() + effRange - i, center.getColumn() - i + j));
                            }
                        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        Cell[] res = new Cell[2];
        Cell LocalMinThrow = null;
        Cell min=null;
        Cell minThrow=null;

        /////////////////////////////////////////////
        for (int i = 0; i < cells.size(); i++) {
            if (min.equals(null)) {
                for (int j = 0; j < ThrowCells.size(); j++) {
                    if (LocalMinThrow.equals(null))
                        LocalMinThrow = ThrowCells.get(i);
                    else if (world.manhattanDistance(ThrowCells.get(i), cells.get(i)) < world.manhattanDistance(LocalMinThrow, cells.get(i)))
                        LocalMinThrow = ThrowCells.get(i);
                    min = cells.get(i);
                    minThrow=LocalMinThrow;
                }
                LocalMinThrow=null;
            } else
                for (int j = 0; j < ThrowCells.size(); j++) {
                    if (LocalMinThrow.equals(null))
                        LocalMinThrow = ThrowCells.get(i);
                    else if (world.manhattanDistance(ThrowCells.get(i), cells.get(i)) < world.manhattanDistance(LocalMinThrow, cells.get(i)))
                        LocalMinThrow = ThrowCells.get(i);
                }
            if (world.manhattanDistance(hero.getCurrentCell(), cells.get(i)) < world.manhattanDistance(hero.getCurrentCell(), min)) {
                min=cells.get(i);
                minThrow=LocalMinThrow;
            }
            LocalMinThrow=null;
        }
        /////////////////////////////////////////////////////////
        res[0]=min;
        res[1]=minThrow;
        return res;
    }


}
