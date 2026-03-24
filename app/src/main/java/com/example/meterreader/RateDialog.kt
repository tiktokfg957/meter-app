package com.example.meterreader

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RateDialog : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_rate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnRate = view.findViewById<Button>(R.id.btnRateNow)
        val btnLater = view.findViewById<Button>(R.id.btnRateLater)

        btnRate.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.rustore.ru/catalog/app/com.example.meterreader"))
            startActivity(intent)
            dismiss()
        }

        btnLater.setOnClickListener {
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setView(LayoutInflater.from(context).inflate(R.layout.dialog_rate, null))
            .setCancelable(true)
            .create()
    }
}
