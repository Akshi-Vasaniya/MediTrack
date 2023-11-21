package com.example.meditrack.utility.ownDialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import com.example.meditrack.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class UserInfoUpdateDialog(context: Context, private val listener: CustomDialogListener, private val initialText: String,
                           private val fieldName:String) : Dialog(context) {

    interface CustomDialogListener {
        fun onUpdateButtonClicked(text: String,fieldName:String)
    }

    var inputText:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.custom_dialog_layout)

        val editText = findViewById<TextInputEditText>(R.id.edit_text)
        val inputLayout = findViewById<TextInputLayout>(R.id.input_layout)
        val cancelButton = findViewById<Button>(R.id.cancel_button)
        val updateButton = findViewById<Button>(R.id.update_button)
        val closeButton = findViewById<ImageView>(R.id.close_button)

        editText.setText(initialText)
        inputLayout.helperText=null
        inputText=initialText

        editText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                inputText = s.toString()
                if(inputText=="")
                {
                    inputText=""
                    inputLayout.helperText="Required"
                }
                else{
                    inputLayout.helperText=null
                }
            }

            override fun afterTextChanged(s: Editable?) {}

        })

        // Close dialog on clicking the cancel button
        cancelButton.setOnClickListener {
            dismiss()
        }

        // Implement the update button logic here
        updateButton.setOnClickListener {
            // Add your logic here
            listener.onUpdateButtonClicked(inputText,fieldName)
            dismiss()
        }

        // Close the dialog on clicking the close icon
        closeButton.setOnClickListener {
            dismiss()
        }

        // Set the width of the dialog
        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        window?.setLayout((width * 0.9).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
    }
}
