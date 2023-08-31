package com.example.mdpremotecontrol.boundary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.example.mdpremotecontrol.R;
import com.example.mdpremotecontrol.control.GridControl;
import com.example.mdpremotecontrol.enums.GridState;


public class GridView extends View{
    private int gridSize; // size (height/width) of the grid
    private int dim; // number of boxes in the grid
    private int gridInterval; // size of grid boxes
    private Paint innerBorderPaint = new Paint();
    private Paint outerBorderPaint = new Paint();
    private Paint paint=new Paint();
    private Paint paint_back=new Paint();
    private Paint paint_stroke = new Paint();
    private int[] print_back=new int[]{-1,-1};

    //Other obstacles
    private Rect[][] cells;
    private GridControl gridControl;

    private Boolean toCreateBlock =true;

    private Boolean makeBlock=false;

    private int[] greyShadow=new int[]{-1,-1};

    private int[] greenShadow=new int[]{-2,-2};


    public GridView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SquareGridView,
                0, 0);
        try {
            dim = a.getInt(R.styleable.SquareGridView_dim, 21);
            innerBorderPaint.setColor(a.getColor(R.styleable.SquareGridView_border_color, Color.BLACK));
            innerBorderPaint.setStrokeWidth(a.getInt(R.styleable.SquareGridView_inner_border_width, 2));
            outerBorderPaint.setColor(a.getColor(R.styleable.SquareGridView_border_color, Color.BLACK));
            outerBorderPaint.setStrokeWidth(a.getInt(R.styleable.SquareGridView_outer_border_width, 3));
            paint_stroke.setStyle(Paint.Style.STROKE);
            paint_stroke.setColor(a.getColor(R.styleable.SquareGridView_border_color, Color.BLACK));
            paint_stroke.setStrokeWidth(a.getInt(R.styleable.SquareGridView_inner_border_width, 2));
        } finally {
            a.recycle();
        }
        setBackgroundColor(Color.GRAY);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    public void setSides(double val){
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width=(int) val;
        params.height=(int) val;
        setLayoutParams(params);
        gridInterval = (int) val / dim;
        gridSize = gridInterval * dim;
        params.height = gridSize;
        params.width = gridSize;
        setLayoutParams(params);
        invalidate();
    }



    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //For other obstacles
        cells = new Rect[20][20];
        gridControl=GridControl.getInstance();
        gridControl.setCellStates(new GridState[20][20]);
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                cells[i][j] = new Rect((i+1) * gridInterval, j * gridInterval, (i + 2) * gridInterval, (j + 1) * gridInterval);
                gridControl.setCellState(i,j,GridState.EMTPY); //0==Empty,1==block, 2==image obstacle
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setX(0);
        setY(0);



