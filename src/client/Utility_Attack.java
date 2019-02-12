package client;
import client.model.AbilityName;
import client.model.Hero;

import static client.Utility.ATTACK_STATE.DORADOR;
import static client.Utility.ATTACK_STATE.TANBETAN;
import static client.Utility.ATTACK_STATE.CANTATTACK;
import static client.Utility.ATTACK_STATE.SCAPE;

/**
 * @author :  Amirhossein Azimyzadeh
 * */

public class Utility_Attack {
    private final static int range_of_blaster_bomb = 5;
    private final static int range_of_blaster_attack = 4 ;
    private final static int range_of_bomb = 2;

    private final static int damage_of_sentry_ray = 50;
    private final static int damage_of_blaster_bomb=40;
    private final static int damage_of_blaster_attack=30;

    public static Utility.ATTACK_STATE blasterAttackToHealer(Hero fHero , Hero sHero){
        //first Hero is Blaster and second Hero is Healer
        /** if distance is OK and blaster bomb is OK too @return : DORADOR */
        int distance =Utility.Distance(fHero.getCurrentCell(),sHero.getCurrentCell());
        if(fHero.getAbility(AbilityName.BLASTER_BOMB ).isReady() && distance<=range_of_blaster_bomb+range_of_blaster_bomb ){
            return DORADOR;
        }
        if(distance<=range_of_blaster_attack){
            return TANBETAN;
        }
        /**check distance*/
        return CANTATTACK;
    }
    public static Utility.ATTACK_STATE blasterAttackToSentry(Hero fHero , Hero sHero){
        //first Hero is Blaster and second Hero is Sentry
        //check scape state --> 1- check current HP of our Blaster 2- check state of enemy ray
        int distance = Utility.Distance(fHero.getCurrentCell(),sHero.getCurrentCell());
        //check if HP of enemy is less than blaster bomb
        if(fHero.getAbility(AbilityName.BLASTER_BOMB).isReady()&&
                distance<=range_of_blaster_bomb+range_of_bomb &&
                sHero.getCurrentHP()<=damage_of_blaster_bomb){
            return DORADOR;
        }
        //check if we can kill with one attack
        if(sHero.getCurrentHP()<=damage_of_blaster_attack){
            return TANBETAN;
        }
        if(fHero.getCurrentHP()<=damage_of_sentry_ray &&
                sHero.getAbility(AbilityName.SENTRY_RAY).isReady()) {
            return SCAPE;
        }
        //check if we can hit enemy with bomb -->hdt
        if(fHero.getAbility(AbilityName.BLASTER_BOMB).isReady() &&
                distance<=range_of_blaster_bomb+range_of_bomb){
            return DORADOR;
        }
        //check if we can attack with blaster attack
        if(distance<=range_of_blaster_attack){
            return TANBETAN;
        }
        return CANTATTACK;
    }
    public static Utility.ATTACK_STATE blasterAttackToBlaster(Hero fHero , Hero sHero){
        //first Hero is Blaster and second Hero is Blaster
        //TODO

        return CANTATTACK;
    }
    public static Utility.ATTACK_STATE blasterAttackToGuardian(Hero fHero , Hero sHero){
        //first Hero is Blaster and second Hero is Guardian
        //TODO

        return  CANTATTACK;
    }
}
