package com.kiylx.recyclerviewneko.wrapper.loadstate

import android.content.Context
import android.util.Log
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.kiylx.recyclerviewneko.utils.RecyclerViewScrollListener

/**
 * 生成header和footer（其实就是持有一个itemview的LoadStateAdapter），
 * 通过ConcatAdapter包装给普通Adapter
 */
class NekoAdapterLoadStatusWrapperUtil(
    val rv: RecyclerView,
    //需要添加footer或header的原始adapter
    val adapter: Adapter<out RecyclerView.ViewHolder>,
    val context: Context
) {
    internal var header: NekoPaging3LoadStatusAdapter? = null
    internal var footer: NekoPaging3LoadStatusAdapter? = null

    //给普通adapter通过使用concatAdapter的方式添加header和footer
    lateinit var concatAdapter: ConcatAdapter
    var whenEnd: (() -> Unit)? = null
    var whenTop: (() -> Unit)? = null
    var whenNotFull: (() -> Unit)? = null

    /**
     * 给pagingAdapter添加状态加载状态item
     * 生成一个包含itemview的NekoPaging3LoadStatusAdapter
     * @param block 配置NekoPaging3LoadStatusAdapter，添加itemview
     */
    fun withFooter(block: Paging3LoadStatusConfig.() -> Unit) {
        val tmp = Paging3LoadStatusConfig()
        tmp.block()
        footer = NekoPaging3LoadStatusAdapter(tmp)
    }

    /**
     * 给pagingAdapter添加状态加载状态item
     * 生成一个包含itemview的NekoPaging3LoadStatusAdapter
     * @param block 配置NekoPaging3LoadStatusAdapter，添加itemview
     */
    fun withHeader(block: Paging3LoadStatusConfig.() -> Unit) {
        val tmp = Paging3LoadStatusConfig()
        tmp.block()
        header = NekoPaging3LoadStatusAdapter(tmp)
    }

    /**
     * 完成包装，且监听recyclerview的滑动，在滑动到底部时，触发[whenEnd]的回调
     *
     */
    fun completeConfig(): NekoAdapterLoadStatusWrapperUtil {
        if (this::concatAdapter.isInitialized) {
            throw Exception("已经初始化完成")
        } else {
            // 1. 定义Config
            val config = ConcatAdapter.Config.Builder()
                .setIsolateViewTypes(true)
                .setStableIdMode(ConcatAdapter.Config.StableIdMode.NO_STABLE_IDS)
                .build()
            header?.let { it1 ->
                footer?.let { it2 ->
                    concatAdapter = ConcatAdapter(config, it1, adapter, it2)
                } ?: let {
                    concatAdapter = ConcatAdapter(config, it1, adapter)
                }
            } ?: let {
                footer?.let { it2 ->
                    concatAdapter = ConcatAdapter(config, adapter, it2)
                } ?: let {
                    throw Exception("header and footer does not exist!!")
                }
            }
            rv.addOnScrollListener(object : RecyclerViewScrollListener(rv) {
                override fun onScrollToDataEnd() {
                    Log.d("tag", "scroll to onScrollToDataEnd")
                    footer?.let {
                        if (it.config.autoLoading) {
                            it.loadState = LoadState.Loading
                        }
                        finish(it, true, it.config.autoClose)//自动关闭状态
                        whenEnd?.invoke()
                    }
                }

                override fun onScrollToDataStart() {
//                    Log.d("tag", "scroll to onScrollToDataStart")
                    header?.let {
                        if (it.config.autoLoading) {
                            it.loadState = LoadState.Loading
                        }
                        finish(it, true, it.config.autoClose)//自动关闭状态
                        whenTop?.invoke()
                    }
                }

                override fun onDataNotFull() {
                    whenNotFull?.invoke()
                }
            })
        }
        return this
    }

    /**
     * 为rv设置adapter
     */
    fun done(): NekoAdapterLoadStatusWrapperUtil {
        rv.adapter = concatAdapter
        return this
    }


    /**
     * 设置当滑动到底部时的回调监听
     */
    fun whenScrollToEnd(block: () -> Unit) {
        this.whenEnd = block
    }

    /**
     * 设置当滑动到顶部时的回调监听
     */
    fun whenScrollToTop(block: () -> Unit) {

        this.whenTop = block
    }

    /**
     * 当数据不满一屏，手指滑动时触发
     */
    fun whenNotFull(block: () -> Unit) {
        this.whenNotFull = block
    }

    /**
     * 通过此方法改变header的itemview状态
     */
    fun headerState(loadState: LoadState) {
        header?.let {
            it.loadState = loadState
        } ?: throw Exception("header does not exist")
    }

    /**
     * 通过此方法改变footer的itemview状态
     */
    fun footerState(loadState: LoadState) {
        footer?.let {
            it.loadState = loadState
        } ?: throw Exception("footer does not exist")
    }

    /**
     * @param b: 数据加载是否成功
     * @param stateAdapter 要关闭状态的adapter
     * @param time 多久后关闭
     */
    fun finish(stateAdapter: NekoPaging3LoadStatusAdapter, b: Boolean, time: Long) {
        if (time > 0) {
            rv.handler.postDelayed(Runnable {
                stateAdapter.loadState =
                    if (b) {
                        LoadState.NotLoading(endOfPaginationReached = true)
                    } else {
                        LoadState.NotLoading(endOfPaginationReached = false)
                    }
            }, time)
        }
    }
}