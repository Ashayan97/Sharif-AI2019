package client;

import client.model.*;

import java.util.*;
import java.util.Map;

public class AI {

    private int PICK_PHASE_COUNTER = 0;
    private Hero[] herosInVision;
    private Cell[] objectiveCells;
    private History[] histories;
    private Vector<Cell> wallsCell;
    private Vector<Hero> atttackTo;
    private int whoAttackID;
    private boolean inAttack = false;
    private World world;
    private int flag = 0;
    private Sentry_AI sentry;
    private Map<Integer, Hero> heroInAttack = new HashMap<>();
    private Map<Integer, Cell> dodgeMap = new HashMap<>();
    private Map<Integer, Cell> nextCell = new HashMap<>();
    private Phase phase=new Phase();

    //****************************************
    void preProcess(World world) {
        this.world = world;
        objectiveCells = world.getMap().getObjectiveZone();
        initWallCell();
        Blaster.set(world);
    }

    void pickTurn(World world) {
        this.world = world;
        pickHeroInPhase();
    }

    void moveTurn(World world) {
        this.world = world;
        Utility.printMap(world);
        init();
//        Blaster.blasterMove(this, world, world.getMyHeroes()[0], histories[indexOfHeroInHistory(world.getMyHeroes()[0])]);
//        Blaster.blasterMove(this, world, world.getMyHeroes()[1], histories[indexOfHeroInHistory(world.getMyHeroes()[1])]);
//        Blaster.blasterMove(this,world, world.getMyHeroes()[2], histories[indexOfHeroInHistory(world.getMyHeroes()[2])]);
//        Blaster.blasterMove(this,world, world.getMyHeroes()[3], histories[indexOfHeroInHistory(world.getMyHeroes()[3])]);
        Guardian_AI g = new Guardian_AI(world.getMyHeroes()[0],world);
        g.movePhase();
        g = new Guardian_AI(world.getMyHeroes()[1],world);
        g.movePhase();
//        g = new Guardian_AI(world.getMyHeroes()[2],world);
//        g.movePhase();
//        g = new Guardian_AI(world.getMyHeroes()[3],world);
//        g.movePhase();
//        Blaster.blasterMove(this,world,world.getMyHeroes()[2],histories[indexOfHeroInHistory(world.getMyHeroes()[2])]);
        sentry = new Sentry_AI(world.getMyHeroes()[2], world);
        sentry.SentryMove();
        sentry = new Sentry_AI(world.getMyHeroes()[3], world);
        sentry.SentryMove();
        phase.number++;
        if (phase.number==6)
            phase.number=0;

    }

    void actionTurn(World world) {
        this.world = world;
        init();
//        Blaster.blasterAttack(this, world, world.getMyHeroes()[0]);
//        Blaster.blasterAttack(this, world, world.getMyHeroes()[1]);
//        Blaster.blasterAttack(this,world, world.getMyHeroes()[2]);
//        Blaster.blasterAttack(this,world, world.getMyHeroes()[3]);
//        Guardian_AI g = new Guardian_AI(world.getMyHeroes()[0],world);
//        g.actionPhase();
//        g = new Guardian_AI(world.getMyHeroes()[1],world);
//        g.actionPhase();
//        g = new Guardian_AI(world.getMyHeroes()[2],world);
//        g.actionPhase();
//        g = new Guardian_AI(world.getMyHeroes()[3],world);
//        g.actionPhase();
//        Blaster.blasterAttack(this,world,world.getMyHeroes()[2]);
        sentry = new Sentry_AI(world.getMyHeroes()[2], world);
        sentry.actionPhase();
        sentry = new Sentry_AI(world.getMyHeroes()[3], world);
        sentry.actionPhase();
    }

    //****************************************

    /**
     * this method initialize our need across the phase or turn
     */
    private void init() {
        initHistorys(world.getMyHeroes());
        initHeroInVision();
    }

    private void initHeroInVision() {
        herosInVision = Utility.getSawHero(world);
    }

    private void initWallCell() {
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
    private Hero[] whoSeeThisHero(Hero oppHeroe) {
        Vector<Hero> heroes = new Vector<>();
        Hero[] myHeroes = world.getMyHeroes();
        Cell oppHeroCurrentCell = oppHeroe.getCurrentCell();
        for (Hero myHero : myHeroes)
            if (world.isInVision(myHero.getCurrentCell(), oppHeroCurrentCell))
                heroes.add(myHero);
        return heroes.toArray(new Hero[]{});
    }

    /**
     * we pick our hero for game in this method
     */
    private void pickHeroInPhase() {
        switch (PICK_PHASE_COUNTER) {
            case 0:
//                world.pickHero(HeroName.BLASTER);
//                world.pickHero(HeroName.SENTRY);
                world.pickHero(HeroName.GUARDIAN);
                break;
            case 1:
//                world.pickHero(HeroName.BLASTER);
//                world.pickHero(HeroName.SENTRY);
                world.pickHero(HeroName.GUARDIAN);
                break;
            case 2:
//                world.pickHero(HeroName.BLASTER);
                world.pickHero(HeroName.SENTRY);
//                world.pickHero(HeroName.GUARDIAN);
                break;
            case 3:
//                world.pickHero(HeroName.BLASTER);
                world.pickHero(HeroName.SENTRY);
//                world.pickHero(HeroName.GUARDIAN);
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

    private void move(int HEROID, Cell src, Cell dst, History history, boolean saveCell) {
        Utility.move(world, HEROID, src, dst);
        if (saveCell)
            history.addLastStep(src);
    }

    private void move(int HERODID, Cell src, Cell dest, History history) {
        move(HERODID, src, dest, history, true);
    }

    private void setGpAttack(Hero whoAttack, Collection<Hero> toAttack) {
        this.atttackTo.addAll(toAttack);
        this.whoAttackID = whoAttack.getId();
        inAttack = true;
    }

    private void clearGpAttack() {
        whoAttackID = -1;
        atttackTo = null;
        inAttack = false;
    }

    private void printCell(String str, Cell cell) {
        System.out.println(str + cell.getRow() + "-" + cell.getColumn());
    }

    void setInAttack(Hero fael, Hero maful) {
        heroInAttack.put(fael.getId(), maful);
    }

    void dodgeTo(Hero in, Cell dodgeTo) {
        dodgeMap.put(in.getId(), dodgeTo);
    }

    void addCell(Hero h, Cell cell) {
        nextCell.put(h.getId(), cell);
    }

    Cell getNextCell(Hero h) {
        if (nextCell.containsKey(h.getId()))
            return nextCell.get(h.getId());
        return null;
    }

    Cell[] getInAttackCell() {
        Hero[] hs = heroInAttack.values().toArray(new Hero[0]);
        Cell[] cs = new Cell[hs.length];
        for (int i = 0; i < hs.length; i++) {
            cs[i] = hs[i].getCurrentCell();
        }
        return cs;
    }
}
