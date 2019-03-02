package client;

import client.model.*;
import client.model.Phase;

import java.util.ArrayList;

public class Sentry_AI {
    static private int Blaster_Danger_Range = 6;
    static private int Guardian_Danger_Range = 4;
    static private int Healer_Danger_Range = 4;
    static private int Sentry_Danger_Range = 0;

    ///////////
    private Hero hero;
    private World world;
    private Map map;
    private Range_fight rangeFight;
    private Hero[] heroes;
    private boolean needToMove = false;
    private boolean atkMode = false;
    private Hero lastAtkTo = null;
    private Hero[] inVision;
    private Hero[] inRangeAtkHeroes;
    private Hero[] ourHeroes;
    private LastData lastData;
//    private Cell[] bestOfZone;

    public Sentry_AI(Hero hero, World world, LastData lastData) {
        if (world == null)
            throw new RuntimeException("NULL WORLD");
        this.hero = hero;
        this.world = world;
        this.map = world.getMap();
        rangeFight = new Range_fight(world);
        heroes = rangeFight.inVisionEnemy(hero, 7);
        inVision = rangeFight.inVisionEnemy(hero);
        inRangeAtkHeroes = rangeFight.InRangeAtk(hero, 7);
        ourHeroes = world.getMyHeroes();
        this.lastData = lastData;
    }


    public boolean canAtk() {
        for (Hero heroe : heroes) {
            return world.isInVision(heroe.getCurrentCell(), hero.getCurrentCell());
        }
        return false;
    }


    public void newActionPhase() {
        Hero inATK = null;
        if (hero.getAbility(AbilityName.SENTRY_RAY).getAPCost() <= world.getAP()) {
            if (hero.getAbility(AbilityName.SENTRY_RAY).isReady()) {
                for (Hero heroe : inVision) {
                    if (inATK == null)
                        inATK = heroe;
                    else if (heroe.getCurrentHP() <= inATK.getCurrentHP())
                        inATK = heroe;
                }
                if (inATK != null)
                    world.castAbility(hero, AbilityName.SENTRY_RAY, inATK.getCurrentCell());
            }
        }

        if (hero.getAbility(AbilityName.SENTRY_DODGE).getAPCost() <= world.getAP()) {
            if (needToDodge()) {
                if (hero.getAbility(AbilityName.SENTRY_DODGE).isReady()) {
                    Cell Des = rangeFight.bestDodge(hero.getCurrentCell(), 3, 6);
                    if (!hero.getCurrentCell().equals(Des)) {
                        world.castAbility(hero, AbilityName.SENTRY_DODGE, Des);
                        return;
                    }
                }
            }
        }

        if (hero.getAbility(AbilityName.SENTRY_ATTACK).getAPCost() <= world.getAP()) {
            for (Hero heroe : inVision) {
                if (inATK == null)
                    inATK = heroe;
                else if (heroe.getCurrentHP() < inATK.getCurrentHP())
                    inATK = heroe;
            }
            if (inATK != null)
                world.castAbility(hero, AbilityName.SENTRY_ATTACK, inATK.getCurrentCell());

        }
    }


    public boolean needToDodge() {
        if (hero.getCurrentCell().isInMyRespawnZone())
            return true;
        for (Hero inRangeAtkHeroe : inRangeAtkHeroes) {
            if (inRangeAtkHeroe.getName().equals(HeroName.BLASTER)) {
                if (world.manhattanDistance(inRangeAtkHeroe.getCurrentCell(), hero.getCurrentCell()) < Blaster_Danger_Range || lastData.returnEnemyBombActivation(inRangeAtkHeroe))
                    return true;
            }
            if (inRangeAtkHeroe.getName().equals(HeroName.GUARDIAN))
                if (world.manhattanDistance(inRangeAtkHeroe.getCurrentCell(), hero.getCurrentCell()) < Guardian_Danger_Range)
                    return true;
            if (inRangeAtkHeroe.getName().equals(HeroName.HEALER))
                if (world.manhattanDistance(inRangeAtkHeroe.getCurrentCell(), hero.getCurrentCell()) < Healer_Danger_Range)
                    return true;
            if (inRangeAtkHeroe.getName().equals(HeroName.SENTRY))
                if (world.manhattanDistance(inRangeAtkHeroe.getCurrentCell(), hero.getCurrentCell()) < Sentry_Danger_Range)
                    return true;
        }
        return false;
    }

