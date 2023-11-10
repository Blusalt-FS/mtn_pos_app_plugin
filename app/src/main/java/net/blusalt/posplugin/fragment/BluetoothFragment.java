package net.blusalt.posplugin.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import net.blusalt.posplugin.adapter.BluetoothDeviceAdapter;

import net.blusalt.posplugin.fragment.BluetoothFragmentDirections;

import net.blusalt.posplugin.model.BtDevice;
import net.blusalt.posplugin.R;
import net.blusalt.posplugin.databinding.FragmentBluetoothBinding;
import net.blusalt.posplugin.model.TerminalResponse;
import net.blusalt.posplugin.model.TransData;
import net.blusalt.posplugin.util.AppPreferenceHelper;
import net.blusalt.posplugin.util.PrefConstant;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothFragment extends Fragment {

    final String TAG = BluetoothFragment.class.getSimpleName();

    SendReceive sendReceive;

    static boolean isSending = false;
    static final int REQUEST_ENABLE_BT = 1;
    static final int REQUEST_LOC = 2;
    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;

    int REQUEST_ENABLE_BLUETOOTH = 1;

    private static final String APP_NAME = "MPOS";
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-223a-11e0-ac64-0803450c9a66");

    public BluetoothFragment() {
        // Required empty public constructor
    }

    FragmentBluetoothBinding binding;
    BluetoothDeviceAdapter adapter;
    BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice mDevice;
    View progressBar;
    Button rescanBtn;
    NavController navController;
    Boolean isConnected = false;
    final static String DEVICE_MODEL = "QCOM-BTD";
    private AppPreferenceHelper appPreferenceHelper;

    @RequiresApi(api = Build.VERSION_CODES.S)
    private static final String[] ANDROID_12_BLE_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        adapter = new BluetoothDeviceAdapter(getContext());
        navController = NavHostFragment.findNavController(this);
        appPreferenceHelper = new AppPreferenceHelper(requireContext());

        // prevent onBackPress
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
//                requireActivity().finish();
            }
        });
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }


    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bluetooth, container, false);
        ListView deviceList = binding.btList;

        deviceList.setOnItemClickListener((parent, view, position, id) -> {
            mDevice = adapter.getItem(position);
            if (TextUtils.isEmpty(mDevice.getName()) && !mDevice.getName().startsWith(DEVICE_MODEL)) {
                showDeviceSelectionError();
            } else {
                if (validateMACAddress(mDevice.getAddress())) {
                    ClientClass clientClass = new ClientClass(mDevice);
                    clientClass.start();

                    Log.e(TAG, "About Connecting");
                } else {
                    Log.e("TAG", "Invalid MAC Address");
                }
            }
        });

        binding.framelist.setVisibility(View.VISIBLE);
        binding.bluetoothImage.setVisibility(View.GONE);
        binding.connectedText.setVisibility(View.GONE);
        binding.continueButton.setVisibility(View.GONE);

        binding.sendImage.setVisibility(View.GONE);
        binding.sendText.setVisibility(View.GONE);
        binding.aboutToText.setVisibility(View.GONE);
        binding.amountText.setVisibility(View.GONE);
