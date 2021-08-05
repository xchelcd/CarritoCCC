package com.example.marcador;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private Button gLeftButton;

    private Button gRightButton;
    private Button rLeftButton;

    private Button rRightButton;
    private Button onButton;
    private Button offButton;
    private Button infoButton;
    private TextView carritoCCCTextView;
    private TextView seekBarTextView;
    private SeekBar velocidadesSeekBar;
    private SeekBar leftMotorSB;
    private SeekBar rightMotorSB;
    private TextView RighMotorVeltxt;
    private TextView LeftMotorVeltxt;
    private Button reverseButton;
    private Button goButton;
    private Switch claxonButton;
    //private TextView deviceConectedTextView;

    private boolean onOff = false;
    private int velRightMotor = 0;
    private int velLeftMotor = 0;


    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothAdapter mb;
    private Device deviceDiscovered;
    private Device deviceSelected;
    private Set<BluetoothDevice> pairedDevices;

    SeekBar brightness;
    String address = null;
    private ProgressDialog progress;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private boolean sbMotorLeft = false;
    private boolean sbMotorRight = false;
    private boolean reverseBoolean = false;
    private boolean claxonBandera = false;
    static final int REQUEST_ENABLE_BT = 912;

    private int valorVelocidad = 0;
    private int sorpresa = 0;
    int aux = 0;
    int i = 0;
    int j = 100;

    private final int on = 255;//cambiar, el modo enable del puente H
    private final int off = 162; //0xA2 apagar

    private final static int go = 210;//0xB2
    private final static int sentidosH = 210; //0xD2 acelerar
    private final static int sentidosA = 213; //0xD5 reversa
    private final static int cambiarVelocidades = 194; //cambiar velocidades


    //0xA2 = OFF = 0d162 = 0b1010 0010
    //0xB2 = ON  = 0d178 = 0b1011 0010

    UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            //SE CONECTO

        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED) {
            //error y no esta conectado

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        inicializarComponentes();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            msg("No se puede conectar a bluetooth desde este celular...");

        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
        }
        ajustesOnclicks();

        //velocidadesSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        //    @Override
        //    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        //        valorVelocidad = i * 20;
        //        seekBarTextView.setText("Velocidad\n      " + valorVelocidad);
        //    }
//
        //    @Override
        //    public void onStartTrackingTouch(SeekBar seekBar) {
//
        //    }
//
        //    @Override
        //    public void onStopTrackingTouch(SeekBar seekBar) {
