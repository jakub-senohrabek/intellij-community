
// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.refactoring.migration;

import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MigrationMap {
  private @Nls String myName;
  private @Nls String myDescription;
  private final ArrayList<MigrationMapEntry> myEntries = new ArrayList<>();
  private String myFileName;
  private int order = 0;

  public MigrationMap() {
  }

  public MigrationMap(MigrationMapEntry[] entries) {
    for (MigrationMapEntry entry : entries) {
      addEntry(entry);
    }
  }

  public MigrationMap cloneMap() {
    MigrationMap newMap = new MigrationMap();
    newMap.myName = myName;
    newMap.myDescription = myDescription;
    newMap.myFileName = myFileName;
    for(int i = 0; i < myEntries.size(); i++){
      MigrationMapEntry entry = getEntryAt(i);
      newMap.addEntry(entry.cloneEntry());
    }
    return newMap;
  }

  public @Nls String getName() {
    return myName;
  }

  public void setName(@Nls String name) {
    myName = name;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public @Nls String getDescription() {
    return myDescription;
  }

  public void setDescription(@Nls String description) {
    myDescription = description;
  }

  public void addEntry(MigrationMapEntry entry) {
    myEntries.add(entry);
  }

  public void removeEntryAt(int index) {
    myEntries.remove(index);
  }

  public void removeAllEntries() {
    myEntries.clear();
  }

  public int getEntryCount() {
    return myEntries.size();
  }

  public MigrationMapEntry getEntryAt(int index) {
    return myEntries.get(index);
  }

  public void setEntryAt(MigrationMapEntry entry, int index) {
    myEntries.set(index, entry);
  }

  @Override
  public String toString() {
    return getName();
  }

  public @NotNull String getFileName() {
    if (myFileName == null) {
      return FileUtil.sanitizeFileName(myName, false);
    }
    return myFileName;
  }

  public void setFileName(String fileName) {
    myFileName = fileName;
  }
}



