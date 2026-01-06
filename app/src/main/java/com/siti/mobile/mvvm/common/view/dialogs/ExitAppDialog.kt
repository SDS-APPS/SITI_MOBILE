package com.siti.mobile.mvvm.common.view.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.siti.mobile.R

interface  onClickOk{
    fun invoke()
}

class ExitAppDialog(
    private val onClickOk : onClickOk
) : DialogFragment() {

    private lateinit var tvYes : TextView
    private lateinit var tvNo : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BlurredDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_exit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvYes = view.findViewById(R.id.tvYes)
        tvNo = view.findViewById(R.id.tvNo)

        tvNo.setOnClickListener { dismiss() }
        tvYes.setOnClickListener { onClickOk.invoke() }
    }

}