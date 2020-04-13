package com.luiz.reminder.ui.activities.include_reminde

import android.text.Editable
import android.util.Log
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.luiz.reminder.api.responses.NoteResponse
import com.luiz.reminder.ui.activities.main.MainActivity
import com.luiz.reminder.ui.base.BaseViewModel
import com.luiz.reminder.util.MaskWatcher
import com.luiz.reminder.util.Utils
import com.luiz.reminder.util.Utils.openActivity
import com.luiz.reminder.util.Utils.toast
import com.mlykotom.valifi.ValiFiForm
import com.mlykotom.valifi.fields.ValiFieldText
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody


class IncludeRemindeViewModel : BaseViewModel() {

    val title = ValiFieldText().addNotEmptyValidator("Campo obrigatório!")
    val date = ValiFieldText().addNotEmptyValidator("Campo obrigatório!")
        .addExactLengthValidator("Data inválida!", 10)
    val description = ValiFieldText().addNotEmptyValidator("Campo obrigatório!")
    val isNotified = MutableLiveData<Boolean>()
    val form = ValiFiForm(title, description)

    private fun reminderBody(date: String): RequestBody {
        loading.value = true

        val json = JsonObject()
        json.addProperty("title", title.value)
        json.addProperty("description", description.value)
        json.addProperty("date", date)
        json.addProperty("isNotified", isNotified.value)

        Log.d("_res", "Json: ${Gson().toJson(json)}")
        return RequestBody.create(MediaType.parse("application/json"), json.toString())
    }

    fun registerReminde(data: String) {
        if (data.isNotEmpty()) {
            disposable.add(
                apiRepository.registerNote(reminderBody(data))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<NoteResponse>() {
                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                            loadError.value = Utils.getMessageErrorObject(e)
                            loading.value = false
                        }

                        override fun onSuccess(t: NoteResponse) {
                            loadError.value = null
                            loading.value = false

                            redirectToHome()
                        }
                    })
            )
        } else {
            date.value = data
        }
    }

    private fun redirectToHome() {
        context.toast("Lembrete criado com sucesso!")
        context.openActivity<MainActivity>()
    }

    fun onTextChange(editable: Editable?) {
        Log.d("TAG", "New text: ${editable.toString()}")
        MaskWatcher("##/##/####").afterTextChanged(editable!!)
    }

    val setMaskDate: MaskWatcher
        get() = MaskWatcher.buildDate()

    fun setMask(editText: AppCompatEditText, textWatcher: MaskWatcher?) {
        editText.addTextChangedListener(textWatcher)
    }

}