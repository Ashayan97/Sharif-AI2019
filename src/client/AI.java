
package client;

import client.model.*;

import java.util.*;
import java.util.Map;

public class AI {

    private int PICK_PHASE_COUNTER = 0;
    private Hero[] herosInVision;
    private Cell[][] objectiveCells;
    private History[] histories;
    private Vector<Hero> atttackTo;
    private int whoAttackID;
    private boolean inAttack = false;
    private World world;
    private int flag = 0;
    private LastData lastData = new LastData();
    private Sentry_AI sentry;
    private Map<Integer, Hero> heroInAttack = new HashMap<>();
    private Map<Integer, Cell> dodgeMap = new HashMap<>();
    private Map<Integer, Cell> nextCell = new HashMap<>();
    private int FIRST_HERO = -1; // is Guardian
    private int SECOND_HERO = -1; // f b
    private int THERD_HERO = -1; //  s b
    private int FORTH_HERO = -1;
    private int BEST_FOR_GUARDIAN_RESPAWNZONE = -1;
    private int inProcess;
    private Cell rezereved = null;
    private int whoRezereved = -1;
    private int r, c1, c2;

    private void setInProcess(int index) {
        inProcess = index;
    }

    int getRowGoal() {
        return r;
    }

    int getColGoal1() {
        return c1;
    }

    int getColGoal2() {
        return c2;
    }

    int getInProcess() {
        return inProcess;
    }

    int FB() {
        return SECOND_HERO;
    }

    void setRezereved(Cell goal) {
        whoRezereved = getInProcess();
        rezereved = goal;
    }

    int getWhoRezereved() {
        return whoRezereved;
    }

    Cell getRezerevedCell() {
        return rezereved;
    }

    boolean isRezerevd() {
        return getRezerevedCell() != null;
    }

    //****************************************
    void preProcess(World world) {
        this.world = world;
        initMap();
        Blaster.set(objectiveCells);
        Cell[] res = world.getMap().getMyRespawnZone();
        for (int i = 0; i < res.length; i++) {
            Cell[] avires = Utility.availableCells(world.getMap(), 1, res[i]);
            for (int j = 0; j < avires.length; j++) {
                if (avires[j].isWall() && Utility.distance(res[i], avires[j]) == 1) {
                    BEST_FOR_GUARDIAN_RESPAWNZONE = i;
                    break;
                }
            }
            if (BEST_FOR_GUARDIAN_RESPAWNZONE != -1)
                break;
        }
        setINDEX();
    }

    void pickTurn(World world) {
        this.world = world;
        pickHeroInPhase();
    }

    void moveTurn(World world) {
        this.world = world;
        Utility.printMap(world);
        init();
        Hero[] heroes = world.getMyHeroes();
        if (flag == 0) {
            lastData.world = world;
            lastData.setBlasterEnemy(world.getOppHeroes());
            flag++;
        }
        lastData.world = world;
        lastData.bombReducer();
        lastData.isAnyBombUsed(world);
        Sentry_AI sentry;
        Blaster_AI blaster;
        Guardian_AI guardian;

        sentry = new Sentry_AI(heroes[FORTH_HERO], world, lastData);
        sentry.SentryMove();

        setInProcess(SECOND_HERO);
        blaster = new Blaster_AI(world, this, heroes[SECOND_HERO], histories[indexOfHeroInHistory(heroes[SECOND_HERO])], objectiveCells);
        blaster.move();

        setInProcess(THERD_HERO);
        blaster = new Blaster_AI(world, this, heroes[THERD_HERO], histories[indexOfHeroInHistory(heroes[THERD_HERO])], objectiveCells);
        blaster.move();

        guardian = new Guardian_AI(heroes[FIRST_HERO], world);
        guardian.movePhase();

    }

    void actionTurn(World world) {
        this.world = world;
        init();
        Hero[] heroes = world.getMyHeroes();

        Sentry_AI sentry;
        Blaster_AI blaster;
        Guardian_AI guardian;

        sentry = new Sentry_AI(heroes[FORTH_HERO], world, lastData);
        sentry.actionPhase();

//        Blaster.blasterAttack(this,world,heroes[FORTH_HERO]);
        setInProcess(SECOND_HERO);
        blaster = new Blaster_AI(world, this, heroes[SECOND_HERO], histories[indexOfHeroInHistory(heroes[SECOND_HERO])], objectiveCells);
        blaster.attack();
//        Blaster.blasterAttack(this, world, heroes[SECOND_HERO]);
        setInProcess(THERD_HERO);
        blaster = new Blaster_AI(world, this, heroes[THERD_HERO], histories[indexOfHeroInHistory(heroes[THERD_HERO])], objectiveCells);
        blaster.attack();
//        Blaster.blasterAttack(this, world, heroes[THERD_HERO]);

        guardian = new Guardian_AI(heroes[FIRST_HERO], world);
        guardian.actionPhase();

    }

//****************************************

