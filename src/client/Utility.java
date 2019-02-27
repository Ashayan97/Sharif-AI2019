package client;

import client.model.*;

import java.util.ArrayList;
import java.util.Vector;

public class Utility {
    final static int BLASTER_DODGE_RANGE = 4;
    final static int BLASTER_ATTACK_RADIUS = 1;
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
        Cell[] blockCells = new Cell[3];
        int k = 0;
        for (Hero h : world.getMyHeroes())
            if(h.getId()!=heroID) blockCells[k++] = h.getCurrentCell();
        Direction dir[] = world.getPathMoveDirections(src,des,blockCells);
        if(dir.length == 0)
            return;
        move(world,heroID,dir[0]);
    }
    static void move(World world,int heroId,Direction direction){
        world.moveHero(heroId,direction);
    }

    static Cell nextCell(World world,Cell src,Direction dir){
        int row = src.getRow();
        int col = src.getColumn();
        return world.getMap()
                .getCells()
                [row+(  dir==Direction.DOWN?1:
                dir==Direction.UP?-1: 0)]
                [col+(  dir==Direction.RIGHT?1:
                dir==Direction.LEFT?-1: 0)];
    }
    static Cell nextCell(World world,Cell src,Cell des,Cell[] blockCells){
        Direction dir = world.getPathMoveDirections(src.getRow(),src.getColumn(),des.getRow(),des.getColumn(),blockCells)[0];
        return nextCell(world,src,dir);
    }
    static Cell nextCell(World world,Cell src,Cell des){
        return nextCell(world,src,world.getPathMoveDirections(src,des)[0]);
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

    public static void sortOnDistance(Cell cell,Hero[] heros) {
        for (int i = 1; i <heros.length ; i++) {
            int j=i;
            while (j>=1){
                if(distance(cell,heros[j].getCurrentCell())<distance(cell,heros[j-1].getCurrentCell()))
                    swap(j,j-1,heros);
                j--;
            }
        }
    }

    private static void swap(int i, int j,Object[] heroes) {
        Object tmp = heroes[i];
        heroes[i] = heroes[j];
        heroes[j]=tmp;
    }

    public static void sortOnHP(Hero[] heros) {
        for (int i = 1; i < heros.length; i++) {
            int j = i;
            while (j>=1){
                if(heros[j].getCurrentHP()<heros[j-1].getCurrentHP())
                    swap(j,j-1,heros);
                j--;
            }
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

    public static Cell getDOWN(Cell src,Cell dst){
        return src.getRow()>=dst.getRow()?src:dst;
    }
    public static Cell getUP(Cell src,Cell dst){
        return src.getRow()<=dst.getRow()?src:dst;
    }
    public static Cell getRIGHT(Cell src,Cell dst){
        return src.getColumn()>=dst.getColumn()?src:dst;
    }
    public static Cell getLEFT(Cell src,Cell dst){
        return src.getColumn()<=dst.getColumn()?src:dst;
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

    public static Cell[] effectiveCells(World world, Cell firstCell , Cell secondCell){
        Cell results[] =null;
        if(firstCell.getRow() == secondCell.getRow()){
            results =  new Cell[1];
            results[0]=world.getMap().getCell(firstCell.getRow(),
                    (firstCell.getColumn()+secondCell.getColumn())/2);
        }else if(firstCell.getColumn() == secondCell.getColumn()){
            results =  new Cell[1];
            results[0]=world.getMap().getCell((firstCell.getRow()+secondCell.getRow())/2,
                    firstCell.getColumn());
        } else {
            Cell upCell = getUP(firstCell,secondCell);
            Cell downCell =  getDOWN(firstCell,secondCell);
            Cell aimCell1=null , aimCell2=null;
            if(world.getMap().isInMap(upCell.getRow(),downCell.getColumn()))
                aimCell1 = world.getMap().getCell(upCell.getRow(),downCell.getColumn());
            if(world.getMap().isInMap(downCell.getRow(),upCell.getColumn()))
                aimCell2 = world.getMap().getCell(downCell.getRow(),upCell.getColumn());
            if(aimCell1!=null && aimCell2!=null){
                results =  new Cell[2];
                results[0]=aimCell1;
                results[1]=aimCell2;
            }else if(aimCell2!=null){
                results = new Cell[1];
                results[0]=aimCell2;
            }else if(aimCell1!=null){
                results = new Cell[1];
                results[0]=aimCell1;
            }
        }
        return results;
    }

    static Hero[] getInAttackRange(World world,Hero hero,AbilityName... abilityNames) {
        int radius = abilityNames[0] == AbilityName.BLASTER_BOMB?7:5;
        Cell[] available = Utility.availableCells(world.getMap(),radius,hero.getCurrentCell());
        Vector<Hero> heroes = new Vector<>();
        Cell heroCell = hero.getCurrentCell();
        for (Cell anAvailable : available) {
            if (    world.getOppHero(anAvailable) != null&&
                    !hero.getAbility(abilityNames[0]).isLobbing() &&
                    world.isInVision(heroCell, anAvailable))
                heroes.add(world.getOppHero(anAvailable));
            else if (   world.getOppHero(anAvailable) != null&&
                        hero.getAbility(abilityNames[0]).isLobbing())
                heroes.add(world.getOppHero(anAvailable));
        }
        return heroes.toArray(new Hero[]{});
    }

    /**
     * hero haii k mibinim ro return mikone
     * */
    static Hero[] getSawHero(World world){
        Vector<Hero> hs = new Vector<>();
        Hero oppHs[] = world.getOppHeroes();
        for (Hero oppH : oppHs)
            if (oppH.getCurrentCell().getColumn() != -1)
                hs.add(oppH);
        return hs.toArray(new Hero[0]);
    }

    /**
     * return opp guardians
     * */
    static Hero[] getGuardians(World world,Hero h){
        return getGuardians(world,h.getCurrentCell());
    }
    static Hero[] getGuardians(World world,Cell center){
        Cell[] cl = availableCells(world.getMap(),3,center);
        Vector<Hero> guardians = new Vector<>();
        for (Cell cell: cl)
            if (world.getOppHero(cell)!=null&&
                    world.getOppHero(cell).getName().equals(HeroName.GUARDIAN))
                guardians.add(world.getOppHero(cell));
        return guardians.toArray(new Hero[0]);
    }
}
