package com.example.mdpremotecontrol.control;

import com.example.mdpremotecontrol.enums.GridState;

/** Holds the grid map cell state
 * @author Jacques
 * @version 1.0
 */
public class GridControl {

    private static GridControl instance = null;
    private GridState[][] cellStates;
    private int sideLength;

    public GridControl() {
        this.cellStates =new GridState[20][20];
        this.sideLength=20;
    }

    /**
     * For singleton pattern
     * @return GridControl instance
     */
    public static GridControl getInstance() {
        if (instance == null) {
            instance = new GridControl();
        }
        return instance;
    }

    public void setCellStates(GridState[][] gridState){
        this.cellStates = gridState;
        this.sideLength=gridState.length;
    }

    public void setCellState(int x, int y,GridState gridState){
        this.cellStates[x][y]=gridState;
    }
    public void robotSetCellState(int x, int y,GridState gridState){
        if (this.cellStates[x][y]==GridState.BLOCK || this.cellStates[x][y]==GridState.IMAGEBLOCK) return;
        else this.cellStates[x][y]=gridState;
    }

    public GridState getCellState(int x,int y){
        return this.cellStates[x][y];
    }

    public int getSideLength(){
        return sideLength;
    }

    public void fullReset(){
        for (int i=0;i<cellStates.length;i++){
            for (int j=0;j<cellStates[i].length;j++){
                if (cellStates[i][j]==GridState.BLOCK){
                    cellStates[i][j]=GridState.EMTPY;
                }
            }
        }
    }


}