//
        //    }
        //});
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.acttionbar, menu);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME);
        //getSupportActionBar().setIcon(R.mipmap.icono2_round);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.BuscarButton) {
            verDevicesSincronizados();
            return true;
        } else if (id == R.id.VerDevices) {
            verDevicesSincronizados();

        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void inicializarComponentes() {
        //gLeftButton = findViewById(R.id.gLeftButton);
        //goButton = findViewById(R.id.goButton);
        //gRightButton = findViewById(R.id.gRigthButton);
        //rLeftButton = findViewById(R.id.rLeftButton);
        reverseButton = findViewById(R.id.reverseButton);
        //rRightButton = findViewById(R.id.rRightButton);
        onButton = findViewById(R.id.onButton);
        offButton = findViewById(R.id.offButton);
        infoButton = findViewById(R.id.infoButton);
        carritoCCCTextView = findViewById(R.id.carritoCCCTextView);
        leftMotorSB = findViewById(R.id.leftMotorSeekBar);
        rightMotorSB = findViewById(R.id.rigthMotorSeekBar);
        //velocidadesSeekBar = findViewById(R.id.velocidadesSeekBar);
        //seekBarTextView = findViewById(R.id.seekBarTextView);

        RighMotorVeltxt = findViewById(R.id.txtRighMotorVel);
        LeftMotorVeltxt = findViewById(R.id.txtLeftMotorVel);
        claxonButton = findViewById(R.id.claxonButton);
        //deviceConectedTExtView = findViewById(R.id.deviceConectedTExtView);
    }

    public void DiscoverDevices() {
        mBluetoothAdapter.startDiscovery();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    public void verDevicesSincronizados() {
        //Toast.makeText(this, "Funciona", Toast.LENGTH_SHORT).show();
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.devices_list);

        final RecyclerView listView = dialog.findViewById(R.id.detalles_rc);


        pairedDevices = mBluetoothAdapter.getBondedDevices();
        final List<Device> devices = new ArrayList<>();
        int index = 0;
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                devices.add(new Device(device.getName(), device.getAddress()));

            }
            devicesAdapter arrayAdapter = new devicesAdapter(devices);
            arrayAdapter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //deviceSelected= new Device(v.findViewById(R.id.name_device).toString(),
                    //v.findViewById(R.id.identificador).toString());
                    // double precioDouble =
                    // (double) productosArrayList.get(rc.getChildAdapterPosition(v)).getPrecio() / 100;
                    deviceSelected = (Device) devices.get(listView.getChildAdapterPosition(v));
                    dialog.dismiss();
                    Toast.makeText(MainActivity.this, deviceSelected.getName(), Toast.LENGTH_SHORT).show();
                    ConnectBt cn = new ConnectBt();
                    cn.execute();
                }
            });
            listView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
            listView.setAdapter(arrayAdapter);


            dialog.show();
        } else {

            DiscoverDevices();
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    public void ajustesOnclicks() {

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);

                dialog.setTitle("Proyecto final" +
                        "\nIntegrante:")
                        .setMessage("-XCHEL ALONSO CARRANZA DE LA O")
                        .setIcon(R.drawable.sable_espada_web_esgrima)
                        .show();
            }
        });

        carritoCCCTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sorpresa++;
                if (sorpresa == 10) {
                    //METER CODIGO
                    //msg("Pues 100, no?");
                    sorpresa = 0;
                }
            }
        });

        onButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isBtConnected && btSocket != null) {
                    try {
                        btSocket.getOutputStream().write(on);
                        btSocket.getOutputStream().write(254);
                        rightMotorSB.setProgress(0);
                        leftMotorSB.setProgress(0);
                        onOff = true;
                        if (onOff) {
                            offButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
                            onButton.setBackgroundColor(Color.GREEN);

                        }
                        btSocket.getOutputStream().write(240);
                    } catch (IOException e) {
                        msg("Error");
                        //deviceConectedTExtView.setText("onButton");


                        mb = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                        mb.enable();
                        BluetoothDevice dispositivo = mb.getRemoteDevice(deviceSelected.getId());
                        try {
                            btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                        try {
                            btSocket.close();//start connection
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    msg("No Connected...");
                }
            }
        });
        offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBtConnected && btSocket != null) {
                    try {
                        btSocket.getOutputStream().write(off);
                        onOff = false;
                        if (!onOff) {
                            offButton.setBackgroundColor(Color.RED);
                            onButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        }
                        btSocket.getOutputStream().write(241);
                    } catch (IOException e) {
                        msg("Error");
                        //deviceConectedTExtView.setText("offButton");


                        mb = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                        mb.enable();
                        BluetoothDevice dispositivo = mb.getRemoteDevice(deviceSelected.getId());
                        try {
                            btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                        try {
                            btSocket.close();//start connection
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    msg("No Connected...");
                }
            }
        });

        rightMotorSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (isBtConnected && btSocket != null) {
                    try {
                        btSocket.getOutputStream().write(off);
                        if (onOff) {
                            velRightMotor = i;
                            RighMotorVeltxt.setText("" + i);
                            btSocket.getOutputStream().write(194);
                            btSocket.getOutputStream().write(velLeftMotor);
                            btSocket.getOutputStream().write(i);
                            btSocket.getOutputStream().write(178);
                            btSocket.getOutputStream().write(210);
                        } else {
                            msg("offed");
                        }
                    } catch (IOException e) {
                        msg("Error");
                        //deviceConectedTExtView.setText("seekBarRigth");


                        mb = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                        mb.enable();
                        BluetoothDevice dispositivo = mb.getRemoteDevice(deviceSelected.getId());
                        try {
                            btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                        try {
                            btSocket.close();//start connection
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }

                    }
                } else {
                    msg("No Connected...");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                sbMotorRight = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                sbMotorRight = true;
                if (sbMotorLeft && sbMotorRight) {
                    leftMotorSB.setProgress(0);
                    rightMotorSB.setProgress(0);
                    sbMotorRight = false;
                }

            }
        });

        leftMotorSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (isBtConnected && btSocket != null) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
                            btSocket.getOutputStream().write(off);
                            if (onOff) {
                                velLeftMotor = i;
                                LeftMotorVeltxt.setText("" + i);

                                btSocket.getOutputStream().write(194);

                                btSocket.getOutputStream().write(i);
                                btSocket.getOutputStream().write(velRightMotor);
                                btSocket.getOutputStream().write(178);
                                btSocket.getOutputStream().write(210);
                            } else {
                                msg("offed");
                            }
                        }
                    } catch (IOException e) {
                        msg("Error");
                        //deviceConectedTExtView.setText("seekBarLeft");

                        mb = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                        mb.enable();
                        BluetoothDevice dispositivo = mb.getRemoteDevice(deviceSelected.getId());
                        try {
                            btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                        try {
                            btSocket.close();//start connection
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }


                    }
                } else {
                    msg("No Connected...");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                sbMotorLeft = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                sbMotorLeft = true;
                if (sbMotorLeft && sbMotorRight) {
                    leftMotorSB.setProgress(0);
                    rightMotorSB.setProgress(0);
                    sbMotorLeft = false;
                }


            }
        });

        reverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (reverseBoolean) {
                    reverseBoolean = false;
                    reverseButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    try {
                        btSocket.getOutputStream().write(off);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    reverseBoolean = true;
                    reverseButton.setBackgroundColor(Color.GREEN);
                    try {
                        //btSocket.getOutputStream().write(245);
                        btSocket.getOutputStream().write(194);
                        btSocket.getOutputStream().write(35);
                        btSocket.getOutputStream().write(30);
                        btSocket.getOutputStream().write(178);
                        btSocket.getOutputStream().write(213);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        claxonButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (isBtConnected && btSocket != null) {
                    try {
                        btSocket.getOutputStream().write(off);
                        if (onOff) {
                            if (b) {
                                //msg("no");
                                btSocket.getOutputStream().write(255);//apagar claxon
                                claxonBandera = false;
                            } else {
                                //msg("sí");
                                btSocket.getOutputStream().write(254);//encender claxon
                                claxonBandera = true;
                            }
                        } else {
                            msg("offed");
                        }
                    } catch (IOException e) {
                        msg("Error");
                        //deviceConectedTExtView.setText("seekBarLeft");
                        mb = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                        mb.enable();
                        BluetoothDevice dispositivo = mb.getRemoteDevice(deviceSelected.getId());
                        try {
                            btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                        try {
                            btSocket.close();//start connection
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    // msg("No Connected...");
                }
            }
        });

        //claxonButton.setOnTouchListener(new View.OnTouchListener() {
        //    @Override
        //    public boolean onTouch(View view, MotionEvent motionEvent) {
        //            if (isBtConnected && btSocket != null) {
        //                try {
        //                    btSocket.getOutputStream().write(off);
        //                    if (onOff) {
//
        //                        if(claxonBandera){
        //                            //msg("no");
        //                            btSocket.getOutputStream().write(254);//apagar claxon
        //                            claxonBandera = false;
        //                        }else{
        //                            //msg("sí");
        //                            btSocket.getOutputStream().write(255);//encender claxon
        //                            claxonBandera = true;
        //                        }
        //                    } else {
        //                        msg("offed");
        //                    }
        //                } catch (IOException e) {
        //                    msg("Error");
        //                    //deviceConectedTExtView.setText("seekBarLeft");
        //                    mb = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
        //                    mb.enable();
        //                    BluetoothDevice dispositivo = mb.getRemoteDevice(deviceSelected.getId());
        //                    try {
        //                        btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(MY_UUID);
        //                    } catch (IOException ex) {
        //                        ex.printStackTrace();
        //                    }
        //                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        //                    try {
        //                        btSocket.close();//start connection
        //                    } catch (IOException ex) {
        //                        ex.printStackTrace();
        //                    }
        //                }
        //            } else {
        //                // msg("No Connected...");
        //            }
        //        return true;
        //    }
        //});

        //claxonButton.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //
        //        if (isBtConnected && btSocket != null) {
        //            try {
        //                btSocket.getOutputStream().write(off);
        //                if (onOff) {
//
//
//
//
        //                    if(!claxonButton.isPressed()){
        //                            btSocket.getOutputStream().write(255);//apagar claxon
        //                        msg("no");
        //                    }else{
        //                        btSocket.getOutputStream().write(255);//encender claxon
        //                        msg("sí");
        //                    }
//
//
//
//
//
        //                } else {
        //                    msg("offed");
        //                }
        //            } catch (IOException e) {
        //                msg("Error");
        //                //deviceConectedTExtView.setText("seekBarLeft");
//
        //                mb = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
        //                mb.enable();
        //                BluetoothDevice dispositivo = mb.getRemoteDevice(deviceSelected.getId());
        //                try {
        //                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(MY_UUID);
        //                } catch (IOException ex) {
        //                    ex.printStackTrace();
        //                }
        //                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        //                try {
        //                    btSocket.close();//start connection
        //                } catch (IOException ex) {
        //                    ex.printStackTrace();
        //                }
//
//
        //            }
        //        } else {
        //            // msg("No Connected...");
        //        }
        //    }
        //});

        //goButton.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        if (isBtConnected && btSocket != null) {
        //            try {
        //                btSocket.getOutputStream().write(off);
        //                //codigo
//
        //            } catch (IOException e) {
        //                msg("Error");
        //            }
        //        } else {
        //            msg("No Connected...");
        //        }
        //    }
        //});

        //reverseButton.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        if (isBtConnected && btSocket != null) {
        //            try {
        //                btSocket.getOutputStream().write(off);
        //                //codigo
//
        //            } catch (IOException e) {
        //                msg("Error");
        //            }
        //        } else {
        //            msg("No Connected...");
        //        }
        //    }
        //});

        //gLeftButton.setOnTouchListener(new View.OnTouchListener() {
        //    @Override
        //    public boolean onTouch(View v, MotionEvent event) {
        //        if (event.getAction() == MotionEvent.ACTION_DOWN) {
        //            try {
        //                btSocket.getOutputStream().write(194);
        //                btSocket.getOutputStream().write(99);
        //                btSocket.getOutputStream().write(50);
        //                btSocket.getOutputStream().write(178);
        //                btSocket.getOutputStream().write(210);
        //            } catch (IOException e) {
        //                msg("Error");
        //            }
        //        } else if (event.getAction() == MotionEvent.ACTION_UP) {
        //            try {
        //                btSocket.getOutputStream().write(162);
        //            } catch (IOException e) {
        //                msg("Error");
        //            }
        //        }
        //        return true;
        //    }
        //});
        //goButton.setOnTouchListener(new View.OnTouchListener() {
        //    @Override
        //    public boolean onTouch(View v, MotionEvent event) {
        //        if (event.getAction() == MotionEvent.ACTION_DOWN) {
        //            try {
        //                btSocket.getOutputStream().write(194);
        //                btSocket.getOutputStream().write(50);
        //                btSocket.getOutputStream().write(50);
        //                btSocket.getOutputStream().write(178);
        //                btSocket.getOutputStream().write(210);
        //            } catch (IOException e) {
        //                msg("Error");
        //            }
        //        } else if (event.getAction() == MotionEvent.ACTION_UP) {
        //            try {
        //                btSocket.getOutputStream().write(162);
        //            } catch (IOException e) {
        //                msg("Error");
        //            }
        //        }
        //        return true;
        //    }
        //});
        //gRightButton.setOnTouchListener(new View.OnTouchListener() {
        //    @Override
        //    public boolean onTouch(View v, MotionEvent event) {
        //        if (event.getAction() == MotionEvent.ACTION_DOWN) {
        //            try {
        //                btSocket.getOutputStream().write(194);
        //                btSocket.getOutputStream().write(50);
        //                btSocket.getOutputStream().write(99);
        //                btSocket.getOutputStream().write(178);
        //                btSocket.getOutputStream().write(210);
        //            } catch (IOException e) {
        //                msg("Error");
        //            }
        //        } else if (event.getAction() == MotionEvent.ACTION_UP) {
        //            try {
        //                btSocket.getOutputStream().write(162);
        //            } catch (IOException e) {
        //                msg("Error");
        //            }
        //        }
        //        return true;
        //    }
        //});
        //rLeftButton.setOnTouchListener(new View.OnTouchListener() {
        //    @Override
        //    public boolean onTouch(View v, MotionEvent event) {
        //        if (event.getAction() == MotionEvent.ACTION_DOWN) {
        //            try {
        //                btSocket.getOutputStream().write(194);
        //                btSocket.getOutputStream().write(99);
        //                btSocket.getOutputStream().write(50);
        //                btSocket.getOutputStream().write(178);
        //                btSocket.getOutputStream().write(213);
        //            } catch (IOException e) {
        //                msg("Error");
        //            }
        //        } else if (event.getAction() == MotionEvent.ACTION_UP) {
        //            try {
        //                btSocket.getOutputStream().write(162);
        //            } catch (IOException e) {
        //                msg("Error");
        //            }
        //        }
        //        return true;
        //    }
        //});
        //reverseButton.setOnTouchListener(new View.OnTouchListener() {
        //    @Override
        //    public boolean onTouch(View v, MotionEvent event) {
        //        if (event.getAction() == MotionEvent.ACTION_DOWN) {
        //            try {
        //                btSocket.getOutputStream().write(194);
        //                btSocket.getOutputStream().write(99);
        //                btSocket.getOutputStream().write(99);
        //                btSocket.getOutputStream().write(178);
        //                btSocket.getOutputStream().write(213);
        //            } catch (IOException e) {
        //                msg("Error");
        //            }
        //        } else if (event.getAction() == MotionEvent.ACTION_UP) {
        //            try {
        //                btSocket.getOutputStream().write(162);
        //            } catch (IOException e) {
        //                msg("Error");
        //            }
        //        }
        //        return true;
        //    }
        //});
        //rRightButton.setOnTouchListener(new View.OnTouchListener() {
        //    @Override
        //    public boolean onTouch(View v, MotionEvent event) {
        //            if (event.getAction() == MotionEvent.ACTION_DOWN) {
        //                try {
        //                    btSocket.getOutputStream().write(194);
        //                    btSocket.getOutputStream().write(50);
        //                    btSocket.getOutputStream().write(99);
        //                    btSocket.getOutputStream().write(178);
        //                    btSocket.getOutputStream().write(213);
        //                } catch (IOException e) {
        //                    msg("Error");
        //                }
        //            } else if (event.getAction() == MotionEvent.ACTION_UP) {
        //                try {
        //                    btSocket.getOutputStream().write(162);
        //                } catch (IOException e) {
        //                    msg("Error");
        //                }
        //            }
        //        return true;
        //    }
        //});

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                deviceDiscovered = new Device(device.getName(), device.getAddress());
            }
        }
    };

    private class ConnectBt extends AsyncTask<Void, Void, Void> {

        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait!!!");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    mb = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    mb.enable();
                    BluetoothDevice dispositivo = mb.getRemoteDevice(deviceSelected.getId());
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection

                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;
                try {
                    btSocket.getOutputStream().write(243);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            progress.dismiss();
        }
    }
}
