package com.sum.stater.task

import android.content.Context
import com.sum.framework.log.LogUtil
import com.sum.stater.dispatcher.TaskDispatcher.Companion.context
import com.sum.stater.utils.DispatcherExecutor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService

/**
 * 任务抽象类
 */
abstract class Task : ITask {
    protected var mContext: Context? = context

    // 是否正在等待
    @Volatile
    var isWaiting = false

    // 是否正在执行
    @Volatile
    var isRunning = false

    // Task是否执行完成
    @Volatile
    var isFinished = false

    // Task是否已经被分发
    @Volatile
    var isSend = false

    // 当前Task依赖的Task数量（需要等待被依赖的Task执行完毕才能执行自己），默认没有依赖
    private val mDepends = CountDownLatch(
        dependsOn()?.size ?: 0
    )

    /**
     * 当前Task等待，让依赖的Task先执行
     */
    fun waitToSatisfy() {
        try {
            LogUtil.d("${this::class.java.simpleName}开始等待")
            mDepends.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /**
     * 依赖的Task执行完一个
     */
    fun satisfy() {
        mDepends.countDown()
    }

    /**
     * 是否需要尽快执行，解决特殊场景的问题：一个Task耗时非常多但是优先级却一般，很有可能开始的时间较晚，
     * 导致最后只是在等它，这种可以早开始。
     *
     * @return
     */
    fun needRunAsSoon(): Boolean {
        return false
    }


    /**
     * Task执行在哪个线程池，默认在IO的线程池；
     * CPU 密集型的一定要切换到DispatcherExecutor.getCPUExecutor();
     *
     * @return
     */
    override fun runOn(): ExecutorService? {
        return DispatcherExecutor.iOExecutor
    }



    /**
     * 当前Task依赖的Task集合（需要等待被依赖的Task执行完毕才能执行自己），默认没有依赖
     *
     * @return
     */
    override fun dependsOn(): List<Class<out Task?>?>? {
        return null
    }

    /**
     * 运行在主线程
     */
    override fun runOnMainThread(): Boolean {
        return false
    }


    override val tailRunnable: Runnable?
        get() = null



}
