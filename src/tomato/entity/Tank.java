package tomato.entity;

import tomato.core.Utils;

public class Tank extends Entity{

    public Tank(double x, double y) {
        super(x, y);
        this.currentDirection = Direction.SOUTH;
        this.currentSprite = Utils.loadQOI("/tomato/assets/tank.qoi");
    }

    protected void moveForward(double distance) {
        switch (currentDirection) {
            case NORTH: setY(this.y - distance); break;
            case SOUTH: setY(this.y + distance); break;
            case EAST:  setX(this.x + distance); break;
            case WEST:  setX(this.x - distance); break;
        }
    }

}
