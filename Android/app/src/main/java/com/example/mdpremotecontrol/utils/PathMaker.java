package com.example.mdpremotecontrol.utils;

import android.graphics.Path;
import android.media.FaceDetector;

import com.example.mdpremotecontrol.enums.FaceDirection;

public class PathMaker{
    float mRadius;
    float mControlX1;
    float mControlY1;
    float mControlX2;
    float mControlY2;
    float mEndX;
    float mEndY;

    public PathMaker(float mRadius){
        this.mRadius=3*mRadius;
    }

    public Path make(float startX,float startY,String instruction, FaceDirection faceDirection){
        switch (faceDirection){
            case NORTH:
                return makeNorth(startX,startY,instruction);
            case EAST:
                return makeEast(startX,startY,instruction);
            case WEST:
                return makeWest(startX,startY,instruction);
            case SOUTH:
                return makeSouth(startX,startY,instruction);
            default:
                return null;
        }
    }

    private Path makeNorth(float startX,float startY,String instruction){
        switch (instruction){
            case "FL":
                mControlX1 = startX;
                mControlY1 = startY - mRadius / 2;
                mControlX2 = startX - mRadius /2;
                mControlY2 = startY - mRadius;
                mEndX = startX - mRadius;
                mEndY = startY - mRadius;
                break;
            case "FR":
                mControlX1 = startX;
                mControlY1 = startY - mRadius / 2;
                mControlX2 = startX + mRadius /2;
                mControlY2 = startY - mRadius;
                mEndX = startX + mRadius;
                mEndY = startY - mRadius;
                break;
            case "BL":
                mControlX1 = startX;
                mControlY1 = startY + mRadius / 2;
                mControlX2 = startX - mRadius /2;
                mControlY2 = startY + mRadius;
                mEndX = startX - mRadius;
                mEndY = startY + mRadius;
                break;
            case "BR":
                mControlX1 = startX;
                mControlY1 = startY + mRadius / 2;
                mControlX2 = startX + mRadius /2;
                mControlY2 = startY + mRadius;
                mEndX = startX + mRadius;
                mEndY = startY + mRadius;
                break;
            default:
                return null;
        }
        Path path = new Path();
        path.moveTo(startX, startY);
        path.cubicTo(mControlX1, mControlY1, mControlX2, mControlY2, mEndX, mEndY);
        return path;
    }

    private Path makeEast(float startX,float startY,String instruction){
        switch (instruction){
            case "FL":
                mControlX1 = startX + mRadius / 2;
                mControlY1 = startY ;
                mControlX2 = startX + mRadius ;
                mControlY2 = startY - mRadius /2;
                mEndX = startX + mRadius;
                mEndY = startY - mRadius;
                break;
            case "FR":
                mControlX1 = startX+ mRadius / 2;
                mControlY1 = startY ;
                mControlX2 = startX + mRadius;
                mControlY2 = startY + mRadius /2;
                mEndX = startX + mRadius;
                mEndY = startY + mRadius;
                break;
            case "BL":
                mControlX1 = startX - mRadius / 2;
                mControlY1 = startY;
                mControlX2 = startX - mRadius;
                mControlY2 = startY - mRadius /2;
                mEndX = startX - mRadius;
                mEndY = startY - mRadius;
                break;
            case "BR":
                mControlX1 = startX - mRadius / 2;
                mControlY1 = startY;
                mControlX2 = startX - mRadius;
                mControlY2 = startY + mRadius /2;
                mEndX = startX - mRadius;
                mEndY = startY + mRadius;
                break;
            default:
                return null;
        }
        Path path = new Path();
        path.moveTo(startX, startY);
        path.cubicTo(mControlX1, mControlY1, mControlX2, mControlY2, mEndX, mEndY);
        return path;
    }

    private Path makeSouth(float startX,float startY,String instruction){
        switch (instruction){
            case "FL":
                mControlX1 = startX;
                mControlY1 = startY + mRadius / 2;
                mControlX2 = startX + mRadius /2;
                mControlY2 = startY + mRadius;
                mEndX = startX + mRadius;
                mEndY = startY + mRadius;
                break;
            case "FR":
                mControlX1 = startX;
                mControlY1 = startY + mRadius / 2;
                mControlX2 = startX - mRadius /2;
                mControlY2 = startY + mRadius;
                mEndX = startX - mRadius;
                mEndY = startY + mRadius;
                break;
            case "BL":
                mControlX1 = startX;
                mControlY1 = startY - mRadius / 2;
                mControlX2 = startX + mRadius /2;
                mControlY2 = startY - mRadius;
                mEndX = startX + mRadius;
                mEndY = startY - mRadius;
                break;
            case "BR":
                mControlX1 = startX;
                mControlY1 = startY - mRadius / 2;
                mControlX2 = startX - mRadius /2;
                mControlY2 = startY - mRadius;
                mEndX = startX - mRadius;
                mEndY = startY - mRadius;
                break;
            default:
                return null;
        }
        Path path = new Path();
        path.moveTo(startX, startY);
        path.cubicTo(mControlX1, mControlY1, mControlX2, mControlY2, mEndX, mEndY);
        return path;
    }

    private Path makeWest(float startX,float startY,String instruction){
        switch (instruction){
            case "FL":
                mControlX1 = startX - mRadius / 2;
                mControlY1 = startY ;
                mControlX2 = startX - mRadius ;
                mControlY2 = startY + mRadius /2;
                mEndX = startX - mRadius;
                mEndY = startY + mRadius;
                break;
            case "FR":
                mControlX1 = startX - mRadius / 2;
                mControlY1 = startY ;
                mControlX2 = startX - mRadius;
                mControlY2 = startY - mRadius /2;
                mEndX = startX - mRadius;
                mEndY = startY - mRadius;
                break;
            case "BL":
                mControlX1 = startX + mRadius / 2;
                mControlY1 = startY;
                mControlX2 = startX + mRadius;
                mControlY2 = startY + mRadius /2;
                mEndX = startX + mRadius;
                mEndY = startY + mRadius;
                break;
            case "BR":
                mControlX1 = startX + mRadius / 2;
                mControlY1 = startY;
                mControlX2 = startX + mRadius;
                mControlY2 = startY - mRadius /2;
                mEndX = startX + mRadius;
                mEndY = startY - mRadius;
                break;
            default:
                return null;
        }
        Path path = new Path();
        path.moveTo(startX, startY);
        path.cubicTo(mControlX1, mControlY1, mControlX2, mControlY2, mEndX, mEndY);
        return path;
    }
}
