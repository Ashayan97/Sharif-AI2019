package client;

public class DeadsList {
    private int HeroId;
    private int LeftTurn=5;
    public DeadsList(int HeroId){
        this.HeroId=HeroId;
    }

    public int getHeroId() {
        return HeroId;
    }

    public int getLeftTurn() {
        return LeftTurn;
    }

    public void setLeftTurn() {
        LeftTurn--;
    }
}
