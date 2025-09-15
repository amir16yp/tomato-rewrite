package tomato.entity;

import tomato.core.Utils;

public class Tank extends Entity{

    public Tank(double x, double y) {
        super(x, y);
        this.currentDirection = Direction.SOUTH;
        this.currentSprite = Utils.loadQOI("/tomato/assets/tank.qoi");
    }

}
