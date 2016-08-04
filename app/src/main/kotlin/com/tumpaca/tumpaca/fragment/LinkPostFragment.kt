package com.tumpaca.tumpaca.fragment

/**
 * Created by yabu on 7/11/16.
 */

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.model.TPRuntime
import com.tumpaca.tumpaca.util.blogAvatarAsync

class LinkPostFragment : PostFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val post = TPRuntime.tumblrService!!.postList?.get(postIndex)

        // データを取得
        val bundle = arguments
        val blogName = bundle.getString("blogName")
        val subText = bundle.getString("subText")

        // View をつくる
        val view = inflater.inflate(R.layout.post_link, container, false)

        val titleView = view.findViewById(R.id.title) as TextView
        titleView.text = blogName

        val subTextView = view.findViewById(R.id.sub) as WebView
        val mimeType = "text/html; charset=utf-8"
        subTextView.loadData(subText, mimeType, null)

        val iconView = view.findViewById(R.id.icon) as ImageView
        post?.blogAvatarAsync { bitmap ->
            iconView.setImageBitmap(bitmap)
        }

        return view
    }

}