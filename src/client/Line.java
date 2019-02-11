package client;

import client.model.Cell;

/**
 * create by @omidekz
 * */
public class Line {
    //row is x
    //col is y
    private float m;
    private int x0,y0;

    private Line(){}
    private Line(int tmpX0,int tmpY0,int tmpX1,int tmpY1){
        m=((tmpY1-tmpY0)*1f)/(tmpX1-tmpX0);
        x0 = tmpX0;
        y0 = tmpY0;
    }
    private Line(Cell fPoint,Cell sPoint){
        this(fPoint.getRow(),fPoint.getColumn(),sPoint.getRow(),sPoint.getColumn());
    }

    static Line CREATOR(Cell fPoint,Cell sPoint){
        return new Line(fPoint,sPoint);
    }
    static Line CREATOR(int x0,int y0,int x1,int y1){
        return new Line(x0,y0,x1,y1);
    }
    boolean isOnLine(Cell point){
        int y = point.getColumn();
        int x = point.getRow();
        return isOnLine(x,y);
    }
    boolean isOnLine(int x,int y){
        return y == (m*(x-x0)+y0);
    }

    // in class ro test krdm
    // moshkeli ndasht
    // ishala k ok test krde basham :]

}
