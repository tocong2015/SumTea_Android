package com.sum.stater.task

import java.util.concurrent.Executor

interface ITask {


    /**
     * 任务真正执行的地方
     */
    fun run()

    /**
     * Task执行所在的线程池，可指定，一般默认
     *
     * @return
     */
    fun runOn(): Executor?

    /**
     * 依赖关系
     *
     * @return
     */
    fun dependsOn(): List<Class<out Task?>?>?



    /**
     * 是否在主线程执行
     *
     * @return
     */
    fun runOnMainThread(): Boolean



    /**
     * Task主任务执行完成之后需要执行的任务
     *
     * @return
     */
    val tailRunnable: Runnable?



}

