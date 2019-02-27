package client;

import client.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author : Amirhossein
 * @version : 1.1
 * guardian hero
 * */
public class Guardian_AI {
    private Hero guardian;
    private World world;
    private Map map;
    private Hero[] alliedHero;
    private static final int criticalHealthPoint = 40 ;

    public Guardian_AI(Hero guardian, World world) {
        if(guardian==null)
            throw new RuntimeException("HERO PASSED IS NULL");
        this.guardian = guardian;
        this.world = world;
        this.map = world.getMap();
        this.alliedHero = world.getMyHeroes();
    }

    public void actionPhase(){
        if(guardian.getCurrentHP()==0 || world.getAP()<15)
            return;
        // if guardian in objective Zone -->
        if(canSeeAnyOne()){
            Hero[] enemyHeroes = world.getOppHeroes();
            ArrayList<Hero> enemyHeroInVision = getEnemyHeroesInVision(enemyHeroes);
            ArrayList<Hero> attackAbleEnemies =  getAttackAbleEnemies(enemyHeroes);
            ArrayList<Hero> enemiesInObjective =  getEnemyHeroesInObjective(enemyHeroes);
            //attack
            if(!attackAbleEnemies.isEmpty()){
                // fortify is available
                if(isFortifyReady() && world.getAP() >= guardian.getAbility(AbilityName.GUARDIAN_FORTIFY).getAPCost()) {
                    world.castAbility(guardian, AbilityName.GUARDIAN_FORTIFY, guardian.getCurrentCell());
                    return;
                }
                else {
                    Cell effectiveCell = findEffectiveCell(attackAbleEnemies);
                    world.castAbility(guardian, AbilityName.GUARDIAN_ATTACK, effectiveCell);
                    return; // stop continue this method
                }
            }
            //Fortify if necessary
            if(isDangerTime()){
                if(isFortifyReady()){
                    world.castAbility(guardian, AbilityName.GUARDIAN_FORTIFY, guardian.getCurrentCell());
                    Logger.log("============================FORTIFY==========================",Logger.YELLOW);
                    return;
                }else if(isDogeReady()){
                    Hero nearEnemy = findNearHero(world.getOppHeroes());
                    Cell[] areaOfDogeRange = new Range_fight(world).cellsOfArea(guardian.getCurrentCell(),
                            guardian.getAbility(AbilityName.GUARDIAN_DODGE).getRange());
                    //find cell to doge
                     Cell unVisibleCell = null ;
                    for (int i = 0; i < areaOfDogeRange.length; i++) {
                        if(world.isInVision(areaOfDogeRange[i],nearEnemy.getCurrentCell()))
                            unVisibleCell = areaOfDogeRange[i];
                    }
                    if(unVisibleCell!=null){
                        world.castAbility(guardian,AbilityName.GUARDIAN_DODGE,unVisibleCell);
                    } else {
                        //doge to the furthest cell
                        Cell furthestCell = null;
                        int maxDistance  =  Integer.MIN_VALUE;
                        for (int i = 0; i < areaOfDogeRange.length; i++) {
                            if(!areaOfDogeRange[i].isWall())
                                if(world.manhattanDistance(nearEnemy.getCurrentCell(),areaOfDogeRange[i])>=maxDistance){
                                    maxDistance=world.manhattanDistance(nearEnemy.getCurrentCell(),areaOfDogeRange[i]);
                                    furthestCell=areaOfDogeRange[i];
                                }
                        }
                        if(furthestCell!=null){
                            world.castAbility(guardian,AbilityName.GUARDIAN_DODGE,furthestCell);
                            return;
                        }
                        return;
                    }
                    return;
                    //last Action before death -->
                }else {
                    if(!attackAbleEnemies.isEmpty()){
                        Cell effectiveCell = findEffectiveCell(attackAbleEnemies);
                        world.castAbility(guardian,AbilityName.GUARDIAN_ATTACK,effectiveCell);
                        return; // stop continue this method
                    }
                }
            }
            //guardian can see enemy and enemy is in objective zone
            if (!enemiesInObjective.isEmpty()){
                if(isDogeReady()){
                    //doge to near enemy
                    Cell nearEnemyCell = findNearHero(enemiesInObjective).getCurrentCell();
                    world.castAbility(guardian,guardian.getAbility(AbilityName.GUARDIAN_DODGE),
                            nearCellToDoge(nearEnemyCell));
                    return; // stop continue this method
                }
                if(enemyHeroInVision.isEmpty()){
                    // doge near friends
                    if(isDogeReady()) {
                        world.castAbility(guardian, guardian.getAbility(AbilityName.GUARDIAN_DODGE),
                                nearCellToDoge(findNearHero(alliedHero).getCurrentCell()));
                        return; //stop continue method
                    }
                }else {
                    int HealthPoint =  guardian.getCurrentHP();
                    Hero nearEnemy = findNearHero(world.getOppHeroes());
                    Hero nearFriend =  findNearHero(world.getMyHeroes());
                    int nearFriendDistance = world.manhattanDistance(nearFriend.getCurrentCell(),guardian.getCurrentCell());
                    //priority of defense is for friends
                    if(isDangerTime()){
                        if(isFortifyReady()) {
                            world.castAbility(guardian, AbilityName.GUARDIAN_FORTIFY, guardian.getCurrentCell());
                            Logger.log("============================FORTIFY==========================",Logger.BLUE);
                            return;
                        }else if(isDogeReady()){
                            world.castAbility(guardian, guardian.getAbility(AbilityName.GUARDIAN_DODGE),
                                    nearCellToDoge(findNearHero(alliedHero).getCurrentCell()));
                        } else {
                            // :( GOODBYE GUARDIAN
                        }
                    }
                }
            }
        }
        // if guardian is outside of objective zone
        if(!isInObjectiveZone() && isDogeReady()){
            if(world.manhattanDistance(guardian.getCurrentCell(),findNearestCellOurSide())<=
                    guardian.getAbility(AbilityName.GUARDIAN_DODGE).getRange()) {
                world.castAbility(guardian, AbilityName.GUARDIAN_DODGE, findNearestCellOurSide());
            }else {
                world.castAbility(guardian, AbilityName.GUARDIAN_DODGE, nearOutsideOfObjective());
            }
            return; // stop continue this method
        }
    }

