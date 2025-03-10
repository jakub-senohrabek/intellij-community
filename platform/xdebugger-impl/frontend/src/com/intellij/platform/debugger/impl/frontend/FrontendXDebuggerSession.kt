// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.platform.debugger.impl.frontend

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DataSink
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.platform.debugger.impl.frontend.evaluate.quick.FrontendXDebuggerEvaluator
import com.intellij.platform.debugger.impl.frontend.evaluate.quick.FrontendXValue
import com.intellij.platform.debugger.impl.frontend.evaluate.quick.createFrontendXDebuggerEvaluator
import com.intellij.platform.util.coroutines.childScope
import com.intellij.xdebugger.XDebugSessionListener
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.intellij.xdebugger.frame.XExecutionStack
import com.intellij.xdebugger.frame.XStackFrame
import com.intellij.xdebugger.frame.XSuspendContext
import com.intellij.xdebugger.impl.frame.XDebugSessionProxy
import com.intellij.xdebugger.impl.frame.XValueMarkers
import com.intellij.xdebugger.impl.rpc.*
import com.intellij.xdebugger.impl.ui.XDebugSessionData
import com.intellij.xdebugger.impl.ui.XDebugSessionTab
import com.intellij.xdebugger.ui.XDebugTabLayouter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

internal class FrontendXDebuggerSession(
  override val project: Project,
  scope: CoroutineScope,
  sessionDto: XDebugSessionDto,
) : XDebugSessionProxy {
  private val cs = scope.childScope("Session ${sessionDto.id}")
  private val localEditorsProvider = sessionDto.editorsProviderDto.editorsProvider
  val id = sessionDto.id
  val evaluator: StateFlow<FrontendXDebuggerEvaluator?> =
    channelFlow {
      XDebugSessionApi.getInstance().currentEvaluator(id).collectLatest { evaluatorDto ->
        if (evaluatorDto == null) {
          send(null)
          return@collectLatest
        }
        supervisorScope {
          val evaluator = createFrontendXDebuggerEvaluator(project, this, evaluatorDto)
          send(evaluator)
          awaitCancellation()
        }
      }
    }.stateIn(cs, SharingStarted.Eagerly, null)

  val sourcePosition: StateFlow<XSourcePosition?> =
    channelFlow {
      XDebugSessionApi.getInstance().currentSourcePosition(id).collectLatest { sourcePositionDto ->
        if (sourcePositionDto == null) {
          send(null)
          return@collectLatest
        }
        supervisorScope {
          send(sourcePositionDto.sourcePosition())
          awaitCancellation()
        }
      }
    }.stateIn(cs, SharingStarted.Eagerly, null)

  private val sessionState: StateFlow<XDebugSessionState> =
    channelFlow {
      XDebugSessionApi.getInstance().currentSessionState(id).collectLatest { sessionState ->
        send(sessionState)
      }
    }.stateIn(cs, SharingStarted.Eagerly, sessionDto.initialSessionState)

  val isStopped: Boolean
    get() = sessionState.value.isStopped

  val isPaused: Boolean
    get() = sessionState.value.isPaused

  val isReadOnly: Boolean
    get() = sessionState.value.isReadOnly

  val isPauseActionSupported: Boolean
    get() = sessionState.value.isPauseActionSupported

  val isSuspended: Boolean
    get() = sessionState.value.isSuspended

  val editorsProvider: XDebuggerEditorsProvider = localEditorsProvider
                                                  ?: FrontendXDebuggerEditorsProvider(id, sessionDto.editorsProviderDto.fileTypeId)

  val valueMarkers: XValueMarkers<FrontendXValue, XValueMarkerId> = FrontendXValueMarkers(project)

  init {
    cs.launch {
      XDebugSessionApi.getInstance().sessionTabInfo(id).collectLatest { tabDto ->
        if (tabDto == null) return@collectLatest
        initTabInfo(tabDto)
        this.cancel() // Only one tab expected
      }
    }
  }

  private fun initTabInfo(tabDto: XDebuggerSessionTabDto) {
    val (tabInfo, pausedFlow) = tabDto
    cs.launch {
      if (tabInfo !is XDebuggerSessionTabInfo) return@launch

      val proxy = createProxy(this@FrontendXDebuggerSession)
      withContext(Dispatchers.EDT) {
        XDebugSessionTab.create(proxy, tabInfo.icon, tabInfo.executionEnvironment, tabInfo.contentToReuse,
                                tabInfo.forceNewDebuggerUi, tabInfo.withFramesCustomization).apply {
          proxy.onTabInitialized(this)
          if (tabInfo.shouldShowTab) {
            showTab()
          }
          pausedFlow.toFlow().collectLatest { paused ->
            if (paused == null) return@collectLatest
            withContext(Dispatchers.EDT) {
              onPause(paused.pausedByUser, paused.topFrameIsAbsent)
            }
          }
        }
      }
    }
  }

  // TODO all of the methods below
  // TODO pass in DTO?
  override val sessionName: String
    get() = TODO("Not yet implemented")
  override val sessionData: XDebugSessionData?
    get() = TODO("Not yet implemented")
  override val consoleView: ConsoleView?
    get() = TODO("Not yet implemented")
  override val restartActions: List<AnAction>
    get() = TODO("Not yet implemented")
  override val extraActions: List<AnAction>
    get() = TODO("Not yet implemented")
  override val extraStopActions: List<AnAction>
    get() = TODO("Not yet implemented")
  override val processHandler: ProcessHandler
    get() = TODO("Not yet implemented")
  override val coroutineScope: CoroutineScope
    get() = TODO("Not yet implemented")

  override fun getFrameSourcePosition(frame: XStackFrame): XSourcePosition? {
    TODO("Not yet implemented")
  }

  override fun getCurrentExecutionStack(): XExecutionStack? {
    TODO("Not yet implemented")
  }

  override fun getCurrentStackFrame(): XStackFrame? {
    TODO("Not yet implemented")
  }

  override fun setCurrentStackFrame(executionStack: XExecutionStack, frame: XStackFrame, isTopFrame: Boolean) {
    TODO("Not yet implemented")
  }

  override fun hasSuspendContext(): Boolean {
    TODO("Not yet implemented")
  }

  override fun isSteppingSuspendContext(): Boolean {
    TODO("Not yet implemented")
  }

  override fun computeExecutionStacks(provideContainer: () -> XSuspendContext.XExecutionStackContainer) {
    TODO("Not yet implemented")
  }

  override fun createTabLayouter(): XDebugTabLayouter {
    TODO("Not yet implemented")
  }

  override fun addSessionListener(listener: XDebugSessionListener, disposable: Disposable) {
    TODO("Not yet implemented")
  }

  override fun rebuildViews() {
    TODO("Not yet implemented")
  }

  override fun registerAdditionalActions(leftToolbar: DefaultActionGroup, topLeftToolbar: DefaultActionGroup, settings: DefaultActionGroup) {
    TODO("Not yet implemented")
  }

  override fun putKey(sink: DataSink) {
    TODO("Not yet implemented")
  }

  override fun updateExecutionPosition() {
    TODO("Not yet implemented")
  }

  override fun onTabInitialized(tab: XDebugSessionTab) {
    TODO("Not yet implemented")
  }

  override suspend fun sessionId(): XDebugSessionId {
    TODO("Not yet implemented")
  }

  fun closeScope() {
    cs.cancel()
  }
}

private fun createProxy(session: FrontendXDebuggerSession): XDebugSessionProxy = session
