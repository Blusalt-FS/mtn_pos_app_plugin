package com.blusalt.posplugin.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.blusalt.posplugin.R;
import com.blusalt.posplugin.databinding.FragmentBluetoothBinding;
import com.blusalt.posplugin.databinding.FragmentPaymentMethodBinding;

import timber.log.Timber;

public class PaymentMethodFragment extends Fragment {

    final String TAG = PaymentMethodFragment.class.getSimpleName();


    public PaymentMethodFragment() {
        // Required empty public constructor
    }

    FragmentPaymentMethodBinding binding;
    NavController navController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // prevent onBackPress
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        });
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_payment_method, container, false);

        binding.toolbar.setOnClickListener((it) -> {
//            if (!navController.navigateUp())
            Navigation.findNavController(it).navigateUp();
//                requireActivity().finishAfterTransition();
        });

        binding.connectBleText.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(PaymentMethodFragmentDirections.actionPaymentMethodFragmentToConnectionFragment())
        );
        binding.connectBleButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(PaymentMethodFragmentDirections.actionPaymentMethodFragmentToConnectionFragment())
        );

        return binding.getRoot();
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
        Timber.tag(TAG).d("On Pause is Called");
    }

    @Override
    public void onDestroy() {
        Timber.tag(TAG).d("On Destroy is Called");
        super.onDestroy();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.tag(TAG).d("On DestroyView is Called");
        binding = null;
    }
}
