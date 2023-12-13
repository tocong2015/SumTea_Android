package com.sum.stater.dispatcher

import android.app.Application
import android.os.Looper
import androidx.annotation.UiThread
import com.sum.stater.sort.TaskSortUtil
import com.sum.stater.task.DispatchRunnable
import com.sum.stater.task.Task
import com.sum.stater.utils.StaterUtils
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 启动器调用类
 */
class TaskDispatcher private constructor() {
    private var mStartTime: Long = 0
    private val mFutures: MutableList<Future<*>> = ArrayList()
    private var mAllTasks: MutableList<Task> = ArrayList()
    private val mClsAllTasks: MutableList<Class<out Task>> = ArrayList()

    @Volatile
    private var mMainThreadTasks: MutableList<Task> = ArrayList()
    private var mCountDownLatch: CountDownLatch? = null

    //保存需要Wait的Task的数量
    private val mNeedWaitCount = AtomicInteger()

    //调用了await的时候还没结束的且需要等待的Task
    private val mNeedWaitTasks: MutableList<Task> = ArrayList()

    //已经结束了的Task
    @Volatile
    private var mFinishedTasks: MutableList<Class<out Task>> = ArrayList(100)
    private val mDependedHashMap = HashMap<Class<out Task>, ArrayList<Task>?>()


    fun addTask(task: Task?): TaskDispatcher {
        task?.let {
            collectDepends(it)
            mAllTasks.add(it)
            mClsAllTasks.add(it.javaClass)
            // 主线程运行的task不需要CountDownLatch也是同步的,
            // 非主线程运行的task,需要记录一下，因为最后要等待所有的非主线程运行task运行完，再继续往下执行
            if (ifNeedWait(it)) {
                mNeedWaitTasks.add(it)
                mNeedWaitCount.getAndIncrement()
            }
        }
        return this
    }

    private fun collectDepends(task: Task) {
        task.dependsOn()?.let { list ->
            for (cls in list) {
                cls?.let {
                    //要完成c,就先完成 a,b,
                    // a ---> c
                    // b ---> c
                    if (mDependedHashMap[it] == null) {
                        mDependedHashMap[it] = ArrayList()
                    }
                    mDependedHashMap[it]?.add(task)
                    //如果被依赖的任务完成了，那么主任务就要 countDown
                    if (mFinishedTasks.contains(it)) {
                        task.satisfy()
                    }
                }
            }
        }
    }

    private fun ifNeedWait(task: Task): Boolean {
        return !task.runOnMainThread()
    }

    @UiThread
    fun start() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw RuntimeException("must be called from UiThread")
        }

        if (mAllTasks.isNotEmpty()) {
            mAllTasks = TaskSortUtil.getSortResult(mAllTasks, mClsAllTasks).toMutableList()
            mCountDownLatch = CountDownLatch(mNeedWaitCount.get())
            sendAndExecuteAsyncTasks()
            executeTaskMain()
        }

    }

    fun cancel() {
        for (future in mFutures) {
            future.cancel(true)
        }
    }

    /**
     * 执行所有的 需在主线程运行的 task
     * */
    private fun executeTaskMain() {
        mStartTime = System.currentTimeMillis()
        for (task in mMainThreadTasks) {
            DispatchRunnable(task, this).run()
        }

    }

    /**
     * 分别存储 需在主线程运行 和 需在子线程运行的task
     * 需在子线程运行的task存储前就执行
     * */
    private fun sendAndExecuteAsyncTasks() {
        for (task in mAllTasks) {
            sendTaskReal(task)
            task.isSend = true
        }
    }


    /**
     * 通知Children一个前置任务已完成
     *
     * @param launchTask
     */
    fun satisfyChildren(launchTask: Task) {
        val arrayList = mDependedHashMap[launchTask.javaClass]
        if (!arrayList.isNullOrEmpty()) {
            for (task in arrayList) {
                task.satisfy()
            }
        }
    }

    fun markTaskDone(task: Task) {
        if (ifNeedWait(task)) {
            mFinishedTasks.add(task.javaClass)
            mNeedWaitTasks.remove(task)
            mCountDownLatch?.countDown()
            mNeedWaitCount.getAndDecrement()
        }
    }

    private fun sendTaskReal(task: Task) {
        if (task.runOnMainThread()) {
            mMainThreadTasks.add(task)

        } else {
            // 直接发，是否执行取决于具体线程池
            val future = task.runOn()?.submit(DispatchRunnable(task, this))
            future?.let {
                mFutures.add(it)
            }
        }
    }

    fun executeTask(task: Task) {
        if (ifNeedWait(task)) {
            mNeedWaitCount.getAndIncrement()
        }
        task.runOn()?.execute(DispatchRunnable(task, this))
    }


    /**
     * 等待所有的需在子线程运行的task执行完毕
     * */
    @UiThread
    fun await() {
        try {
            if (mNeedWaitCount.get() > 0) {
                mCountDownLatch?.await(WAIT_TIME.toLong(), TimeUnit.MILLISECONDS)
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val WAIT_TIME = 10000
        var context: Application? = null
            private set
        var isMainProcess = false
            private set

        @Volatile
        private var sHasInit = false

        fun init(context: Application?) {
            context?.let {
                Companion.context = it
                sHasInit = true
                isMainProcess = StaterUtils.isMainProcess(context)
            }
        }

        /**
         * 注意：每次获取的都是新对象
         *
         * @return
         */
        fun createInstance(): TaskDispatcher {
            if (!sHasInit) {
                throw RuntimeException("must call TaskDispatcher.init first")
            }
            return TaskDispatcher()
        }
    }
}