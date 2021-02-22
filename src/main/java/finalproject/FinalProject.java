package finalproject;

import com.almasb.fxgl.app.FXGL;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.entity.Entities;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.settings.GameSettings;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import javafx.scene.text.Font;

import javax.swing.*;
import java.util.ArrayList;

//BIG CREDITS TO ALMAS BAIGAMBETOV FOR BASIC FRAMEWORK OF THE GAME AND GENERAL KNOWLEDGE OF FXGL

public class FinalProject extends GameApplication {

    private Entity player;//main character of the game created by the entity object of the FXGL library
    private Entity door;//the "portal" between the overworld and the world with the dragon/final boss
    private Entity dragon;//the final boss
    private int health = 10;//our health
    private int gold = 0;//our gold used to buy 5 pieces of armor each worth 2 gold
    private int armor = 0;//5 pieces of armor gives 10 armor which will unlock the door
    private int backArmorCount = 0;//you can lose armor in the overworld due to monsters so therefore you will not be able to access the final boss so this is a background armor count
    private String bulletDir = "";//R:right,L:left,U:up,D:down // direction of the bullet of the player
    private long pBulletDelay = 0;//the time that the player will have to wait in order to shoot another bullet
    private long mBulletNano = 0;//the system time that will be used for monster bullets
    private Label goldLbl = new Label("GOLD: 0");//shows the amount of gold we have
    private Label healthLbl = new Label("HEALTH: 10");//shows our health
    private Label armorLbl = new Label("ARMOR: 0");//shows our armor

    private ArrayList<Entity> monsters = new ArrayList<>();//list of monsters in the overworld map
    private ArrayList<Entity> collidingObj = new ArrayList<>();//list of objects that the player can collide with

    private AnchorPane weaponPane;//an anchorpane that will have options for you to buy a sword//1
    private AnchorPane armorPane;//anchorpane for buying armor//4
    private AnchorPane retryPane;//if you die or win, this will give you the choice to restart
    private Label retryLbl;//the label will be changed later to accomodate if you won or died

