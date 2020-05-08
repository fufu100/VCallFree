package ufree.call.international.phone.wifi.vcallfree.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.provider.ContactsContract
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.*
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.adapter.BaseAdapter
import ufree.call.international.phone.wifi.vcallfree.api.Contact
import ufree.call.international.phone.wifi.vcallfree.databinding.FragmentTabContractsBinding
import ufree.call.international.phone.wifi.vcallfree.lib.BaseDataBindingFragment
import ufree.call.international.phone.wifi.vcallfree.utils.Dispatcher

/**
 * Created by lyf on 2020/4/27.
 */
class ContractsFragment : BaseDataBindingFragment<FragmentTabContractsBinding>(),BaseAdapter.OnItemClick<Contact> {
    val list: MutableList<Contact> = mutableListOf()
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

        GlobalScope.launch {
            println("-----")
            getContacts()
            println("+++++")
            withContext(Dispatchers.Main){
                dataBinding.recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    private suspend fun getContacts(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(context!!,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED){
            return;
        }
        list.clear()
        val phoneCursor = context?.contentResolver?.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
                ContactsContract.CommonDataKinds.Phone._ID
            ),
            null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED"
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        println("ContractsFragment onRequestPermissionsResult $permissions,$grantResults")
    }

    override fun onItemClick(id: Int, position: Int, t: Contact) {
        Dispatcher.dispatch(context){
            navigate(DialActivity::class.java)
            extra("contact",t)
            defaultAnimate()
        }.go()
    }

    fun dial(v:View){
        println("跳转--")
        Dispatcher.dispatch(context){
            navigate(DialActivity::class.java)
            defaultAnimate()
        }.go()
    }
}