//        Bundle bundle = getArguments();
//        String message =  bundle.getString("amount");
//        Log.e("TAG", message);

        deviceList.setAdapter(adapter);
        progressBar = binding.btLoad;
        rescanBtn = binding.manualButton;
        rescanBtn.setOnClickListener(v -> discovery());

        binding.continueButton.setOnClickListener(v -> {
            binding.framelist.setVisibility(View.GONE);
            binding.bluetoothImage.setVisibility(View.GONE);
            binding.connectedText.setVisibility(View.GONE);
            binding.continueButton.setVisibility(View.GONE);

            binding.sendImage.setVisibility(View.VISIBLE);
            binding.sendText.setVisibility(View.VISIBLE);
            binding.aboutToText.setVisibility(View.VISIBLE);
            binding.amountText.setVisibility(View.VISIBLE);
            binding.sendButton.setVisibility(View.VISIBLE);

            binding.amountText.setText(appPreferenceHelper.getSharedPreferenceString(PrefConstant.AMOUNT));
            Log.e("TAG amount Int",  appPreferenceHelper.getSharedPreferenceString(PrefConstant.AMOUNT_INT));
            Log.e("TAG amount", appPreferenceHelper.getSharedPreferenceString(PrefConstant.AMOUNT));

            binding.toolbarText.setText("Send Transaction");
            binding.toolbarText.setTextColor(getResources()
                    .getColor(R.color.black));
            binding.toolbar.setBackgroundDrawable(new ColorDrawable(getResources()
                    .getColor(R.color.toolbar_color)));
        });

        binding.sendButton.setOnClickListener(v -> {
            if (isConnected) {
                binding.sendText.setText("Continue Transaction on POS");
                binding.sendButton.setVisibility(View.GONE);
                String amount = appPreferenceHelper.getSharedPreferenceString(PrefConstant.AMOUNT_INT);

                TransData transData = new TransData();
                transData.setAmount(amount);
                transData.setApikey(appPreferenceHelper.getSharedPreferenceString(PrefConstant.APIKEY));

                String fullTransData = (new Gson()).toJson(transData);

                sendReceive.write(fullTransData.getBytes());
            } else {
                Toast.makeText(getActivity(), "Please Re-connect Bluetooth", Toast.LENGTH_SHORT).show();
            }
        });

        binding.toolbar.setOnClickListener((it) -> {
            mDevice = null;
            Navigation.findNavController(it).navigateUp();
        });

        if (getActivity() != null) {
            getActivity().registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            getActivity().registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
            getActivity().registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        }

        return binding.getRoot();
    }

    private void showDeviceSelectionSuccess(BtDevice btDevice, BluetoothDevice bluetoothDevice) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Bluetooth Confirmation");
        String message = "Device Name: " + btDevice.getName() +
                "\n" +
                "MAC address: " + btDevice.getAddress();
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Proceed", (dialog, id) -> {


                }).show();
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case STATE_LISTENING:
                    Log.e(TAG, "Listening");
                    break;
                case STATE_CONNECTING:
                    isConnected = false;
                    Toast.makeText(requireContext(), "Connecting", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Connecting");
                    break;
                case STATE_CONNECTED:
                    Toast.makeText(requireContext(), "Connected", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Connected");
                    isConnected = true;
                    binding.framelist.setVisibility(View.GONE);
                    binding.manualButton.setVisibility(View.GONE);
                    binding.bluetoothImage.setVisibility(View.VISIBLE);
                    binding.connectedText.setVisibility(View.VISIBLE);
                    binding.continueButton.setVisibility(View.VISIBLE);
                    binding.toolbarText.setTextColor(getResources()
                            .getColor(R.color.black));
                    binding.toolbar.setBackgroundDrawable(new ColorDrawable(getResources()
                            .getColor(R.color.toolbar_color)));

//                    BtDevice btDevice = new BtDevice(mDevice.getName(), mDevice.getAddress());
//                    showDeviceSelectionSuccess(btDevice, mDevice);
                    break;
                case STATE_CONNECTION_FAILED:
                    isConnected = false;
                    Toast.makeText(requireContext(), "Connection failed, please reconnect", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    Log.e(TAG, "MESSAGE_RECEIVED");

                    try {
                        byte[] readBuff = (byte[]) msg.obj;
                        String result = new String(readBuff, 0, msg.arg1);
                        Log.e(TAG, "tempMsg " + result);

                        TerminalResponse response = new Gson().fromJson(result, TerminalResponse.class);
                        String string = String.valueOf(response.responseDescription + " " + "Response Code: " + response.responseCode);
                        sendReceive.write(string.getBytes());

                        if(response.responseCode.equals("03") && response.responseDescription.equals("Card Malfunction")) {
                            Navigation.findNavController(getView()).navigate(BluetoothFragmentDirections.actionBluetoothFragmentToConnectionFragment());

                        }else {
                            Navigation.findNavController(getView()).navigate(BluetoothFragmentDirections.actionBluetoothFragmentToStatusFragment(result));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    msg_box.setText(tempMsg);
//                    startAccountSelectionActivity(Double.valueOf(String.valueOf(tempMsg)));
                    break;
            }
            return true;
        }
    });

    private class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;

        @SuppressLint("MissingPermission")
        public ServerClass() {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            BluetoothSocket socket = null;

            while (socket == null) {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);

                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if (socket != null) {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    sendReceive = new SendReceive(socket);
                    sendReceive.start();

                    break;
                }
            }
        }
    }

    private class ClientClass extends Thread {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        @SuppressLint("MissingPermission")
        public ClientClass(BluetoothDevice device1) {
            device = device1;

            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @SuppressLint("MissingPermission")
        public void run() {
            try {
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);

                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    private class SendReceive extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempIn;
            outputStream = tempOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

//            while (true) {
                try {
                    Log.e(TAG, "inputStream");
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "catch");
                    try {
                        inputStream.close();
                        bluetoothSocket.close();
//                        inputStream.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
//                    Toast.makeText(getActivity(), "Bluetooth Disconnected, Please Re-connect", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
//            }
        }

        public void write(byte[] bytes) {
            Log.e(TAG, "outputStream");
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    boolean validateMACAddress(String macAddress) {
        boolean isValid = false;
        if (!TextUtils.isEmpty(macAddress)) {
            isValid = BluetoothAdapter.checkBluetoothAddress(macAddress);
        }
        return isValid;
    }

    void makeCurrentDeviceDefault() {
//        appPreferenceHelper.setSharedPreferenceBoolean(PrefConstant.IS_DEFAULT_DEVICE_SELECTED, true);
    }


    private void showDeviceSelectionError() {
        final AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Error")
                .setMessage("Wrong Device Selected")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());

                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    progressBar.setVisibility(View.GONE);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (rescanBtn != null) {
                    rescanBtn.setText(getText(R.string.search_again));
                    rescanBtn.setEnabled(true);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                if (rescanBtn != null) {
                    rescanBtn.setText(R.string.searching);
                    rescanBtn.setEnabled(false);
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            } else if (!isBluetoothScanEnabled(getContext())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestPermissions(ANDROID_12_BLE_PERMISSIONS, REQUEST_LOC);
                }
            } else {
                discovery();
            }
        } else {
            Toast.makeText(getContext(), "Bluetooth not in device", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isBluetoothScanEnabled(Context context) {
        int bluetoothScan = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothScan = context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN);
        }
        return bluetoothScan == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isBluetoothConnectEnabled(Context context) {
        int bluetoothConnect = -1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothConnect = context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT);
        }
        return bluetoothConnect == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isLocationEnabled(Context context) {
        int accessCoarseLocation = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            accessCoarseLocation = context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        int accessFineLocation = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            accessFineLocation = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        return accessCoarseLocation == PackageManager.PERMISSION_GRANTED && accessFineLocation == PackageManager.PERMISSION_GRANTED;
    }


    private void accessLocationPermission(Context context) {
        int accessCoarseLocation = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            accessCoarseLocation = context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        int accessFineLocation = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            accessFineLocation = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }

        List<String> listRequestPermission = new ArrayList<>();

        if (accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (accessFineLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!listRequestPermission.isEmpty()) {
            String[] strRequestPermission = listRequestPermission.toArray(new String[listRequestPermission.size()]);
            requestPermissions(strRequestPermission, REQUEST_LOC);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                discovery();
            } else {
                Toast.makeText(getContext(), "Bluetooth not enabled", Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    void discovery() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (adapter != null && bluetoothAdapter != null) {
            adapter.clear();
            Set<BluetoothDevice> bonded = bluetoothAdapter.getBondedDevices();
            Set<BluetoothDevice> mposDevices = new HashSet<>();
            for (BluetoothDevice device : bonded) {
                if (device != null && !TextUtils.isEmpty(device.getName())  && device.getName().startsWith("QCOM-BTD")) {
                    mposDevices.add(device);
                }
            }
            if (!mposDevices.isEmpty()) {
                adapter.addAll(mposDevices);
                progressBar.setVisibility(View.GONE);
            } else {
                // There are no paired devices
                showNoPairedDevicesAlertDialog();
            }
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            bluetoothAdapter.startDiscovery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOC) {
            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                discovery();
            }
        }
    }


    private void showNoPairedDevicesAlertDialog() {
        Log.e("TAG","showNoPairedDevicesAlertDialog IN");
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Message");
        builder.setMessage("No Horizon POS device paired.\n\nPlease go to Settings-> Bluetooth to pair with a Horizon POS");
        builder.setCancelable(true);
        builder.setOnCancelListener(DialogInterface::dismiss);
        builder.setNeutralButton("OK", (dialogInterface, i) -> {
            Log.e("TAG","showNoPairedDevicesAlertDialog:run:onClick IN");
            dialogInterface.dismiss();
            Log.e("No Horizon POS paired.", "\n\nPlease go to Settings-> Bluetooth to pair with a Horizon POS");
        });
        builder.create().show();
        rescanBtn.setText(getText(R.string.search_again));
        rescanBtn.setEnabled(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("TAG","On Pause is Called");
        if (bluetoothAdapter != null) {
            progressBar.setVisibility(View.GONE);
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            bluetoothAdapter.cancelDiscovery();
        }
    }

    @Override
    public void onDestroy() {
        Log.e("TAG","On Destroy is Called");
        super.onDestroy();
        if (bluetoothAdapter != null) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            bluetoothAdapter.cancelDiscovery();
        }
        if (getActivity() != null) {
            getActivity().unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e("TAG","On DestroyView is Called");
        binding = null;
    }
}
