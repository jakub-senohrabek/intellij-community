// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.zmlx.hg4idea.repo;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.zmlx.hg4idea.command.HgShowConfigCommand;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public final class HgConfig {

  private final @NotNull Map<String, Map<String, String>> myConfigMap;

  public static HgConfig getInstance(Project project, VirtualFile root) {
    return new HgConfig(project, root);
  }

  private HgConfig(@NotNull Project project, @NotNull VirtualFile repo) {
    // todo: may be should change showconfigCommand to parse hgrc file
    // but default values for extension and repository root are not included in hgrc, so perform showconfig is better
    // in windows configuration Mercurial.ini file may be used instead of hgrc
    myConfigMap = new HgShowConfigCommand(project).execute(repo);
  }

  public @Nullable String getDefaultPath() {
    return getNamedConfig("paths", "default");
  }

  public @Nullable String getDefaultPushPath() {
    String path = getNamedConfig("paths", "default:pushurl");
    if (path == null) {
      path = getNamedConfig("paths", "default-push");
    }
    if (path == null) {
      path = getNamedConfig("paths", "default");
    }
    return path;
  }

  public @Nullable String getNamedConfig(@NotNull @NonNls String sectionName, @Nullable @NonNls String configName) {
    if (StringUtil.isEmptyOrSpaces(sectionName) || StringUtil.isEmptyOrSpaces(configName)) {
      return null;
    }
    Map<String, String> sectionValues = myConfigMap.get(sectionName);
    return sectionValues != null ? sectionValues.get(configName) : null;
  }

  public @NotNull Collection<String> getPaths() {
    Map<String, String> pathOptions = myConfigMap.get("paths"); //NON-NLS
    return pathOptions != null ? pathOptions.values() : Collections.emptyList();
  }

  public boolean isMqUsed() {
    String value = getNamedConfig("extensions", "mq");
    return (value != null && !value.trim().startsWith("!"));
  }
}
