package com.example.meterreader

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ChangelogDialog : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_changelog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnClose = view.findViewById<Button>(R.id.btnChangelogClose)
        btnClose.setOnClickListener {
            dismiss() // закрываем диалог
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setView(LayoutInflater.from(context).inflate(R.layout.dialog_changelog, null))
            .setCancelable(true) // разрешаем закрытие по нажатию вне диалога
            .create()
    }
}
