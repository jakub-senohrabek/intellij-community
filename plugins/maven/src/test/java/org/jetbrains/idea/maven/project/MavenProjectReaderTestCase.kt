// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.idea.maven.project

import com.intellij.maven.testFramework.MavenTestCase
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.idea.maven.model.MavenExplicitProfiles
import org.jetbrains.idea.maven.model.MavenId
import org.jetbrains.idea.maven.model.MavenModel
import org.jetbrains.idea.maven.server.MavenEmbedderWrapper
import org.jetbrains.idea.maven.server.MavenServerManager
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

abstract class MavenProjectReaderTestCase : MavenTestCase() {
  protected suspend fun readProject(file: VirtualFile, vararg profiles: String): MavenModel {
    val readResult = readProject(file, NullProjectLocator(), *profiles)
    assertProblems(readResult)
    return readResult.mavenModel
  }

  protected suspend fun readProject(file: VirtualFile,
                                    locator: MavenProjectReaderProjectLocator,
                                    vararg profiles: String): MavenProjectReaderResult {
    val mavenEmbedderWrappers = MavenEmbedderWrappersTestImpl(project)
    val reader = MavenProjectReader(project, mavenEmbedderWrappers, mavenGeneralSettings, MavenExplicitProfiles(listOf(*profiles)), locator)
    val result = mavenEmbedderWrappers.use { reader.readProjectAsync(file) }
    return result
  }

  protected class NullProjectLocator : MavenProjectReaderProjectLocator {
    override fun findProjectFile(coordinates: MavenId): VirtualFile? {
      return null
    }
  }

  protected fun assertProblems(readerResult: MavenProjectReaderResult, vararg expectedProblems: String?) {
    val actualProblems: MutableList<String?> = ArrayList()
    for (each in readerResult.readingProblems) {
      actualProblems.add(each.description)
    }
    assertOrderedElementsAreEqual(actualProblems, *expectedProblems)
  }
}

internal class MavenEmbedderWrappersTestImpl(private val myProject: Project) : MavenEmbedderWrappers {
  private val myEmbedders = ConcurrentHashMap<Path, MavenEmbedderWrapper>()

  override fun getAlwaysOnlineEmbedder(baseDir: String) = getEmbedder(Path.of(baseDir), true)

  override fun getEmbedder(baseDir: Path) = getEmbedder(baseDir, false)

  private fun getEmbedder(baseDir: Path, alwaysOnline: Boolean): MavenEmbedderWrapper {
    val embedderDir = baseDir.toString()
    return myEmbedders.computeIfAbsent(baseDir) {
      MavenServerManager.getInstance().createEmbedder(myProject, alwaysOnline, embedderDir)
    }
  }

  override fun close() {
    myEmbedders.values.forEach { it.release() }
    myEmbedders.clear()
  }
}
