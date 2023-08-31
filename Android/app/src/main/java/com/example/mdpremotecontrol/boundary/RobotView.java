package com.example.mdpremotecontrol.boundary;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mdpremotecontrol.R;
import com.example.mdpremotecontrol.control.GridControl;
import com.example.mdpremotecontrol.enums.FaceDirection;
import com.example.mdpremotecontrol.enums.GridState;
import com.example.mdpremotecontrol.utils.MyDragShadowBuilder;
import com.example.mdpremotecontrol.utils.PathMaker;

import java.util.LinkedList;
import java.util.Queue;


public class RobotView extends androidx.appcompat.widget.AppCompatTextView {
    private String ROBOT_TAG="ROBOT_TAG";
    private int gridInterval;
    public double gridX;
    public double gridY;
    public FaceDirection faceDirection;
    private final int SIZE_SCALE = 3; // no of boxes the robot should take up
    private final int MIN_COORD_Y = SIZE_SCALE;
    private final int MIN_COORD_X = 1;
    private final int MAX_COORD_Y = 20;
    private final int MAX_COORD_X = 21-SIZE_SCALE;
    private GridControl gridControl;
    private PathMaker pathMaker;
    private int SPEED = 1000;
    private boolean isMoving = false;
    ImageView car;
    private boolean initialized=false;

    private boolean collisionCheck=false;

    private Queue<String> movementQ = new LinkedList<>();