//        // Draw vertical lines
//        // for each grid box, draw the left border
//        for (int i = 1+1; i < dim; i++)
//            canvas.drawLine(
//                    i * gridInterval,
//                    0,
//                    i * gridInterval,
//                    gridSize-gridInterval,
//                    innerBorderPaint);
//
//        // Draw vertical lines
//        // for each grid box, draw the bottom border
//        for (int i = 1; i < dim-1; i++)
//            canvas.drawLine(
//                    gridInterval,
//                    i * gridInterval,
//                    gridSize,
//                    i * gridInterval,
//                    innerBorderPaint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint_back.setStyle(Paint.Style.FILL);
        paint_back.setTextAlign(Paint.Align.CENTER);
        paint_back.setTextSize(gridInterval*5);
        paint_back.setColor(Color.GRAY);


        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                if (gridControl.getCellState(i, j) == GridState.BLOCK) {
                    paint.setColor(Color.BLACK);
                    canvas.drawRect(cells[i][j], paint);
                }
                else if (greyShadow[0]==i && greyShadow[1]==j) {
                    paint.setColor(Color.GRAY);
                    canvas.drawRect(cells[i][j], paint);
                }
                else if (greyShadow[0]==i && greyShadow[1]<j){
                    paint.setColor(getResources().getColor(R.color.green_in));
                    canvas.drawRect(cells[i][j],paint);
                    canvas.drawRect(cells[i][j], paint_stroke);
                }

                else if (greyShadow[1]==j && greyShadow[0]>i){
                    paint.setColor(getResources().getColor(R.color.green_in));
                    canvas.drawRect(cells[i][j],paint);
                    canvas.drawRect(cells[i][j], paint_stroke);
                }
                else if (greenShadow[0]>=i-1 && greenShadow[0]<=i+1 && greenShadow[1]>=j-1 && greenShadow[1]<=j+1) {
                    paint.setColor(getResources().getColor(R.color.green_out));
                    canvas.drawRect(cells[i][j], paint);
                    canvas.drawRect(cells[i][j], paint_stroke);
                }

                else if (i!=-1 && j!=20){
                    if (i!=-1 && j!=20) {
                        paint.setColor(getResources().getColor(R.color.white_soft));
                    }
                    else{
                        paint.setColor(Color.GRAY);
                    }
                    canvas.drawRect(cells[i][j], paint);
                    canvas.drawRect(cells[i][j], paint_stroke);
                }


                paint.setColor(getResources().getColor(R.color.white_soft));

                if ((i == 0 || j == 19) && (i == 0 && j == 19)) {
                    canvas.drawText(String.format("%d", 19-j), cells[i][j].left + (gridInterval / 2) -gridInterval, cells[i][j].bottom - (gridInterval / 2), paint);
                    canvas.drawText(String.format("%d", i), cells[i][j].left + (gridInterval / 2), cells[i][j].bottom - (gridInterval / 2) + gridInterval, paint);
                }
                else if ((i == 0)) {
                    canvas.drawText(String.format("%d", 19-j), cells[i][j].left + (gridInterval / 2)-gridInterval, cells[i][j].bottom - (gridInterval / 2), paint);
                }
                else if ((j == 19)) {
                    canvas.drawText(String.format("%d", i), cells[i][j].left + (gridInterval / 2), cells[i][j].bottom - (gridInterval / 2) + gridInterval, paint);
                }
            }
            }

        if (print_back[0]!=-1){
            canvas.drawText(String.format("(%d,%d)", print_back[0],print_back[1]), gridSize/2, gridSize/2, paint_back);
        }


        // Draw grid outer border
        canvas.drawLines(new float[]{
                        gridInterval, 0, gridSize, 0, // top border
                        gridInterval, gridSize-gridInterval, gridSize, gridSize-gridInterval, // bottom border
                        gridInterval, 0, gridInterval, gridSize-gridInterval, // left border
                        gridSize, 0, gridSize, gridSize-gridInterval, // right border
                },
                outerBorderPaint);
    }

    public int getGridInterval(){
        return gridInterval;
    }

    public void setPrint_back(int x,int y){
        print_back[0]=x-1;
        print_back[1]=19-y;
        greyShadow[0]=x-1;
        greyShadow[1]=y;
        if (print_back[0]>19 || print_back[0]<0 ||print_back[1]>19 || print_back[1]<0){
            cancelPrint_back();
        }
        invalidate();
    }

    public void setPrint_back_robot(int x,int y){
        if (x==1) x=2;
        if (x==20) x=19;
        if (y==0) y=1;
        if (y==19) y=18;
        if (x>1 && x<20 && y>0 && y<19) {
            print_back[0] = x - 1;
            print_back[1] = 19 - y;
            greenShadow[0] = x - 1;
            greenShadow[1] = y;
            if (print_back[0] > 19 || print_back[0] < 0 || print_back[1] > 19 || print_back[1] < 0) {
                cancelPrint_back_robot();
            }
            invalidate();
        }
//        else cancelPrint_back_robot();
    }

    public void setPrint_back(int x,int y,boolean shadow){
        print_back[0]=x-1;
        print_back[1]=19-y;
        if (shadow) {
            greyShadow[0] = x - 1;
            greyShadow[1] = y;
        }
        if (print_back[0]>19 || print_back[0]<0 ||print_back[1]>19 || print_back[1]<0){
            cancelPrint_back();
        }
        invalidate();
    }

    public void cancelPrint_back(){
        print_back[0]=-1;
        print_back[1]=-1;
        greyShadow[0]=-1;
        greyShadow[1]=-1;
        invalidate();
    }

    public void cancelPrint_back_robot(){
        print_back[0]=-1;
        print_back[1]=-1;
        greenShadow[0]=-2;
        greenShadow[1]=-2;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX() / gridInterval;
        int y = (int) event.getY() / gridInterval;
        if (!isValidBlockPlacement(x,y)||!makeBlock){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setPrint_back(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    setPrint_back(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    cancelPrint_back();
                    break;
            }
        }
        else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setPrint_back(x, y,false);
                    switch (gridControl.getCellState(x-1, y)) {
                        case EMTPY:
                            gridControl.setCellState(x-1, y, GridState.BLOCK);
                            toCreateBlock = true;
                            invalidate();
                            break;
                        case BLOCK:
                            gridControl.setCellState(x-1, y, GridState.EMTPY);
                            toCreateBlock = false;
                            invalidate();
                            break;
                        default:
                            toCreateBlock = true;
                            break;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (toCreateBlock == true) {
                        setPrint_back(x, y,false);
                        switch (gridControl.getCellState(x-1, y)) {
                            case EMTPY:
                                gridControl.setCellState(x-1, y, GridState.BLOCK);
                                invalidate();
                                break;
                            default:
                                break;
                        }
                    } else {
                        switch (gridControl.getCellState(x-1, y)) {
                            case BLOCK:
                                gridControl.setCellState(x-1, y, GridState.EMTPY);
                                toCreateBlock = false;
                                invalidate();
                                break;
                            default:
                                break;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    cancelPrint_back();
                    break;
            }
            return true;
        }
        return true;
    }


    public boolean isValidBlockPlacement(int x, int y){
        if (!(x>=1 && x<21 && y>=0 && y<20) || (gridControl.getCellState(x-1,y)==GridState.IMAGEBLOCK || gridControl.getCellState(x-1,y)==GridState.ROBOT)){
            return false;
        }
        else{
            return true;
        }
    }

    public void printMap(){
        String mapStr=new String();
        mapStr+='\n';
        for (int i=0;i<gridControl.getSideLength();i++){
            String rowStr=new String();
            for (int j=0;j<gridControl.getSideLength();j++){
                rowStr+=String.format("%d",gridControl.getCellState(j,i).getValue());
            }
            rowStr+='\n';
            mapStr+=rowStr;
        }
        Log.d("MAP", mapStr);
    }

    public void setMakeBlock(Boolean makeBlock){
        this.makeBlock=makeBlock;
    }
}