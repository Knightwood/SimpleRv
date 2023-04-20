# RecyclerviewNeko

* 支持多种adapter,也可以自己定义新的adapter传入
* 没有侵入和魔改
* 支持状态页回调
* 支持下拉显示header和上拉显示footer,上拉加载等

# 使用

* 版本 [![](https://jitpack.io/v/Knightwood/RecyclerViewNeko.svg)](https://jitpack.io/#Knightwood/RecyclerViewNeko)

  ```css
  dependencies {
  	   implementation 'com.github.Knightwood:RecyclerViewNeko:Tag'
  }
  ```

## 快速开始

```
		//预定义数据
        val d: MutableList<String> = mutableListOf()
        d.addAll(listOf("a", "b", "c", "item"))

        //泛型指定了此recyclerview显示什么类型的数据
        val neko = neko<String>(rv) {
            //内置默认LinearLayoutManager,可以在这里修改
            //layoutManager=GridLayoutManager(this@MainActivity2,2) //替换默认的布局管理器
            layoutManager.apply {
                //修改布局管理器的配置
            }
            
            //添加"viewholder"
            addSingleItemView(R.layout.item_1) { holder, data, position ->
            	//数据绑定到viewholder
                holder.getView<TextView>(R.id.tv1)?.text = data.toString()
            }
            // .....
            
        }.show(d)//调用show方法完成recycleview的显示
        
        
        //刷新数据
        (neko.iNekoAdapter as NekoAdapter).notifyItemChanged(1)
        //刷新数据
        neko.refreshData(d)
        //刷新数据
        neko.nekoAdapter.notifyItemChanged(3)
```



## 示例

```
class MainActivity2 : AppCompatActivity() {
    lateinit var handler: Handler
    lateinit var rv: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        handler = Handler(Looper.getMainLooper())
        rv = findViewById<View>(R.id.rv) as RecyclerView
    }

    /**
     * 代替viewholder
     */
    inner class Delegate1 : ItemViewDelegate<String>(1, R.layout.item_1) {
        /**
         * 绑定数据到viewholder
         */
        override fun convert(holder: BaseViewHolder, data: String, position: Int) {
            holder.getView<TextView>(R.id.tv1)?.text = data.toString()
        }
    }

    inner class Delegate2 : ItemViewDelegate<String>(2, R.layout.item_2) {
        //在有多种viewholder时，
        //此方法的作用是判断此viewholder是否应该显示某数据类型的数据
        //如果此viewholder应该显示此数据类型数据，就让他返回true
        //如果指定viewTypeParser，则这里不起作用,因此指定了viewTypeParser可以不重写此方法
        override fun isForViewType(data: String, position: Int): Boolean {
            //此类型的viewholder显示字符串是"item"的
            return data == "item"
        }

        override fun convert(holder: BaseViewHolder, data: String, position: Int) {
            holder.getView<TextView>(R.id.tv2)?.text = data.toString()
        }

    }

    /**
     * 普通的adapter，多种viewtype
     */
    fun nekoTest() {
        //预定义数据
        val d: MutableList<String> = mutableListOf()
        d.addAll(listOf("a", "b", "c", "item"))

        //两个viewholder类型
        val item1 = Delegate1()
        val item2 = Delegate2()

        //泛型指定了此recyclerview显示什么类型的数据
        val neko = neko<String>(rv) {
            //内置默认LinearLayoutManager,可以在这里修改
            //layoutManager=GridLayoutManager(this@MainActivity2,2) //替换默认的布局管理器
            layoutManager.apply {
                //修改布局管理器的配置
            }

            //在有多种viewholder时，根据数据类型返回不同的viewtype
            //当不指定这个解析器时，就得重写ItemViewDelegate中的isForViewType方法来判断viewtype
            viewTypeParser = ViewTypeParser<String> { data, pos ->
                if (data == "item") 1 else 2
            }

            mDatas = d.toMutableList()//指定adapter的数据。也可以现在不指定数据，在后面的show方法中传入数据

            //多种viewtype可以使用[addItemViews]将多种viewholder添加进去
            addItemViews(item1, item2)
            
            //可以像上面批量添加或者像这样一个个添加
            addItemView(R.layout.item_1, 1, isThisView = { _, _ ->
                //像上面那样有viewTypeParser的话，isThisView参数可以不写
                return@addItemView true
            }) { holder, data, position ->
                //数据绑定到viewholder
            }
            

            //给整个itemview设置点击事件
            itemClickListener = ItemClickListener { view, holder, position ->
                Toast.makeText(applicationContext, mDatas[position], Toast.LENGTH_LONG).show()
            }
            //设置长按事件
            itemLongClickListener = ItemLongClickListener { view, holder, position ->
                Toast.makeText(applicationContext, mDatas[position], Toast.LENGTH_LONG).show()
                true
            }

        }.show()//调用show方法完成recycleview的显示

        neko.mDatas[1] = "eee"
        //刷新数据
        (neko.iNekoAdapter as NekoAdapter).notifyItemChanged(1)
        //刷新数据
        neko.refreshData(d)
        //刷新数据
        neko.nekoAdapter.notifyItemChanged(3)
    }

    /**
     * 仅有一种viewHolder的示例
     */
    fun nekoSingleTest() {
        //预定义数据
        val d: MutableList<String> = mutableListOf()
        d.addAll(listOf("a", "b", "c", "item"))

        //泛型指定了此recyclerview显示什么类型的数据
        val neko = neko<String>(rv) {
            // .....
            //仅有一种viewHolder,
            //不需要指定viewTypeParser
            //不需要重写ItemViewDelegate中的isForViewType方法来判断ViewType
            //仅添加"viewholder"
            addSingleItemView(R.layout.item_1) { holder, data, position ->
                holder.getView<TextView>(R.id.tv1)?.text = data.toString()
            }
            // .....
        }.show(d)//调用show方法完成recycleview的显示

    }

    /**
     * listadapter，多种viewtype
     */
    fun listNekoTest() {
        val d: MutableList<String> = mutableListOf<String>()
        val item1 = Delegate1()
        val item2 = Delegate2()
        val myDiffCallback = object : DiffUtil.ItemCallback<String>() {
            //指定diffCallback
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                TODO("Not yet implemented")
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                TODO("Not yet implemented")
            }
        }

        val neko = listNeko<String>(
            recyclerView = rv,
            // 若没有指定[asyncConfig]，则用[diffCallback]参数创建NekoListAdapter
            // 若指定了[asyncConfig]，则[diffCallback]参数不起作用
            //diffCallback和asyncConfig是listAdapter的构造函数参数，不明白去看下ListAdapter
            diffCallback = myDiffCallback,
            asyncConfig = AsyncDifferConfig.Builder<String>(myDiffCallback).build()
            ) {
                //这部分代码跟上面一样
        }.show()


        neko.mDatas[1] = "eee"
        //刷新数据
        neko.submitList(d)
        //刷新数据
        neko.nekoListAdapter.submitList(null)
    }

    /**
     * pagingDataAdapter
     */
    fun pagingTest() {
      val pagingAdapter=  paging3Neko(rv, object : DiffUtil.ItemCallback<String>() {
            //这里指定了数据是否相同的判断。不明白的再去学一遍pagingDataAdapter。
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                TODO("Not yet implemented")
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                TODO("Not yet implemented")
            }
        }) {
                //这里跟其他的几种情况一模一样，不再重复写
        }
        //刷新数据
        pagingAdapter.nekoPagingAdapter.notifyItemChanged(3)
    }

    /**
     * concatAdapter
     */
    fun concatTest() {

        //预定义数据
        val d1: MutableList<String> = mutableListOf()
        d1.addAll(listOf("a", "b", "c", "item"))

        //预定义数据
        val d2: MutableList<String> = mutableListOf()
        d2.addAll(listOf("a", "b", "c", "item"))

        val neko1 = neko<String>(rv) {
            mDatas = d1.toMutableList()//指定adapter的数据
            //仅有一种viewHolder
            addSingleItemView(R.layout.item_1) { holder, data, position ->
                holder.getView<TextView>(R.id.tv1)?.text = data.toString()
            }
        }
        val neko2 = neko<String>(rv) {
            mDatas = d2.toMutableList()//指定adapter的数据
            //仅有一种viewHolder
            addSingleItemView(R.layout.item_2) { holder, data, position ->
                holder.getView<TextView>(R.id.tv2)?.text = data.toString()
            }
        }
        //将两个adapter合并，此方法可以传入更多的adapter进行合并
        concatNeko(neko1, neko2) {
            // todo 自定义配置
        }.show()
        
        //刷新数据
        neko1.nekoAdapter.notifyItemChanged(2)
    }

 	/**
     * 添加状态页回调
     */
    fun wrapperTest() {
        //预定义数据
        val d1: MutableList<String> = mutableListOf()
        d1.addAll(listOf("a", "b", "c", "item"))

        //预定义数据
        val d2: MutableList<String> = mutableListOf()
        d2.addAll(listOf("a", "b", "c", "item"))

        val neko1 = neko<String>(rv) {
            mDatas = d1.toMutableList()//指定adapter的数据
            //仅有一种viewHolder
            addSingleItemView(R.layout.item_1) { holder, data, position ->
                holder.getView<TextView>(R.id.tv1)?.text = data.toString()
            }
        }
        val neko2 = neko<String>(rv) {
            mDatas = d2.toMutableList()//指定adapter的数据
            //仅有一种viewHolder
            addSingleItemView(R.layout.item_2) { holder, data, position ->
                holder.getView<TextView>(R.id.tv2)?.text = data.toString()
            }
        }

        //将两个adapter合并，此方法可以传入更多的adapter进行合并
        val concat = concatNeko(neko1, neko2) {
            // todo 自定义配置
        }.show()

        //对现有的recyclerview添加状态页
        val w =concat.wrapper{
            //例如设置空布局
            setEmpty(R.layout.empty) {
                it.setOnClickListener {
                    Log.d(tag, "被点击")
                }
            }
        }
        //1s后显示空布局
        handler.postDelayed(Runnable {
            w.showEmpty()
        }, 1000)
        //2s后由空布局转换到内容
        handler.postDelayed(Runnable {
            w.showContent()
        }, 2000)

        neko1.mDatas[1] = "olskdfbsf"
        neko1.nekoAdapter.notifyItemChanged(1)

    }
    
    
     /**
     * header和footer
     */
    fun loadStateTest() {
        //预定义数据
        val d1: MutableList<String> = mutableListOf()
        d1.addAll(listOf("a", "b", "c", "item"))

        //预定义数据
        val d2: MutableList<String> = mutableListOf()
        d2.addAll(listOf("a", "b", "c", "item"))

        val neko1 = neko<String>(rv) {
            mDatas = d1.toMutableList()//指定adapter的数据
            //仅有一种viewHolder
            setSingleItemView(R.layout.item_1) { holder, data, position ->
                holder.getView<TextView>(R.id.tv1)?.text = data.toString()
            }
        }.show()

        //给rv添加上header和footer，
        neko1.withLoadStatus {
            //配置footer
            withFooter {
                //footer配置成一个跟loadstate没关联的itemview
                setItemDelegate(com.kiylx.libx.R.layout.footer_item) { header, loadstate ->
                    //itemview绑定
                }
            }
            //当滑动到底部时
            whenScrollToEnd {
                //改变footer的状态
                footerState(LoadState.Loading)
                Log.d(tag, "scroll to end")
                handler.postDelayed(Runnable {
                    footerState(LoadState.NotLoading(true))
                }, 1000)
            }
            done()
        }

        neko1.mDatas[1] = "olskdfbsf"
        neko1.nekoAdapter.notifyItemChanged(1)

    }

}
```
