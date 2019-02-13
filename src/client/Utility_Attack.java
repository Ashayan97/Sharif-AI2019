package client;
import client.model.AbilityName;
import client.model.Hero;

import static client.ATTACK_STATE.DORADOR;
import static client.ATTACK_STATE.TANBETAN;
import static client.ATTACK_STATE.CANTATTACK;
import static client.ATTACK_STATE.SCAPE;

/**
 * @author :  Amirhossein Azimyzadeh
 * */

public class Utility_Attack {
    public final static int range_of_blaster_bomb = 5;
    public final static int range_of_blaster_attack = 4 ;
    public final static int range_of_bomb = 2;
    public final static int range_of_guardian_attack=1;
    public final static int range_of_healer_attack=4;

    public final static int damage_of_healer_attack=20;
    public final static int damage_of_sentry_ray = 50;
    public final static int damage_of_blaster_bomb=40;
    public final static int damage_of_blaster_attack=30;
    //====================================================================================//
    //Blaster Attacks Methods
    public static ATTACK_STATE blasterAttackToHealer(Hero fHero , Hero sHero){
        //first Hero is Blaster and second Hero is Healer
        /** if distance is OK and blaster bomb is OK too @return : DORADOR */
        int distance =Utility.Distance(fHero.getCurrentCell(),sHero.getCurrentCell());
        //scape if our hero will be die !-->
        if(fHero.getCurrentHP()<=damage_of_healer_attack){
            return SCAPE;
        }
        //if we are too close to the
        if(distance<=range_of_healer_attack){
            int number_of_turn_needed_to_kill_enemy = sHero.getCurrentHP()/damage_of_blaster_attack;
            int number_of_turn_needed_to_kill_me = fHero.getCurrentHP()/damage_of_healer_attack;
            if(number_of_turn_needed_to_kill_enemy>number_of_turn_needed_to_kill_me)
                return SCAPE;
        }
        if(fHero.getAbility(AbilityName.BLASTER_BOMB ).isReady() && distance<=range_of_blaster_bomb+range_of_blaster_bomb ){
            return DORADOR;
        }
        if(distance<=range_of_blaster_attack){
            return TANBETAN;
        }
        /**check distance*/
        return CANTATTACK;
    }
    public static ATTACK_STATE blasterAttackToSentry(Hero fHero , Hero sHero){
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
    public static ATTACK_STATE blasterAttackToBlaster(Hero fHero , Hero sHero){
        //first Hero is Blaster and second Hero is Blaster
        int distance = Utility.Distance(fHero.getCurrentCell(),sHero.getCurrentCell());
        int myHeroHP = fHero.getCurrentHP();
        int enemyHeroHP = sHero.getCurrentHP();

        if(myHeroHP>=enemyHeroHP&&
                fHero.getAbility(AbilityName.BLASTER_BOMB).isReady()&&
                distance<=range_of_blaster_bomb+range_of_bomb){
            return DORADOR;
        }
        if(myHeroHP>=enemyHeroHP&&
                distance<=range_of_blaster_attack){
            return TANBETAN;
        }
        //check for scape -->
        if(myHeroHP<enemyHeroHP&&
                distance<=range_of_blaster_attack) {
            return SCAPE;
        }
        if(myHeroHP<enemyHeroHP&&
                sHero.getAbility(AbilityName.BLASTER_BOMB).isReady()&&
                distance<=range_of_blaster_bomb) {
            return SCAPE;
        }
        return CANTATTACK;
    }
    public static ATTACK_STATE blasterAttackToGuardian(Hero fHero , Hero sHero){
        //first Hero is Blaster and second Hero is Guardian
        //TODO
        int distance = Utility.Distance(fHero.getCurrentCell(),sHero.getCurrentCell());
        int myHeroHP = fHero.getCurrentHP();
        int enemyHeroHP = sHero.getCurrentHP();

        //check if enemy guardian is loser -->
        if(enemyHeroHP<=damage_of_blaster_attack &&
                distance<=range_of_blaster_attack &&
                !sHero.getAbility(AbilityName.GUARDIAN_FORTIFY).isReady()){
            return TANBETAN;
        }
        if(fHero.getAbility(AbilityName.BLASTER_BOMB).isReady() &&
                distance<=range_of_blaster_bomb+range_of_bomb &&
                enemyHeroHP<=damage_of_blaster_bomb &&
                !sHero.getAbility(AbilityName.GUARDIAN_FORTIFY).isReady()){
            return DORADOR;
        }
        // if enemy hero (guardian) is in Bomb range(or Attack range) of our blaster  and -->
        //guardian can't attack to us
        if(distance<=range_of_blaster_bomb && distance> range_of_guardian_attack){
            if(fHero.getAbility(AbilityName.BLASTER_BOMB).isReady())
                return DORADOR;
            if(distance<=range_of_blaster_attack)
                return TANBETAN;
        }
        //if we are too close to the guardian
        //calculate condition
        if(distance<=range_of_guardian_attack){
            int number_of_turn_needed_to_kill_enemy = enemyHeroHP/range_of_blaster_attack ;
            int number_Of_turn_needed_to_kill_me =  myHeroHP/range_of_guardian_attack ;

            if(number_of_turn_needed_to_kill_enemy>number_Of_turn_needed_to_kill_me)
                return SCAPE;
            else
                return TANBETAN;
        }

        return  CANTATTACK;
    }
    //====================================================================================//
    //Sentry Attacks Methods
    public static ATTACK_STATE sentryAttackToHealer(Hero fHero , Hero sHero){
        //TODO
        return CANTATTACK;
    }
    public static ATTACK_STATE sentryAttackToSentry(Hero fHero , Hero sHero){
        //TODO
        return CANTATTACK;
    }
    public static ATTACK_STATE sentryAttackToBlaster(Hero fHero , Hero sHero){
        //TODO
        return CANTATTACK;
    }
    public static ATTACK_STATE sentryAttackToGuardian(Hero fHero , Hero sHero){
        //TODO
        return CANTATTACK;
    }
    //====================================================================================//
    //Healer Attacks Methods
    public static ATTACK_STATE healerAttackToHealer(Hero fHero , Hero sHero){
        //TODO
        return CANTATTACK;
    }
    public static ATTACK_STATE healerAttackToSentry(Hero fHero , Hero sHero){
        //TODO
        return CANTATTACK;
    }
    public static ATTACK_STATE healerAttackToBlaster(Hero fHero , Hero sHero){
        //TODO
        return CANTATTACK;
    }
    public static ATTACK_STATE healerAttackToGuardian(Hero fHero , Hero sHero){
        //TODO
        return CANTATTACK;
    }
    //====================================================================================//
    //Guardian Attack Methods
    public static ATTACK_STATE guardianAttackToHealer(Hero fHero,Hero sHero){
        //TODO
        return CANTATTACK;
    }
    public static ATTACK_STATE guardianAttackToSentry(Hero fHero,Hero sHero){
        //TODO
        return CANTATTACK;
    }
    public static ATTACK_STATE guardianAttackToBlaster(Hero fHero,Hero sHero){
        //TODO
        return CANTATTACK;
    }
    public static ATTACK_STATE guardianAttackToGuardian(Hero fHero,Hero sHero){
        //TODO
        return CANTATTACK;
    }
    //====================================================================================//


    static ATTACK_STATE CanAttack_Guardian(Hero fHero, Hero sHero) {
        switch (sHero.getName()){
            case HEALER:
            case SENTRY:
            case BLASTER:
            case GUARDIAN:
        }
        return CANTATTACK;
    }

     static ATTACK_STATE CanAttack_Healer(Hero fHero, Hero sHero) {
        switch (sHero.getName()){
            case HEALER:
                return healerAttackToHealer(fHero,sHero);
            case SENTRY:
                return healerAttackToSentry(fHero,sHero);
            case BLASTER:
                return healerAttackToBlaster(fHero,sHero);
            case GUARDIAN:
                return healerAttackToGuardian(fHero,sHero);
        }
        return CANTATTACK;
    }

     static ATTACK_STATE CanAttack_Sentry(Hero fHero, Hero sHero) {
        switch (sHero.getName()){
            case HEALER:
                return sentryAttackToHealer(fHero,sHero);
            case SENTRY:
                return sentryAttackToSentry(fHero,sHero);
            case BLASTER:
                return sentryAttackToBlaster(fHero,sHero);
            case GUARDIAN:
                return sentryAttackToGuardian(fHero,sHero);
        }
        return CANTATTACK;
    }

     static ATTACK_STATE CanAttack_Blaster(Hero fHero, Hero sHero) {
        switch (sHero.getName()){
            case HEALER:
                return Utility_Attack.blasterAttackToHealer(fHero,sHero);
            case SENTRY:
                return Utility_Attack.blasterAttackToSentry(fHero,sHero);
            case BLASTER:
                return Utility_Attack.blasterAttackToBlaster(fHero,sHero);
            case GUARDIAN:
                return Utility_Attack.blasterAttackToGuardian(fHero,sHero);
        }
        return CANTATTACK;
    }
}
