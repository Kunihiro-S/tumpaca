package com.tumpaca.tp.util

import android.content.*
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.tumblr.jumblr.types.Photo
import com.tumblr.jumblr.types.PhotoSize
import com.tumblr.jumblr.types.Post
import com.tumpaca.tp.R
import com.tumpaca.tp.model.AdPost
import com.tumpaca.tp.model.TPRuntime
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe

fun Context.editSharedPreferences(name: String, mode: Int = Context.MODE_PRIVATE, actions: (SharedPreferences.Editor) -> Unit) {
    val editor = getSharedPreferences(name, mode).edit()
    actions(editor)
    val committed = editor.commit()
    assert(committed)
}

fun Post.likeAsync(callback: (Post, Boolean) -> Unit) {
    if (this.isLiked) {
        TPToastManager.show(TPRuntime.mainApplication.resources.getString(R.string.unlike))
    } else {
        TPToastManager.show(TPRuntime.mainApplication.resources.getString(R.string.like))
    }

    object : AsyncTask<Unit, Unit, Boolean>() {
        override fun doInBackground(vararg args: Unit): Boolean {
            try {
                if (isLiked) {
                    unlike()
                } else {
                    like()
                }
                return true
            } catch(e: Exception) {
                Log.e("LikeTask", e.message.orEmpty())
                return false
            }
        }

        override fun onPostExecute(result: Boolean) {
            callback(this@likeAsync, result)
        }
    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
}

fun Post.reblogAsync(blogName: String, comment: String?): Observable<Boolean> {
    TPToastManager.show(TPRuntime.mainApplication.resources.getString(R.string.reblog))
    val observableOnSubscribe = object : ObservableOnSubscribe<Boolean> {
        override fun subscribe(e: ObservableEmitter<Boolean>) {
            e.onNext(true)
            e.onComplete()
        }
    }
    return Observable
            .create(observableOnSubscribe)
}

fun Post.blogAvatarAsync(callback: (Bitmap?) -> Unit) {
    object : AsyncTask<Void, Void, String?>() {
        override fun doInBackground(vararg args: Void): String? {
            return TPRuntime.avatarUrlCache.getIfNoneAndSet(blogName, {
                client.blogInfo(blogName).avatar()
            })
            // TODO エラー処理
        }

        override fun onPostExecute(avatarUrl: String?) {
            avatarUrl?.let {
                DownloadImageTask(callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, it)
            }
        }
    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
}

/**
 * PHOTOポストの写真に対して最適なサイズを取得する
 * 高画質写真設定がtrueの場合:
 *   画面解像度の幅を超える最小の画像を取得
 * 高画質写真設定がfalseの場合:
 *   幅500px以下の最大の画像を取得
 * 上記条件で取得できない場合:
 *   サイズリストの最大の画像を取得
 */
fun Photo.getBestSizeForScreen(metrics: DisplayMetrics): PhotoSize {
    val w = metrics.widthPixels
    val biggest = sizes.first()
    val optimal = sizes.lastOrNull { it.width >= w }
    val better = sizes.firstOrNull { it.width <= 500 }

    if (optimal != null && optimal != biggest) {
        Log.d("Util", "画面の解像度：${metrics.widthPixels}x${metrics.heightPixels}　採択した解像度：")
        sizes.forEach { Log.d("Util", it.debugString() + (if (it == optimal) " optimal" else if (it == better) " better" else "")) }
    }

    if (TPRuntime.settings.highResolutionPhoto) {
        return optimal ?: biggest
    } else {
        return better ?: biggest
    }
}

fun PhotoSize.debugString(): String {
    return "${width}x${height}"
}

fun ViewGroup.children(): List<View> {
    return (0 until childCount).map { getChildAt(it) }
}

fun <T> List<T>.enumerate(): List<Pair<Int, T>> {
    return (0 until size).map { it to get(it) }
}

fun Context.isOnline(): Boolean {
    val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connMgr.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

fun Context.onNetworkRestored(callback: () -> Unit): BroadcastReceiver {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.let {
                if (it.isOnline()) {
                    Log.d("Util", "インターネット接続が復活した")
                    callback()
                }
            }
        }
    }
    registerReceiver(receiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    return receiver
}

/**
 * バージョン名取得
 */
fun Context.getVersionName(): String {
    val pm = this.packageManager
    try {
        val packageInfo = pm.getPackageInfo(this.packageName, 0)
        return packageInfo.versionName
    } catch (e: Exception) {
        Log.e("Context", "not found version name")
        return ""
    }
}


fun List<Post>.lastNonAdId(): Long? {
    return findLast { it !is AdPost }?.id
}

fun AdRequest.Builder.configureForTest() {
    val deviceId = if (BuildConfig.ADMOB_TESTDEVICE.isEmpty()) {
        AdRequest.DEVICE_ID_EMULATOR
    } else {
        BuildConfig.ADMOB_TESTDEVICE
    }
    addTestDevice(deviceId)
}
