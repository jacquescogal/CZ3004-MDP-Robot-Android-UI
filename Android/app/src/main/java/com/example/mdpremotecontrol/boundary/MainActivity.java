package com.example.mdpremotecontrol.boundary;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;

import com.example.mdpremotecontrol.R;
import com.example.mdpremotecontrol.adapter.AdapterResponse;
import com.example.mdpremotecontrol.adapter.AdapterRobotStatus;
import com.example.mdpremotecontrol.control.BluetoothControl;
import com.example.mdpremotecontrol.control.GridControl;
import com.example.mdpremotecontrol.control.ResponseControl;
import com.example.mdpremotecontrol.control.RobotStatusControl;
import com.example.mdpremotecontrol.enums.FaceDirection;
import com.example.mdpremotecontrol.enums.SettingEnvironment;
import com.example.mdpremotecontrol.utils.SpacingItemDecoration;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mdpremotecontrol.databinding.ActivityMainBinding;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import java.util.Stack;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static String TAG = "MainActivity_TAG";

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private BluetoothDialogFragment bluetoothDialog;
    private static MainActivity instance;
    private final int[] degreeDirection = { 0, 180, 90, -90 };

    private int sentinalValueX=2;

    int seconds;
    int minutes;

    FloatingActionButton buttonBluetooth;
    ObstacleBoxView obstacleBoxView;
    ObstacleView[] obstacleViews;
    RobotView robotView;

    public static MainActivity getInstance() {
        return instance;
    }

    GridView gridMap;
    GridControl gridControl;

    AdapterResponse adapterResponse;
    SwipeRefreshLayout swipeRefreshLayoutResponse;
    ResponseControl responseControl;
    AdapterRobotStatus adapterRobotStatus;
    SwipeRefreshLayout swipeRefreshLayoutRobotStatus;
    RobotStatusControl robotStatusControl;
    RecyclerView recyclerViewRobotStatus;
    RecyclerView recyclerViewResponse;
    BluetoothControl bluetoothControl;

    ImageButton forward;
    ImageButton backward;
    ImageButton turnLeft;
    ImageButton turnRight;
    ImageButton turnBackLeft;
    ImageButton turnBackRight;

    Button buttonReset;
