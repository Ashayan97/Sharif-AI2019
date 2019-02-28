
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
    private LastData lastData = new LastData();
    private Sentry_AI sentry;
    private Map<Integer, Hero> heroInAttack = new HashMap<>();
    private Map<Integer, Cell> dodgeMap = new HashMap<>();
    private Map<Integer, Cell> nextCell = new HashMap<>();
    private int FIRST_HERO = -1; // is Guardian
    private int SECOND_HERO = -1;
    private int THERD_HERO = -1;
    private int FORTH_HERO = -1;
    private int BEST_FOR_GUARDIAN_RESPAWNZONE = -1;

    //****************************************
    void preProcess(World world) {
        this.world = world;
        objectiveCells = world.getMap().getObjectiveZone();
        initWallCell();
        Blaster.set(world);
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
        Hero[] hero = world.getMyHeroes();
        if (flag == 0) {
            lastData.world = world;
            lastData.setBlasterEnemy(world.getOppHeroes());
            flag++;
        }
        lastData.world = world;
        lastData.bombReducer();
        lastData.isAnyBombUsed(world);
        Sentry_AI sentry = new Sentry_AI(hero[FORTH_HERO], world, lastData);
        if (hero[FORTH_HERO].getCurrentHP() != 0)
            sentry.SentryMove();
//        Blaster.blasterMove(this,world,hero[FORTH_HERO],histories[indexOfHeroInHistory(hero[FORTH_HERO])]);
        Blaster.blasterMove(this, world, hero[SECOND_HERO], histories[indexOfHeroInHistory(hero[SECOND_HERO])]);
        Blaster.blasterMove(this, world, hero[THERD_HERO], histories[indexOfHeroInHistory(hero[THERD_HERO])]);
//        Blaster.blasterMove(this,world,hero[FIRST_HERO],histories[indexOfHeroInHistory(hero[FIRST_HERO])]);

        Guardian_AI guardian;
//        guardian= new Guardian_AI(hero[0],world);
//        guardian.movePhase();
//        guardian= new Guardian_AI(hero[1],world);
//        guardian.movePhase();
        guardian = new Guardian_AI(hero[FIRST_HERO], world);
        guardian.movePhase();

    }

    void actionTurn(World world) {
        this.world = world;
        init();
        Hero[] heroes = world.getMyHeroes();

        Sentry_AI sentry = new Sentry_AI(heroes[FORTH_HERO], world, lastData);
        if (heroes[FORTH_HERO].getCurrentHP() != 0)
            sentry.actionPhase();

//        Blaster.blasterAttack(this,world,heroes[FORTH_HERO]);
        Blaster.blasterAttack(this, world, heroes[SECOND_HERO]);
        Blaster.blasterAttack(this, world, heroes[THERD_HERO]);
//        Blaster.blasterAttack(this,world,heroes[FIRST_HERO]);

        Guardian_AI guardian;
//        guardian  = new Guardian_AI(heroes[0],world);
//        guardian.actionPhase();
//        guardian  = new Guardian_AI(heroes[1],world);
//        guardian.actionPhase();
        guardian = new Guardian_AI(heroes[FIRST_HERO], world);
        guardian.actionPhase();

    }

//****************************************
            /*this
    method initialize
    our need
    across the
    phase or
    turn
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
        } else if (FIRST_HERO == 1) {
            SECOND_HERO = 0;
            FORTH_HERO = 2;
            THERD_HERO = 3;
        } else if (BEST_FOR_GUARDIAN_RESPAWNZONE == 2) {
            SECOND_HERO = 0;
            FORTH_HERO = 1;
            THERD_HERO = 3;
        } else {
            SECOND_HERO = 0;
            FORTH_HERO = 1;
            THERD_HERO = 2;
        }
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

    /*
    in method
    check migkone
    ke ag
    histories ma
    init nashode
     *
    initesh kone
     */

    private void initHistorys(Hero[] myHero) {
        if (histories == null) {
            histories = new History[4];
            for (int i = 0; i < 4; i++)
                histories[i] = new History(myHero[i].getId());
        }
    }

    /*
    in method
    mige kodum
    az hero

    haye ma(@myHeroes),hero'ye doshman(oppHero) ro didan
            */

    private Hero[] whoSeeThisHero(Hero oppHeroe) {
        Vector<Hero> heroes = new Vector<>();
        Hero[] myHeroes = world.getMyHeroes();
        Cell oppHeroCurrentCell = oppHeroe.getCurrentCell();
        for (Hero myHero : myHeroes)
            if (world.isInVision(myHero.getCurrentCell(), oppHeroCurrentCell))
                heroes.add(myHero);
        return heroes.toArray(new Hero[]{});
    }

    /*
    we pick
    our hero for
    game in this method
     */

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