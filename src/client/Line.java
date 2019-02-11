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
        this(getX(fPoint),getY(fPoint),getX(sPoint),getY(sPoint));
    }

    static Line CREATOR(Cell fPoint,Cell sPoint){
        return new Line(fPoint,sPoint);
    }
    static Line CREATOR(int x0,int y0,int x1,int y1){
        return new Line(x0,y0,x1,y1);
    }
    private static Line CREATOR(float m,int x0,int y0){
        Line line = new Line();
        line.m=m;
        line.x0=x0;
        line.y0=y0;
        return line;
    }

    Line perpendicularLine(Cell point){
        return perpendicularLine(getX(point),getY(point));
    }
    Line perpendicularLine(int x0,int y0){
        float m = -1/this.m;
        return CREATOR(m,x0,y0);
    }

    float distanceFromLine(Cell from){
        return distanceFromLine(getX(from),getY(from));
    }
    float distanceFromLine(int x,int y){
        // m(x1-x0)+y0 = m'(x1-x'0)+y'0
        // mx1-mx0+y0=m'x1-m'x'0+y'0
        // mx1-m'x1 = mx0-m'x'0 + y'0-y0
        // x1 = (mx0-m'x'0 + y'0-y0)/(m-m')
        Line perpendicularLine = perpendicularLine(x,y);
        float   mp=perpendicularLine.m;
        int     x0p=perpendicularLine.x0;
        int     yp0=perpendicularLine.y0;

        float   xBarkhord = ((m*x0 - mp*x0p + yp0 - y0)/(m-mp));
        float   yBarkhord =   calcY((this),xBarkhord);

        return distance(x,y,xBarkhord,yBarkhord);
    }

    float distance(Cell start,Cell end){
        return distance(getX(start),getY(start),getX(end),getY(end));
    }
    float distance(float x,float y,float x1,float y1){
        return (float) Math.sqrt(Math.pow(x-x1,2)+Math.pow(y-y1,2));
    }

    boolean isOnLine(Cell point){
        int y = point.getColumn();
        int x = point.getRow();
        return isOnLine(x,y);
    }
    boolean isOnLine(int x,int y){
        return y == (int)calcY(this,x);
    }

    static float calcY(Line line,float x){
        // y = m(x1-x0) + y0
        return line.m * (x-line.x0) + line.y0;
    }
    static float calcX(Line line,float y){
        // y = m(x1-x0) + y0
        //=> ((y-y0)+mx0)/m = x1
        float m = line.m;
        int y0 = line.y0;
        int x0 = line.x0;
        return ((y-y0)+m*x0)/m;
    }

    static int getX(Cell cell){
        return cell.getColumn();
    }
    static int getY(Cell cell){
        return cell.getRow();
    }

    // in class ro test krdm
    // moshkeli ndasht
    // ishala k ok test krde basham :]

    public static void main(String[] args) {
        Line line = Line.CREATOR(0,0,5,5);
        System.out.println(line.distanceFromLine(1,0));
    }

}