    /**
     * this
     * method initialize
     * our need
     * across the
     * phase or
     * turn
     */
    private void init() {
        initHistorys(world.getMyHeroes());
        initHeroInVision();
    }

    private void setINDEX() {
        FIRST_HERO = BEST_FOR_GUARDIAN_RESPAWNZONE;
        if (BEST_FOR_GUARDIAN_RESPAWNZONE == 0) {
            SECOND_HERO = 1;
            FORTH_HERO = 2;
            THERD_HERO = 3;
        } else if (BEST_FOR_GUARDIAN_RESPAWNZONE == 1) {
            SECOND_HERO = 0;
            THERD_HERO = 3;
            FORTH_HERO = 2;
        } else if (BEST_FOR_GUARDIAN_RESPAWNZONE == 2) {
            SECOND_HERO = 0;
            THERD_HERO = 3;
            FORTH_HERO = 1;
        } else {
            FIRST_HERO = 3;
            SECOND_HERO = 0;
            THERD_HERO = 2;
            FORTH_HERO = 1;
        }
    }

    private void initHeroInVision() {
        herosInVision = Utility.getSawHero(world);
    }

    private void initMap() {
        client.model.Map map = world.getMap();
        Cell objtive[] = map.getObjectiveZone();
        int minRow = Integer.MAX_VALUE, maxRow = Integer.MIN_VALUE, minCol = Integer.MAX_VALUE, maxCol = Integer.MIN_VALUE;
        for (int i = 0; i < objtive.length; i++) {
            if (minRow > objtive[i].getRow())
                minRow = objtive[i].getRow();

            if (maxRow < objtive[i].getRow())
                maxRow = objtive[i].getRow();

            if (minCol > objtive[i].getColumn())
                minCol = objtive[i].getColumn();

            if (maxCol < objtive[i].getColumn())
                maxCol = objtive[i].getColumn();
        }
        objectiveCells = new Cell[maxRow - minRow + 1][maxCol - minCol + 1];
        int row = 0, col = 0;
        for (int i = minRow; i <= maxRow; i++) {
            for (int j = minCol; j <= maxCol; j++) {
                objectiveCells[row][col++] = map.getCells()[i][j];
            }
            col = 0;
            row++;
        }
        r = objectiveCells[objectiveCells.length / 2][0].getRow();
        c1 = objectiveCells[objectiveCells.length / 2][0].getColumn();
        c2 = objectiveCells[objectiveCells.length / 2][objectiveCells.length - 1].getColumn();
    }

    /**
     * in method
     *     check migkone
     *     ke ag
     *     histories ma
     *     init nashode
     *     initesh kone
     * */
    private void initHistorys(Hero[] myHero) {
        if (histories == null) {
            histories = new History[4];
            for (int i = 0; i < 4; i++)
                histories[i] = new History(myHero[i].getId());
        }
    }

    /**
     *      in method
     *      mige kodum
     *      az hero
     *
     *      haye ma(@myHeroes),hero'ye doshman(oppHero) ro didan
     * */
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
     *      we pick
     *      our hero for
     *      game in this method
     * */
    private void pickHeroInPhase() {
        if (PICK_PHASE_COUNTER == FIRST_HERO) {
            world.pickHero(HeroName.GUARDIAN);
        } else if (PICK_PHASE_COUNTER == SECOND_HERO) {
            world.pickHero(HeroName.BLASTER);
        } else if (PICK_PHASE_COUNTER == FORTH_HERO) {
            world.pickHero(HeroName.SENTRY);
        } else if (PICK_PHASE_COUNTER == THERD_HERO) {
            world.pickHero(HeroName.BLASTER);
        }

        PICK_PHASE_COUNTER++;

    }


    private int indexOfHeroInHistory(Hero hero) {
        for (int i = 0; i < 4; i++) {
            if (histories[i].getHeroID() == hero.getId()) {
                return i;
            }
        }
        System.out.println(String.format("i:%d , HEREID:%d", -1, hero.getId()));
        return -1;
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

    public Hero getBlaster() {
        if (getInProcess() == SECOND_HERO)
            return world.getMyHeroes()[THERD_HERO];
        return world.getMyHeroes()[SECOND_HERO];
    }
}