//    Button buttonSimulate;
    Button buttonSendExploration;
    Button buttonSendFastest;
    FloatingActionButton buttonReconnect;
    FloatingActionButton buttonDisconnect;

    ImageButton buttonToggleInstruct;
    EditText editTextInstruct;
    Button buttonInstruct;

    boolean isButtonInstructManual;


    SettingEnvironment settingEnvironment = SettingEnvironment.NULL;

    LinearLayout buttonHolder;
    ImageButton imageButtonReset;

    ImageButton imageButtonRedo;
    ImageButton imageButtonUndo;

    Thread sendThread;

    int configOrient;

    String orientStr="a";

    Stack<String> actionStack = new Stack<String>(); //go back to previous item

    TextView textTimer;
    Button buttonStart;

    Button buttonPreset;
    FloatingActionButton buttonBluetooth2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        configOrient=getResources().getConfiguration().orientation;
        if (configOrient==Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_main);
            orientStr="p";
        }
        else {
            setContentView(R.layout.activity_main_land);
            orientStr="h";
        }


        bluetoothControl = BluetoothControl.getInstance();


        checkPermissions();
        // setContentView(R.layout.activity_main);
        instance = this;

        buttonBluetooth = findViewById(R.id.bt_fab);
        buttonBluetooth.setVisibility(View.VISIBLE);
        buttonBluetooth2 = findViewById(R.id.bt_fab_2);
        buttonBluetooth.setOnClickListener(this);
        initializeGrid();
        initializeButtons();
        initializeMap();
        inializeRecycleViews();

    }

    public void sendMovement(String t, Context context) {
        String toSend = "STM:" + t;
        Log.d("BLUETOOTH", "Sending up with letter " + t);
        try {
            BluetoothControl.getInstance().write(toSend.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            Toast.makeText(context, "Bluetooth not connected", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendData(String t, Context context) {
        try {
            BluetoothControl.getInstance().write(t.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            Toast.makeText(context, "Bluetooth not connected", Toast.LENGTH_SHORT).show();
        }
    }
    public void sendData(String t, Context context, boolean toToast) {
        try {
            BluetoothControl.getInstance().write(t.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            Log.e(TAG, "sendData: Bluetooth not connected" );
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_fab:
                bluetoothDialog = new BluetoothDialogFragment();

                Bundle bundle = new Bundle();
                bundle.putBoolean("notAlertDialog", true);

                bluetoothDialog.setArguments(bundle);

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                bluetoothDialog.show(ft, "dialog");
                break;
            default:
                break;
        }
    }

    public void pressButton(int x) {
        ImageButton forward = findViewById(R.id.button_forward);
        ImageButton backward = findViewById(R.id.button_backward);
        ImageButton turnLeft = findViewById(R.id.button_turn_left);
        ImageButton turnRight = findViewById(R.id.button_turn_right);
        ImageButton turnBackLeft = findViewById(R.id.button_back_left);
        ImageButton turnBackRight = findViewById(R.id.button_back_right);
        switch (x) {
            case 1:
                forward.performClick();
                break;
            case 2:
                backward.performClick();
                break;
            case 3:
                turnLeft.performClick();
                break;
            case 4:
                turnRight.performClick();
                break;
            case 5:
                turnBackLeft.performClick();
                break;
            case 6:
                turnBackRight.performClick();
                break;
            default:
                break;
        }
    }

    private void initializeButtons() {
        forward = findViewById(R.id.button_forward);
        backward = findViewById(R.id.button_backward);
        turnLeft = findViewById(R.id.button_turn_left);
        turnRight = findViewById(R.id.button_turn_right);
        turnBackLeft = findViewById(R.id.button_back_left);
        turnBackRight = findViewById(R.id.button_back_right);

        buttonHolder = findViewById(R.id.ll_button_holder);
        imageButtonReset = findViewById(R.id.image_button_reset);
//        buttonSimulate = findViewById(R.id.button_simulate);
        buttonSendExploration = findViewById(R.id.button_explore);
        buttonSendFastest = findViewById(R.id.button_fastest);
        buttonReconnect = findViewById(R.id.rc_fab);
        buttonDisconnect = findViewById(R.id.dc_fab);

        buttonToggleInstruct = findViewById(R.id.m_input);
        editTextInstruct = findViewById(R.id.edit_text_custom_instruction);
        buttonInstruct = findViewById(R.id.button_send_custom_instruction);

        isButtonInstructManual=false;

        imageButtonUndo=findViewById(R.id.image_button_undo);
        imageButtonRedo=findViewById(R.id.image_button_redo);

        textTimer=findViewById(R.id.text_timer);
        buttonStart=findViewById(R.id.button_start);

        actionStack=new Stack<String>();

        buttonPreset=findViewById(R.id.button_preset);

        buttonBluetooth2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonBluetooth.callOnClick();
            }
        });
        buttonPreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupPreset();
            }
        });
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                seconds = 0;
                minutes = 0;

                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        seconds++;
                        if (seconds == 60) {
                            seconds = 0;
                            minutes++;
                        }
                        String time = String.format("Timer: %02d:%02d", minutes, seconds);
                        textTimer.setText(time);
                        handler.postDelayed(this, 1000);
                    }
                };
                handler.postDelayed(runnable, 1000);
            }
        });

        imageButtonUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sentinalValueX==2){
                    robotView.fullReset();
                    sentinalValueX--;
                }
                else if (sentinalValueX==1){
                    obstacleViews[1].fullReset(orientStr);
                }
            }
        });

        imageButtonRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sentinalValueX==1){
                    obstacleViews[1].moveShrink(10,10,FaceDirection.EAST);

                    sentinalValueX++;
                }
                else if (sentinalValueX==2){
                    turnRight.callOnClick();
                }
            }
        });

        imageButtonReset.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                gridControl.fullReset();
                for (ObstacleView obstacleView : obstacleViews) {
                    obstacleView.fullReset(orientStr);
                    Log.e(TAG, String.valueOf(obstacleView.getX()) );
                    Log.e(TAG, String.valueOf(obstacleView.getY()) );
                }
                robotView.fullReset();
                gridMap.invalidate();
                gridMap.printMap();
                return true;
            }
        });



        buttonToggleInstruct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageButton v = (ImageButton) view;
                if (!isButtonInstructManual) {
                    isButtonInstructManual=true;
                    forward.setVisibility(View.INVISIBLE);
                    turnLeft.setVisibility(View.INVISIBLE);
                    turnRight.setVisibility(View.INVISIBLE);
                    turnBackLeft.setVisibility(View.INVISIBLE);
                    turnBackRight.setVisibility(View.INVISIBLE);
                    backward.setVisibility(View.INVISIBLE);
                    buttonInstruct.setVisibility(View.VISIBLE);
                    editTextInstruct.setVisibility(View.VISIBLE);
                } else if (isButtonInstructManual) {
                    isButtonInstructManual=false;
                    forward.setVisibility(View.VISIBLE);
                    turnLeft.setVisibility(View.VISIBLE);
                    turnRight.setVisibility(View.VISIBLE);
                    turnBackLeft.setVisibility(View.VISIBLE);
                    turnBackRight.setVisibility(View.VISIBLE);
                    backward.setVisibility(View.VISIBLE);
                    buttonInstruct.setVisibility(View.INVISIBLE);
                    editTextInstruct.setVisibility(View.INVISIBLE);
                }
            }
        });

        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    BluetoothControl.getInstance().cancelConnection();
                } catch (Exception e) {
                    Toast.makeText(view.getContext(), "Bluetooth not connected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        buttonReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    BluetoothControl.getInstance().retryConnection();
                } catch (Exception e) {
                    Toast.makeText(view.getContext(), "Bluetooth not connected", Toast.LENGTH_SHORT).show();
                }

            }
        });

//        buttonSimulate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String toSend = "PC:START/SIMULATE/(R,1,1,0)";
//                for (ObstacleView obstacleView : obstacleViews) {
//                    if (obstacleView.getHasEntered()) {
//                        toSend += String.format("/(%02d,%02d,%02d,%d)", obstacleView.getObstacleId(),
//                                obstacleView.getGridX(),
//                                obstacleView.getGridY(), degreeDirection[obstacleView.getImageFace().ordinal()]);
//                    }
//                }
//                sendData(toSend, view.getContext());
//            }
//        });
        buttonSendExploration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button v=(Button) view;
                v.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green_prime)));
                v.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                v.setTextColor(Color.WHITE);
                buttonSendFastest.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                buttonSendFastest.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
                buttonSendFastest.setTextColor(Color.BLACK);
