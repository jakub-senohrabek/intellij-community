// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.siyeh.ig.bugs;

import com.intellij.codeInspection.ex.InspectionElementsMerger;
import org.jetbrains.annotations.NotNull;

/**
 * @author Bas Leijdekkers
 */
public final class ThrowableNeverThrownInspectionMerger extends InspectionElementsMerger {

  @Override
  public @NotNull String getMergedToolName() {
    return "ThrowableNotThrown";
  }

  @Override
  public String @NotNull [] getSourceToolNames() {
    return new String[] {"ThrowableInstanceNeverThrown", "ThrowableResultOfMethodCallIgnored"};
  }
}
