package com.kiylx.recyclerviewneko.wrapper.loadstate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import com.kiylx.recyclerviewneko.viewholder.BaseViewHolder

/**
 * 继承LoadStateAdapter，简化并统一构造LoadStateAdapter的方式
 */
class NekoPaging3LoadStatusAdapter(var config: Paging3LoadStatusConfig) :
    LoadStateAdapter<BaseViewHolder>() {

    override fun onBindViewHolder(holder: BaseViewHolder, loadState: LoadState) {
        config.onBindViewHolder(holder, loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): BaseViewHolder {
        return config.onCreateViewHolder(parent, loadState)
    }

}

/**
 * NekoPagingStatusAdapter中的创建和绑定ViewHolder委托给此类
 * 此类带承担着添加不同的itemview的职责
 */
class Paging3LoadStatusConfig() {
    private val stateHashMap: HashMap<LoadState, Paging3LoadStatusItemViewWrapper> = hashMapOf()

    //内部使用，用于添加状态页
    var innerDelegates: Paging3LoadStatusItemViewWrapper? = null

    /**
     * 是否以loadState区分不同的viewHolder
     */
    var useType = false

    internal fun onBindViewHolder(holder: BaseViewHolder, loadState: LoadState) {
        val itemViewWrapper: Paging3LoadStatusItemViewWrapper? = if (useType) {
            stateHashMap[loadState] ?: innerDelegates
        } else {
            innerDelegates
        }
        itemViewWrapper?.itemViewDelegate?.convert(holder, loadState) ?:throw Exception("没有与状态相关的itemview且没有默认itemview")
    }

    /**
     * 如果有添加根loadState关联的视图，
     */
    internal fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): BaseViewHolder {
        val itemViewWrapper: Paging3LoadStatusItemViewWrapper? = if (useType) {
            stateHashMap[loadState] ?: innerDelegates
        } else {
            innerDelegates
        }
        itemViewWrapper?.let {
            val view =
                LayoutInflater.from(parent.context).inflate(itemViewWrapper.layoutId, parent, false)
            val holder = BaseViewHolder(parent.context, view)
            return holder
        }?: throw Exception("没有与状态相关的itemview且没有默认itemview")

    }

    /**
     * 对某种状态添加itemview，若是没有调用[setItemDelegate]，若是缺失与某种状态对应的itemview，会抛出异常
     * 因此，可以仅调用[setItemDelegate]或混合调用，以确保没有与状态对应的itemview时的处理
     * 若重复添加，会替换之前已经添加的itemview
     */
    fun addItemDelegate(
        loadState: LoadState,
        layoutId: Int,
        dataConvert: (holder: BaseViewHolder, loadState: LoadState) -> Unit
    ) {
        useType = true
        stateHashMap[loadState] =
            Paging3LoadStatusItemViewWrapper(
                layoutId, loadState
            ) { h, ls ->
                dataConvert(h, ls)
            }
    }

    /**
     * 添加没有loadState关联的ItemViewDelegate
     */
    fun setItemDelegate(
        layoutId: Int,
        dataConvert: (holder: BaseViewHolder, loadState: LoadState) -> Unit
    ) {
        innerDelegates =
            Paging3LoadStatusItemViewWrapper(layoutId) { h, ls ->
                dataConvert(h, ls)
            }

    }


}

data class Paging3LoadStatusItemViewWrapper(
    val layoutId: Int,
    val loadState: LoadState? = null,
    val itemViewDelegate: Paging3LoadStatusItemViewDelegate
)

/**
 * 抽象然后取代bindViewHolder
 */
fun interface Paging3LoadStatusItemViewDelegate {
    fun convert(holder: BaseViewHolder, loadState: LoadState)
}