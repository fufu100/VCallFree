package vcall.free.international.phone.wifi.calling.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.coroutines.*
import vcall.free.international.phone.wifi.calling.MainActivity
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.adapter.BaseAdapter
import vcall.free.international.phone.wifi.calling.api.Contact
import vcall.free.international.phone.wifi.calling.databinding.FragmentTabContractsBinding
import vcall.free.international.phone.wifi.calling.lib.BaseDataBindingFragment
import vcall.free.international.phone.wifi.calling.utils.Dispatcher
import vcall.free.international.phone.wifi.calling.utils.UserManager
import java.lang.Exception
import java.util.*

/**
 * Created by lyf on 2020/4/27.
 */
class ContractsFragment : BaseDataBindingFragment<FragmentTabContractsBinding>(),BaseAdapter.OnItemClick<Contact>,TextWatcher {
    val list: MutableList<Contact> = mutableListOf()
    var contractsObserver:ContractsObserver? = null
    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CONTACTS
    )
    override fun getLayoutResId(): Int = R.layout.fragment_tab_contracts

    override fun initView(v: View) {
        dataBinding.fragment = this
        dataBinding.recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        dataBinding.recyclerView.adapter = BaseAdapter<Contact>(list){
            R.layout.item_contacts
        }.apply {
            mOnItemClickListener = this@ContractsFragment
            mShowFooterItem = false
        }
        dataBinding.search.addTextChangedListener(this)
        GlobalScope.launch {
            println("-----")
            getContacts()
            println("+++++")
            withContext(Dispatchers.Main){
                dataBinding.recyclerView.adapter?.notifyDataSetChanged()
            }
        }

    }
    var job:Job? = null
    fun search(keyword:String){
        job?.cancel()
        job = GlobalScope.launch {
            delay(500)
            println("搜索$keyword")
            getContacts(keyword)

            withContext(Dispatchers.Main){
                dataBinding.recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun getContacts(keyword: String = ""){
        println("getContact ${context != null}")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && ActivityCompat.checkSelfPermission(context!!,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED){
            if (lackPermission()) {
                requestPermissions( PERMISSIONS, 0)
            }
            return
        }
        list.clear()
        var where:String? = null
        var values:Array<String>? = null
        if(keyword.isNotEmpty()){
            where = ""
            where += "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} like ?"
            where += " or "
            where += "${ContactsContract.CommonDataKinds.Phone.NUMBER} like ?"
            values = arrayOf("%$keyword%","%$keyword%")
        }
        println("搜索条件： $where")
//        values?.forEach {
//            println("values:$it")
//        }
        if(contractsObserver == null){
            contractsObserver = ContractsObserver(Handler(Looper.getMainLooper()))
        }
        context?.contentResolver?.registerContentObserver(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,true,contractsObserver!!)
        val phoneCursor = context?.contentResolver?.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
            ),
            where, values, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED"
        )
        while (phoneCursor?.moveToNext() == true) {
            val phoneId = phoneCursor.getLong(4)
            val contactId = phoneCursor.getLong(0)
            val phone = phoneCursor.getString(2)
            val username = phoneCursor.getString(1)
            val photoId = phoneCursor.getLong(3)
            list.add(Contact(phoneId,contactId, username, phone, photoId))
//            println("$fragmentTag ${phoneCursor.getString(5)}")
        }
        phoneCursor?.close()
    }

    override fun onDetach() {
        super.onDetach()
        if(contractsObserver != null) {
            context?.contentResolver?.unregisterContentObserver(contractsObserver!!)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        println("ContractsFragment onRequestPermissionsResult $permissions,$grantResults")
        GlobalScope.launch {
            println("-----")
            getContacts()
            println("+++++")
            withContext(Dispatchers.Main){
                dataBinding.recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onItemClick(id: Int, position: Int, t: Contact) {
        if(id == -1){
            var iso:String? = ""
            var phone = t.phone
            println("onItemClick ${Locale.getDefault().country}")
            try {
                val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
                val phoneNumber = phoneNumberUtil.parseAndKeepRawInput(t.phone, "CN")
                println("onItemClick ${phoneNumber == null} ${Locale.getDefault().country}")

                iso = phoneNumberUtil.getRegionCodeForNumber(phoneNumber)
                phone = phoneNumberUtil.getNationalSignificantNumber(phoneNumber)
            }catch (e:Exception){
                e.printStackTrace()
            }
            (activity as MainActivity).dial(phone,iso?:"",t.username)
        }else if(id == R.id.invite){
            Dispatcher.dispatch(context){
                action(Intent.ACTION_SENDTO)
                data(Uri.parse("smsto:${t.phone}"))
                extra("sms_body",context?.getString(R.string.invite_text,UserManager.get().user?.invite ?: "") ?: "")
                defaultAnimate()
            }.go()
        }

    }

    fun dial(v:View){
        (activity as MainActivity).dial()
    }

    override fun afterTextChanged(s: Editable?) {
        search(s.toString())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    private fun lackPermission(): Boolean {
        for (permission in PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) {
                return true
            }
        }
        return false
    }


    inner class ContractsObserver(handler:Handler):ContentObserver(handler){
        override fun onChange(selfChange: Boolean) {
            println("ContractFragment ContractsObserver onChange $selfChange")
            search("")
        }
    }
}