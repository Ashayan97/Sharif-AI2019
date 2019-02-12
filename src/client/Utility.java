package client;

import client.model.*;

import java.util.ArrayList;

public class Utility {
    public static final double SECOND_ROOT_OF_2=1.41421730950488016887/2;
    static int Distance(Cell start, Cell end, boolean check) {
        if (check && (start.isWall() || end.isWall()))
            return -1;
        return Distance(start.getRow(), start.getColumn(), end.getRow(), end.getColumn());
    } // check that cells are an wall or not if be wall return -1
    static int Distance(Cell start,Cell end){
        return Distance(start,end,false);
    } // it's an fast method that not need to your boolean
    static int Distance(int startRow, int startCol, int endRow, int endCol) {
        return  Math.abs(startRow - endRow) +
                Math.abs(startCol - endCol);
    }

    static enum ATTACK_STATE{
        TANBETAN,
        DORADOR,
        CANTATTACK
    }

    static Cell[] AvailableCells(Map map , int radius,Cell currentCell){
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
            if(str.length() == 1)
                System.out.print(str+"  ");
            else
                System.out.print(str+" ");
        }
        System.out.println();
        for (int i = 0; i < world.getMap().getRowNum(); i++) {
            System.out.print(i+" ");
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
    static ATTACK_STATE CanAttack(Hero fHero,Hero sHero){
        switch (fHero.getHeroConstants().getName()){
            case BLASTER:
                return CanAttack_Blaster(fHero,sHero);
            case SENTRY:
                return CanAttack_Sentry(fHero,sHero);
            case HEALER:
                return CanAttack_Healer(fHero,sHero);
            case GUARDIAN:
                return CanAttack_Guardian(fHero,sHero);
        }
        return ATTACK_STATE.CANTATTACK;
    }

    private static ATTACK_STATE CanAttack_Guardian(Hero fHero, Hero sHero) {
        return ATTACK_STATE.CANTATTACK;
    }

    private static ATTACK_STATE CanAttack_Healer(Hero fHero, Hero sHero) {
        return ATTACK_STATE.CANTATTACK;
    }

    private static ATTACK_STATE CanAttack_Sentry(Hero fHero, Hero sHero) {
        return ATTACK_STATE.CANTATTACK;
    }

    private static ATTACK_STATE CanAttack_Blaster(Hero fHero, Hero sHero) {
        // omidekz
        // ajab
        //ghjkl
        return ATTACK_STATE.CANTATTACK;
    }

}