//                    Log.e(TAG, "onClick: set new send thread" );
//
//                    sendThread = new Thread(new Runnable() {
//                        public void run() {
//                            try {
//                                while (!Thread.currentThread().isInterrupted()) {
//                                    String toSend = "PC:START/EXPLORE/(R,1,1,0)";
//                                    for (ObstacleView obstacleView : obstacleViews) {
//                                        if (obstacleView.getHasEntered()) {
//                                            toSend += String.format("/(%02d,%02d,%02d,%d)", obstacleView.getObstacleId(),
//                                                    obstacleView.getGridX(),
//                                                    obstacleView.getGridY(), degreeDirection[obstacleView.getImageFace().ordinal()]);
//
//                                        }
//                                    }
//                                    sendData(toSend, view.getContext(), false);
//                                    Log.e(TAG, "onClick: send Explore");
//                                    try {
//                                        Thread.sleep(5000); // waits for 5 seconds
//                                    } catch (InterruptedException e) {
//                                        Thread.currentThread().interrupt();
//                                    }
//                                }
//                            } catch (Error error) {
//                                Log.e(TAG, error.getMessage());
//                            }
//                        }
//                    });
//                    BluetoothControl.getInstance().setSendThread(sendThread);
//                    BluetoothControl.getInstance().startSendThread();
            }
        });
        buttonSendFastest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.e(TAG, "onClick: set new send thread" );
//                String toSend = "PC:START/PATH";
//                sendData(toSend, view.getContext(),false);
//                Log.e(TAG, "onClick: send Fastest");
                Button v=(Button) view;
                v.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green_prime)));
                v.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                v.setTextColor(Color.WHITE);
                buttonSendExploration.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                buttonSendExploration.setCompoundDrawableTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
                buttonSendExploration.setTextColor(Color.BLACK);
            }
        });

