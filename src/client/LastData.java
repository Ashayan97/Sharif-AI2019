package client;

import client.model.*;

import java.util.ArrayList;

public class LastData {
    public Cell Des = null;
    public Cell lastCell = null;
    public Hero[] inVision;
    public Hero[] inRangeAtkHeroes;
    public Hero[] ourHeroes;
    public Hero[] blasterEnemy;
    public Hero inAtk=null;
    public int[] bomber = new int[4];
    public World world;

    public void bombReducer() {
        if (world.getMovePhaseNum() == 1) {
            for (int i = 0; i < bomber.length; i++) {
                if (bomber[i] != 0)
                    bomber[i]--;
            }
        }
    }

    public void setBlasterEnemy(Hero[] Enemy) {
        ArrayList<Hero> blaster = new ArrayList<>();
        for (Hero aEnemy : Enemy) {
            if (aEnemy.getName().equals(HeroName.BLASTER))
                blaster.add(aEnemy);
        }
        for (int i = 0; i < bomber.length; i++) {
            bomber[i] = 0;
        }
        blasterEnemy = blaster.toArray(new Hero[blaster.size()]);
    }

    public void isAnyBombUsed() {
        if (world.getMovePhaseNum() == 1) {
            CastAbility[] castAbilities = world.getOppCastAbilities();
            System.out.println("\n\n\n======================");
            for (int i = 0; i < castAbilities.length; i++) {
                if (castAbilities[i].getAbilityName().equals(AbilityName.BLASTER_BOMB))
                    System.out.println("BOOOOOOOOOOOOOOOOOOOOOOOOOOOOOMMMMMMMMMMMMMMMMMMMBBBBBBBBBBBBBBBBBBBBBB");
                if (castAbilities[i].getAbilityName().equals(AbilityName.SENTRY_RAY))
                    System.out.println("RAYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
                System.out.println("=============================   "+castAbilities.length+"    ========================");

            }
            System.out.println("+===================================================\n");
            for (int i = 0; i < castAbilities.length; i++) {
                for (int j = 0; j < blasterEnemy.length; j++) {
                    if (castAbilities[i].getAbilityName().equals(AbilityName.BLASTER_BOMB)&&castAbilities[i].getCasterId() == blasterEnemy[j].getId() && bomber[j] != 0) {
                        bomber[j] = 4;
                    }
                }
            }
        } else
            return;
    }

    public boolean returnEnemyBombActivation(Hero enemy) {
        for (int i = 0; i < blasterEnemy.length; i++) {
            if (blasterEnemy[i].getId() == enemy.getId())
                if (bomber[i] == 0)
                    return true;
        }
        return false;
    }

}
