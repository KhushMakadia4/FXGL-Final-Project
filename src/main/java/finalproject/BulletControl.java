package finalproject;

import com.almasb.fxgl.entity.component.Component;

public class BulletControl extends Component {
    private int xTrans;//the amount the x of the bullet will change by
    private int yTrans;//the amount the y of the bullet will change by
    private double startX;//the x where the bullet started
    private double startY;//the y where teh bullet started

    //precondition: get basic information like translate information and start position of the bullet
    //postcondition: initializes all the global variables
    public BulletControl(int xT,int yT, double xS, double xY) {
        xTrans = xT;
        yTrans = yT;
        startX = xS;
        startY = xY;
    }

    //precondition: will update at a speed/ratio of tpf seconds
    //postcondition:will constantly change the x and y of the bullet and eventually remove the entity from the world once it has traveled 100 map pixels
    @Override
    public void onUpdate(double tpf) {
        entity.translateX(xTrans);
        entity.translateY(yTrans);
        if (Math.abs(entity.getX()-startX) >100 || Math.abs(entity.getY()-startY)>100) {
            entity.getWorld().removeEntity(entity);
        }
    }
}