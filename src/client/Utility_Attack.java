package client;
import client.model.AbilityName;
import client.model.Hero;

import static client.Utility.ATTACK_STATE.DORADOR;
import static client.Utility.ATTACK_STATE.TANBETAN;
import static client.Utility.ATTACK_STATE.CANTATTACK;

public class Utility_Attack {
    private final static int range_of_blaster_bomb = 5;
    private final static int range_of_blaster_attack = 4 ;
    private final static int range_of_bomb = 2;
    public static Utility.ATTACK_STATE blasterAttackToHealer(Hero fHero , Hero sHero){
        //first Hero is Blaster and second Hero is Healer
        //TODO
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
        //TODO
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
