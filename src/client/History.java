package client;

import client.model.Cell;
import client.model.Hero;
import client.model.Pair;

import java.util.EmptyStackException;
import java.util.Stack;
import java.util.Vector;

public class History {
    private boolean inScape=false;
    private boolean inAttack=false;
    private Stack<Cell> lastStep=new Stack<>();
    private int HeroID;
    private Vector<Hero> sawHeroes; // Hero haii ke in baba dide

    History(int HEROID){
        HeroID = HEROID;
    }

    void move(Cell lastStep,boolean inScape,boolean inAttack){
        addLastStep(lastStep);
        this.inScape = inScape;
        this.inAttack=inAttack;
    }
    void move(Cell lastStep,boolean inScape){
            move(lastStep,inScape,false);
    }
    void move(Cell lastStep){
        move(lastStep,false,false);
    }

    boolean isEmptySawHero(){
        return sawHeroes==null || sawHeroes.size() == 0;
    }
    void addHero(Hero hero){
        if(isEmptySawHero())
            sawHeroes = new Vector<>();
        sawHeroes.add(hero);
    }
    boolean isISeeThisHero(Hero hero){
        return sawHeroes.indexOf(hero)!=-1;
    }
    void cleareSawHeroes(){
        sawHeroes = new Vector<>();
    }

    Vector<Hero> getSawHeroes(){
        return sawHeroes;
    }

    void setInScape(boolean inScape) {
        this.inScape = inScape;
    }
    void setInAttack(boolean inAttack) {
        this.inAttack = inAttack;
    }
    void setHeroID(int heroID) {
        HeroID = heroID;
    }

    void addLastStep(Cell lastStep) {
        this.lastStep.push(lastStep);
    }
    Stack<Cell> getLaststepsStack(){
        return lastStep;
    }

    Cell getLastStep(){
        try {
            return lastStep.pop();
        }catch (EmptyStackException e){
            return null;
        }
    }
    boolean isInScape(){
        return inScape;
    }
    boolean isInAttack(){
        return inAttack;
    }

    public int getHeroID(){
        return HeroID;
    }
}
