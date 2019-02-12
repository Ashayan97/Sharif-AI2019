package client;

import client.Utility;
import client.model.Hero;

public class Utility_Attack {
    public static Utility.ATTACK_STATE blasterAttackToHealer(Hero fHero , Hero sHero){
        //first Hero is Blaster and second Hero is Healer
        return Utility.ATTACK_STATE.CANTATTACK;
    }
    public static Utility.ATTACK_STATE blasterAttackToSentry(Hero fHero , Hero sHero){
        //first Hero is Blaster and second Hero is Sentry

        return Utility.ATTACK_STATE.CANTATTACK;
    }
    public static Utility.ATTACK_STATE blasterAttackToBlaster(Hero fHero , Hero sHero){
        //first Hero is Blaster and second Hero is Blaster

        return Utility.ATTACK_STATE.CANTATTACK;
    }
    public static Utility.ATTACK_STATE blasterAttackToGuardian(Hero fHero , Hero sHero){
        //first Hero is Blaster and second Hero is Guardian

        return Utility.ATTACK_STATE.CANTATTACK;
    }
}
