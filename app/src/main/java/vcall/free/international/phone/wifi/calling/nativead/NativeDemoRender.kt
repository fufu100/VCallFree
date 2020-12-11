package vcall.free.international.phone.wifi.calling.nativead

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.anythink.nativead.api.ATNativeAdRenderer
import com.anythink.nativead.api.ATNativeImageView
import com.anythink.nativead.unitgroup.api.CustomNativeAd
import vcall.free.international.phone.wifi.calling.R


/**
 * Created by lyf on 2020/11/10.
 */
class NativeDemoRender(val context: Context):ATNativeAdRenderer<CustomNativeAd> {
    lateinit var mDevelopView: View
    var mNetworkType = 0
    val mClickView = mutableListOf<View>()

    override fun createView(ctx: Context?, networkType: Int): View {
        if (!::mDevelopView.isInitialized) {
            mDevelopView = LayoutInflater.from(ctx).inflate(R.layout.native_ad_item, null)
        }
        mNetworkType = networkType
        if (mDevelopView.parent != null) {
            (mDevelopView.parent as ViewGroup).removeView(mDevelopView)
        }
        return mDevelopView
    }

    override fun renderAdView(view: View, ad: CustomNativeAd) {
        mClickView.clear()
        val titleView: TextView =
            view.findViewById<View>(R.id.native_ad_title) as TextView
        val descView: TextView =
            view.findViewById<View>(R.id.native_ad_desc) as TextView
        val ctaView: TextView =
            view.findViewById<View>(R.id.native_ad_install_btn) as TextView
        val adFromView: TextView =
            view.findViewById<View>(R.id.native_ad_from) as TextView
        val contentArea: FrameLayout =
            view.findViewById<View>(R.id.native_ad_content_image_area) as FrameLayout
        val iconArea: FrameLayout =
            view.findViewById<View>(R.id.native_ad_image) as FrameLayout
        //since v5.6.5
        val logoView =
            view.findViewById<View>(R.id.native_ad_logo) as ATNativeImageView
        titleView.setText("")
        descView.setText("")
        ctaView.setText("")
        adFromView.setText("")
        titleView.setText("")
        contentArea.removeAllViews()
        iconArea.removeAllViews()
        logoView.setImageDrawable(null)
        val mediaView = ad.getAdMediaView(contentArea, contentArea.getWidth())
        Log.d("NativeDemoRendeer", "renderAdView ${mediaView == null},${ad.isNativeExpress} ${ad.title} $ad ")
        if (ad.isNativeExpress) {
            titleView.setVisibility(View.GONE)
            descView.setVisibility(View.GONE)
            ctaView.setVisibility(View.GONE)
            logoView.visibility = View.GONE
            iconArea.setVisibility(View.GONE)
            if (mediaView!!.parent != null) {
                (mediaView.parent as ViewGroup).removeView(mediaView)
            }
            contentArea.addView(
                mediaView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            )
            return
        }
        titleView.setVisibility(View.VISIBLE)
        descView.setVisibility(View.VISIBLE)
        ctaView.setVisibility(View.VISIBLE)
        logoView.visibility = View.VISIBLE
        iconArea.setVisibility(View.VISIBLE)
        val adiconView = ad.adIconView

        //since v5.6.5
        val iconView = ATNativeImageView(context)
        if (adiconView == null) {
            iconArea.addView(iconView)
            iconView.setImage(ad.iconImageUrl)
            mClickView.add(iconView)
        } else {
            iconArea.addView(adiconView)
        }
        if (!TextUtils.isEmpty(ad.adChoiceIconUrl)) {
            logoView.setImage(ad.adChoiceIconUrl)
        } else {
//            logoView.setImageResource(R.drawable.ad_logo);
        }
        if (mediaView != null) {
            if (mediaView.parent != null) {
                (mediaView.parent as ViewGroup).removeView(mediaView)
            }
            contentArea.addView(
                mediaView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            )
        } else {
            //since v5.6.5
            val imageView = ATNativeImageView(context)
            imageView.setImage(ad.mainImageUrl)
            val params: ViewGroup.LayoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            imageView.layoutParams = params
            contentArea.addView(imageView, params)
            mClickView.add(imageView)
        }
        titleView.setText(ad.title)
        descView.setText(ad.descriptionText)
        ctaView.setText(ad.callToActionText)
        if (!TextUtils.isEmpty(ad.adFrom)) {
            adFromView.setText(if (ad.adFrom != null) ad.adFrom else "")
            adFromView.setVisibility(View.VISIBLE)
        } else {
            adFromView.setVisibility(View.GONE)
        }
        mClickView.add(titleView)
        mClickView.add(descView)
        mClickView.add(ctaView)
    }

    fun getClickView(): List<View?>? {
        return mClickView
    }
}