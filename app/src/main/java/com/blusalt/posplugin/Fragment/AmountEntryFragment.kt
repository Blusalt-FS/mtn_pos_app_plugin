package com.blusalt.posplugin.Fragment

import androidx.navigation.NavController
import androidx.annotation.RequiresApi
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.blusalt.posplugin.R
import com.blusalt.posplugin.databinding.FragmentAmountEntryBinding
import com.blusalt.posplugin.util.AppPreferenceHelper
import com.blusalt.posplugin.util.PrefConstant
import com.davidmiguel.numberkeyboard.NumberKeyboardListener
import timber.log.Timber

class AmountEntryFragment : Fragment() {
    val TAG = AmountEntryFragment::class.java.simpleName
    var binding: FragmentAmountEntryBinding? = null
    var navController: NavController? = null
    var appPreferenceHelper: AppPreferenceHelper? = null

    @RequiresApi(api = Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appPreferenceHelper = AppPreferenceHelper(requireContext())

        // prevent onBackPress
        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        })
    }

    override fun onDetach() {
        super.onDetach()
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_amount_entry, container, false)

        var builder: java.lang.StringBuilder = StringBuilder().append("")

        binding!!.amountText.apply {
            setCurrency("₦")
            setDecimals(true)
            setSpacing(true)
            setDelimiter(false)
            setSeparator(".")
        }

        binding!!.amountKeypad.setListener(
            object : NumberKeyboardListener {
                override fun onNumberClicked(number: Int) {
                    builder.append(number)
                    binding!!.amountText.setText(builder.toString())
                }

                override fun onLeftAuxButtonClicked() {
                }

                override fun onRightAuxButtonClicked() {
                    val co = builder.toString().length
                    if (co > 0) {
                        builder.deleteCharAt(co - 1)
                        binding!!.amountText.setText(builder.toString())
                    }
                }
            }
        )

        binding!!.continueButton.setOnClickListener { v ->

            Log.e("TAG amount", binding!!.amountText.text.toString())
            Log.e("TAG amount", binding!!.amountText.cleanDoubleValue.toString())
            Log.e("TAG amount", binding!!.amountText.cleanIntValue.toString())

            appPreferenceHelper!!.setSharedPreferenceString(
                PrefConstant.AMOUNT,
                binding!!.amountText.text.toString()
            )
            appPreferenceHelper!!.setSharedPreferenceString(
                PrefConstant.AMOUNT_INT,
                binding!!.amountText.cleanIntValue.toString()
            )

            Navigation.findNavController(v)
                .navigate(AmountEntryFragmentDirections.actionAmountEntryFragmentToInitiateFragment())
        }

        binding!!.toolbar.setOnClickListener { it: View? -> if (!navController!!.navigateUp()) requireActivity().finishAfterTransition() }
        return binding!!.getRoot()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        Timber.tag(TAG).d("On Pause is Called")
    }

    override fun onDestroy() {
        Timber.tag(TAG).d("On Destroy is Called")
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.tag(TAG).d("On DestroyView is Called")
        binding = null
    }

    companion object {
        const val DEVICE_MODEL = "QCOM-BTD"
    }
}