//        WallToggle wallToggle = findViewById(R.id.wall_lock);
//
//        wallToggle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                WallToggle v = (WallToggle) view;
//                if (v.isChecked()) {
//                    gridMap.setMakeBlock(false);
//                    v.setChecked(false);
//                } else {
//                    gridMap.setMakeBlock(true);
//                    v.setChecked(true);
//                }
//            }
//        });


        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tmp = "w";
                sendMovement(tmp, view.getContext());
                // robotView.move(robotView.getGridX(),robotView.getGridY()+1,"up");
                gridMap.invalidate();

                if (!robotView.getIsMoving()) {
                    robotView.addMovement("FF");
                }
                gridMap.printMap();

            }
        });

        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tmp = "s";
                sendMovement(tmp, view.getContext());
                gridMap.invalidate();

                if (!robotView.getIsMoving()) {
                    robotView.addMovement("BB");
                }
                gridMap.printMap();
            }
        });

        turnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tmp = "q";
                sendMovement(tmp, view.getContext());
                gridMap.invalidate();
                if (!robotView.getIsMoving()) {
                    robotView.addMovement("FL");
                }
                gridMap.printMap();
            }
        });

        turnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tmp = "e";
                sendMovement(tmp, view.getContext());
                gridMap.invalidate();

                if (!robotView.getIsMoving()) {
                    robotView.addMovement("FR");
                }
                gridMap.printMap();
            }
        });

        turnBackLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tmp = "a";
                sendMovement(tmp, view.getContext());
                gridMap.invalidate();

                if (!robotView.getIsMoving()) {
                    robotView.addMovement("BL");
                }
                gridMap.printMap();
            }
        });

        turnBackRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tmp = "d";
                sendMovement(tmp, view.getContext());
                gridMap.invalidate();

                if (!robotView.getIsMoving()) {
                    robotView.addMovement("BR");
                }
                gridMap.printMap();
            }
        });

        //
        // strafeLeft.setOnClickListener(new View.OnClickListener() {
        // @Override
        // public void onClick(View view) {
        // String tmp = "sl";
        // sendMovement(tmp, view.getContext());
        // }
        // });
        //
        // strafeRight.setOnClickListener(new View.OnClickListener() {
        // @Override
        // public void onClick(View view) {
        // String tmp = "sr";
        // sendMovement(tmp, view.getContext());
        // }
        // });
    }

    public void checkPermissions() {
        // Get permission
        ArrayList permissionList = new ArrayList<String>();

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.BLUETOOTH);
        }

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.BLUETOOTH_ADMIN);
        }

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.BLUETOOTH_SCAN);
        }

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.BLUETOOTH_ADVERTISE);
        }

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.BLUETOOTH_CONNECT);
        }

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (permissionList.size() > 0) {
            String[] stringArray = (String[]) permissionList.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, stringArray,
                    1);
        }
    }

    private void showPopupPreset(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.popup_preset, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(200, 400);

        Button cancelButton=view.findViewById(R.id.pp_close_button);
        Button[] presetButttons= new Button[3];
        presetButttons[0]=view.findViewById(R.id.preset_1);
        presetButttons[1]=view.findViewById(R.id.preset_2);
        presetButttons[2]=view.findViewById(R.id.preset_3);
        Button saveButton=view.findViewById(R.id.pp_save);
        Button loadButton=view.findViewById(R.id.pp_load);


        for (Button b:presetButttons){
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Button v=(Button) view;
                    v.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green_prime)));
                    v.setTextColor(Color.WHITE);
                    for (Button a:presetButttons){
                        if (!a.equals(v)) {
                            a.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                            a.setTextColor(Color.BLACK);
                        }
                    }
                }
            });
        }

        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                obstacleViews[0].moveShrink(1,19,FaceDirection.SOUTH);
                obstacleViews[1].moveShrink(7,7,FaceDirection.EAST);
                obstacleViews[2].moveShrink(10,0,FaceDirection.NORTH);
                obstacleViews[3].moveShrink(19,6,FaceDirection.WEST);
                obstacleViews[4].moveShrink(18,11,FaceDirection.WEST);
                obstacleViews[5].moveShrink(11,18,FaceDirection.SOUTH);
                dialog.dismiss();
                robotView.fullReset();
            }
        });


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

    }

    private void showPopup(ObstacleView obstacle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.popup_image_face, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView imageFaceText = view.findViewById(R.id.image_face_text);
        ImageView robotCarPop = view.findViewById(R.id.robotCar_popup);
        imageFaceText.setVisibility(View.VISIBLE);
        robotCarPop.setVisibility(View.INVISIBLE);
        imageFaceText.setText(String.valueOf(obstacle.getObstacleId()));

        ImageButton[] imageButtons = new ImageButton[4];
        imageButtons[0] = view.findViewById(R.id.up_button);
        imageButtons[1] = view.findViewById(R.id.left_button);
        imageButtons[2] = view.findViewById(R.id.down_button);
        imageButtons[3] = view.findViewById(R.id.right_button);

        for (ImageButton o : imageButtons) {
            o.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String tmp = "";
                    switch (view.getId()) {
                        case R.id.up_button:
                            obstacle.setImageFace(FaceDirection.NORTH);
                            tmp = String.format("PC:{ID:%d,X:%d,Y:%d,Face:%s}", obstacle.getObstacleId(),
                                    obstacle.getGridX(), obstacle.getGridY(), obstacle.getImageFace().name());
                            // BluetoothControl.getInstance().write(tmp.getBytes(StandardCharsets.UTF_8));
                            dialog.dismiss();
                            break;
                        case R.id.left_button:
                            obstacle.setImageFace(FaceDirection.WEST);
                            tmp = String.format("PC:{ID:%d,X:%d,Y:%d,Face:%s}", obstacle.getObstacleId(),
                                    obstacle.getGridX(), obstacle.getGridY(), obstacle.getImageFace().name());
                            // BluetoothControl.getInstance().write(tmp.getBytes(StandardCharsets.UTF_8));
                            dialog.dismiss();
                            break;
                        case R.id.right_button:
                            obstacle.setImageFace(FaceDirection.EAST);
                            tmp = String.format("PC:{ID:%d,X:%d,Y:%d,Face:%s}", obstacle.getObstacleId(),
                                    obstacle.getGridX(), obstacle.getGridY(), obstacle.getImageFace().name());
                            // BluetoothControl.getInstance().write(tmp.getBytes(StandardCharsets.UTF_8));
                            dialog.dismiss();
                            break;
                        case R.id.down_button:
                            obstacle.setImageFace(FaceDirection.SOUTH);
                            tmp = String.format("PC:{ID:%d,X:%d,Y:%d,Face:%s}", obstacle.getObstacleId(),
                                    obstacle.getGridX(), obstacle.getGridY(), obstacle.getImageFace().name());
                            // BluetoothControl.getInstance().write(tmp.getBytes(StandardCharsets.UTF_8));
                            dialog.dismiss();
                            break;

                    }
                }
            });
        }
    }

    private void showPopup(RobotView robotCar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.popup_image_face, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView imageFaceText = view.findViewById(R.id.image_face_text);
        ImageView robotCarPop = view.findViewById(R.id.robotCar_popup);
        imageFaceText.setVisibility(View.INVISIBLE);
        robotCarPop.setVisibility(View.VISIBLE);

        ImageButton[] imageButtons = new ImageButton[4];
        imageButtons[0] = view.findViewById(R.id.up_button);
        imageButtons[1] = view.findViewById(R.id.left_button);
        imageButtons[2] = view.findViewById(R.id.down_button);
        imageButtons[3] = view.findViewById(R.id.right_button);

        for (ImageButton o : imageButtons) {
            o.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String tmp = "";
                    switch (view.getId()) {
                        case R.id.up_button:
                            Toast.makeText(MainActivity.this, "up", Toast.LENGTH_SHORT).show();
                            robotCar.setRotation("0");
                            dialog.dismiss();
                            break;
                        case R.id.left_button:
                            Toast.makeText(MainActivity.this, "left", Toast.LENGTH_SHORT).show();
                            robotCar.setRotation("90");
                            dialog.dismiss();
                            break;
                        case R.id.right_button:
                            Toast.makeText(MainActivity.this, "right", Toast.LENGTH_SHORT).show();
                            robotCar.setRotation("-90");
                            dialog.dismiss();
                            break;
                        case R.id.down_button:
                            Toast.makeText(MainActivity.this, "down", Toast.LENGTH_SHORT).show();
                            robotCar.setRotation("180");
                            dialog.dismiss();
                            break;

                    }
                }
            });
        }
    }

    private void inializeRecycleViews() {
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = time.format(formatter);
        responseControl = ResponseControl.getInstance(); //
        if (responseControl.getResponseList().size()<1)
        responseControl.add("Awaiting Bluetooth Connection",formattedTime);
        adapterResponse = new AdapterResponse(responseControl.getResponseList(),responseControl.getTimeList());//

        recyclerViewResponse = (RecyclerView) findViewById(R.id.response_feed);
        recyclerViewResponse.setLayoutManager(new LinearLayoutManager(this));
        SpacingItemDecoration itemDecorationResponse = new SpacingItemDecoration(-20);
        recyclerViewResponse.addItemDecoration(itemDecorationResponse);
        recyclerViewResponse.setItemAnimator(new DefaultItemAnimator());
        recyclerViewResponse.setAdapter(adapterResponse);
        swipeRefreshLayoutResponse = findViewById(R.id.swipeRefreshLayout_response);

        swipeRefreshLayoutResponse.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapterResponse.setData(responseControl.getResponseList(),responseControl.getTimeList());
                adapterResponse.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "refresh", Toast.LENGTH_SHORT).show();
                swipeRefreshLayoutResponse.bringToFront();
                swipeRefreshLayoutResponse.setRefreshing(false);
            }
        });

        robotStatusControl = RobotStatusControl.getInstance(); //
        if (robotStatusControl.getRobotStatusList().size()<1)
            robotStatusControl.add("Awaiting Bluetooth Connection",formattedTime);
        adapterRobotStatus = new AdapterRobotStatus(robotStatusControl.getRobotStatusList(),robotStatusControl.getRobotTimeList());//

        recyclerViewRobotStatus = (RecyclerView) findViewById(R.id.robot_feed);
        recyclerViewRobotStatus.setLayoutManager(new LinearLayoutManager(this));
        SpacingItemDecoration itemDecorationRobotStatus = new SpacingItemDecoration(-20);
        recyclerViewRobotStatus.addItemDecoration(itemDecorationRobotStatus);
        recyclerViewRobotStatus.setItemAnimator(new DefaultItemAnimator());
        recyclerViewRobotStatus.setAdapter(adapterRobotStatus);
        swipeRefreshLayoutRobotStatus = findViewById(R.id.swipeRefreshLayout_robot);

        swipeRefreshLayoutRobotStatus.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapterRobotStatus.setData(robotStatusControl.getRobotStatusList(),robotStatusControl.getRobotTimeList());
                adapterRobotStatus.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "refresh", Toast.LENGTH_SHORT).show();
                swipeRefreshLayoutRobotStatus.bringToFront();
                swipeRefreshLayoutRobotStatus.setRefreshing(false);
            }
        });
    }

    public void robotStatusAddAndRefresh(String string) {
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = time.format(formatter);
        robotStatusControl.add(string,formattedTime);
        adapterRobotStatus.setData(robotStatusControl.getRobotStatusList(),robotStatusControl.getRobotTimeList());
        adapterRobotStatus.notifyDataSetChanged();
        recyclerViewRobotStatus.scrollToPosition(adapterRobotStatus.getItemCount() - 1);
    }

    public void responseAddAndRefresh(String string) {
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = time.format(formatter);
        responseControl.add(string,formattedTime);
        adapterResponse.setData(responseControl.getResponseList(),responseControl.getTimeList());
        adapterResponse.notifyDataSetChanged();
        recyclerViewResponse.scrollToPosition(adapterResponse.getItemCount() - 1);
    }
    /*
     * public final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver()
     * {
     * 
     * @SuppressLint("MissingPermission")
     * 
     * @Override
     * public void onReceive(Context context, Intent intent) {
     * final String action = intent.getAction();
     * 
     * if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
     * BluetoothDevice mDevice =
     * intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
     * //3 cases:
     * //case1: bonded already
     * if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
     * Toast.makeText(context, "BroadcastReceiver: BOND_BONDED",
     * Toast.LENGTH_LONG).show();
     * //Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
     * }
     * //case2: creating a bone
     * if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
     * Toast.makeText(context, "BroadcastReceiver: BOND_BONDING",
     * Toast.LENGTH_LONG).show();
     * //Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
     * }
     * //case3: breaking a bond
     * if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
     * Toast.makeText(context, "BroadcastReceiver: BOND_NONE",
     * Toast.LENGTH_LONG).show();
     * //Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
     * }
     * }
     * }
     * };
     */

    public void getTextView(int x, String t) {
        if (x == 1) {
            responseAddAndRefresh(t);
        }
        // TextView tv = (TextView) findViewById(R.id.status_Text);
        // return tv;
    }

    public ObstacleView getOV(int id) {
        for (ObstacleView obstacleView : obstacleViews) {
            if (obstacleView.getObstacleId() == id)
                return obstacleView;
        }
        return null;
    }

    public RobotView getRV() {
        return robotView;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            BluetoothControl.getInstance().cancelConnection();
        } catch (Exception e) {

        }
        Log.e(TAG, "Exiting application");
    }

    public void robotMove(String s){
        robotView.addMovement(s);
    }

    public void initializeMap(){
        ConstraintLayout constrainMap = (ConstraintLayout) findViewById(R.id.constrain_map);
        ImageView car = (ImageView) findViewById(R.id.robotCar);
        robotView.setCar(car);
        constrainMap.post(new Runnable() {
            @Override
            public void run() {
                if (configOrient== Configuration.ORIENTATION_PORTRAIT) {
                    gridMap.setSides(constrainMap.getWidth() * 0.8);
                    gridMap.invalidate();
                    for (ObstacleView obstacle : obstacleViews) {
                        obstacle.setGridInterval(gridMap.getGridInterval());
                    }
                    robotView.setGridInterval(gridMap.getGridInterval());
                    gridMap.printMap();
                    for (ObstacleView obstacleView : obstacleViews) {
                        obstacleView.getLayoutParams().width = (int) (constrainMap.getWidth() * 0.2);
                        obstacleView.getLayoutParams().height = (int) (constrainMap.getWidth() * 0.2);
                        obstacleView.setOriginalParamHeight((int) (constrainMap.getWidth() * 0.2));
                        obstacleView.setOriginalParamWidth((int) (constrainMap.getWidth() * 0.2));
                    }

                    findViewById(R.id.obstacle_text).getLayoutParams().width = (int) (constrainMap.getWidth() * 0.2);
                    findViewById(R.id.obstacle_text).getLayoutParams().height = (int) (gridMap.getGridInterval() * 6
                            - constrainMap.getWidth() * 0.2);
                    findViewById(R.id.obstacle_box).getLayoutParams().width = (int) (constrainMap.getWidth() * 0.2);
                    findViewById(R.id.obstacle_box).getLayoutParams().height = (int) (constrainMap.getWidth() * 0.2);

                    buttonHolder.getLayoutParams().width = (int) (constrainMap.getWidth() * 0.2);
                    buttonHolder.getLayoutParams().height = (int) (gridMap.getGridInterval() * 14 / 5 * 1.5);
                    imageButtonReset.getLayoutParams().width = buttonHolder.getLayoutParams().width;
                    imageButtonReset.getLayoutParams().height = buttonHolder.getLayoutParams().height;
                    findViewById(R.id.ll_do_button).getLayoutParams().width = buttonHolder.getLayoutParams().width;
                    findViewById(R.id.ll_do_button).getLayoutParams().height = buttonHolder.getLayoutParams().height;


//                    buttonSimulate.getLayoutParams().width = (int) (constrainMap.getWidth() * 0.2);
//                    buttonSimulate.getLayoutParams().height = (int) (gridMap.getGridInterval() * 14 / 6);
                    buttonPreset.getLayoutParams().width = (int) (constrainMap.getWidth() * 0.2);
                    buttonPreset.getLayoutParams().height = (int) (gridMap.getGridInterval() * 14 / 5 *1 );

                    buttonSendFastest.getLayoutParams().width = (int) (constrainMap.getWidth() * 0.2);
                    buttonSendFastest.getLayoutParams().height = (int) (gridMap.getGridInterval() * 14 / 5 *1 );
                    buttonSendExploration.getLayoutParams().width = (int) (constrainMap.getWidth() * 0.2);
                    buttonSendExploration.getLayoutParams().height = (int) (gridMap.getGridInterval() * 14 / 5 * 1);

                    LinearLayout llTimeHolder=findViewById(R.id.ll_timer_holder);
                    llTimeHolder.getLayoutParams().width= (int) (constrainMap.getWidth() * 0.2);
                    llTimeHolder.getLayoutParams().height = (int) (gridMap.getGridInterval() * 14 / 5 * 1);
                    textTimer.getLayoutParams().width = llTimeHolder.getLayoutParams().width;
                    buttonStart.getLayoutParams().width = llTimeHolder.getLayoutParams().width;

                }
                else{
                    gridMap.setSides(constrainMap.getHeight() * 0.8);
                    gridMap.invalidate();
                    constrainMap.setMaxWidth((int)(constrainMap.getHeight()*0.8));
                    for (ObstacleView obstacle : obstacleViews) {
                        obstacle.setGridInterval(gridMap.getGridInterval());
                    }
                    robotView.setGridInterval(gridMap.getGridInterval());
                    gridMap.printMap();
                    for (ObstacleView obstacleView : obstacleViews) {
                        obstacleView.getLayoutParams().width = (int) (constrainMap.getHeight() * 0.2*0.9*0.95);
                        obstacleView.getLayoutParams().height = (int) (constrainMap.getHeight() * 0.2*0.9*0.95);
                        obstacleView.setOriginalParamHeight((int) (constrainMap.getHeight() * 0.2*0.9*0.95));
                        obstacleView.setOriginalParamWidth((int) (constrainMap.getHeight() * 0.2*0.9*0.95));
                        obstacleView.setX((float)(obstacleView.getX()+constrainMap.getHeight() * 0.2*0.9*0.025));
                        obstacleView.setY((float)(obstacleView.getY()+constrainMap.getHeight() * 0.2*0.9*0.025));
                    }

                    findViewById(R.id.obstacle_text).getLayoutParams().width = (int) (constrainMap.getHeight() * 0.2*0.9);
                    findViewById(R.id.obstacle_text).getLayoutParams().height = (int) (constrainMap.getHeight()*0.2*0.1);
                    findViewById(R.id.obstacle_box).getLayoutParams().width = (int) (constrainMap.getHeight() * 0.2*0.9);
                    findViewById(R.id.obstacle_box).getLayoutParams().height = (int) (constrainMap.getHeight() * 0.2*0.9);

                    buttonHolder.getLayoutParams().width = (int) (gridMap.getGridInterval() * 15 / 3);
                    buttonHolder.getLayoutParams().height = (int) (constrainMap.getHeight()*0.2);
                    imageButtonReset.getLayoutParams().width = buttonHolder.getLayoutParams().width;
                    imageButtonReset.getLayoutParams().height = buttonHolder.getLayoutParams().width;
                    findViewById(R.id.ll_do_button).getLayoutParams().width = buttonHolder.getLayoutParams().width;
                    findViewById(R.id.ll_do_button).getLayoutParams().height = buttonHolder.getLayoutParams().height;

//                    buttonSimulate.getLayoutParams().width = (int) (constrainMap.getWidth() * 0.2);
//                    buttonSimulate.getLayoutParams().height = (int) (gridMap.getGridInterval() * 14 / 6);

                    LinearLayout llRunHolder=findViewById(R.id.ll_run_holder);
                    llRunHolder.getLayoutParams().width = (int) (gridMap.getGridInterval() * 15 / 3 * 1 );
                    llRunHolder.getLayoutParams().height = (int) (constrainMap.getHeight()*0.2);
                    buttonPreset.getLayoutParams().width = llRunHolder.getLayoutParams().width;
                    buttonPreset.getLayoutParams().height = (int) (llRunHolder.getLayoutParams().height/3);
                    buttonSendFastest.getLayoutParams().width = llRunHolder.getLayoutParams().width;
                    buttonSendFastest.getLayoutParams().height = (int) (llRunHolder.getLayoutParams().height/3);
                    buttonSendExploration.getLayoutParams().width = llRunHolder.getLayoutParams().width;
                    buttonSendExploration.getLayoutParams().height = (int) (llRunHolder.getLayoutParams().height/3);

                    LinearLayout llTimeHolder=findViewById(R.id.ll_timer_holder);
                    llTimeHolder.getLayoutParams().height= (int) (constrainMap.getHeight()*0.2);
                    llTimeHolder.getLayoutParams().width = (int) (gridMap.getGridInterval() * 15 / 3 * 1 );
//                    findViewById(R.id.text_timer).getLayoutParams().height=(int) (findViewById(R.id.ll_timer_holder).getHeight()/2);
                    textTimer.getLayoutParams().width = llTimeHolder.getLayoutParams().width;
                    buttonStart.getLayoutParams().width = llTimeHolder.getLayoutParams().width;
                }
            }
        });
    }

    public void initializeGrid(){
        obstacleBoxView = findViewById(R.id.obstacle_box);
        gridControl = GridControl.getInstance();

        // instantiate obstacle view
        int[] obstacleIDs = new int[] {
                R.id.obstacle1,
                R.id.obstacle2,
                R.id.obstacle3,
                R.id.obstacle4,
                R.id.obstacle5,
                R.id.obstacle6,
                R.id.obstacle7,
                R.id.obstacle8,
        };
        obstacleViews = new ObstacleView[obstacleIDs.length];
        for (int i = 0; i < obstacleIDs.length; i++) {
            obstacleViews[i] = findViewById(obstacleIDs[i]);
        }
        robotView = findViewById(R.id.robotBound);

        // set on drag listener for grid map
        gridMap = (GridView) findViewById(R.id.grid_map);



        gridMap.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                int x = (int) dragEvent.getX() / gridMap.getGridInterval();
                int y = (int) dragEvent.getY() / gridMap.getGridInterval();
                if (dragEvent.getLocalState() instanceof ObstacleView) {
                    switch (dragEvent.getAction()) {
                        case DragEvent.ACTION_DRAG_STARTED:
                            ObstacleView draggedObstacle = (ObstacleView) dragEvent.getLocalState();
                            draggedObstacle.setVisibility(View.INVISIBLE);
                            view.setBackgroundColor(getResources().getColor(R.color.green_out));
                            if (draggedObstacle.getHasEntered()) {
                                draggedObstacle.imageClear();
                            }
                            view.invalidate();
                            break;
                        case DragEvent.ACTION_DRAG_LOCATION:
                            gridMap.setPrint_back(x, y);
                            break;
                        case DragEvent.ACTION_DRAG_ENTERED:
                            view.setBackgroundColor(Color.GRAY);
                            view.invalidate();
                            break;
                        case DragEvent.ACTION_DRAG_EXITED:
                            view.setBackgroundColor(getResources().getColor(R.color.green_out));
                            gridMap.cancelPrint_back();
                            view.invalidate();
                            break;
                        case DragEvent.ACTION_DROP:
                            gridMap.cancelPrint_back();
                            ObstacleView droppedObstacle = (ObstacleView) dragEvent.getLocalState();
                            droppedObstacle.shrink();
                            Log.d("GRIDTAG", String.format("Obstacle %d was dropped on the map.",
                                    droppedObstacle.getObstacleId()));
                            int[] movePotential = droppedObstacle.getMovePotential(dragEvent.getX(), dragEvent.getY());
                            if (gridMap.isValidBlockPlacement((int) dragEvent.getX() / gridMap.getGridInterval(),
                                    (int) dragEvent.getY() / gridMap.getGridInterval())) {
                                droppedObstacle.move(dragEvent.getX(), dragEvent.getY());

                                Log.d("GRIDTAG",
                                        String.format("Obstacle %d was moved to (%d, %d) on the map.",
                                                droppedObstacle.getObstacleId(),
                                                droppedObstacle.getGridX(),
                                                droppedObstacle.getGridY()));
                                droppedObstacle.imagePlace();
                                droppedObstacle.setHasEntered(true);
                                showPopup(droppedObstacle);

                            } else {
                                Log.d("GRIDTAG", String.format("Obstacle %d cannot move to (%d, %d) as it is occupied.",
                                        droppedObstacle.getObstacleId(),
                                        movePotential[0],
                                        movePotential[1]));
                                droppedObstacle.reset();
                                if (!droppedObstacle.getHasEntered()) {
                                    droppedObstacle.enlarge();
                                } else {
                                    droppedObstacle.imagePlace();
                                }
                                Log.d("GRIDTAG",
                                        String.format("Obstacle %d was reset.",
                                                droppedObstacle.getObstacleId()));
                            }
                            view.setBackgroundColor(Color.GRAY);
                            view.invalidate();
                            Log.d("DROPPED_OBS", String.format("onDrag: %d %d", droppedObstacle.getLastSnapX(),
                                    droppedObstacle.getLastSnapY()));
                            break;
                        case DragEvent.ACTION_DRAG_ENDED:
                            if (!dragEvent.getResult()) {
                                droppedObstacle = (ObstacleView) dragEvent.getLocalState();
                                Log.d("GRIDTAG",
                                        String.format("Obstacle %d was dropped outside of the map.",
                                                droppedObstacle.getObstacleId()));
                                droppedObstacle.fullReset(orientStr);
                                droppedObstacle.setHasEntered(false);
                                gridMap.printMap();
                                Log.d("GRIDTAG",
                                        String.format("Obstacle %d was reset.",
                                                droppedObstacle.getObstacleId()));
                            }
                            view.setBackgroundColor(Color.GRAY);
                            view.invalidate();
                            break;
                    }
                }
                else if (dragEvent.getLocalState() instanceof RobotView){
                    RobotView draggedRobot = (RobotView) dragEvent.getLocalState();
                    switch (dragEvent.getAction()){
                        case DragEvent.ACTION_DRAG_STARTED: //long press
                            draggedRobot.startDrag();
                            break;
                        case DragEvent.ACTION_DRAG_LOCATION: //Updating location
                            gridMap.setPrint_back_robot(x, y);
                            break;
                        case DragEvent.ACTION_DRAG_ENTERED: //entered map
                            break;
                        case DragEvent.ACTION_DROP: //dropped in map
                            draggedRobot.stopDrag();
                            if (x==1) x=2;
                            if (x==20) x=19;
                            if (y==0) y=1;
                            if (y==19) y=18;
                            if (x>1 && x<20 && y>0 && y<19) {
                                draggedRobot.teleport(x-1, 19-y, "0");
                            }
                            gridMap.cancelPrint_back_robot();
                            showPopup(draggedRobot);
                            break;
                        case DragEvent.ACTION_DRAG_ENDED: //check if dropped out of map
                            if (!dragEvent.getResult()) {
                                draggedRobot.stopDrag();
                                gridMap.cancelPrint_back_robot();
                            }
                            break;

                    }
                }
                return true;
            }
        });
    }

    public void connectShow(String s){
        if (s.equals("RC")){
            buttonReconnect.setVisibility(View.VISIBLE);
            buttonDisconnect.setVisibility(View.INVISIBLE);
        }
        else if (s.equals("DC")){
            buttonDisconnect.setVisibility(View.VISIBLE);
            buttonReconnect.setVisibility(View.INVISIBLE);
        }
    }


    public void reinit(){
        initializeGrid();
        initializeButtons();
        initializeMap();
        inializeRecycleViews();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        configOrient=newConfig.orientation;
        if (configOrient==Configuration.ORIENTATION_PORTRAIT){
            orientStr="p";
            setContentView(R.layout.activity_main);
            buttonBluetooth = findViewById(R.id.bt_fab);
            buttonBluetooth.setVisibility(View.VISIBLE);
            buttonBluetooth2 = findViewById(R.id.bt_fab_2);
            buttonBluetooth.setOnClickListener(this);
            reinit();
        }
        else{
            orientStr="h";
            setContentView(R.layout.activity_main_land);
            buttonBluetooth = findViewById(R.id.bt_fab);
            buttonBluetooth.setVisibility(View.VISIBLE);
            buttonBluetooth2 = findViewById(R.id.bt_fab_2);
            buttonBluetooth.setOnClickListener(this);
            reinit();
        }
    }

    public void btSwitch(){
        if (buttonBluetooth.getVisibility()==View.VISIBLE){
            bluetoothDialog.dismiss();
            buttonBluetooth.setVisibility(View.INVISIBLE);
            buttonBluetooth2.setVisibility(View.VISIBLE);
            buttonDisconnect.setVisibility(View.VISIBLE);
        }
        else{
            bluetoothDialog.dismiss();
//            buttonBluetooth.setVisibility(View.VISIBLE);
//            buttonBluetooth2.setVisibility(View.INVISIBLE);
        }
    }
}