/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.python.inspections.quickfix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.PyPsiBundle;
import com.jetbrains.python.PythonUiService;
import com.jetbrains.python.psi.PyTargetExpression;
import org.jetbrains.annotations.NotNull;

public class PyMakePublicQuickFix implements LocalQuickFix {

  @Override
  public @NotNull String getFamilyName() {
    return PyPsiBundle.message("QFIX.make.public");
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    final PsiElement element = PyQuickFixUtil.dereference(descriptor.getPsiElement());
    if (element == null) {
      return;
    }
    if (element instanceof PyTargetExpression) {
      final String name = ((PyTargetExpression)element).getName();
      if (name == null) return;
      final VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
      if (virtualFile != null) {
        final String publicName = StringUtil.trimLeading(name, '_');
        PythonUiService.getInstance().runRenameProcessor(project, element, publicName, false, false);
      }
    }
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }
}
