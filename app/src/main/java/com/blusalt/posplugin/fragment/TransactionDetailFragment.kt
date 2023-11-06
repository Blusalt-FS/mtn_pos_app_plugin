package com.blusalt.posplugin.fragment

import android.bluetooth.BluetoothAdapter
import androidx.navigation.NavController
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.activity.OnBackPressedCallback
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.blusalt.posplugin.R
import com.blusalt.posplugin.model.TerminalResponse
import com.google.gson.Gson
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.blusalt.posplugin.databinding.FragmentTransactionDetailBinding
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception

class TransactionDetailFragment : Fragment() {

    val TAG = TransactionDetailFragment::class.java.simpleName
    var binding: FragmentTransactionDetailBinding? = null
    var bluetoothAdapter: BluetoothAdapter? = null
    var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        appPreferenceHelper = new AppPreferenceHelper(requireContext());
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        navController = NavHostFragment.findNavController(this)


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
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_transaction_detail,
            container,
            false
        )
        try {

            var result = TransactionStatusFragmentArgs.fromBundle(requireArguments()).result

            val response = Gson().fromJson(result, TerminalResponse::class.java)
            Log.e("ATG", Gson().toJson(response))
            Log.e("ATG", response.data.receiptInfo.transactionAmount)
            Log.e("TAG response", Gson().toJson(response))
            Log.e("TAG amount", response.data.receiptInfo.transactionAmount)

            if (result != null) {
                // The getPrivacyPolicyLink() method will be created automatically.

                binding?.receiptValue?.text = response.data.receiptInfo.reference
                binding?.terminalValue?.text = response.data.receiptInfo.merchantTID
                binding?.dateValue?.text =
                    response.data.receiptInfo.transactionDate + " " + response.data.receiptInfo.transactionTime
                binding?.cardValue?.text = response.data.cardScheme
                binding?.cardExValue?.text = response.data.receiptInfo.customerCardExpiry
                binding?.clientValue?.text = response.data.receiptInfo.customerCardName
                binding?.panValue?.text = response.data.receiptInfo.customerCardPan
                binding?.aidValue?.text = response.data.receiptInfo.transactionAID
                if (response.data.posResponseCode == "00") {
                    binding?.messageValue?.text = "Transaction Approved"
                } else {
                    binding?.messageValue?.text = "Transaction Declined"
                }
                binding?.stanValue?.text = response.data.receiptInfo.transactionSTAN
                binding?.rrnValue?.text = response.data.receiptInfo.rrn
                binding?.amountText?.text = "₦ " + response.data.receiptInfo.transactionAmount + ".00"

                timer.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        binding?.toolbar?.setOnClickListener { it: View? -> if (!navController!!.navigateUp()) requireActivity().finishAfterTransition() }
        return binding!!.getRoot()
    }

    val timer = object : CountDownTimer(1000, 1000) {
        override fun onTick(millisUntilFinished: Long) {

        }
        override fun onFinish() {
            shareData()
        }
    }

    private fun shareData() {
        val bitmap = getBitmapFromView(binding!!.receiptParentView)

        if (bitmap != null) {

            //Save the image inside the APPLICTION folder
            val mediaStorageDir = File(
                requireActivity().applicationContext.externalCacheDir.toString() + "Image.png"
            )
            try {
                val outputStream = FileOutputStream(mediaStorageDir.toString())
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val imageUri: Uri = FileProvider.getUriForFile(
                requireActivity().applicationContext, requireActivity().applicationContext.packageName + ".provider",
                mediaStorageDir
            )
            val waIntent = Intent(Intent.ACTION_SEND)
            waIntent.type = "image/*"
            waIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
            startActivity(Intent.createChooser(waIntent, "Share with"))
        }

    }

    fun getBitmapFromView(view: View): Bitmap? {

        Log.e("View: ", view.rootView.toString())
        Log.e("Width: ", view.rootView.width.toString())
        Log.e("Width: ", view.width.toString())
        Log.e("Height: ", view.rootView.height.toString())
        Log.e("Height: ", view.height.toString())

//        try {
        //Define a bitmap with the same size as the view
        val returnedBitmap: Bitmap =
            Bitmap.createBitmap(view.rootView.width, view.rootView.height, Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas)
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        }
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
        return returnedBitmap

    }



    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
       Log.e("TAG","On Pause is Called")
    }

    override fun onDestroy() {
       Log.e("TAG","On Destroy is Called")
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
       Log.e("TAG","On DestroyView is Called")
        binding = null
    }

}