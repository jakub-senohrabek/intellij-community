// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.diff.tools.util.base;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.icons.AllIcons;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.diff.DiffBundle;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.util.Predicates;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import javax.swing.*;
import java.util.function.Predicate;

public enum HighlightingLevel {
  INSPECTIONS("option.highlighting.level.inspections", AllIcons.Ide.HectorOn, Predicates.alwaysTrue()),

  ADVANCED("option.highlighting.level.syntax", AllIcons.Ide.HectorSyntax, rangeHighlighter -> {
    if (rangeHighlighter.getLayer() > HighlighterLayer.ADDITIONAL_SYNTAX) return false;
    HighlightInfo info = HighlightInfo.fromRangeHighlighter(rangeHighlighter);
    return info == null || info.getSeverity().compareTo(HighlightSeverity.GENERIC_SERVER_ERROR_OR_WARNING) < 0 && info.type != HighlightInfoType.TODO;
  }),

  SIMPLE("option.highlighting.level.none", AllIcons.Ide.HectorOff, rangeHighlighter ->
    rangeHighlighter.getLayer() <= HighlighterLayer.SYNTAX);

  private final @NotNull String myTextKey;
  private final @Nullable Icon myIcon;
  private final @NotNull Predicate<? super RangeHighlighter> myCondition;

  HighlightingLevel(@NotNull @PropertyKey(resourceBundle = DiffBundle.BUNDLE) String textKey,
                    @Nullable Icon icon,
                    @NotNull Predicate<? super RangeHighlighter> condition) {
    myTextKey = textKey;
    myIcon = icon;
    myCondition = condition;
  }

  public @Nls @NotNull String getText() {
    return DiffBundle.message(myTextKey);
  }

  public @Nullable Icon getIcon() {
    return myIcon;
  }

  public @NotNull Predicate<? super RangeHighlighter> getCondition() {
    return myCondition;
  }
}
