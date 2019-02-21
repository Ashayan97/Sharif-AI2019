package client;

import client.model.*;

import java.util.ArrayList;

public class Utility_Group_See {
    public static MOVE_STRATEGY BlasterStrategy(Hero hero, World world,boolean phase) {
        MOVE_STRATEGY strategy=new MOVE_STRATEGY();
        Hero[] EnemyHeroes = world.getOppHeroes();
        Cell[] OurLocationArray = new Cell[3];
        ArrayList<Cell> onAttackHero;
        ArrayList<Cell> EnemyLocationArray = new ArrayList<>();
        for (int i = 0; i < 4; i++)
            if (EnemyHeroes[i].getCurrentCell().getRow() != (-1))
                EnemyLocationArray.add(EnemyHeroes[i].getCurrentCell());
        if (hero.getAbility(AbilityName.BLASTER_BOMB).isReady()) {
            int min = -1;
            Cell MinLocation = null;
            for (int i = 0; i < EnemyLocationArray.size(); i++) {
                if (min == -1)
                    min = world.manhattanDistance(EnemyLocationArray.get(i), hero.getCurrentCell());
                else if (world.manhattanDistance(EnemyLocationArray.get(i), hero.getCurrentCell()) < min)
                    MinLocation = EnemyLocationArray.get(i);
            }
            Cell AttackLocation;
            onAttackHero=new ArrayList<>();
            for (int i=0;i<EnemyLocationArray.size();i++){
                if (world.manhattanDistance(EnemyLocationArray.get(i),MinLocation) <= 2)
                    onAttackHero.add(EnemyLocationArray.get(i));
            }
            if(onAttackHero.size()<2) {
//                if (world.get)
                strategy.attack_state = Utility_Attack.blasterAttackToBlaster(hero, world.getOppHero(EnemyLocationArray.get(0)));
                Direction[] path=world.getPathMoveDirections(hero.getCurrentCell(),EnemyLocationArray.get(0));
                for (int i = path.length; i >=0; i--) {

                }
            }




        }

        return null;
    }

    public static boolean IsInEffectArea(World world, Cell center, Cell target, int range) {
        if (world.manhattanDistance(center, target) <= 2)
            return true;
        else
            return false;
    }

}
