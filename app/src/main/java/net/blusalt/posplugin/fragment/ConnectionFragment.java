package net.blusalt.posplugin.fragment;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import net.blusalt.posplugin.adapter.BluetoothDeviceAdapter;
import net.blusalt.posplugin.R;
import net.blusalt.posplugin.databinding.FragmentConnectionBinding;
import net.blusalt.posplugin.fragment.ConnectionFragmentDirections;


public class ConnectionFragment extends Fragment {

    final String TAG = ConnectionFragment.class.getSimpleName();

    public ConnectionFragment() {
        // Required empty public constructor
    }

    FragmentConnectionBinding binding;
    BluetoothDeviceAdapter adapter;
    BluetoothAdapter bluetoothAdapter;
    NavController navController;
    final static String DEVICE_MODEL = "QCOM-BTD";

    @RequiresApi(api = Build.VERSION_CODES.S)

    private static final String[] ANDROID_12_BLE_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION
    };

    final static int REQUEST_ENABLE_BLUETOOTH = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        adapter = new BluetoothDeviceAdapter(getContext());
        navController = NavHostFragment.findNavController(this);

        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            Toast.makeText(getActivity(), "Bluetooth Enabled", Toast.LENGTH_SHORT);
        } else {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Log.e("TAG","Requesting permission");
                    requestBlePermissions(requireActivity());
                    return;
                }
            }
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        }
        // prevent onBackPress
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getActivity(), "Bluetooth Enabled", Toast.LENGTH_SHORT);
            } else {
                Log.e("TAG", "Bluetooth not in device");
            }
        }
    }

    private static void requestBlePermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            String[] ANDROID_12_BLE_PERMISSIONS =  new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
            ActivityCompat.requestPermissions(
                    activity,
                    ANDROID_12_BLE_PERMISSIONS,
                    REQUEST_ENABLE_BLUETOOTH
            );
        }
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_connection, container, false);

        binding.searchButton.setOnClickListener(v ->

        Navigation.findNavController(v).navigate(ConnectionFragmentDirections.actionConnectionFragmentToBluetoothFragment())
//                navController.navigate(ConnectionFragmentDirections.actionConnectionFragmentToBluetoothFragment())
        );

        binding.toolbar.setOnClickListener((it) -> {
            Navigation.findNavController(it).navigateUp();
        });

        return binding.getRoot();
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.e("TAG","On Pause is Called");
    }

    @Override
    public void onDestroy() {
        Log.e("TAG","On Destroy is Called");
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e("TAG","On DestroyView is Called");
        binding = null;
    }
}
