package tomato.entity;

import tomato.core.Utils;

public class EnemyTank extends Tank
{
    public EnemyTank(double x, double y) {
        super(x, y);
        this.currentSprite = Utils.loadQOI("/tomato/assets/tank_red.qoi");
    }
}
