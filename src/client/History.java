package client;

import client.model.Cell;

public class History {
    private boolean inScape=false;
    private boolean inAttack=false;
    private boolean inNormal=true;
    private Cell lastStep=null;
    private int HeroID;
    History(int HEROID){
        HeroID = HEROID;
    }

    void move(Cell lastStep,boolean inScape,boolean inAttack,boolean inNormal){
        this.lastStep = lastStep;
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

    public void setInScape(boolean inScape) {
        this.inScape = inScape;
    }
    public void setInAttack(boolean inAttack) {
        this.inAttack = inAttack;
    }
    public void setInNormal(boolean inNormal) {
        this.inNormal = inNormal;
    }
    public void setLastStep(Cell lastStep) {
        this.lastStep = lastStep;
    }
    public void setHeroID(int heroID) {
        HeroID = heroID;
    }

    Cell getLastStep(){
        return lastStep;
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
