package tomato.entity;

import tomato.core.Utils;

public class LandmineEntity extends Entity{
    public LandmineEntity(double x, double y) {
        super(x, y);
        this.entityType = EntityType.LANDMINE;
        this.health = 1;
        this.rotatable = false;
        this.currentSprite = Utils.loadQOI("/tomato/assets/landmine.qoi");
        this.setCollisionAction(new LandmineCollisionAction());
    }
}
