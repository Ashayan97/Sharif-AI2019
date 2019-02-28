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
    private LastData lastData = new LastData();
//    private boolean flag = true;

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
        Hero[] hero = world.getMyHeroes();
        if (flag == 0) {
            lastData.setBlasterEnemy(world.getOppHeroes());
            flag++;
        }
//        System.out.println("///////////////////////////////////////////// blasters");
//        for (int i = 0; i < lastData.blasterEnemy.length; i++) {
//            System.out.println(lastData.blasterEnemy[i]);
//            System.out.println("_______________");
//            System.out.println(lastData.bomber[i]);
//        }
//
//
//        System.out.println("///////////////////////////////////////////end");
        lastData.world = world;
        lastData.bombReducer();
        lastData.isAnyBombUsed();
        Sentry_AI sentry = new Sentry_AI(hero[3], world, lastData);
        if (hero[3].getCurrentHP() != 0)
            sentry.SentryMove();
        Blaster.blasterMove(this, world, hero[0], histories[indexOfHeroInHistory(hero[0])]);
        Blaster.blasterMove(this, world, hero[1], histories[indexOfHeroInHistory(hero[1])]);
//        Blaster.blasterMove(this,world,hero[2],histories[indexOfHeroInHistory(hero[2])]);

        Guardian_AI guardian;
//        guardian= new Guardian_AI(hero[0],world);
//        guardian.movePhase();
//        guardian= new Guardian_AI(hero[1],world);
//        guardian.movePhase();

        guardian = new Guardian_AI(hero[2], world);
        guardian.movePhase();

    }

    void actionTurn(World world) {
        this.world = world;
        init();
        Hero[] heroes = world.getMyHeroes();

        Sentry_AI sentry = new Sentry_AI(heroes[3], world, lastData);
        if (heroes[3].getCurrentHP() != 0)
            sentry.actionPhase();

        Blaster.blasterAttack(this, world, heroes[0]);
        Blaster.blasterAttack(this, world, heroes[1]);
//        Blaster.blaster(this,world,heroes[2],histories[indexOfHeroInHistory(heroes[2])]);

        Guardian_AI guardian;
//        guardian  = new Guardian_AI(heroes[0],world);
//        guardian.actionPhase();
//        guardian  = new Guardian_AI(heroes[1],world);
//        guardian.actionPhase();
        guardian = new Guardian_AI(heroes[2], world);
        guardian.actionPhase();

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
                world.pickHero(HeroName.BLASTER);
                break;
            case 1:
                world.pickHero(HeroName.BLASTER);

                break;
            case 2:
                world.pickHero(HeroName.GUARDIAN);
                break;
            case 3:
                world.pickHero(HeroName.SENTRY);
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