    public void movePhase(){
        if(guardian.getCurrentHP()==0 || world.getAP()<8)
            return;
        //if guardian are not in Objective zone :
        if(!isInObjectiveZone()){
            if(nearestObjectiveCell().length!=0)
                world.moveHero(guardian,nearestObjectiveCell()[0]);
        }else {
            // guardian are in Objective zone
            // if can't see any one
            Cell cell = findNearestCellOurSide();
            if (!canSeeAnyOne()){
                // go to nearest row of objective Zone after Blaster
                if(world.getPathMoveDirections(guardian.getCurrentCell(),findNearestCellOurSide()).length!=0)
                    world.moveHero(guardian,world.getPathMoveDirections(guardian.getCurrentCell(),findNearestCellOurSide())[0]);
            } else { // guardian an see enemyHeroes -->
                //find near enemy and go to kill that
                int movePhaseCount = world.getMovePhaseNum()%6+1;
                ArrayList<Hero> enemiesInObjective = getEnemyHeroesInObjective(world.getOppHeroes());
                //enemies in objective -->
                if(!enemiesInObjective.isEmpty()){
                    Hero nearEnemy = findNearHero(enemiesInObjective);

                    int distanceToNearEnemy = world.manhattanDistance(nearEnemy.getCurrentCell(),guardian.getCurrentCell());
                    if(movePhaseCount==5){
                        if(distanceToNearEnemy<=1){
                            Logger.log("=== === === === EFFECTIVE AP === === === ===",Logger.BLUE);
                            return;
                        }
                    }
                    if(movePhaseCount==6){
                        if(distanceToNearEnemy<=2){
                            Logger.log("=== === === === EFFECTIVE AP === === === ===",Logger.BLUE);
                            return;
                        }
                    }
                    //check if in current cell
                    if(!guardian.getCurrentCell().equals(nearEnemy.getCurrentCell())) {
                        if (world.getPathMoveDirections(guardian.getCurrentCell(),
                                nearEnemy.getCurrentCell(), getHeroesLocation(world.getMyHeroes())).length != 0) {
                            // find best cell with range 1 from
                            Cell bestCell = findNearCellWithRangeOne(nearEnemy.getCurrentCell());
                            if(world.manhattanDistance(bestCell,guardian.getCurrentCell())>=5){
                                //do nothing if we can find him
                                Logger.log("=== === === === EFFECTIVE AP === === === ===",Logger.BLUE);
                                return;
                            }
                            if(world.getPathMoveDirections(guardian.getCurrentCell(),
                                    bestCell, getHeroesLocation(world.getMyHeroes())).length!=0)
                                world.moveHero(guardian, world.getPathMoveDirections(guardian.getCurrentCell(),
                                        bestCell, getHeroesLocation(world.getMyHeroes()))[0]);
                        }
                    }
                } else {
                    //TODO ببینیم اصلا می صرفه بریم سمت دشمنی که نزدیک هست؟؟
                }
            }
        }

    }