    //precondition:settings for the game window
    //postcondition:creates a game window that will open and let us play the game
    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(880);
        settings.setHeight(550);
        settings.setTitle("Final Project");
        settings.setVersion("0.1");
        settings.setIntroEnabled(false);
        settings.setMenuEnabled(false);
    }

    //precondition: none
    //postcondition: creates the input actions by which whenever user presses a certain key, it will do a certain action
    @Override
    protected void initInput() {
        Input input = getInput();

        input.addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {//moves the player to the right
                if (!(player.getPositionComponent().getX() + 11 > 880) && !checkCollision(2, 0)) {//As long as player isnt colliding with a wall and it isnt colliding with an object
                    player.getPositionComponent().translateX(2); // move right 5 pixels
                    bulletDir = "R";
                }
            }
        }, KeyCode.D);

        input.addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {//moves the player to the left
                if (!(player.getPositionComponent().getX() < 0) && !checkCollision(-2, 0)) {
                    player.getPositionComponent().translateX(-2); // move left 5 pixels
                    bulletDir = "L";

                }
            }
        }, KeyCode.A);

        input.addAction(new UserAction("Move Up") {
            @Override
            protected void onAction() {//moves the player upward
                if (!(player.getPositionComponent().getY() < 0) && !checkCollision(0, -2)) {
                    player.getPositionComponent().translateY(-2); // move up 5 pixels
                    bulletDir = "U";
                }
            }
        }, KeyCode.W);

        input.addAction(new UserAction("Move Down") {
            @Override
            protected void onAction() {//moves the player downward
                if (!(player.getPositionComponent().getY() + 11 > 550) && !checkCollision(0, 2)) {
                    player.getPositionComponent().translateY(2); // move down 5 pixels
                    bulletDir = "D";
                }
            }
        }, KeyCode.S);

        input.addAction(new UserAction("Shoot") {
            Entity tempBullet;

            //precondition:none
            //postcondition:creates and moves a bullet based on the direction of it
            @Override
            public void onAction() {
                if ((System.nanoTime() - pBulletDelay) / 1000000000 > .5) {//one bullet per second
                    pBulletDelay = System.nanoTime();
                    switch (bulletDir) {//checks if bullet will go right (R), Left(L), Up(U) or Down(D)
                        case "R":
                            tempBullet = Entities.builder()
                                    .type(EntityType.BULLET)
                                    .at(player.getX() + 11, player.getY() + 5.5)
                                    .viewFromNodeWithBBox(new Rectangle(8, 2, Color.GOLDENROD))
                                    .with(new CollidableComponent(true))
                                    .with(new BulletControl(2, 0, player.getX() + 11, player.getY() + 5.5))
                                    .buildAndAttach();
                            break;
                        case "L":
                            tempBullet = Entities.builder()
                                    .type(EntityType.BULLET)
                                    .at(player.getX() - 8, player.getY() + 5.5)
                                    .viewFromNodeWithBBox(new Rectangle(8, 2, Color.GOLDENROD))
                                    .with(new CollidableComponent(true))
                                    .with(new BulletControl(-2, 0, player.getX() - 8, player.getY() + 5.5))
                                    .buildAndAttach();
                            break;
                        case "U":
                            tempBullet = Entities.builder()
                                    .type(EntityType.BULLET)
                                    .at(player.getX() + 5.5, player.getY() - 8)
                                    .viewFromNodeWithBBox(new Rectangle(2, 8, Color.GOLDENROD))
                                    .with(new CollidableComponent(true))
                                    .with(new BulletControl(0, -2, player.getX() + 5.5, player.getY() - 8))
                                    .buildAndAttach();
                            break;
                        default://D
                            tempBullet = Entities.builder()
                                    .type(EntityType.BULLET)
                                    .at(player.getX() + 5.5, player.getY() + 11)
                                    .viewFromNodeWithBBox(new Rectangle(2, 8, Color.GOLDENROD))
                                    .with(new CollidableComponent(true))
                                    .with(new BulletControl(0, 2, player.getX() + 5.5, player.getY() + 11))
                                    .buildAndAttach();
                    }
                    tempBullet.getComponent(BulletControl.class).onUpdate(1.0);
                }
            }
        }, KeyCode.SPACE);

    }

    //precondition: the x and y by which the player will move
    //postcondition: checks if the player is colliding to any other objects or monsters if it moves
    public boolean checkCollision(int xTrans, int yTrans) {
        player.getPositionComponent().translateX(xTrans);
        player.getPositionComponent().translateY(yTrans);
        for (Entity x : collidingObj) {//checks if we will collided to any of the shops or the closed portal door
            if (player.isColliding(x)) {
                player.getPositionComponent().translateX(-1 * xTrans);
                player.getPositionComponent().translateY(-1 * yTrans);
                return true;
            }
        }
        for (Entity x : monsters) {//checks if we will collide with the monsters
            if (player.isColliding(x)) {
                player.getPositionComponent().translateX(-1 * xTrans);
                player.getPositionComponent().translateY(-1 * yTrans);
                return true;
            }
        }

        player.getPositionComponent().translateX(-1 * xTrans);
        player.getPositionComponent().translateY(-1 * yTrans);
        return false;
    }

    public enum EntityType {//creates the types of entities in this world
        PLAYER, MONSTER, WEAPONSHOP, ARMORSHOP, BULLET, COIN, MONSTER_BULLET, DOOR, DRAGON, DRAGON_BULLET
    }

    //precondition:none
    //postcondition:creates the entities in the world
    @Override
    protected void initGame() {
        getGameWorld().setLevelFromMap("map.tmx");//sets the map behind the entities

        player = Entities.builder()
                .type(EntityType.PLAYER)
                .at(300, 300)
                .viewFromNodeWithBBox(new Rectangle(11, 11, Color.BLUE))
                .with(new CollidableComponent(true))
                .buildAndAttach(getGameWorld());

        door = Entities.builder()
                .type(EntityType.DOOR)
                .at(18 * 11, 7 * 11)
                .viewFromNodeWithBBox(new Rectangle(2 * 11, 3 * 11, Color.TRANSPARENT))
                .with(new CollidableComponent(true))
                .buildAndAttach();
        collidingObj.add(door);

        Entity weaponShop = Entities.builder()
                .at(15 * 11, 27 * 11)
                .viewFromNodeWithBBox(new Rectangle(4 * 11, 4 * 11, Color.TRANSPARENT))
                .with(new CollidableComponent(true))
                .buildAndAttach(getGameWorld());
        collidingObj.add(weaponShop);

        Entity armorShop = Entities.builder()
                .at(54 * 11, 26 * 11)
                .viewFromNodeWithBBox(new Rectangle(5 * 11, 5 * 11, Color.TRANSPARENT))
                .with(new CollidableComponent(true))
                .buildAndAttach(getGameWorld());
        collidingObj.add(armorShop);

        boolean colliding = true;
        for (int i = 0; i < 10; i++) {//creates 10 monsters
            Entity monster = Entities.builder()
                    .type(EntityType.MONSTER)
                    .at(Math.random() * 870, Math.random() * 540)
                    .viewFromNodeWithBBox(new Rectangle(11, 11, Color.DARKGREEN))
                    .with(new CollidableComponent(true))
                    .buildAndAttach(getGameWorld());
            while (colliding) {//checks if the monsters are colliding with other objects or themselves
                colliding = false;
                for (Entity x : collidingObj) {
                    if (monster.isColliding(x)) {
                        colliding = true;
                        monster.setX(Math.random() * 870);
                        monster.setY(Math.random() * 540);
                    }
                }

                for (Entity x : monsters) {
                    if (monster.isColliding(x)) {
                        colliding = true;
                        monster.setX(Math.random() * 870);
                        monster.setY(Math.random() * 540);
                    }
                }

                if (monster.isColliding(player)) {
                    colliding = true;
                    monster.setX(Math.random() * 870);
                    monster.setY(Math.random() * 540);
                }
            }
            colliding = true;
            monsters.add(monster);
        }


        Entity weaponShopDoor = Entities.builder()
                .type(EntityType.WEAPONSHOP)
                .at((16 * 11) + 3.5, (31 * 11))
                .viewFromNodeWithBBox(new Rectangle(16, 6.5, Color.TRANSPARENT))
                .with(new CollidableComponent(true))
                .buildAndAttach();

        Entity armorShopDoor = Entities.builder()
                .type(EntityType.ARMORSHOP)
                .at((56 * 11) + 3.5, (31 * 11))
                .viewFromNodeWithBBox(new Rectangle(16, 6.5, Color.TRANSPARENT))
                .with(new CollidableComponent(true))
                .buildAndAttach();

        mBulletNano = System.nanoTime();
        //collision entities
    }

    //precondition:none
    //postcondition: makes the collision handlers (detect collision between 2 types of entities) for the game
    @Override
    protected void initPhysics() {

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.BULLET, EntityType.MONSTER) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity monster) {//checks if player's bullet has touched the monster and if so, removes both and makes a coin to collect there
                bullet.removeFromWorld();
                monster.removeFromWorld();
                monsters.remove(monster);
                Entity coin = Entities.builder()
                        .at(monster.getX(), monster.getY())
                        .viewFromNodeWithBBox(new Circle(5.5, Color.YELLOW))
                        .with(new CollidableComponent(true))
                        .type(EntityType.COIN)
                        .buildAndAttach();
            }
        });

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.COIN) {
            @Override
            protected void onCollisionBegin(Entity player, Entity coin) {//allows player to pick up a coin
                gold++;
                coin.removeFromWorld();
            }
        });

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.MONSTER_BULLET) {
            @Override
            protected void onCollisionBegin(Entity player, Entity mBullet) {//damages player if player touches a monster's bullet
                mBullet.removeFromWorld();
                if (armor > 0) {//checks if they have armor on and if not then health is decreased
                    armor--;
                } else {
                    health -= 2;
                }

                if (health<=0) {//checks if the player has died in which it will stop the game and show the retry pane
                    FXGL.getGameWorld().removeEntity(player);
                    FXGL.getGameWorld().removeEntities(collidingObj);
                    FXGL.getGameScene().clearUINodes();
                    retryLbl.setText("YOU DIED");
                    collidingObj.clear();
                    FXGL.getGameScene().addUINode(retryPane);
                }
            }
        });

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.WEAPONSHOP) {
            @Override
            protected void onCollisionBegin(Entity player, Entity weaponDoor) {//opens up the shop pane for weapons
                FXGL.getGameScene().addUINode(weaponPane);
            }

            @Override
            protected void onCollisionEnd(Entity player, Entity weaponDoor) {//closes the shop pane once the player is out of the door of the shop
                FXGL.getGameScene().removeUINode(weaponPane);
            }
        });

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.ARMORSHOP) {
            @Override
            protected void onCollisionBegin(Entity player, Entity armorDoor) {//opens armor shop
                FXGL.getGameScene().addUINode(armorPane);
            }

            @Override
            protected void onCollisionEnd(Entity player, Entity armorDoor) {//closes armor shop
                FXGL.getGameScene().removeUINode(armorPane);
            }
        });

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.DOOR) {
            @Override
            protected void onCollisionBegin(Entity player, Entity door) {//allows player to go the final dimension and fight the end final boss
                FXGL.getGameWorld().removeEntities(monsters);
                monsters.clear();
                initFinalMap();
            }
        });

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.BULLET, EntityType.DRAGON) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity dragon) {//hurts dragon through player's bullets
                bullet.removeFromWorld();
                dragon.getComponent(DragonControl.class).healthAttack();

                if (dragon!=null && dragon.getComponent(DragonControl.class).getHealth()<=0) {//checks if dragon's health is 0 by which it will allow player to restart
//                    getGameWorld().clear();
                    FXGL.getGameWorld().removeEntity(player);
                    FXGL.getGameWorld().removeEntities(collidingObj);
                    FXGL.getGameScene().clearUINodes();
                    retryLbl.setText("YOU WON!!");
                    collidingObj.clear();
                    FXGL.getGameScene().addUINode(retryPane);
                }
            }
        });

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.DRAGON_BULLET) {
            @Override
            protected void onCollisionBegin(Entity player, Entity dBullet) {//checks if player is attacked by the dragon bullet and will hurt the player badly
                dBullet.removeFromWorld();
                if (armor>=2) {//checks if they have enough armor to protect them or if they have some or none
                    armor-=2;
                } else if (armor==1) {
                    armor =0;
                    health-=2;
                } else {
                    health-=3;
                }

                if (health<=0) {//checks if player died and if so will allow the player to restart the game
//                    getGameWorld().clear();
                    FXGL.getGameWorld().removeEntity(player);
                    FXGL.getGameWorld().removeEntities(collidingObj);
                    FXGL.getGameScene().clearUINodes();
                    retryLbl.setText("YOU DIED");
                    collidingObj.clear();
                    FXGL.getGameScene().addUINode(retryPane);
                }
            }
        });
    }

    //precondition: none
    //postcondition: initiates the final boss map
    public void initFinalMap() {
        FXGL.getGameWorld().removeEntity(player);
        FXGL.getGameWorld().removeEntities(collidingObj);
        collidingObj.clear();
        FXGL.getGameScene().clearUINodes();
        initUI();
        FXGL.getGameWorld().clear();
        FXGL.getGameWorld().setLevelFromMap("end.tmx");
        player = Entities.builder()
                .type(EntityType.PLAYER)
                .at(100, 300)
                .viewFromNodeWithBBox(new Rectangle(11, 11, Color.BLUE))
                .with(new CollidableComponent(true))
                .buildAndAttach(getGameWorld());

        dragon = Entities.builder()
                .type(EntityType.DRAGON)
                .at(400, 215)
                .viewFromNodeWithBBox(new Rectangle(150, 150, Color.RED))
                .with(new CollidableComponent(true))
                .with(new DragonControl(player))
                .buildAndAttach(getGameWorld());
        dragon.getComponent(DragonControl.class).onUpdate(10000000);
        collidingObj.add(dragon);
    }

    //precondition:none
    //postcondition:initializes the ui of the game
    @Override
    protected void initUI() {
        goldLbl.setFont(new Font("Comic Sans MS Bold Italic", 20));//gold label
        goldLbl.setTextFill(Color.DARKGOLDENROD);
        FXGL.getGameScene().addUINode(goldLbl);

        healthLbl.setFont(new Font("Comic Sans MS Bold Italic", 20));//health label
        healthLbl.setTextFill(Color.ORANGERED);
        healthLbl.setLayoutY(20);
        FXGL.getGameScene().addUINode(healthLbl);

        armorLbl.setFont(new Font("Comic Sans MS Bold Italic", 20));//armor label
        armorLbl.setTextFill(Color.CORNFLOWERBLUE);
        armorLbl.setLayoutY(40);
        FXGL.getGameScene().addUINode(armorLbl);

        //anchorpanes
        //start armor pane
        armorPane = new AnchorPane();//makes a header, 4 images (hat,chest armor, leg armor, boots), and 4 buy buttons
        armorPane.setLayoutX(19*11);
        armorPane.setLayoutY(16*11);
        armorPane.setPrefHeight(15*11);
        armorPane.setPrefWidth(35*11);
        armorPane.setStyle("-fx-background-color: white");

        makeLbl("ARMOR SHOP", new Font("Comic Sans MS", 15), armorPane, 23,2,6,.5);

        makeImgView("helmet.png", armorPane, .5, 3);
        makeImgView("chestplate.png", armorPane, 9.25, 3);
        makeImgView("leggings.png", armorPane, 18,3);
        makeImgView("boots.png", armorPane, 26.75, 3);

        makeBtn(new Font("Comic Sans MS", 12), armorPane, 7.75, 3.5, .5, 11);
        makeBtn(new Font("Comic Sans MS", 12), armorPane, 7.75, 3.5, 9.25, 11);
        makeBtn(new Font("Comic Sans MS", 12), armorPane, 7.75, 3.5, 18, 11);
        makeBtn(new Font("Comic Sans MS", 12), armorPane, 7.75, 3.5, 26.75, 11);
        //end armor pane

        //start weapon pane
        weaponPane = new AnchorPane();//weapon buying pane with 1 header, 1 image of a sword, and 1 buy button
        weaponPane.setLayoutX(30*11);
        weaponPane.setLayoutY(16*11);
        weaponPane.setPrefHeight(15*11);
        weaponPane.setPrefWidth(13*11);
        weaponPane.setStyle("-fx-background-color: white");

        makeLbl("WEAPON SHOP", new Font("Comic Sans MS", 13), weaponPane, 9,2,2,.5);

        makeImgView("sword.png",weaponPane, 2,3);

        makeBtn(new Font("Comic Sans MS", 15), weaponPane,9,3.5,2,11);
        //end weapon pane

        //start retry pane
        retryPane = new AnchorPane();//retry anchorpane if you die or win with 1 header saying you died of won and 1 button to retry
        retryPane.setLayoutX(30*11);
        retryPane.setLayoutY(16*11);
        retryPane.setPrefHeight(15*11);
        retryPane.setPrefWidth(13*11);
        retryPane.setStyle("-fx-background-color: white");

        retryLbl = new Label("YOU WON!!");
        retryLbl.setFont(new Font("Comic Sans MS", 17));
        retryLbl.setAlignment(Pos.CENTER);
        retryLbl.setPrefSize(9*11, 5*11);
        retryPane.getChildren().add(retryLbl);
        retryPane.getChildren().get(retryPane.getChildren().indexOf(retryLbl)).setLayoutX(2*11);
        retryPane.getChildren().get(retryPane.getChildren().indexOf(retryLbl)).setLayoutY(.5*11);

        Button retBtn = new Button("RETRY");
        retBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            //precondition:none
            //postcondition:resets the game for the user to be able to play it again
            @Override
            public void handle(MouseEvent event) {
                FXGL.getGameScene().removeUINode(retryPane);
                getGameWorld().setLevelFromMap("map.tmx");
                armor = 0;
                gold = 0;
                backArmorCount = 0;
                health = 10;
                initUI();
                initGame();
            }
        });
        retBtn.setAlignment(Pos.CENTER);
        retBtn.setFont(new Font("Comic Sans MS", 15));
        retBtn.setPrefSize(9*11, 5*11);
        retryPane.getChildren().add(retBtn);
        retryPane.getChildren().get(retryPane.getChildren().indexOf(retBtn)).setLayoutX(2*11);
        retryPane.getChildren().get(retryPane.getChildren().indexOf(retBtn)).setLayoutY(7*11);
    }

    //precondition:text of the label, font of the label, destination anchorpane, specifications
    //postcondition:makes label with correct specifications and attaches it to the anchorpane of desire
    public void makeLbl(String text, Font font, AnchorPane destPane, double w, double h, double x, double y) {
        Label lbl = new Label(text);
        lbl.setFont(font);
        lbl.setAlignment(Pos.CENTER);
        lbl.setPrefSize(w*11,h*11);
        destPane.getChildren().add(lbl);
        destPane.getChildren().get(destPane.getChildren().indexOf(lbl)).setLayoutX(x*11);
        destPane.getChildren().get(destPane.getChildren().indexOf(lbl)).setLayoutY(y*11);
    }

    //precondition:String for the filepath of the image, desitination anchorpane, x and y of imgview in the pane
    //postcondition: makes and adds an imageview to an anchorpane at desired specifications
    public void makeImgView(String imgUrl, AnchorPane destPane, double x, double y) {
        ImageView imgView = new ImageView();
        imgView.setImage(new Image(imgUrl));
        destPane.getChildren().add(imgView);
        destPane.getChildren().get(destPane.getChildren().indexOf(imgView)).setLayoutX(x*11);
        destPane.getChildren().get(destPane.getChildren().indexOf(imgView)).setLayoutY(y*11);
    }

    //precondition:Font for the button, An anchorpane to put the button in, width, height, x value on the anchorpane for the btn, y value on the anchorpane for the btn
    //postcondition: make a button in the anchor pane with the specifications
    public void makeBtn(Font font, AnchorPane destPane, double w, double h, double x, double y) {
        Button btn = new Button("BUY-2");
        btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            //precondition: none
            //postcondition: will check if user has enough gold to buy a piece of armor
            @Override
            public void handle(MouseEvent event) {
                if (gold<2) {//checks if user has enough gold
                    JOptionPane.showMessageDialog(null, "YOU DO NOT HAVE ENOUGH GOLD!");
                } else {//if so it will buy the armor and disable the button
                    gold-=2;
                    armor+=2;
                    backArmorCount+=2;
                    btn.setDisable(true);
                    btn.setText("OWNED");
                }
            }
        });
        btn.setFont(font);
        btn.setPrefSize(w*11,h*11);
        destPane.getChildren().add(btn);
        destPane.getChildren().get(destPane.getChildren().indexOf(btn)).setLayoutX(x*11);
        destPane.getChildren().get(destPane.getChildren().indexOf(btn)).setLayoutY(y*11);
    }

    //precondition:none
    //postcondition:checks if it is time to randomly pick a monster to shoot a bullet
    public void monsterShoot() {
        if ((System.nanoTime()-mBulletNano)/1000000000>1 && monsters.size()>0) {//checks if monsters are available and if it has been time
            Entity mTempBullet;//random bullet for the monster
            int rand = (int)(Math.random()*monsters.size());//picks a monster from the list of monsters
            switch ((int)(Math.random()*4)+1) {//picks a direction for the monster to shoot
                case 1://R //the bullet will be faced right and shot
                    mTempBullet = Entities.builder()
                            .type(EntityType.MONSTER_BULLET)
                            .at(monsters.get(rand).getX()+11, monsters.get(rand).getY()+5.5)
                            .viewFromNodeWithBBox(new Rectangle(8,2,Color.RED))
                            .with(new CollidableComponent(true))
                            .with(new BulletControl(2,0,monsters.get(rand).getX()+11, monsters.get(rand).getY()+5.5))
                            .buildAndAttach();
                    break;
                case 2://L //the bullet will be faced left and shot
                    mTempBullet = Entities.builder()
                            .type(EntityType.MONSTER_BULLET)
                            .at(monsters.get(rand).getX()-8, monsters.get(rand).getY()+5.5)
                            .viewFromNodeWithBBox(new Rectangle(8, 2, Color.RED))
                            .with(new CollidableComponent(true))
                            .with(new BulletControl(-2, 0, monsters.get(rand).getX()-8, monsters.get(rand).getY()+5.5))
                            .buildAndAttach();
                    break;
                case 3://U //the bullet will be faced up and shot
                    mTempBullet = Entities.builder()
                            .type(EntityType.MONSTER_BULLET)
                            .at(monsters.get(rand).getX()+5.5, monsters.get(rand).getY()-8)
                            .viewFromNodeWithBBox(new Rectangle(2,8,Color.RED))
                            .with(new CollidableComponent(true))
                            .with(new BulletControl(0, -2, monsters.get(rand).getX()+5.5, monsters.get(rand).getY()-8))
                            .buildAndAttach();
                    break;
                default://D //the bullet will be faced down and shot
                    mTempBullet = Entities.builder()
                            .type(EntityType.MONSTER_BULLET)
                            .at(monsters.get(rand).getX()+5.5, monsters.get(rand).getY()+11)
                            .viewFromNodeWithBBox(new Rectangle(2,8,Color.RED))
                            .with(new CollidableComponent(true))
                            .with(new BulletControl(0,2,monsters.get(rand).getX()+5.5, monsters.get(rand).getY()+11))
                            .buildAndAttach();
                    break;
            }
            mTempBullet.getComponent(BulletControl.class).onUpdate(1.0);//moves the bullet
            mBulletNano = System.nanoTime();
        }
    }

    //precondition:none
    //postcondition:checks if player is eligible to fight the final boss
    public void checkArmorCol() {
        if (backArmorCount==10) {//checks if player has 10 pieces of armor in total no matter the player's actual armor count
            collidingObj.remove(door);
            door.setViewFromTextureWithBBox("openDoor.png");//opens the door and makes it collidable
            backArmorCount = 0;
        }
    }

    //precondition:the function will execute at a speed/ratio of tpf
    //postcondition:updates labels, checks if monsters are to shoot, checks if the player has enough armor to open the door and go to the final boss
    @Override
    protected void onUpdate(double tpf) {
        goldLbl.setText("GOLD: "+gold);
        healthLbl.setText("HEALTH: " + health);
        armorLbl.setText("ARMOR: " + armor);
        monsterShoot();
        checkArmorCol();
    }

    public static void main(String[] args) {
        launch(args);
    }
}