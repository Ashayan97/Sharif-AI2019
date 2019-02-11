package client;

import client.model.Cell;
import client.model.Pair;

import java.util.EmptyStackException;
import java.util.Stack;

public class History {
    private boolean inScape=false;
    private boolean inAttack=false;
    private boolean inNormal=true;
    private Stack<Cell> lastStep=new Stack<>();
    private int HeroID;
    History(int HEROID){
        HeroID = HEROID;
    }

    void move(Cell lastStep,boolean inScape,boolean inAttack,boolean inNormal){
        addLastStep(lastStep);
        this.inScape = inScape;
        this.inAttack=inAttack;
        this.inNormal=inNormal;
    }
    void move(Cell lastStep,boolean inScape,boolean inAttack){
        move(lastStep,inScape,inAttack,false);
    }
    void move(Cell lastStep,boolean inScape){
            move(lastStep,inScape,false,false);
    }
    void move(Cell lastStep){
        move(lastStep,false,false,true);
    }

    void setInScape(boolean inScape) {
        this.inScape = inScape;
    }
    void setInAttack(boolean inAttack) {
        this.inAttack = inAttack;
    }
    void setInNormal(boolean inNormal) {
        this.inNormal = inNormal;
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
    boolean isInNormal(){
        return inNormal;
    }

    public int getHeroID(){
        return HeroID;
    }
}
