package com.example.mdpremotecontrol.boundary;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mdpremotecontrol.R;
import com.example.mdpremotecontrol.control.GridControl;
import com.example.mdpremotecontrol.enums.FaceDirection;
import com.example.mdpremotecontrol.enums.GridState;
import com.example.mdpremotecontrol.utils.PathMaker;

public class RobotBoxView extends androidx.appcompat.widget.AppCompatTextView {
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
    private int SPEED = 500;
    private boolean isMoving = false;

    public RobotBoxView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setBackgroundResource(R.color.green_prime);
        setGravity(Gravity.CENTER);
        gridControl=GridControl.getInstance();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // draw triangle arrow
        int height = getHeight();
        int width = getWidth();
        Drawable triangleArrow = getResources().getDrawable(R.drawable.triangle_up, null);
        triangleArrow.setBounds(0, 0, getWidth(), getHeight());
        triangleArrow.draw(canvas);
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
    }

    public void placeInitial(double x, double y){

        //Look north initially
        setRotation(0);
        this.faceDirection=FaceDirection.NORTH;

        // set coordinates
        setX((float) ((MIN_COORD_X) * gridInterval));
        setY((float) ((20 - MIN_COORD_Y) * gridInterval));
        this.gridX = x;
        this.gridY = y;
        updateCellStates((int)gridX,(int)gridY, GridState.ROBOT); // How to update with new system? Though this initial is still correct
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        fullReset();
    }

    //Accept either:
    // 1. Forward Straight(FF)/Backward Straight(BB) 1 block
    // 2. Forward left (FL)/right (FR) & Backward left (BL)/right (BR) pi/2 turn

    public void moveForward(){
        // Check out of bounds and obstacles on next move
        // make buffer for button
//        if (checkBoundary(instruction) && !isBlockedPath((int)x,(int)y,direction)) {
////            updateCellStates((int)gridX,(int)gridY,GridState.EMTPY);
//            setX((float) ((x+1) * gridInterval));
//            setY((float) ((19 - y) * gridInterval));
//            this.gridX = x;
//            this.gridY = y;
//            updateCellStates((int)gridX,(int)gridY,GridState.ROBOT);
//        }
        ObjectAnimator translator;
        switch (faceDirection){
            case NORTH:
                if (!checkBoundary(gridX,gridY+1)){
                    return;
                }
                translator = ObjectAnimator.ofFloat(this,"y", getY()-gridInterval);
                translator.setDuration(SPEED/3);
                gridY+=1;
                break;
            case EAST:
                if (!checkBoundary(gridX+1,gridY)){
                    return;
                }
                translator = ObjectAnimator.ofFloat(this,"x", getX()+gridInterval);
                translator.setDuration(SPEED/3);
                gridX+=1;
                break;
            case SOUTH:
                if (!checkBoundary(gridX,gridY-1)){
                    return;
                }
                translator = ObjectAnimator.ofFloat(this,"y", getY()+gridInterval);
                translator.setDuration(SPEED/3);
                gridY-=1;
                break;
            case WEST:
                if (!checkBoundary(gridX-1,gridY)){
                    return;
                }
                translator = ObjectAnimator.ofFloat(this,"x", getX()-gridInterval);
                translator.setDuration(SPEED/3);
                gridX-=1;
                break;
            default:
                return;
        }
        translator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {
                isMoving=true;
            }
            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                isMoving=false;
            }
            @Override
            public void onAnimationCancel(@NonNull Animator animator) {}
            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {}
        });
        translator.start();

        // set coordinates

        printCoord();
    }

    public void moveBackward(){
        // Check out of bounds and obstacles on next move
        // make buffer for button

        ObjectAnimator translator;
        switch (faceDirection){
            case NORTH:
                if (!checkBoundary(gridX,gridY-1)){
                    return;
                }
                translator = ObjectAnimator.ofFloat(this,"y", getY()+gridInterval);
                translator.setDuration(SPEED/3);
                gridY-=1;
                break;
            case EAST:
                if (!checkBoundary(gridX-1,gridY)){
                    return;
                }
                translator = ObjectAnimator.ofFloat(this,"x", getX()-gridInterval);
                translator.setDuration(SPEED/3);
                gridX-=1;
                break;
            case SOUTH:
                if (!checkBoundary(gridX,gridY+1)){
                    return;
                }
                translator = ObjectAnimator.ofFloat(this,"y", getY()-gridInterval);
                translator.setDuration(SPEED/3);
                gridY+=1;
                break;
            case WEST:
                if (!checkBoundary(gridX+1,gridY)){
                    return;
                }
                translator = ObjectAnimator.ofFloat(this,"x", getX()+gridInterval);
                translator.setDuration(SPEED/3);
                gridX+=1;
                break;
            default:
                return;
        }
        translator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {
                isMoving=true;
            }
            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                isMoving=false;
            }
            @Override
            public void onAnimationCancel(@NonNull Animator animator) {}
            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {}
        });
        translator.start();

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
        ObjectAnimator rotator;
        switch (faceDirection){
            case NORTH:
                if (!checkBoundary(gridX-3,gridY+3)){
                    return;
                }
                gridX-=3;
                gridY+=3;
                path=pathMaker.make(getX(),getY(),"FL",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                rotator = ObjectAnimator.ofFloat(this, "rotation", 360f, 270f);
                rotator.setDuration(SPEED);
                setFaceDirection(FaceDirection.WEST);
                break;
            case EAST:
                if (!checkBoundary(gridX+3,gridY+3)){
                    return;
                }
                gridX+=3;
                gridY+=3;
                path=pathMaker.make(getX(),getY(),"FL",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                rotator = ObjectAnimator.ofFloat(this, "rotation", 90f, 0f);
                rotator.setDuration(SPEED);
                setFaceDirection(FaceDirection.NORTH);
                break;
            case SOUTH:
                if (!checkBoundary(gridX+3,gridY-3)){
                    return;
                }
                gridX+=3;
                gridY-=3;
                path=pathMaker.make(getX(),getY(),"FL",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                rotator = ObjectAnimator.ofFloat(this, "rotation", 180f, 90f);
                rotator.setDuration(SPEED);
                setFaceDirection(FaceDirection.EAST);
                break;
            case WEST:
                if (!checkBoundary(gridX-3,gridY-3)){
                    return;
                }
                gridX-=3;
                gridY-=3;
                path=pathMaker.make(getX(),getY(),"FL",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                rotator = ObjectAnimator.ofFloat(this, "rotation", 270f, 180f);
                rotator.setDuration(SPEED);
                setFaceDirection(FaceDirection.SOUTH);
                break;
            default:
                return;
        }
        translator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {
                isMoving=true;
            }
            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                isMoving=false;
            }
            @Override
            public void onAnimationCancel(@NonNull Animator animator) {}
            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {}
        });
        translator.start();
        rotator.start();

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
        ObjectAnimator rotator;
        switch (faceDirection){
            case NORTH:
                if (!checkBoundary(gridX+3,gridY+3)){
                    return;
                }
                gridX+=3;
                gridY+=3;
                path=pathMaker.make(getX(),getY(),"FR",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                rotator = ObjectAnimator.ofFloat(this, "rotation", 0f, 90f);
                rotator.setDuration(SPEED);
                setFaceDirection(FaceDirection.EAST);
                break;
            case EAST:
                if (!checkBoundary(gridX+3,gridY-3)){
                    return;
                }
                gridX+=3;
                gridY-=3;
                path=pathMaker.make(getX(),getY(),"FR",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                rotator = ObjectAnimator.ofFloat(this, "rotation", 90f, 180f);
                rotator.setDuration(SPEED);
                setFaceDirection(FaceDirection.SOUTH);
                break;
            case SOUTH:
                if (!checkBoundary(gridX-3,gridY-3)){
                    return;
                }
                gridX-=3;
                gridY-=3;
                path=pathMaker.make(getX(),getY(),"FR",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                rotator = ObjectAnimator.ofFloat(this, "rotation", 180f, 270f);
                rotator.setDuration(SPEED);
                setFaceDirection(FaceDirection.WEST);
                break;
            case WEST:
                if (!checkBoundary(gridX-3,gridY+3)){
                    return;
                }
                gridX-=3;
                gridY+=3;
                path=pathMaker.make(getX(),getY(),"FR",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                rotator = ObjectAnimator.ofFloat(this, "rotation", 270f, 360f);
                rotator.setDuration(SPEED);
                setFaceDirection(FaceDirection.NORTH);
                break;
            default:
                return;
        }
        translator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {
                isMoving=true;
            }
            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                isMoving=false;
            }
            @Override
            public void onAnimationCancel(@NonNull Animator animator) {}
            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {}
        });
        translator.start();
        rotator.start();





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
        ObjectAnimator rotator;
        switch (faceDirection){
            case NORTH:
                if (!checkBoundary(gridX-3,gridY-3)){
                    return;
                }
                gridX-=3;
                gridY-=3;
                path=pathMaker.make(getX(),getY(),"BL",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                rotator = ObjectAnimator.ofFloat(this, "rotation", 0f, 90f);
                rotator.setDuration(SPEED);
                setFaceDirection(FaceDirection.EAST);
                break;
            case EAST:
                if (!checkBoundary(gridX-3,gridY+3)){
                    return;
                }
                gridX-=3;
                gridY+=3;
                path=pathMaker.make(getX(),getY(),"BL",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                rotator = ObjectAnimator.ofFloat(this, "rotation", 90f, 180f);
                rotator.setDuration(SPEED);
                setFaceDirection(FaceDirection.SOUTH);
                break;
            case SOUTH:
                if (!checkBoundary(gridX+3,gridY+3)){
                    return;
                }
                gridX+=3;
                gridY+=3;
                path=pathMaker.make(getX(),getY(),"BL",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                rotator = ObjectAnimator.ofFloat(this, "rotation", 180f, 270f);
                rotator.setDuration(SPEED);
                setFaceDirection(FaceDirection.WEST);
                break;
            case WEST:
                if (!checkBoundary(gridX+3,gridY-3)){
                    return;
                }
                gridX+=3;
                gridY-=3;
                path=pathMaker.make(getX(),getY(),"BL",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                rotator = ObjectAnimator.ofFloat(this, "rotation", 270f, 360f);
                rotator.setDuration(SPEED);
                setFaceDirection(FaceDirection.NORTH);
                break;
            default:
                return;
        }
        translator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {
                isMoving=true;
            }
            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                isMoving=false;
            }
            @Override
            public void onAnimationCancel(@NonNull Animator animator) {}
            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {}
        });
        translator.start();
        rotator.start();
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
        ObjectAnimator rotator;
        switch (faceDirection){
            case NORTH:
                if (!checkBoundary(gridX+3,gridY-3)){
                    return;
                }
                gridX+=3;
                gridY-=3;
                path=pathMaker.make(getX(),getY(),"BR",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                rotator = ObjectAnimator.ofFloat(this, "rotation", 360f, 270f);
                rotator.setDuration(SPEED);
                setFaceDirection(FaceDirection.WEST);
                break;
            case EAST:
                if (!checkBoundary(gridX-3,gridY-3)){
                    return;
                }
                gridX-=3;
                gridY-=3;
                path=pathMaker.make(getX(),getY(),"BR",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                rotator = ObjectAnimator.ofFloat(this, "rotation", 90f, 0f);
                rotator.setDuration(SPEED);
                setFaceDirection(FaceDirection.NORTH);
                break;
            case SOUTH:
                if (!checkBoundary(gridX-3,gridY+3)){
                    return;
                }
                gridX-=3;
                gridY+=3;
                path=pathMaker.make(getX(),getY(),"BR",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                rotator = ObjectAnimator.ofFloat(this, "rotation", 180f, 90f);
                rotator.setDuration(SPEED);
                setFaceDirection(FaceDirection.EAST);
                break;
            case WEST:
                if (!checkBoundary(gridX+3,gridY+3)){
                    return;
                }
                gridX+=3;
                gridY+=3;
                path=pathMaker.make(getX(),getY(),"BR",faceDirection);
                translator = ObjectAnimator.ofFloat(this, "x", "y", path);
                translator.setDuration(SPEED);
                rotator = ObjectAnimator.ofFloat(this, "rotation", 270f, 180f);
                rotator.setDuration(SPEED);
                setFaceDirection(FaceDirection.SOUTH);
                break;
            default:
                return;
        }
        translator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {
                isMoving=true;
            }
            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                isMoving=false;
            }
            @Override
            public void onAnimationCancel(@NonNull Animator animator) {}
            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {}
        });
        translator.start();
        rotator.start();

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
                gridControl.setCellState(x+i,19-y+j,gridState);
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
        placeInitial(MIN_COORD_X, MIN_COORD_Y);
    }

    private void setFaceDirection(FaceDirection faceDirection){
        this.faceDirection=faceDirection;
    }
}