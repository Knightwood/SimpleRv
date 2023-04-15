package com.kiylx.recyclerviewneko

import android.content.Context
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.kiylx.recyclerviewneko.nekoadapter.NekoAdapter
import com.kiylx.recyclerviewneko.nekoadapter.NekoListAdapter
import com.kiylx.recyclerviewneko.nekoadapter.NekoPagingAdapter
import com.kiylx.recyclerviewneko.nekoadapter.config.BaseConfig
import com.kiylx.recyclerviewneko.nekoadapter.config.ConcatConfig
import com.kiylx.recyclerviewneko.nekoadapter.config.DefaultConfig
import com.kiylx.recyclerviewneko.viewholder.ItemViewDelegate
import com.kiylx.recyclerviewneko.nekoadapter.config.ViewTypeParser
import com.kiylx.recyclerviewneko.viewholder.BaseViewHolder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers


/**
 * # 根据配置，生成recyclerview
 * ## 一、添加viewholder
 * 1. 单一viewholder：使用[BaseConfig.addItemView]方法
 * 2. 多种类型的viewholder：使用[BaseConfig.addItemViews]方法
 *
 * ## 二、 若有多种viewholder,，指定不同的viewtype方式
 * * 方式一：重写[ItemViewDelegate]中的[ItemViewDelegate.isForViewType]方法
 * * 方式二：实现[ViewTypeParser]
 * * 若使用了方式二,则方式一不起作用。
 *
 */
fun <T : Any> Context.neko(
    recyclerView: RecyclerView,
    configBlock: DefaultConfig<T>.() -> Unit
): BaseConfig<T> {
    val config = DefaultConfig<T>(this, recyclerView)
    val a = NekoAdapter(config)
    config.iNekoAdapter = a
    config.nekoAdapter = a
    config.configBlock()
    return config
}

/**
 * 根据配置，生成NekoListAdapter
 * 若没有指定[asyncConfig]，则用[diffCallback]参数创建NekoListAdapter
 */
fun <T : Any> Context.listNeko(
    recyclerView: RecyclerView,
    asyncConfig: AsyncDifferConfig<T>? = null,
    diffCallback: DiffUtil.ItemCallback<T>,
    configBlock: DefaultConfig<T>.() -> Unit
): DefaultConfig<T> {
    val config = DefaultConfig<T>(this, recyclerView)
    val a = asyncConfig?.let { NekoListAdapter(config, it) }
        ?: NekoListAdapter(config, diffCallback)
    config.iNekoAdapter = a
    config.nekoListAdapter = a
    config.configBlock()
    return config
}

/**
 * 根据配置，生成NekoPagingAdapter
 */
fun <T : Any> Context.paging3Neko(
    recyclerView: RecyclerView,
    diffCallback: DiffUtil.ItemCallback<T>,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    workerDispatcher: CoroutineDispatcher = Dispatchers.Default,
    configBlock: DefaultConfig<T>.() -> Unit
): DefaultConfig<T> {
    val config = DefaultConfig<T>(this, recyclerView)
    val a = NekoPagingAdapter(config, diffCallback, mainDispatcher, workerDispatcher)
    config.iNekoAdapter = a
    config.nekoPagingAdapter = a
    config.configBlock()
    return config
}

fun <T : Any> Context.customNeko(
    recyclerView: RecyclerView,
    customAdapter: Adapter<BaseViewHolder>,
    configBlock: DefaultConfig<T>.() -> Unit
): DefaultConfig<T> {
    val config = DefaultConfig<T>(this, recyclerView)
    config.iNekoAdapter = customAdapter
    config.configBlock()
    return config
}

fun <T : Any, N : BaseConfig<T>> N.done(): N {
    rv.adapter = iNekoAdapter
    rv.layoutManager = layoutManager
    return this
}

/**
 * 传入多个adapter,将会按照次序连接。
 * @param nekoConfigs 构建出来的多个adapter。注：传入的nekoConfigs不应调用BaseConfig的done方法
 * @param configBlock 配置[ConcatAdapter.Config]
 */
fun <T : Any, N : BaseConfig<T>> N.concat(
    vararg nekoConfigs: N,
    configBlock: ConcatAdapter.Config.Builder.() -> Unit,
    ): ConcatConfig<T, N> {
    val c = ConcatConfig(configList = nekoConfigs)
    c.config.configBlock()
    c.done()
    return c
}

/*
# 如何获取ViewHolder的position

通常来讲我们使用getAdapterPosition()获取viewholder在列表中的位置，用于埋点或者其他一些操作，
但是现在getAdapterPosition()你会发现已经标记过时了，这是因为引入了ConcatAdapter后，这个方法已经产生了歧义。Google提供了2个新的方法

 1.   getBindingAdapterPosition()——获取当前绑定Adapter中的位置
 2.   getAbsoluteAdapterPosition()——获取总列表中的绝对位置

# concatAdapter
1. isolateViewTypes的含义

我们都知道RecyclerViewPool中是根据itemViewType缓存ViewHolder的，相同的itemViewType对应的缓存池相同，否则不同。
而isolateViewTypes是用来隔离itemViewType的，如果isolateViewTypes=true，即使ConcatAdapter的子Adapter中的itemViewType相同，
对于ConcatAdapter来说它们的itemViewType也是不同的。从缓存角度来看，即使使用两个相同的Adapter，它们也不能共用一个缓存池。
反之，isolateViewTypes=false，如果itemViewType相同，则使用同一个缓存池。

 */