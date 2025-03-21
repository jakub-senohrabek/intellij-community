// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.diff.contents;

import com.intellij.diff.util.DiffUtil;
import com.intellij.diff.util.LineCol;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.util.diff.Diff;
import com.intellij.util.diff.FilesTooBigForDiffException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DocumentContentBase extends DiffContentBase implements DocumentContent {
  private final @Nullable Project myProject;
  private final @NotNull Document myDocument;

  public DocumentContentBase(@Nullable Project project,
                             @NotNull Document document) {
    myProject = project;
    myDocument = document;
  }

  public @Nullable Project getProject() {
    return myProject;
  }

  @Override
  public @NotNull Document getDocument() {
    return myDocument;
  }

  @Override
  public @Nullable Navigatable getNavigatable(@NotNull LineCol position) {
    if (!DiffUtil.canNavigateToFile(myProject, getHighlightFile())) return null;
    return new MyNavigatable(myProject, getHighlightFile(), getDocument(), position);
  }

  @Override
  public @Nullable Navigatable getNavigatable() {
    return getNavigatable(new LineCol(0));
  }

  @Override
  public String toString() {
    return super.toString() + ":" + myDocument;
  }

  private static class MyNavigatable implements Navigatable {
    private final @NotNull Project myProject;
    private final @NotNull VirtualFile myTargetFile;
    private final @NotNull Document myDocument;
    private final @NotNull LineCol myPosition;

    MyNavigatable(@NotNull Project project, @NotNull VirtualFile targetFile, @NotNull Document document, @NotNull LineCol position) {
      myProject = project;
      myTargetFile = targetFile;
      myDocument = document;
      myPosition = position;
    }

    @Override
    public void navigate(boolean requestFocus) {
      Document targetDocument = FileDocumentManager.getInstance().getDocument(myTargetFile);
      LineCol targetPosition = translatePosition(myDocument, targetDocument, myPosition);
      Navigatable descriptor = targetDocument != null
                               ? PsiNavigationSupport.getInstance().createNavigatable(myProject, myTargetFile,
                                                                                      targetPosition
                                                                                        .toOffset(targetDocument))
                               : new OpenFileDescriptor(myProject, myTargetFile, targetPosition.line, targetPosition.column);
      if (descriptor.canNavigate()) descriptor.navigate(true);
    }

    @Override
    public boolean canNavigate() {
      return myTargetFile.isValid();
    }

    private static @NotNull LineCol translatePosition(@NotNull Document fromDocument, @Nullable Document toDocument, @NotNull LineCol position) {
      try {
        if (toDocument == null) return position;
        int targetLine = Diff.translateLine(fromDocument.getCharsSequence(), toDocument.getCharsSequence(), position.line, true);
        return new LineCol(targetLine, position.column);
      }
      catch (FilesTooBigForDiffException ignore) {
        return position;
      }
    }
  }
}