    private Cell findNearCellWithRangeOne(Cell currentCell) {
        Cell[] aroundEnemy = new Range_fight(world).cellsOfArea(currentCell,1);
        Cell nearCell = null;
        int minDistance = Integer.MAX_VALUE;
        for (int i= 0 ; i<aroundEnemy.length;i++){
            if(world.manhattanDistance(guardian.getCurrentCell(),aroundEnemy[i])<=minDistance &&
                    !aroundEnemy[i].isWall()){
                minDistance = world.manhattanDistance(guardian.getCurrentCell(),aroundEnemy[i]);
                nearCell=aroundEnemy[i];
            }
        }
        return nearCell;
    }

    private boolean isDangerTime() {
        //TODO --> this method can be smarter
        int currentHP = guardian.getCurrentHP();
        ArrayList<Hero> visibleEnemies = getEnemyHeroesInVision(world.getOppHeroes());
        ArrayList<Hero> tooCloseEnemies =  getAttackAbleEnemies(world.getOppHeroes());
        int numberOfSentryInVision = numberOfSentriesInVision(visibleEnemies);
        int numberOfSentryTooClose =  numberOfSentriesInVision(tooCloseEnemies);
        int numberOfBlasterTooClose = numberOfBlastersTooClose(tooCloseEnemies);
        int numberOfGuardiansTooClose = numberOfGuardiansTooClose(tooCloseEnemies);
        int numberOfHealersTooClose = numberOfHealersTooClose(tooCloseEnemies);

        if(tooCloseEnemies.size()>=3){
            return true;
        }
        if(currentHP<=criticalHealthPoint){
            return true;
        }
        if(currentHP<=numberOfSentryTooClose*Utility_Attack.damage_of_sentry_attack){
            return true;
        }
        if(currentHP<=numberOfSentryInVision*Utility_Attack.damage_of_sentry_ray){
            return true;
        }
        if(currentHP<=numberOfBlasterTooClose*Utility_Attack.damage_of_blaster_bomb ||
                currentHP<=numberOfBlasterTooClose*Utility_Attack.damage_of_blaster_attack){
            return true;
        }
        if(currentHP<=numberOfGuardiansTooClose*Utility_Attack.damage_of_guardian_attack){
            return true;
        }
        if(currentHP<=numberOfHealersTooClose*Utility_Attack.damage_of_healer_attack){
            return true;
        }

        return false;
    }

    private int numberOfHealersTooClose(ArrayList<Hero> tooCloseEnemies) {
        int count = 0;
        for (int i= 0 ; i<tooCloseEnemies.size();i++){
            if(tooCloseEnemies.get(i).getName().equals(HeroName.HEALER))
                count++;
        }
        return count;
    }

    private int numberOfGuardiansTooClose(ArrayList<Hero> tooCloseEnemies) {
        int count = 0;
        for (int i= 0 ; i<tooCloseEnemies.size();i++){
            if(tooCloseEnemies.get(i).getName().equals(HeroName.GUARDIAN))
                count++;
        }
        return count;
    }

    private int numberOfBlastersTooClose(ArrayList<Hero> tooCloseEnemies){
        int count = 0;
        for (int i= 0 ; i<tooCloseEnemies.size();i++){
            if(tooCloseEnemies.get(i).getName().equals(HeroName.BLASTER))
                count++;
        }
        return count;
    }

    private int numberOfSentriesInVision(ArrayList<Hero> visibleEnemies){
        int count = 0;
        for (int i = 0; i < visibleEnemies.size(); i++) {
            if(visibleEnemies.get(i).getName().equals(HeroName.SENTRY))
                count++;
        }
        return count;
    }

