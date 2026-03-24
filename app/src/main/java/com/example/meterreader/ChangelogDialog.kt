package com.example.meterreader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment

class ChangelogDialog : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_changelog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnClose = view.findViewById<Button>(R.id.btnChangelogClose)
        btnClose.setOnClickListener {
            dismiss()
        }
    }

    override fun getTheme(): Int = R.style.AppTheme_Dialog
}
