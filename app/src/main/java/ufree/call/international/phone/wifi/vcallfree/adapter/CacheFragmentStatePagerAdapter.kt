package ufree.call.international.phone.wifi.vcallfree.adapter

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

abstract class CacheFragmentStatePagerAdapter(fm:FragmentManager):FragmentStatePagerAdapter(fm) {
    companion object{
        private val STATE_SUPER_STATE = "superState"
        private val STATE_PAGES = "pages"
        private val STATE_PAGE_INDEX_PREFIX = "pageIndex:"
        private val STATE_PAGE_KEY_PREFIX = "page:"
    }

    private val mFm: FragmentManager = fm
    private val mPages: SparseArray<Fragment> = SparseArray<Fragment>()

    override fun saveState(): Parcelable {
        val p = super.saveState()
        val bundle = Bundle()
        bundle.putParcelable(STATE_SUPER_STATE, p)

        bundle.putInt(STATE_PAGES, mPages.size())
        if (0 < mPages.size()) {
            for (i in 0 until mPages.size()) {
                val position = mPages.keyAt(i)
                bundle.putInt(createCacheIndex(i), position)
                val f = mPages.get(position)
                //见 http://stackoverflow.com/questions/21568341/illegalstateexception-fragment-thisfragment-is-not-currently-in-the-fragmentm
                if (f != null && f.isAdded) {
                    mFm.putFragment(bundle, createCacheKey(position), f)
                }
            }
        }
        return bundle
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        val bundle = state as Bundle
        val pages = bundle.getInt(STATE_PAGES)
        if (0 < pages) {
            for (i in 0 until pages) {
                val position = bundle.getInt(createCacheIndex(i))
                val f = mFm.getFragment(bundle, createCacheKey(position))
                mPages.put(position, f)
            }
        }

        val p = bundle.getParcelable<Parcelable>(STATE_SUPER_STATE)
        super.restoreState(p, loader)
    }

    /**
     * Get a new Fragment instance.
     * Each fragments are automatically cached in this method,
     * so you don't have to do it by yourself.
     * If you want to implement instantiation of Fragments,
     * you should override [.createItem] instead.
     *
     * {@inheritDoc}
     *
     * @param position position of the item in the adapter
     * @return fragment instance
     */
    override fun getItem(position: Int): Fragment {
        val f = createItem(position)
        // We should cache fragments manually to access to them later
        mPages.put(position, f)
        return f
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        if (0 <= mPages.indexOfKey(position)) {
            mPages.remove(position)
        }
        super.destroyItem(container, position, `object`)
    }

    /**
     * Get the item at the specified position in the adapter.
     *
     * @param position position of the item in the adapter
     * @return fragment instance
     */
    fun getItemAt(position: Int): Fragment {
        return mPages.get(position)
    }

    /**
     * Create a new Fragment instance.
     * This is called inside [.getItem].
     *
     * @param position position of the item in the adapter
     * @return fragment instance
     */
    protected abstract fun createItem(position: Int): Fragment

    /**
     * Create an index string for caching Fragment pages.
     *
     * @param index index of the item in the adapter
     * @return key string for caching Fragment pages
     */
    protected fun createCacheIndex(index: Int): String {
        return STATE_PAGE_INDEX_PREFIX + index
    }

    /**
     * Create a key string for caching Fragment pages.
     *
     * @param position position of the item in the adapter
     * @return key string for caching Fragment pages
     */
    protected fun createCacheKey(position: Int): String {
        return STATE_PAGE_KEY_PREFIX + position
    }
}