    private Cell findEffectiveCell(ArrayList<Hero> attackAbleEnemies) {
        Cell effectiveCell = null ;
        if(attackAbleEnemies.size()==1){
            Cell enemyCell =  attackAbleEnemies.get(0).getCurrentCell();
            int distanceToEnemy =  world.manhattanDistance(guardian.getCurrentCell(),enemyCell);
            if(distanceToEnemy<=guardian.getAbility(AbilityName.GUARDIAN_ATTACK).getRange()) {
                effectiveCell = enemyCell;
                return effectiveCell;
            }
            else{
                Cell[] aroundEnemy = new Range_fight(world).cellsOfArea(enemyCell,
                        guardian.getAbility(AbilityName.GUARDIAN_ATTACK).getAreaOfEffect());
                //find near cell
                int minDistance =  Integer.MAX_VALUE;
                for (int i=0 ; i<aroundEnemy.length ;i++){
                    int distance =  world.manhattanDistance(guardian.getCurrentCell(), aroundEnemy[i]);
                    if(distance<minDistance){
                        minDistance=distance;
                        effectiveCell=aroundEnemy[i];
                    }
                }
                return effectiveCell;
            }
        } else if(attackAbleEnemies.size()==2){
            Hero firstEnemy =  attackAbleEnemies.get(0);
            Hero secondEnemy = attackAbleEnemies.get(1);
            //if distance between two enemy is more than Area effect of guardian Attack
            int distanceBetweenEnemies = world.manhattanDistance(firstEnemy.getCurrentCell(),secondEnemy.getCurrentCell());
            if(distanceBetweenEnemies > guardian.getAbility(AbilityName.GUARDIAN_ATTACK).getAreaOfEffect()){
                ArrayList<Hero> forRecursion = new ArrayList<>();
                if(firstEnemy.getCurrentHP()<secondEnemy.getCurrentHP()){
                    forRecursion.add(firstEnemy);
                    return findEffectiveCell(forRecursion);
                }else {
                    forRecursion.add(secondEnemy);
                    return findEffectiveCell(forRecursion);
                }
                //else mean :: if distance is less than Area effect of guardian attack --> we can damage both of them
            } else {
                Cell[] effectiveCells =  Utility.effectiveCells(world,firstEnemy.getCurrentCell(),secondEnemy.getCurrentCell());
                if(effectiveCells.length==1){
                    return effectiveCells[0];
                } else if(effectiveCells.length==2){

                    int dis0 = world.manhattanDistance(guardian.getCurrentCell(),effectiveCells[0]);
                    int dis1 = world.manhattanDistance(guardian.getCurrentCell(),effectiveCells[1]);

                    if(dis0<dis1)
                        return effectiveCells[0];
                    else
                        return effectiveCells[1];
                }
            }
            //else mean ::  if enemies are more than 2 -->
        } else if (attackAbleEnemies.size()==3 || attackAbleEnemies.size()==4) {
            //find a enemy two close enemies -->
            Cell start ,mid ,end ;

            start = attackAbleEnemies.get(0).getCurrentCell();
            mid = attackAbleEnemies.get(1).getCurrentCell();
            end = attackAbleEnemies.get(2).getCurrentCell();

            Cell left = Utility.getLEFT(Utility.getLEFT(start,mid),end);
            Cell right = Utility.getRIGHT(Utility.getRIGHT(start,mid),end);
            Cell up = Utility.getUP(Utility.getUP(start,mid),end);
            Cell down = Utility.getDOWN(Utility.getDOWN(start,mid),end);
            return world.getMap().getCell((up.getRow() + down.getRow())/2,(left.getColumn()+right.getColumn())/2);

        }
        return effectiveCell;
    }



    private Hero findNearHero(Hero[] heroes){
        ArrayList<Hero> result = new ArrayList<>(Arrays.asList(heroes));
        return findNearHero(result);
    }

    private Hero findNearHero(ArrayList<Hero> hero) {
        int minDistance =  Integer.MAX_VALUE;
        Hero result = hero.get(0);
        for (Hero aHero : hero){
            if(world.manhattanDistance(guardian.getCurrentCell(),aHero.getCurrentCell())<minDistance){
                minDistance =world.manhattanDistance(guardian.getCurrentCell(),aHero.getCurrentCell());
                result = aHero;
            }
            //check for same distance and go to loser
            if(world.manhattanDistance(guardian.getCurrentCell(),aHero.getCurrentCell())==minDistance){
                if(result.getCurrentHP()>=aHero.getCurrentHP())
                    result=aHero;
            }
        }
        return result;
    }

    private Cell nearCellToDoge(Cell destination){
        Cell[] availableCells =  Utility.availableCells(map,
                guardian.getAbility(AbilityName.GUARDIAN_FORTIFY).getRange(),guardian.getCurrentCell());
        Cell minCell = availableCells[0];
        int minDistance = Integer.MAX_VALUE;
        for (int i = 0; i < availableCells.length; i++) {
            if(!availableCells[i].isWall() && world.manhattanDistance(destination,availableCells[i])<minDistance){
                minCell=availableCells[i];
                minDistance=world.manhattanDistance(destination,availableCells[i]);
            }
        }
        return minCell;
    }

