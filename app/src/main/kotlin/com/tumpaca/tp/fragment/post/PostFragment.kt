package com.tumpaca.tp.fragment.post

import android.content.BroadcastReceiver
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.tumblr.jumblr.types.Post
import com.tumpaca.tp.R
import com.tumpaca.tp.fragment.FragmentBase
import com.tumpaca.tp.model.TPRuntime
import com.tumpaca.tp.util.blogAvatar
import com.tumpaca.tp.util.isOnline
import com.tumpaca.tp.util.onNetworkRestored

abstract class PostFragment : FragmentBase() {
    companion object {
        private const val TAG = "PostFragment"
    }

    // TODO
    // PostList で対象のポストを管理していると、PostList の先頭に新しい Post がきた場合に対応できないので本当はよくない
    protected var page: Int = -1
    protected var networkReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        page = bundle.getInt("pageNum")
    }

    fun getPost(callback: (Post?) -> Unit) {
        if (context.isOnline()) {
            TPRuntime.tumblrService.postList?.getAsync(page, callback)
        } else {
            Toast.makeText(context, R.string.offline_toast, Toast.LENGTH_SHORT).show()
            networkReceiver = context.onNetworkRestored {
                removeNetworkReceiver()
                TPRuntime.tumblrService.postList?.getAsync(page, callback)
            }
        }
    }

    fun initStandardViews(view: View, blogName: String, subText: String, reblogged: String?, noteCount: Long) {
        val titleView = view.findViewById<TextView>(R.id.title)
        titleView.text = blogName

        val subTextView = view.findViewById<WebView>(R.id.sub)
        val mimeType = "text/html; charset=utf-8"
        subTextView.setBackgroundColor(Color.TRANSPARENT)
        subTextView.loadData(subText, mimeType, null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            subTextView.clipToOutline = true
        }

        val rebloggedView = view.findViewById<TextView>(R.id.reblogged)
        if (reblogged != null) {
            rebloggedView.text = reblogged
        } else { // reblogじゃない場合はリブログアイコンを非表示にする
            val reblogInfoLayout = view.findViewById<LinearLayout>(R.id.post_info)
            val reblogIcon = view.findViewById<View>(R.id.reblog_icon)
            if (reblogIcon != null) {
                reblogInfoLayout.removeView(reblogIcon)
            }
        }

        val noteCountView = view.findViewById<TextView>(R.id.notes)
        // ここで getQuantityString を使っていないのは、count="one" が無視される言語
        // （日本語など）でもあえて one と other を区別したいため。
        val strId = if (noteCount == 1L) R.string.note_one else R.string.note_other
        noteCountView.text = resources.getString(strId, noteCount)
    }

    fun setIcon(view: View, post: Post) {
        val iconView = view.findViewById<ImageView>(R.id.icon)
        post.blogAvatar()
                .subscribe { avatar ->
                    iconView.setImageBitmap(avatar)
                }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeNetworkReceiver()
    }

    fun removeNetworkReceiver() {
        networkReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (e: Throwable) {
                Log.d(TAG, "Receiver was not registered", e)
            }
        }
        networkReceiver = null
    }
}