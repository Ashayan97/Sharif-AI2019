package client;

import client.model.*;

import java.util.ArrayList;

public class Utility {
    final static int DODGE_RANGE = 4;
    static int distance(Cell start, Cell end, boolean check) {
        if (check && (start.isWall() || end.isWall()))
            return -1;
        return distance(start.getRow(), start.getColumn(), end.getRow(), end.getColumn());
    } // check that cells are an wall or not if be wall return -1
    static int distance(Cell start, Cell end){
        if(start == null || end==null)
            return -1;
        return distance(start,end,false);
    } // it's an fast method that not need to your boolean
    static int distance(int startRow, int startCol, int endRow, int endCol) {
        return  Math.abs(startRow - endRow) +
                Math.abs(startCol - endCol);
    }

    static void move(World world, Hero hero, Cell src, Cell dest) {
        move(world,hero.getId(),src,dest);
    }
    static void move(World world,int heroID,Cell src,Cell des){
        Direction dir[] = world.getPathMoveDirections(src,des);
        if(dir.length == 0)
            return;
        move(world,heroID,dir[0]);
    }
    static void move(World world,int heroId,Direction direction){
        world.moveHero(heroId,direction);
    }

    static Cell nextCell(World world,Cell src,Cell des){
        Direction dir = world.getPathMoveDirections(src.getRow(),src.getColumn(),des.getRow(),des.getColumn())[0];
        int row = src.getRow();
        int col = src.getColumn();
        return world.getMap()
                .getCells()
                [row+(  dir==Direction.DOWN?1:
                        dir==Direction.UP?-1:
                        0)]
                [col+(  dir==Direction.RIGHT?1:
                        dir==Direction.LEFT?-1:
                        0)];
    }

    static Cell[] availableCells(Map map , int radius, Cell currentCell){
        ArrayList<Cell> cells = new ArrayList<>();
        int curRow = currentCell.getRow();
        int curCol = currentCell.getColumn();
        for (int tmpRow = -radius; tmpRow <=radius ; tmpRow++) {
            for (int tmpCol = -radius; tmpCol <= radius; tmpCol++) {
                if(Math.abs(tmpCol)+Math.abs(tmpRow)<=radius && map.isInMap(curRow+tmpRow,curCol+tmpCol))
                    cells.add(map.getCells()[curRow+tmpRow][curCol+tmpCol]);
            }
        }
        return cells.toArray(new Cell[]{});
    }

    private static final String OBJECTIVEZONE_SHAPE   = "#  ",
                                OPP_RESPAWNZONE_SHAPE = "+' ",
                                MY_RESPAWNZONE_SHAPE  = "+  ",
                                WALL_SHAPE            = "/\\ ",
                                NORMAL_CELL           = "-- ";
    static void printMap(World world) {
        Cell[][] cells = world.getMap().getCells();
        System.out.print("  ");
        for (int i = 0; i < world.getMap().getColumnNum(); i++) {
            String str = String.valueOf(i);
            System.out.print(str+(str.length()==1?"  ":" "));
        }
        System.out.println();
        for (int i = 0; i < world.getMap().getRowNum(); i++) {
            String str=String.valueOf(i);
            System.out.print(str+(str.length()==1?" ":""));
            for (int j = 0; j < world.getMap().getColumnNum(); j++) {
                Hero inThisCell = world.getMyHero(cells[i][j]);
                Hero oppInThisCell = world.getOppHero(cells[i][j]);
                System.out.print(
                        oppInThisCell!=null?String.format(     "%s' ",oppInThisCell.getName().name().charAt(0)):
                        inThisCell!=null?String.format(     "%s  ",inThisCell.getName().name().charAt(0)):
                                cells[i][j].isWall() ?              WALL_SHAPE:
                                        cells[i][j].isInMyRespawnZone() ?   MY_RESPAWNZONE_SHAPE :
                                                cells[i][j].isInOppRespawnZone() ?  OPP_RESPAWNZONE_SHAPE :
                                                        cells[i][j].isInObjectiveZone() ?   OBJECTIVEZONE_SHAPE :
                                                                NORMAL_CELL);
            }
            System.out.println();
        }
    }

    private void printInfo(Hero hero, Direction dir) {
        System.out.println(hero.getName().name() + " CuCell [" + hero.getCurrentCell().getRow() +
                "," + hero.getCurrentCell().getColumn() + "]");
        System.out.println("dir " + dir.name());
    }

    void printHero(String str,Hero[] heroes) {
        System.out.println(str);
        for (int i = 0; i < heroes.length; i++)
            System.out.print((i + 1) + " - " + heroes[i].getName().name() + " ");
        System.out.println("==============================");
    }

    static Direction pathTo(World world,Cell start,Cell end){
        return world.getPathMoveDirections(start.getRow(),start.getColumn(),end.getRow(),end.getColumn())[0];
    }
    static Direction[] pathTo(World world,Cell start,Cell end,boolean want){
        return world.getPathMoveDirections(start.getRow(),start.getColumn(),end.getRow(),end.getColumn());
    }
    static ATTACK_STATE canAttack(Hero fHero, Hero sHero){
        switch (fHero.getHeroConstants().getName()){
            case BLASTER:
                return Utility_Attack.CanAttack_Blaster(fHero,sHero);
            case SENTRY:
                return Utility_Attack.CanAttack_Sentry(fHero,sHero);
            case HEALER:
                return Utility_Attack.CanAttack_Healer(fHero,sHero);
            case GUARDIAN:
                return Utility_Attack.CanAttack_Guardian(fHero,sHero);
        }
        return ATTACK_STATE.CANTATTACK;
    }

}
