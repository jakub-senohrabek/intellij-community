// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.idea.maven.navigator.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import org.jetbrains.idea.maven.indices.MavenSystemIndicesManager
import org.jetbrains.idea.maven.utils.MavenDataKeys


class IndexUpdateAction : DumbAwareAction() {

  override fun actionPerformed(e: AnActionEvent) {
    val mavenRepo = e.getData(MavenDataKeys.MAVEN_REPOSITORY) ?: return
    val project = e.project ?: return

    val manager = MavenSystemIndicesManager.getInstance()
    manager.updateIndexContent(mavenRepo, project)
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }
}