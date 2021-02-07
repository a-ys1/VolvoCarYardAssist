package com.example.yardassist.classes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.example.yardassist.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class CustomMap extends View {
    private Rect mRect, recAtPos;
    private Paint mPaint;
    private int mColor;
    private int rectWidth;
    docOfCells grid;
    docOfCells pathGrid;
    public int viewWidth, viewHeight;

    public boolean taskMap = false;
    public int pickedCol;
    public int pickedRow;

    //public boolean pathActive = false;

    public CustomMap(Context context) {
        super(context);
        init(null);
    }

    public CustomMap(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CustomMap(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public CustomMap(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(@Nullable AttributeSet set) {
        mRect = new Rect();
        mPaint = new Paint();
        mColor = Color.GREEN;
        rectWidth = 150;
    }

    public void setGrid(docOfCells grids){

            grid = grids;

        //Change the width of the view to be dynamic with number of rows and columns
        ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
        viewWidth = grid.columns * rectWidth;
        viewHeight = grid.rows * rectWidth;
        layoutParams.width = viewWidth;
        layoutParams.height = viewHeight;
        this.setLayoutParams(layoutParams);
        //Set margin so the hole view can be drawned
        //Operator position is not centered yet
        //For now, grid[0][0] should be at top left of screen when entering the map
        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams)this.getLayoutParams();
        marginParams.setMargins(0, 0, -viewWidth, -viewHeight);
        this.requestLayout();
    }
    public void setPathGrid(docOfCells pGrid){
        pathGrid = pGrid;
    }

    public void removePath(){
        for (int i = 0; i< grid.rows; i++){
            for (int j = 0; j < grid.columns; j++){
                grid.cells.get(i).cells.get(j).isPath = false;
            }
        }
    }

    public docOfCells getGrid(){
        return grid;
    }

    public int getRectWidth() {
        return rectWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        String userId = fAuth.getInstance().getCurrentUser().getUid();

        if(grid == null){
            return;
        }
        else {
            //Draw grid
            canvas.drawColor(Color.GRAY);
            int startRecW = 0;
            int startRecH = 0;
            for (int i = 0; i < grid.rows; ++i) {
                for (int j = 0; j < grid.columns; ++j) {
                    mRect.top = startRecH;
                    mRect.left = startRecW;
                    mRect.right = mRect.left + rectWidth;
                    mRect.bottom = mRect.top + rectWidth;
                    //fill
                    mPaint.setStyle(Paint.Style.FILL);

                    if(grid.cells.get(i).cells.get(j).users.contains(userId)){
                        mPaint.setColor(Color.GREEN);
                        canvas.drawRect(mRect, mPaint);
                    }
                    else if(grid.cells.get(i).cells.get(j).occupiedByVehicle){
                        mPaint.setColor(Color.RED);
                        canvas.drawRect(mRect, mPaint);
                    }
                    else if(grid.cells.get(i).cells.get(j).occupiedByOperator){
                        mPaint.setColor(Color.CYAN);
                        canvas.drawRect(mRect, mPaint);
                    }
                    else{
                        if(pathGrid != null){
                            if (pathGrid.cells.get(i).cells.get(j).isPath){
                                mPaint.setColor(Color.YELLOW);
                                canvas.drawRect(mRect, mPaint);
                            }
                        }
                        else if(taskMap) {
                            if(pickedRow == i && pickedCol == j){
                                mPaint.setColor(Color.DKGRAY);
                                canvas.drawRect(mRect, mPaint);
                            }
                        }
                        else{
                            mPaint.setColor(Color.WHITE);
                            canvas.drawRect(mRect, mPaint);
                        }

                    }

                    // border
                    mPaint.setStyle(Paint.Style.STROKE);
                    mPaint.setStrokeWidth(6);
                    mPaint.setColor(Color.BLACK);
                    canvas.drawRect(mRect, mPaint);

                    startRecW = mRect.left + rectWidth;
                }
                startRecH += rectWidth;
                startRecW = 0;
            }
        }
    }

    public void drawMap(){
        postInvalidate();
    }
}
