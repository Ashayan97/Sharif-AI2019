package client;

import client.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

public class AI {

    private int PICK_PHASE_COUNTER = 0;
    private Hero[] herosInVision;
    private Cell[] objectiveCells;
    private History[] histories;
    private Vector<Cell> wallsCell;
    private Vector<Hero> atttackTo;
    private int whoAttackID;
    private boolean inAttack=false;

    //****************************************
    void preProcess(World world) {
        objectiveCells = world.getMap().getObjectiveZone();
        initWallCell(world);
        Blaster.set(world);
    }

    void pickTurn(World world) {
        pickHeroInPhase(world);
    }

    void moveTurn(World world) {
        Utility.printMap(world);
        init(world);
        Blaster.blasterMove(world, world.getMyHeroes()[0],histories[indexOfHeroInHistory(world.getMyHeroes()[0])]);
        Blaster.blasterMove(world, world.getMyHeroes()[1],histories[indexOfHeroInHistory(world.getMyHeroes()[1])]);
        Blaster.blasterMove(world, world.getMyHeroes()[2],histories[indexOfHeroInHistory(world.getMyHeroes()[2])]);
        Blaster.blasterMove(world, world.getMyHeroes()[3],histories[indexOfHeroInHistory(world.getMyHeroes()[3])]);
    }

    void actionTurn(World world) {
        init(world);
        Blaster.blasterAttack(world,world.getMyHeroes()[0]);
        Blaster.blasterAttack(world,world.getMyHeroes()[1]);
        Blaster.blasterAttack(world,world.getMyHeroes()[2]);
        Blaster.blasterAttack(world,world.getMyHeroes()[3]);
    }

    //****************************************

    /**
     * this method initialize our need across the phase or turn
     */
    private void init(World world) {
        initHistorys(world.getMyHeroes());
        initHeroInVision(world);
    }

    private void initHeroInVision(World world) {
        herosInVision = Utility.getSawHero(world);
    }

    private void initWallCell(World world) {
        if (wallsCell == null) {
            wallsCell = new Vector<>();
            for (Cell[] arryCell : world.getMap().getCells())
                for (Cell cell : arryCell)
                    if (cell.isWall())
                        wallsCell.add(cell);
        }
    }

    /**
     * in method check migkone ke ag histories ma init nashode
     * initesh kone
     */
    private void initHistorys(Hero[] myHero) {
        if (histories == null) {
            histories = new History[4];
            for (int i = 0; i < 4; i++)
                histories[i] = new History(myHero[i].getId());
        }
    }

    /**
     * in method mige kodum az hero haye ma(@myHeroes), hero'ye doshman(oppHero) ro didan
     **/
    private Hero[] whoSeeThisHero(World world, Hero oppHeroe) {
        Vector<Hero> heroes = new Vector<>();
        Hero[] myHeroes = world.getMyHeroes();
        Cell oppHeroCurrentCell = oppHeroe.getCurrentCell();
        for (Hero myHero : myHeroes)
            if(world.isInVision(myHero.getCurrentCell(),oppHeroCurrentCell))
                heroes.add(myHero);
        return heroes.toArray(new Hero[]{});
    }

    /**
     * we pick our hero for game in this method
     */
    private void pickHeroInPhase(World world) {
        switch (PICK_PHASE_COUNTER) {
            case 0:
                world.pickHero(HeroName.BLASTER);
                break;
            case 1:
                world.pickHero(HeroName.BLASTER);
                break;
            case 2:
                world.pickHero(HeroName.BLASTER);
                break;
            case 3:
                world.pickHero(HeroName.BLASTER);
                break;
        }
        PICK_PHASE_COUNTER++;

    }

    /**
     * this method get an hero and return the index of he in histories array
     * if heroID not be in history this method return -1
     */
    private int indexOfHeroInHistory(Hero hero) {
        for (int i = 0; i < 4; i++) {
            if (histories[i].getHeroID() == hero.getId()) {
                return i;
            }
        }
        System.out.println(String.format("i:%d , HEREID:%d", -1, hero.getId()));
        return -1;
    }

    private void move(World world,int HEROID, Cell src,Cell dst, History history,boolean saveCell){
        Utility.move(world,HEROID,src,dst);
        if(saveCell)
            history.addLastStep(src);
    }
    private void move(World world,int HERODID,Cell src,Cell dest,History history){
        move(world,HERODID,src,dest,history,true);
    }

    private void setGpAttack(Hero whoAttack, Collection<Hero> toAttack){
        this.atttackTo.addAll(toAttack);
        this.whoAttackID = whoAttack.getId();
        inAttack = true;
    }
    private void clearGpAttack(){
        whoAttackID = -1;
        atttackTo = null;
        inAttack = false;
    }

    private void printCell(String str, Cell cell) {
        System.out.println(str + cell.getRow() + "-" + cell.getColumn());
    }
}