    public RobotView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.CENTER);
        gridControl=GridControl.getInstance();
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                //Drag shadow builder
                View.DragShadowBuilder myShadow = new DragShadowBuilder(car);

                //start drag drop
                view.startDragAndDrop(null,
                        myShadow,
                        RobotView.this,
                        DRAG_FLAG_OPAQUE);
                return true;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        bringToFront();
        car.bringToFront();
        car.setX((float) getX());
        car.setY((float) (getY()));
    }

    public void setGridInterval(int gridInterval) {
        this.gridInterval = gridInterval;

        // resize view
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = SIZE_SCALE * gridInterval;
        params.width = SIZE_SCALE * gridInterval;
        setLayoutParams(params);

        // set robot at 0, 0
        //setX(0);
        //setY(0);
        placeInitial(MIN_COORD_X, MIN_COORD_Y);
        pathMaker=new PathMaker(gridInterval);
        car.setX(getX());
        car.setY(getY());
        car.getLayoutParams().height=SIZE_SCALE * gridInterval;
        car.getLayoutParams().width=SIZE_SCALE * gridInterval;
        bringToFront();
        car.bringToFront();
        fullReset();
    }

    public void placeInitial(double x, double y){

        //Look north initially
        setRotation(0);
        car.setRotation(0);
        this.faceDirection=FaceDirection.NORTH;

        // set coordinates
        setX((float) ((MIN_COORD_X) * gridInterval));
        setY((float) ((20 - MIN_COORD_Y) * gridInterval));
        car.setX((float) ((MIN_COORD_X) * gridInterval));
        car.setY((float) ((20 - MIN_COORD_Y) * gridInterval));
        this.gridX = x;
        this.gridY = y;
        updateCellStates((int)translatedX(),(int)translatedY(),GridState.ROBOT); // How to update with new system? Though this initial is still correct
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        placeInitial(MIN_COORD_X, MIN_COORD_Y);
    }

    //Accept either:
    // 1. Forward Straight(FF)/Backward Straight(BB) 1 block
    // 2. Forward left (FL)/right (FR) & Backward left (BL)/right (BR) pi/2 turn

    public void moveForward(){
        ObjectAnimator translator;
        ObjectAnimator translatorCar;
        String instruction ="FF";
        Log.d("ROBOT_TAG", String.format("moveForward: %s %f %f",gridControl.getCellState((int)translatedX(),(int)translatedY()),translatedX(),translatedY()));
        switch (faceDirection){
            case NORTH:
                if (!checkBoundary(gridX,gridY+1) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                translator = ObjectAnimator.ofFloat(this,"y", getY()-gridInterval);
                translator.setDuration(SPEED/3);
                translatorCar = ObjectAnimator.ofFloat(car,"y", getY()-gridInterval);
                translatorCar.setDuration(SPEED/3);
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridY+=1;
                break;
            case EAST:
                if (!checkBoundary(gridX+1,gridY) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                translator = ObjectAnimator.ofFloat(this,"x", getX()+gridInterval);
                translator.setDuration(SPEED/3);
                translatorCar = ObjectAnimator.ofFloat(car,"x", getX()+gridInterval);
                translatorCar.setDuration(SPEED/3);
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridX+=1;
                break;
            case SOUTH:
                if (!checkBoundary(gridX,gridY-1) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                translator = ObjectAnimator.ofFloat(this,"y", getY()+gridInterval);
                translator.setDuration(SPEED/3);
                translatorCar = ObjectAnimator.ofFloat(car,"y", getY()+gridInterval);
                translatorCar.setDuration(SPEED/3);
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridY-=1;
                break;
            case WEST:
                if (!checkBoundary(gridX-1,gridY) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                translator = ObjectAnimator.ofFloat(this,"x", getX()-gridInterval);
                translator.setDuration(SPEED/3);
                translatorCar = ObjectAnimator.ofFloat(car,"x", getX()-gridInterval);
                translatorCar.setDuration(SPEED/3);
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridX-=1;
                break;
            default:
                return;
        }
        translator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }
            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                goNext();
            }
            @Override
            public void onAnimationCancel(@NonNull Animator animator) {}
            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {}
        });
        translator.start();
        translatorCar.start();
        updateCellStates((int)translatedX(),(int)translatedY(),GridState.ROBOT);

        // set coordinates

        printCoord();
    }

    public void moveBackward(){
        // Check out of bounds and obstacles on next move
        // make buffer for button

        ObjectAnimator translator;
        ObjectAnimator translatorCar;
        String instruction = "BB";
        switch (faceDirection){
            case NORTH:
                if (!checkBoundary(gridX,gridY-1) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                translator = ObjectAnimator.ofFloat(this,"y", getY()+gridInterval);
                translator.setDuration(SPEED/3);
                translatorCar = ObjectAnimator.ofFloat(car,"y", getY()+gridInterval);
                translatorCar.setDuration(SPEED/3);
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridY-=1;
                break;
            case EAST:
                if (!checkBoundary(gridX-1,gridY) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                translator = ObjectAnimator.ofFloat(this,"x", getX()-gridInterval);
                translator.setDuration(SPEED/3);
                translatorCar = ObjectAnimator.ofFloat(car,"x", getX()-gridInterval);
                translatorCar.setDuration(SPEED/3);
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridX-=1;
                break;
            case SOUTH:
                if (!checkBoundary(gridX,gridY+1) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                translator = ObjectAnimator.ofFloat(this,"y", getY()-gridInterval);
                translator.setDuration(SPEED/3);
                translatorCar = ObjectAnimator.ofFloat(car,"y", getY()-gridInterval);
                translatorCar.setDuration(SPEED/3);
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridY+=1;
                break;
            case WEST:
                if (!checkBoundary(gridX+1,gridY) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                translator = ObjectAnimator.ofFloat(this,"x", getX()+gridInterval);
                translator.setDuration(SPEED/3);
                translatorCar = ObjectAnimator.ofFloat(car,"x", getX()+gridInterval);
                translatorCar.setDuration(SPEED/3);
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridX+=1;
                break;
            default:
                return;
        }
        translator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }
            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                goNext();
            }
            @Override
            public void onAnimationCancel(@NonNull Animator animator) {}
            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {}
        });
        translator.start();
        translatorCar.start();
        updateCellStates((int)translatedX(),(int)translatedY(),GridState.ROBOT);

//        // set coordinates
//        if (checkBoundary(x,y) && !isBlockedPath((int)x,(int)y,direction)) {
//            updateCellStates((int)gridX,(int)gridY,GridState.EMTPY);
//            setX((float) ((x+1) * gridInterval));
//            setY((float) ((19 - y) * gridInterval));
//            this.gridX = x;
//            this.gridY = y;
//            updateCellStates((int)gridX,(int)gridY,GridState.ROBOT);
//        }
        printCoord();
    }

    public void moveForwardLeft(){
        Path path;
        ObjectAnimator translator;
        ObjectAnimator translatorCar;
        ObjectAnimator rotatorCar;
        String instruction="FL";
        switch (faceDirection){
            case NORTH:
                if (!checkBoundary(gridX-3,gridY+3) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridX-=3;
                gridY+=3;
                path=pathMaker.make(getX(),getY(),"FL",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                translatorCar = ObjectAnimator.ofFloat(car, "x", "y", path);
                translatorCar.setDuration(SPEED);
                rotatorCar = ObjectAnimator.ofFloat(car, "rotation", 360f, 270f);
                rotatorCar.setDuration(SPEED);
                setFaceDirection(FaceDirection.WEST);
                break;
            case EAST:
                if (!checkBoundary(gridX+3,gridY+3) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridX+=3;
                gridY+=3;
                path=pathMaker.make(getX(),getY(),"FL",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                translatorCar = ObjectAnimator.ofFloat(car, "x", "y", path);
                translatorCar.setDuration(SPEED);
                rotatorCar = ObjectAnimator.ofFloat(car, "rotation", 90f, 0f);
                rotatorCar.setDuration(SPEED);
                setFaceDirection(FaceDirection.NORTH);
                break;
            case SOUTH:
                if (!checkBoundary(gridX+3,gridY-3) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridX+=3;
                gridY-=3;
                path=pathMaker.make(getX(),getY(),"FL",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                translatorCar = ObjectAnimator.ofFloat(car, "x", "y", path);
                translatorCar.setDuration(SPEED);
                rotatorCar = ObjectAnimator.ofFloat(car, "rotation", 180f, 90f);
                rotatorCar.setDuration(SPEED);
                setFaceDirection(FaceDirection.EAST);
                break;
            case WEST:
                if (!checkBoundary(gridX-3,gridY-3) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridX-=3;
                gridY-=3;
                path=pathMaker.make(getX(),getY(),"FL",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                translatorCar = ObjectAnimator.ofFloat(car, "x", "y", path);
                translatorCar.setDuration(SPEED);
                rotatorCar = ObjectAnimator.ofFloat(car, "rotation", 270f, 180f);
                rotatorCar.setDuration(SPEED);
                setFaceDirection(FaceDirection.SOUTH);
                break;
            default:
                return;
        }
        translator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }
            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                goNext();
            }
            @Override
            public void onAnimationCancel(@NonNull Animator animator) {}
            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {}
        });
        translator.start();
        translatorCar.start();
        rotatorCar.start();
        updateCellStates((int)translatedX(),(int)translatedY(),GridState.ROBOT);

//        // set coordinates
//        if (checkBoundary(x,y) && !isBlockedPath((int)x,(int)y,direction)) {
//            updateCellStates((int)gridX,(int)gridY,GridState.EMTPY);
//            setX((float) ((x+1) * gridInterval));
//            setY((float) ((19 - y) * gridInterval));
//            this.gridX = x;
//            this.gridY = y;
//            updateCellStates((int)gridX,(int)gridY,GridState.ROBOT);
//        }
        printCoord();
    }
    public void moveForwardRight(){
        Path path;
        ObjectAnimator translator;
        ObjectAnimator translatorCar;
        ObjectAnimator rotatorCar;
        String instruction = "FR";
        switch (faceDirection){
            case NORTH:
                if (!checkBoundary(gridX+3,gridY+3) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridX+=3;
                gridY+=3;
                path=pathMaker.make(getX(),getY(),"FR",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                translatorCar = ObjectAnimator.ofFloat(car, "x", "y", path);
                translatorCar.setDuration(SPEED);
                rotatorCar = ObjectAnimator.ofFloat(car, "rotation", 0f, 90f);
                rotatorCar.setDuration(SPEED);
                setFaceDirection(FaceDirection.EAST);
                break;
            case EAST:
                if (!checkBoundary(gridX+3,gridY-3) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridX+=3;
                gridY-=3;
                path=pathMaker.make(getX(),getY(),"FR",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                translatorCar = ObjectAnimator.ofFloat(car, "x", "y", path);
                translatorCar.setDuration(SPEED);
                rotatorCar = ObjectAnimator.ofFloat(car, "rotation", 90f, 180f);
                rotatorCar.setDuration(SPEED);
                setFaceDirection(FaceDirection.SOUTH);
                break;
            case SOUTH:
                if (!checkBoundary(gridX-3,gridY-3) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridX-=3;
                gridY-=3;
                path=pathMaker.make(getX(),getY(),"FR",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                translatorCar = ObjectAnimator.ofFloat(car, "x", "y", path);
                translatorCar.setDuration(SPEED);
                rotatorCar = ObjectAnimator.ofFloat(car, "rotation", 180f, 270f);
                rotatorCar.setDuration(SPEED);
                setFaceDirection(FaceDirection.WEST);
                break;
            case WEST:
                if (!checkBoundary(gridX-3,gridY+3) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridX-=3;
                gridY+=3;
                path=pathMaker.make(getX(),getY(),"FR",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                translatorCar = ObjectAnimator.ofFloat(car, "x", "y", path);
                translatorCar.setDuration(SPEED);
                rotatorCar = ObjectAnimator.ofFloat(car, "rotation", 270f, 360f);
                rotatorCar.setDuration(SPEED);
                setFaceDirection(FaceDirection.NORTH);
                break;
            default:
                return;
        }
        translator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }
            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                goNext();
            }
            @Override
            public void onAnimationCancel(@NonNull Animator animator) {}
            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {}
        });
        translator.start();
        translatorCar.start();
        rotatorCar.start();
        updateCellStates((int)translatedX(),(int)translatedY(),GridState.ROBOT);





//        // set coordinates
//        if (checkBoundary(x,y) && !isBlockedPath((int)x,(int)y,direction)) {
//            updateCellStates((int)gridX,(int)gridY,GridState.EMTPY);
//            setX((float) ((x+1) * gridInterval));
//            setY((float) ((19 - y) * gridInterval));
//            this.gridX = x;
//            this.gridY = y;
//            updateCellStates((int)gridX,(int)gridY,GridState.ROBOT);
//        }
        printCoord();
    }

    public void moveBackwardLeft(){
        Path path;
        ObjectAnimator translator;
        ObjectAnimator translatorCar;
        ObjectAnimator rotatorCar;
        String instruction = "BL";
        switch (faceDirection){
            case NORTH:
                if (!checkBoundary(gridX-3,gridY-3) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridX-=3;
                gridY-=3;
                path=pathMaker.make(getX(),getY(),"BL",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                translatorCar = ObjectAnimator.ofFloat(car, "x", "y", path);
                translatorCar.setDuration(SPEED);
                rotatorCar = ObjectAnimator.ofFloat(car, "rotation", 0f, 90f);
                rotatorCar.setDuration(SPEED);
                setFaceDirection(FaceDirection.EAST);
                break;
            case EAST:
                if (!checkBoundary(gridX-3,gridY+3) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridX-=3;
                gridY+=3;
                path=pathMaker.make(getX(),getY(),"BL",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                translatorCar = ObjectAnimator.ofFloat(car, "x", "y", path);
                translatorCar.setDuration(SPEED);
                rotatorCar = ObjectAnimator.ofFloat(car, "rotation", 90f, 180f);
                rotatorCar.setDuration(SPEED);
                setFaceDirection(FaceDirection.SOUTH);
                break;
            case SOUTH:
                if (!checkBoundary(gridX+3,gridY+3) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridX+=3;
                gridY+=3;
                path=pathMaker.make(getX(),getY(),"BL",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                translatorCar = ObjectAnimator.ofFloat(car, "x", "y", path);
                translatorCar.setDuration(SPEED);
                rotatorCar = ObjectAnimator.ofFloat(car, "rotation", 180f, 270f);
                rotatorCar.setDuration(SPEED);
                setFaceDirection(FaceDirection.WEST);
                break;
            case WEST:
                if (!checkBoundary(gridX+3,gridY-3) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridX+=3;
                gridY-=3;
                path=pathMaker.make(getX(),getY(),"BL",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                translatorCar = ObjectAnimator.ofFloat(car, "x", "y", path);
                translatorCar.setDuration(SPEED);
                rotatorCar = ObjectAnimator.ofFloat(car, "rotation", 270f, 360f);
                rotatorCar.setDuration(SPEED);
                setFaceDirection(FaceDirection.NORTH);
                break;
            default:
                return;
        }
        translator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }
            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                goNext();
            }
            @Override
            public void onAnimationCancel(@NonNull Animator animator) {}
            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {}
        });
        translator.start();
        translatorCar.start();
        rotatorCar.start();
        updateCellStates((int)translatedX(),(int)translatedY(),GridState.ROBOT);
//        // set coordinates
//        if (checkBoundary(x,y) && !isBlockedPath((int)x,(int)y,direction)) {
//            updateCellStates((int)gridX,(int)gridY,GridState.EMTPY);
//            setX((float) ((x+1) * gridInterval));
//            setY((float) ((19 - y) * gridInterval));
//            this.gridX = x;
//            this.gridY = y;
//            updateCellStates((int)gridX,(int)gridY,GridState.ROBOT);
//        }
        printCoord();
    }

    public void moveBackwardRight(){
        Path path;
        ObjectAnimator translator;
        ObjectAnimator translatorCar;
        ObjectAnimator rotatorCar;
        String instruction = "BR";
        switch (faceDirection){
            case NORTH:
                if (!checkBoundary(gridX+3,gridY-3) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridX+=3;
                gridY-=3;
                path=pathMaker.make(getX(),getY(),"BR",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                translatorCar = ObjectAnimator.ofFloat(car, "x", "y", path);
                translatorCar.setDuration(SPEED);
                rotatorCar = ObjectAnimator.ofFloat(car, "rotation", 360f, 270f);
                rotatorCar.setDuration(SPEED);
                setFaceDirection(FaceDirection.WEST);
                break;
            case EAST:
                if (!checkBoundary(gridX-3,gridY-3) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridX-=3;
                gridY-=3;
                path=pathMaker.make(getX(),getY(),"BR",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                translatorCar = ObjectAnimator.ofFloat(car, "x", "y", path);
                translatorCar.setDuration(SPEED);
                rotatorCar = ObjectAnimator.ofFloat(car, "rotation", 90f, 0f);
                rotatorCar.setDuration(SPEED);
                setFaceDirection(FaceDirection.NORTH);
                break;
            case SOUTH:
                if (!checkBoundary(gridX-3,gridY+3) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridX-=3;
                gridY+=3;
                path=pathMaker.make(getX(),getY(),"BR",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                translatorCar = ObjectAnimator.ofFloat(car, "x", "y", path);
                translatorCar.setDuration(SPEED);
                rotatorCar = ObjectAnimator.ofFloat(car, "rotation", 180f, 90f);
                rotatorCar.setDuration(SPEED);
                setFaceDirection(FaceDirection.EAST);
                break;
            case WEST:
                if (!checkBoundary(gridX+3,gridY+3) || (collisionCheck && !isColissionFree(instruction,faceDirection))){
                    Toast.makeText(this.getContext(),"Blocked", Toast.LENGTH_SHORT).show();
                    goNext();
                    return;
                }
                updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
                gridX+=3;
                gridY+=3;
                path=pathMaker.make(getX(),getY(),"BR",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                translatorCar = ObjectAnimator.ofFloat(car, "x", "y", path);
                translatorCar.setDuration(SPEED);
                rotatorCar = ObjectAnimator.ofFloat(car, "rotation", 270f, 180f);
                rotatorCar.setDuration(SPEED);
                setFaceDirection(FaceDirection.SOUTH);
                break;
            default:
                return;
        }
        translator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {

            }
            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                goNext();
            }
            @Override
            public void onAnimationCancel(@NonNull Animator animator) {}
            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {}
        });
        translator.start();
        translatorCar.start();
        rotatorCar.start();
        updateCellStates((int)translatedX(),(int)translatedY(),GridState.ROBOT);

//        // set coordinates
//        if (checkBoundary(x,y) && !isBlockedPath((int)x,(int)y,direction)) {
//            updateCellStates((int)gridX,(int)gridY,GridState.EMTPY);
//            setX((float) ((x+1) * gridInterval));
//            setY((float) ((19 - y) * gridInterval));
//            this.gridX = x;
//            this.gridY = y;
//            updateCellStates((int)gridX,(int)gridY,GridState.ROBOT);
//        }
        printCoord();
    }


    public boolean checkBoundary(double newGridX,double newGridY){
        //gridx is there top left corner is x_movement+1
        //grid y is where top left corner is 20-y_movement-size
        return (newGridX>=MIN_COORD_X && newGridX<=MAX_COORD_X && newGridY>=MIN_COORD_Y && newGridY<=MAX_COORD_Y);
    }

    public void updateCellStates(int x, int y, GridState gridState){
        for (int i=0;i<SIZE_SCALE;i++){
            for (int j=0;j<SIZE_SCALE;j++){
                gridControl.robotSetCellState(x+i,y+j,gridState);
            }
        }
    }
//
//    public boolean isBlockedPath(int x,int y,String direction){
//        Log.d("ROBOT_TAG",String.format( "isBlockedPath: %s",direction));
//        switch (direction){
//            case "up":
//                if (y-MIN_COORD<=MAX_COORD){
//                    for (int i=x;i<x+SIZE_SCALE;i++){
//                        if (gridControl.getCellState(i,19-y)!=GridState.EMTPY){
//                            return true;
//                        }
//                    }
//                    return false;
//                }
//                else{
//                    return true;
//                }
//            case "down":
//                if (MIN_COORD <= y){
//                    for (int i=x;i<x+SIZE_SCALE;i++){
//                        if (gridControl.getCellState(i,19-y+SIZE_SCALE-1)!=GridState.EMTPY){
//                            return true;
//                        }
//                    }
//                    return false;
//                }
//                else{
//                    return true;
//                }
//            case "left":
//                if (MIN_COORD<= x+MIN_COORD){
//                    for (int i=y;i>y-SIZE_SCALE;i--){
//                        if (gridControl.getCellState(x,19-i)!=GridState.EMTPY){
//                            return true;
//                        }
//                    }
//                    return false;
//                }
//                else{
//                    return true;
//                }
//            case "right":
//                if (x <= MAX_COORD){
//                    for (int i=y;i>y-SIZE_SCALE;i--){
//                        if (gridControl.getCellState(x+SIZE_SCALE-1,19-i)!=GridState.EMTPY){
//                            return true;
//                        }
//                    }
//                    return false;
//                }
//                else{
//                    return true;
//                }
//            default:
//                return true;
//        }
//    }

    public double getGridX(){
        return gridX;
    }

    public double getGridY(){
        return gridY;
    }


    public void printCoord(){
        Log.d(ROBOT_TAG, String.format("Robot at: x:%f y:%f\n x_minMax:%d %d,y_minMax:%d %d",gridX,gridY,MIN_COORD_X,MAX_COORD_X,MIN_COORD_Y,MAX_COORD_Y));
    }

    public int getSIZE_SCALE(){
        return SIZE_SCALE;
    }

    public boolean getIsMoving(){
        return isMoving;
    }

    public void fullReset(){
        updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
        placeInitial(MIN_COORD_X, MIN_COORD_Y);
    }

    private void setFaceDirection(FaceDirection faceDirection){
        this.faceDirection=faceDirection;
    }

    public void setCar(ImageView car){
        this.car=car;
    }

    private boolean isColissionFree(String instruction,FaceDirection faceDirection){
        int cX=(int) translatedX();
        int cY=(int) translatedY();
        switch (instruction){
            case "FF":
                switch (faceDirection){
                    case NORTH:
                        for (int i=0;i<SIZE_SCALE;i++){
                            Log.d("collission", String.format("isColissionFree: %d",cY));
                            if (gridControl.getCellState(cX+i,cY-1)!=GridState.EMTPY)return false;
                        }
                        return true;
                    case EAST:
                        for (int i=0;i<SIZE_SCALE;i++){
                            if (gridControl.getCellState(cX+3,cY+i)!=GridState.EMTPY)return false;
                        }
                        return true;
                    case SOUTH:
                        for (int i=0;i<SIZE_SCALE;i++){
                            if (gridControl.getCellState(cX+i,cY+3)!=GridState.EMTPY)return false;
                        }
                        return true;
                    case WEST:
                        for (int i=0;i<SIZE_SCALE;i++){
                            if (gridControl.getCellState(cX-1,cY+i)!=GridState.EMTPY)return false;
                        }
                        return true;
                    default:
                        return false;
            }
            case "BB":
                switch (faceDirection){
                    case NORTH:
                        for (int i=0;i<SIZE_SCALE;i++){
                            if (gridControl.getCellState(cX+i,cY+3)!=GridState.EMTPY)return false;
                        }
                        return true;
                    case EAST:
                        for (int i=0;i<SIZE_SCALE;i++){
                            if (gridControl.getCellState(cX-1,cY+i)!=GridState.EMTPY)return false;
                        }
                        return true;
                    case SOUTH:
                        for (int i=0;i<SIZE_SCALE;i++){
                            if (gridControl.getCellState(cX+i,cY-1)!=GridState.EMTPY)return false;
                        }
                        return true;
                    case WEST:
                        for (int i=0;i<SIZE_SCALE;i++){
                            if (gridControl.getCellState(cX+3,cY+i)!=GridState.EMTPY)return false;
                        }
                        return true;
                    default:
                        return false;
                }
            case "FL":
                switch (faceDirection){
                    case NORTH:
                        for (int i=0;i<SIZE_SCALE+3;i++){
                            for (int j=0;j<SIZE_SCALE;j++){
                                if (gridControl.getCellState(cX-3+i,cY-1-j)!=GridState.EMTPY)return false;
                            }
                        }
                        return true;
                    case EAST:
                        for (int i=0;i<SIZE_SCALE+3;i++){
                            for (int j=0;j<SIZE_SCALE;j++){
                                if (gridControl.getCellState(cX+3+j,cY+2-i)!=GridState.EMTPY)return false;
                            }
                        }
                        return true;
                    case SOUTH:
                        for (int i=0;i<SIZE_SCALE+3;i++){
                            for (int j=0;j<SIZE_SCALE;j++){
                                if (gridControl.getCellState(cX+i,cY+5-j)!=GridState.EMTPY)return false;
                            }
                        }
                        return true;
                    case WEST:
                        for (int i=0;i<SIZE_SCALE+3;i++){
                            for (int j=0;j<SIZE_SCALE;j++){
                                if (gridControl.getCellState(cX-3+j,cY+i)!=GridState.EMTPY)return false;
                            }
                        }
                        return true;
                    default:
                        return false;
                }
            case "FR":
                switch (faceDirection){
                    case NORTH:
                        for (int i=0;i<SIZE_SCALE+3;i++){
                            for (int j=0;j<SIZE_SCALE;j++){
                                if (gridControl.getCellState(cX+i,cY-1-j)!=GridState.EMTPY)return false;
                            }
                        }
                        return true;
                    case EAST:
                        for (int i=0;i<SIZE_SCALE+3;i++){
                            for (int j=0;j<SIZE_SCALE;j++){
                                if (gridControl.getCellState(cX+3+j,cY+i)!=GridState.EMTPY)return false;
                            }
                        }
                        return true;
                    case SOUTH:
                        for (int i=0;i<SIZE_SCALE+3;i++){
                            for (int j=0;j<SIZE_SCALE;j++){
                                if (gridControl.getCellState(cX-3+i,cY+5-j)!=GridState.EMTPY)return false;
                            }
                        }
                        return true;
                    case WEST:
                        for (int i=0;i<SIZE_SCALE+3;i++){
                            for (int j=0;j<SIZE_SCALE;j++){
                                if (gridControl.getCellState(cX-3+j,cY+2-i)!=GridState.EMTPY)return false;
                            }
                        }
                        return true;
                    default:
                        return false;
                }
            case "BL":
                switch (faceDirection){
                    case NORTH:
                        for (int i=0;i<SIZE_SCALE+3;i++){
                            for (int j=0;j<SIZE_SCALE;j++){
                                if (gridControl.getCellState(cX-3+i,cY+5-j)!=GridState.EMTPY)return false;
                            }
                        }
                        return true;
                    case EAST:
                        for (int i=0;i<SIZE_SCALE+3;i++){
                            for (int j=0;j<SIZE_SCALE;j++){
                                if (gridControl.getCellState(cX-3+j,cY+2-i)!=GridState.EMTPY)return false;
                            }
                        }
                        return true;
                    case SOUTH:
                        for (int i=0;i<SIZE_SCALE+3;i++){
                            for (int j=0;j<SIZE_SCALE;j++){
                                if (gridControl.getCellState(cX+i,cY-1-j)!=GridState.EMTPY)return false;
                            }
                        }
                        return true;
                    case WEST:
                        for (int i=0;i<SIZE_SCALE+3;i++){
                            for (int j=0;j<SIZE_SCALE;j++){
                                if (gridControl.getCellState(cX+3+j,cY+i)!=GridState.EMTPY)return false;
                            }
                        }
                        return true;
                    default:
                        return false;
                }
            case "BR":
                switch (faceDirection){
                    case NORTH:
                        for (int i=0;i<SIZE_SCALE+3;i++){
                            for (int j=0;j<SIZE_SCALE;j++){
                                if (gridControl.getCellState(cX+i,cY+5-j)!=GridState.EMTPY)return false;
                            }
                        }
                        return true;
                    case EAST:
                        for (int i=0;i<SIZE_SCALE+3;i++){
                            for (int j=0;j<SIZE_SCALE;j++){
                                if (gridControl.getCellState(cX-3+j,cY+i)!=GridState.EMTPY)return false;
                            }
                        }
                        return true;
                    case SOUTH:
                        for (int i=0;i<SIZE_SCALE+3;i++){
                            for (int j=0;j<SIZE_SCALE;j++){
                                if (gridControl.getCellState(cX-3+i,cY-1-j)!=GridState.EMTPY)return false;
                            }
                        }
                        return true;
                    case WEST:
                        for (int i=0;i<SIZE_SCALE+3;i++){
                            for (int j=0;j<SIZE_SCALE;j++){
                                if (gridControl.getCellState(cX+3+j,cY+2-i)!=GridState.EMTPY)return false;
                            }
                        }
                        return true;
                    default:
                        return false;
                }
            default:
                break;
        }
        return false;
    }

    private double translatedX(){
        // take gridX and translate it to actual cell
        return gridX-1;
    }

    private double translatedY(){
        return 20-gridY;
    }
    public int getCentreX(){
        return (int)translatedX()+1;
    }
    public int getCentreY(){
        return (int)translatedY()-1;
    }

    public void setRotation(String rotation){
        switch (rotation){
            case "0":
                car.setRotation(0);
                setRotation(0);
                this.faceDirection=FaceDirection.NORTH;
                break;
            case "-90":
                car.setRotation(90);
                setRotation(90);
                this.faceDirection=FaceDirection.EAST;
                break;
            case "180":
                car.setRotation(180);
                setRotation(180);
                this.faceDirection=FaceDirection.SOUTH;
                break;
            case "90":
                car.setRotation(270);
                setRotation(270);
                this.faceDirection=FaceDirection.WEST;
                break;
            default:
                return;
        }
    }

    public void teleport(int x, int y, String rotation){
        //From center
        switch (rotation){
            case "0":
                car.setRotation(0);
                setRotation(0);
                this.faceDirection=FaceDirection.NORTH;
                break;
            case "-90":
                car.setRotation(90);
                setRotation(90);
                this.faceDirection=FaceDirection.EAST;
                break;
            case "180":
                car.setRotation(180);
                setRotation(180);
                this.faceDirection=FaceDirection.SOUTH;
                break;
            case "90":
                car.setRotation(270);
                setRotation(270);
                this.faceDirection=FaceDirection.WEST;
                break;
            default:
                return;
        }
        setX((x)*gridInterval);
        setY((21-y-SIZE_SCALE)*gridInterval);
        car.setX((x)*gridInterval);
        car.setY((21-y-SIZE_SCALE)*gridInterval);

        updateCellStates((int)translatedX(),(int)translatedY(),GridState.EMTPY);
        gridX=x;
        gridY=y+2;
        Log.d("TAG", String.format("teleport: %f,%f",translatedX(),translatedY()));
        updateCellStates((int)translatedX(),(int)translatedY(),GridState.ROBOT);

    }

    public void setCollisionCheck(boolean bool){
        this.collisionCheck=bool;
    }

    public void addMovement(String move){
        movementQ.add(move);
        Log.e("isMoving", Boolean.toString(isMoving) );
        Log.e("movementQ", Integer.toString(movementQ.size()) );
        if (!isMoving) goNext(); // starts movement Q
    }

    public void goNext(){
        Log.e("ROBOTNEXT", "goNext: WORKING");
        if (!movementQ.isEmpty()) {
            isMoving=true;
            String nextMove = movementQ.remove();
            switch (nextMove) {
                case "FF":
                    moveForward();
                    break;
                case "BB":
                    moveBackward();
                    break;
                case "FL":
                    moveForwardLeft();
                    break;
                case "FR":
                    moveForwardRight();
                    break;
                case "BL":
                    moveBackwardLeft();
                    break;
                case "BR":
                    moveBackwardRight();
                    break;
                default:
                    return;
            }
        }
        else{
            isMoving=false;
        }
    }

    public void startDrag(){
        setVisibility(INVISIBLE);
        car.setVisibility(INVISIBLE);
    }

    public void stopDrag(){
        setVisibility(VISIBLE);
        car.setVisibility(VISIBLE);
    }
}