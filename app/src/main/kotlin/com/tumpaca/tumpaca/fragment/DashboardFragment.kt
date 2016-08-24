package com.tumpaca.tumpaca.fragment;

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.view.*
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.tumblr.jumblr.types.Post
import com.tumpaca.tumpaca.R
import com.tumpaca.tumpaca.model.PostList
import com.tumpaca.tumpaca.model.TPRuntime
import com.tumpaca.tumpaca.util.likeAsync
import com.tumpaca.tumpaca.util.reblogAsync

class DashboardFragment : FragmentBase() {
    companion object {
        const val TAG = "DashboardFragment"
        private const val OFFSCREEN_PAGE_LIMIT = 4
    }

    interface DashboardFragmentListener {
        fun showSettings(fr: DashboardFragment)
    }

    var postList: PostList? = null
    var likeButton: ImageButton? = null
    var viewPager: ViewPager? = null
    var dashboardAdapter: DashboardPageAdapter? = null
    var listener: DashboardFragmentListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fr_dashboard, container, false)

        val page = view.findViewById(R.id.page) as TextView
        val postCount = view.findViewById(R.id.post_count) as TextView

        (view.findViewById(R.id.view_pager) as ViewPager).let {
            viewPager = it
            it.offscreenPageLimit = OFFSCREEN_PAGE_LIMIT
            it.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    page.text = "${position + 1}"
                    postCount.text = "${postList?.size}"
                }

                override fun onPageSelected(position: Int) {
                    toggleLikeButton(postList?.get(position)!!)
                }
            })
        }

        // PostList と ViewPage のバインド
        val tumblrService = TPRuntime.tumblrService!!
        tumblrService.resetPosts()
        postList = tumblrService.postList
        postList?.fetchedListener = object : PostList.FetchedListener {
            override fun onFetched(size: Int) {
                postCount.text = "${size}"
            }
        }
        dashboardAdapter = DashboardPageAdapter(fragmentManager, postList!!)
        viewPager?.adapter = dashboardAdapter

        dashboardAdapter?.onBind()

        // Like
        (view.findViewById(R.id.like_button) as ImageButton).let {
            likeButton = it
            it.setOnClickListener {
                doLike()
            }
        }

        // reblog
        val reblogButton = view.findViewById(R.id.reblog_button)
        reblogButton.setOnClickListener {
            doReblog()
        }

        val settingsButton = view.findViewById(R.id.settings_button)
        settingsButton.setOnClickListener {
            listener?.showSettings(this)
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        getActionBar()?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dashboardAdapter?.onUnbind()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_dashboard, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context !is DashboardFragmentListener) {
            throw UnsupportedOperationException("DashboardFragmentListener is not implementation.")
        } else {
            listener = context
        }
    }

    private fun doLike() {
        val currentPost = postList?.get(viewPager!!.currentItem)
        currentPost?.likeAsync({ post ->
            toggleLikeButton(post)
            val msg = if (post.isLiked) R.string.liked_result else R.string.unliked_result
            Snackbar.make(view!!, msg, Snackbar.LENGTH_SHORT).show()
        })
    }

    private fun doReblog() {
        val currentPost = postList?.get(viewPager!!.currentItem)
        val input = EditText(context)
        input.setHint(R.string.comment_input_hint)
        AlertDialog.Builder(context)
                .setTitle(R.string.reblog_dialog_header)
                .setView(input)
                .setPositiveButton(android.R.string.ok) { dialog, which ->
                    val comment = input.text.toString()
                    val blogName = TPRuntime.tumblrService!!.user?.blogs?.first()?.name!!
                    currentPost?.reblogAsync(blogName, comment, { post ->
                        Snackbar.make(view!!, R.string.reblogged_result, Snackbar.LENGTH_SHORT).show()
                    })
                }
                .setNegativeButton(android.R.string.cancel) { d, w -> }
                .show()
    }


    private fun toggleLikeButton(post: Post) {
        val state = android.R.attr.state_checked * if (post.isLiked) 1 else -1
        likeButton?.setImageState(intArrayOf(state), false)
    }
}
