package com.nemodream.bangkkujaengi.admin.ui.custom

import android.R
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import android.widget.ArrayAdapter
import com.nemodream.bangkkujaengi.databinding.DialogCustomCanceledBinding

class CustomCanceledDialog(
    context: Context,
    private val message: String,
    private val reasons: List<String>, // 드롭다운 데이터
    private val onConfirm: (String) -> Unit, // 선택된 사유 반환
    private val onCancel: () -> Unit
) {
    private val dialog = Dialog(context)
    private lateinit var binding: DialogCustomCanceledBinding

    fun show() {
        // 뷰 바인딩 초기화
        binding = DialogCustomCanceledBinding.inflate(dialog.layoutInflater)

        // 다이얼로그 설정
        with(dialog) {
            setContentView(binding.root)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명
                val params = attributes
                params.width = (context.resources.displayMetrics.widthPixels * 0.85).toInt() // 다이얼로그 너비 설정
                params.height = WindowManager.LayoutParams.WRAP_CONTENT // 높이 자동 조정
                attributes = params
            }
            setCancelable(false) // 외부 터치 시 다이얼로그 닫히지 않음
        }

        with(binding) {
            // 메시지 설정
            tvCanceledMessage.text = message

            // AutoCompleteTextView에 어댑터 설정
            val adapter = ArrayAdapter(dialog.context, android.R.layout.simple_list_item_1, reasons)
            actvCanceledReason.setAdapter(adapter)

            // 드롭다운 표시를 위해 TextInputLayout endIcon 클릭 처리
            actvCanceledReason.setOnClickListener {
                actvCanceledReason.showDropDown()
            }

            // 확인 버튼 클릭 처리
            btnCanceledConfirm.setOnClickListener {
                val selectedReason = actvCanceledReason.text.toString()
                if (selectedReason.isNotEmpty()) {
                    onConfirm(selectedReason) // 선택된 사유를 콜백으로 전달
                    dialog.dismiss()
                } else {
                    actvCanceledReason.error = "취소 사유를 선택해주세요"
                }
            }

            // 취소 버튼 클릭 처리
            btnCanceledCancel.setOnClickListener {
                onCancel() // 취소 버튼 콜백 실행
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}
