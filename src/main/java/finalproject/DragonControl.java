package finalproject;

import com.almasb.fxgl.entity.Entities;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.CollidableComponent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class DragonControl extends Component {
    private int health = 10;//dragon's health
    private long movementTS = System.nanoTime();//the delay time before the final boss moves
    private long bulletTS = System.nanoTime()+(100000000);//the delay time before the final boss shoots its weapon
    private Entity player;//the player is here as the dragon has to check that it won't collide with the player

    //precondition:gets player entity from FinalProject
    //postcondition:initializes global variable player
    public DragonControl(Entity x) {
        player = x;
    }

    //precondition:the function will execute at the speed/ratio of tpf
    //postcondition: it will move the dragon at every 3 seconds, will shoot the mega attack of the dragon every 3 seconds but a little bit after the dragon moves
    @Override
    public void onUpdate(double tpf) {
        if (((double)(System.nanoTime()) - movementTS)/1000000000>3) {//checks if it has been 3 seconds to move the dragon at a random point
            entity.setX((int)(Math.random()*731));
            entity.setY((int)(Math.random()*401));
            while (entity.isColliding(player)) {//checks if the dragon is colliding with the player
                entity.setX((int)(Math.random()*731));
                entity.setY((int)(Math.random()*401));
            }
            movementTS = System.nanoTime();
        }

        if ((System.nanoTime() - bulletTS)/1000000000>3) {//checks if it has been 3 seconds to place the bullets and move them
            placeBullets();
            bulletTS = System.nanoTime();
        }

    }

    //precondition:none
    //postcondition:puts a bombardment of bullets to try and hurt player
    public void placeBullets() {
        Entity tempBullet;//one bullet will act as 20 all together

        for (int i = 1;i<=20;i++) {//loop to make 20 bullets
            if (i < 6) {//right //first 5 will make bullets on the right side of the dragon going to the right
                tempBullet = Entities.builder()
                        .type(FinalProject.EntityType.DRAGON_BULLET)
                        .at(entity.getX(), entity.getY() + (30 * (i - 1)) + 15)
                        .viewFromNodeWithBBox(new Rectangle(8, 2, Color.RED))
                        .with(new CollidableComponent(true))
                        .with(new BulletControl(-2, 0, entity.getX(), entity.getY() + (30 * (i - 1)) + 15))
                        .buildAndAttach();
            } else if (i >= 6 && i < 11) {//left //next 5 will make bullets on the left side of the dragon going to the left
                tempBullet = Entities.builder()
                        .type(FinalProject.EntityType.DRAGON_BULLET)
                        .at(entity.getX() + 150, entity.getY() + (30 * (i - 6)) + 15)//(i-4-1)
                        .viewFromNodeWithBBox(new Rectangle(8, 2, Color.RED))
                        .with(new CollidableComponent(true))
                        .with(new BulletControl(2, 0, entity.getX() + 150, entity.getY() + (30 * (i - 5)) + 15))
                        .buildAndAttach();
            } else if (i>=11 && i <16) {//up //next 5 will make bullets on the upper side of the dragon going up
                tempBullet = Entities.builder()
                        .type(FinalProject.EntityType.DRAGON_BULLET)
                        .at(entity.getX()+(30*(i-11))+15, entity.getY())
                        .viewFromNodeWithBBox(new Rectangle(2,8, Color.RED))
                        .with(new CollidableComponent(true))
                        .with(new BulletControl(0,-2,entity.getX()+(30*(i-11))+15, entity.getY()))
                        .buildAndAttach();
            } else {//down //next 5 will make bullets on the lower side of the dragon going downwards
                tempBullet = Entities.builder()
                        .type(FinalProject.EntityType.DRAGON_BULLET)
                        .at(entity.getX()+(30*(i-16))+15, entity.getY()+150)
                        .viewFromNodeWithBBox(new Rectangle(2,8,Color.RED))
                        .with(new CollidableComponent(true))
                        .with(new BulletControl(0,2,entity.getX()+(30*(i-16))+15, entity.getY()+150))
                        .buildAndAttach();
            }
            tempBullet.getComponent(BulletControl.class).onUpdate(1.0);
        }
    }

    //precondition:none
    //postcondition: reduces health by 1
    public void healthAttack() {
        health--;
    }

    //precondition:none
    //postcondition:returns health
    public int getHealth() {
        return health;
    }
}