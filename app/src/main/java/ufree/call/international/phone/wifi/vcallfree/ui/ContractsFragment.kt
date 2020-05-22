package ufree.call.international.phone.wifi.vcallfree.ui

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.coroutines.*
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.adapter.BaseAdapter
import ufree.call.international.phone.wifi.vcallfree.api.Contact
import ufree.call.international.phone.wifi.vcallfree.databinding.FragmentTabContractsBinding
import ufree.call.international.phone.wifi.vcallfree.lib.BaseDataBindingFragment
import ufree.call.international.phone.wifi.vcallfree.utils.Dispatcher
import java.lang.Exception

/**
 * Created by lyf on 2020/4/27.
 */
class ContractsFragment : BaseDataBindingFragment<FragmentTabContractsBinding>(),BaseAdapter.OnItemClick<Contact>,TextWatcher {
    val list: MutableList<Contact> = mutableListOf()
    var contractsObserver:ContractsObserver? = null
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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(context!!,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED){
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
        values?.forEach {
            println("values:$it")
        }
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
                ContactsContract.CommonDataKinds.Phone._ID
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
        }
        phoneCursor?.close()
    }

    override fun onDetach() {
        super.onDetach()
        context?.contentResolver?.unregisterContentObserver(contractsObserver!!)
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
            var iso = ""
            try {
                val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
                val phoneNumber = phoneNumberUtil.parseAndKeepRawInput(t.phone,null)
                iso = phoneNumberUtil.getRegionCodeForNumber(phoneNumber)
                t.phone = phoneNumberUtil.getNationalSignificantNumber(phoneNumber)
            }catch (e:Exception){
                e.printStackTrace()
            }
            Dispatcher.dispatch(context){
                navigate(DialActivity::class.java)
                extra("contact",t)
                extra("iso",iso)
                defaultAnimate()
            }.go()
        }else if(id == R.id.invite){
            Dispatcher.dispatch(context){
                action(Intent.ACTION_SENDTO)
                data(Uri.parse("smsto:${t.phone}"))
                extra("sms_body",context?.getString(R.string.invite_text) ?: "")
                defaultAnimate()
            }.go()
        }

    }

    fun dial(v:View){
        println("跳转--")
        Dispatcher.dispatch(context){
            navigate(DialActivity::class.java)
            defaultAnimate()
        }.go()
    }

    override fun afterTextChanged(s: Editable?) {
        search(s.toString())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    inner class ContractsObserver(handler:Handler):ContentObserver(handler){
        override fun onChange(selfChange: Boolean) {
            println("ContractFragment ContractsObserver onChange $selfChange")
            search("")
        }
    }
}