    private ArrayList<Hero> getEnemyHeroesInObjective(Hero[] enemyHeroes){
        ArrayList<Hero> enemyHeroesInObjective =  new ArrayList<>();
        for(Hero enemy : enemyHeroes){
            if(enemy.getCurrentCell().isInObjectiveZone())
                enemyHeroesInObjective.add(enemy);
        }
        return enemyHeroesInObjective;
    }

    private ArrayList<Hero> getEnemyHeroesInVision(Hero[] enemyHeroes){
        ArrayList<Hero> enemyHeroInVision = new ArrayList<>();
        for (Hero enemy: enemyHeroes) {
            if(world.isInVision(enemy.getCurrentCell(),guardian.getCurrentCell()))
                enemyHeroInVision.add(enemy);
        }
        return enemyHeroInVision;
    }

    private ArrayList<Hero> getAttackAbleEnemies(Hero[] enemyHeroes){
        ArrayList<Hero> attackAbleEnemies =  new ArrayList<>();
        for (Hero enemy: enemyHeroes) {
            if(world.manhattanDistance(enemy.getCurrentCell(),guardian.getCurrentCell())<=
                    guardian.getAbility(AbilityName.GUARDIAN_ATTACK).getRange()+
                    guardian.getAbility(AbilityName.GUARDIAN_ATTACK).getAreaOfEffect()){
                attackAbleEnemies.add(enemy);
            }
        }
        return attackAbleEnemies;
    }

    private Cell nearOutsideOfObjective() {
        Cell[] path = directionPathToCell();
        for (Cell aPath : path) {
            if (world.manhattanDistance(guardian.getCurrentCell(), aPath) ==
                    guardian.getAbility(AbilityName.GUARDIAN_DODGE).getRange())
                return aPath;
        }
        return null;
    }

    private Cell[] directionPathToCell() {
        Direction[] path = world.getPathMoveDirections(guardian.getCurrentCell(),findNearestCellOurSide());
        Cell[] result =  new Cell[path.length];
        Cell src = guardian.getCurrentCell();
        for (int i = 0; i < result.length; i++) {
            result[i]=Utility.nextCell(world,src,path[i]);
            src=Utility.nextCell(world,src,path[i]);
        }
        return result;
    }



    private Cell findNearestCellOurSide() {
        Cell[] myRespawnZone  = map.getMyRespawnZone();
        Cell[] objectiveZone =  map.getObjectiveZone();
        Cell result = null;
        int minDistance = Integer.MAX_VALUE;
        for (int i = 0; i < objectiveZone.length; i++) {
            for (int j = 0; j < myRespawnZone.length; j++) {
                if(world.manhattanDistance(objectiveZone[i],myRespawnZone[j])<minDistance){
                    minDistance = world.manhattanDistance(objectiveZone[i],myRespawnZone[j]);
                    result = objectiveZone[i];
                }
            }
        }
        return result;
    }

    private boolean isDogeReady() {
        return guardian.getAbility(AbilityName.GUARDIAN_DODGE).isReady();
    }

    private boolean canSeeAnyOne() {
        return new Range_fight(world).inVisionEnemy(guardian).length != 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Guardian_AI)) return false;
        Guardian_AI that = (Guardian_AI) o;
        return Objects.equals(guardian, that.guardian) &&
                Objects.equals(world, that.world) &&
                Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guardian, world, map);
    }

    private boolean isInObjectiveZone(){
        return guardian.getCurrentCell().isInObjectiveZone();
    }

    private boolean isFortifyReady(){
        return guardian.getAbility(AbilityName.GUARDIAN_FORTIFY).isReady();
    }

    private Direction[] nearestObjectiveCell(){
        return world.getPathMoveDirections(guardian.getCurrentCell(),
                new Range_fight(world).findNearestZoneCell(guardian.getCurrentCell()),getHeroesLocation(world.getMyHeroes()));
    }

    private Cell[] getHeroesLocation(Hero[] heroes){
        Cell[] locations = new Cell[heroes.length];
        for (int i = 0; i < heroes.length; i++) {
            locations[i]=heroes[i].getCurrentCell();
        }
        return locations;
    }


    public Hero getGuardian() {
        return guardian;
    }

    public void setGuardian(Hero guardian) {
        this.guardian = guardian;
    }

    public void setWorld(World world) {
        this.world = world;
    }
}
