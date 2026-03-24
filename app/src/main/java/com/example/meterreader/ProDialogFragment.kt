package com.example.meterreader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ProDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_pro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBuy = view.findViewById<Button>(R.id.btnBuyPro)
        btnBuy.setOnClickListener {
            // Демо-режим, реальной оплаты нет
            Toast.makeText(requireContext(), "Демо: оплата не подключена, но функции PRO можно протестировать вручную", Toast.LENGTH_LONG).show()
            dismiss()
        }
    }

    override fun getTheme(): Int = R.style.AppTheme_Dialog
}
