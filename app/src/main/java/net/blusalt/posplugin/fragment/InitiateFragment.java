package net.blusalt.posplugin.fragment;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import net.blusalt.posplugin.R;
import net.blusalt.posplugin.databinding.FragmentInitateBinding;
import net.blusalt.posplugin.fragment.InitiateFragmentDirections;

import net.blusalt.posplugin.util.AppPreferenceHelper;
import net.blusalt.posplugin.util.PrefConstant;


public class InitiateFragment extends Fragment {

    final String TAG = InitiateFragment.class.getSimpleName();

    public InitiateFragment() {
        // Required empty public constructor
    }

    FragmentInitateBinding binding;
    NavController navController;
    final static String DEVICE_MODEL = "QCOM-BTD";
    private AppPreferenceHelper appPreferenceHelper;

    @RequiresApi(api = Build.VERSION_CODES.S)

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appPreferenceHelper = new AppPreferenceHelper(requireContext());

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_initate, container, false);

        binding.amountText.setText(appPreferenceHelper.getSharedPreferenceString(PrefConstant.AMOUNT));

        binding.initiateButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(InitiateFragmentDirections.actionInitiateFragmentToPaymentMethodFragment())
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
