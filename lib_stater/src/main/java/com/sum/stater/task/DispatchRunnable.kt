package com.sum.stater.task

import com.sum.framework.log.LogUtil
import com.sum.stater.dispatcher.TaskDispatcher

/**
 * 任务真正执行的地方
 */
class DispatchRunnable : Runnable {
    private var mTask: Task
    private var mTaskDispatcher: TaskDispatcher? = null

    constructor(task: Task) {
        mTask = task
    }

    constructor(task: Task, dispatcher: TaskDispatcher?) {
        mTask = task
        mTaskDispatcher = dispatcher
    }

    override fun run() {

        mTask.isWaiting = true
        mTask.waitToSatisfy()


        // 执行Task
        LogUtil.d("${mTask::class.java.simpleName} 开始执行任务体")
        mTask.isRunning = true
        mTask.run()

        // 执行Task的尾部任务
        val tailRunnable = mTask.tailRunnable
        tailRunnable?.run()


        mTask.isFinished = true
        mTaskDispatcher?.let {
            it.satisfyChildren(mTask)
            it.markTaskDone(mTask)
        }

    }

}