    private void setLastData() {
        lastData.inRangeAtkHeroes = inRangeAtkHeroes;
        lastData.inVision = inVision;
        lastData.lastCell = hero.getCurrentCell();
        lastData.ourHeroes = ourHeroes;
    }


    public void newMove() {
        if (hero.getCurrentHP() == 0 || hero.getCurrentCell().isInMyRespawnZone()) {
            lastData.Des = null;
            return;
        }
        Direction dir;

        ArrayList<Cell> ourHeroes = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            ourHeroes.add(world.getMyHeroes()[i].getCurrentCell());
        }
        if (hero.getCurrentCell().isInObjectiveZone()&&!rangeFight.canSeeAnyOne())
            return;
        if (heroes.length != 0 && world.manhattanDistance(hero.getCurrentCell(), rangeFight.NearstEnemy(hero.getCurrentCell(), heroes)) == 7&&!isBlaster()) {
            return;

        } else {
            if (rangeFight.canSeeAnyOne()) {
                Cell Des = rangeFight.enemyTarget(hero,lastData);
                if (Des!=null)
                    if (!Des.equals(hero.getCurrentCell()))
                        world.moveHero(hero,world.getPathMoveDirections(hero.getCurrentCell(),Des)[0]);
                else {
                        dir = ObjectMove();
                        world.moveHero(hero,dir);
                    }
//                Cell lastDes = null;
//                ArrayList<Hero> Baned=new ArrayList<>();
//                if (lastData.returnEnemyBombActivation(world.getOppHero(Des))) {
//                    lastDes=Des;
//                    Des=null;
//                    int i=0;
//                    while (lastData.returnEnemyBombActivation(world.getOppHero(lastDes))&&lastDes!=null) {
//                        lastDes=rangeFight.enemyTarget(hero,Baned.toArray(new Hero[Baned.size()]));
//                    }
//                    if (lastDes!=null){
//                        Des=lastDes;
//                    }
//                    else {
//
//                    }
//                }

            } else {
                dir = ObjectMove();
                world.moveHero(hero, dir);
            }
        }
    }


    private Direction ObjectMove() {
        Cell[] cells = new Cell[4];
        for (int i = 0; i < 4; i++) {
            cells[i] = ourHeroes[i].getCurrentCell();
        }
        Cell des = rangeFight.findNearestZoneCell(hero.getCurrentCell());
        Cell next = Utility.nextCell(world, hero.getCurrentCell(), des, cells);
        if (rangeFight.isSafe(next, 8)) {
            Direction[] dir = world.getPathMoveDirections(hero.getCurrentCell(), des, cells);
            return dir[0];
        } else
            return null;
    }

    private Direction ObjectMove(Cell cell) {
        ArrayList<Cell> cells = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (ourHeroes[i].getCurrentHP() != 0)
                cells.add(ourHeroes[i].getCurrentCell());
        }
        cells.add(cell);
        Cell des = rangeFight.findNearestZoneCell(hero.getCurrentCell());
        Cell next = Utility.nextCell(world, hero.getCurrentCell(), des, cells.toArray(new Cell[cells.size()]));
        if (rangeFight.isSafe(next, 6)) {
            Direction[] dir = world.getPathMoveDirections(hero.getCurrentCell(), des, cells);
            return dir[0];
        } else
            return null;
    }


    private boolean isBlaster() {
        for (Hero inRangeAtkHeroe : inRangeAtkHeroes) {
            if (inRangeAtkHeroe.getName().equals(HeroName.BLASTER) && lastData.returnEnemyBombActivation(inRangeAtkHeroe))
                return true;
        }
        return false;
    }


    public Hero getHero() {
        return hero;
    }
}
