package com.samrez.sp_test_case.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.samrez.sp_test_case.R
import com.samrez.sp_test_case.databinding.ActivityEditTextResultBinding

class EditTextResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditTextResultBinding
    private lateinit var textResult: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTextResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textResult = intent.getStringExtra("textResult").toString()

        initUI()
        initListener()
    }

    private fun initUI() {
        binding.etEditResult.setText(textResult)
    }

    private fun initListener() {
        binding.btnEditResult.setOnClickListener {
            val intent = Intent()
            intent.putExtra("textResult", binding.etEditResult.text.toString())
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}