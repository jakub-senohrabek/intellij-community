/*
 * Copyright 2003-2015 Dave Griffith, Bas Leijdekkers
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
package com.siyeh.ig.performance;

import com.intellij.codeInsight.daemon.impl.UnusedSymbolUtil;
import com.intellij.codeInsight.options.JavaInspectionButtons;
import com.intellij.codeInsight.options.JavaInspectionControls;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.dataFlow.CommonDataflow;
import com.intellij.codeInspection.dataFlow.Mutability;
import com.intellij.codeInspection.options.OptPane;
import com.intellij.pom.java.JavaFeature;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspection;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.fixes.ChangeModifierFix;
import com.siyeh.ig.psiutils.ClassUtils;
import com.siyeh.ig.psiutils.ExpressionUtils;
import com.siyeh.ig.psiutils.SideEffectChecker;
import com.siyeh.ig.psiutils.VariableAccessUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.codeInspection.options.OptPane.pane;

public final class FieldMayBeStaticInspection extends BaseInspection {

  @Override
  public BaseInspectionVisitor buildVisitor() {
    return new FieldMayBeStaticVisitor();
  }

  @Override
  protected @NotNull String buildErrorString(Object... infos) {
    return InspectionGadgetsBundle.message(
      "field.may.be.static.problem.descriptor");
  }

  @Override
  public @NotNull OptPane getOptionsPane() {
    return pane(JavaInspectionControls.button(JavaInspectionButtons.ButtonKind.IMPLICIT_WRITE_ANNOTATIONS));
  }

  @Override
  public LocalQuickFix buildFix(Object... infos) {
    return new ChangeModifierFix(PsiModifier.STATIC);
  }

  private static final class FieldMayBeStaticVisitor extends BaseInspectionVisitor {
    @Override
    public void visitField(@NotNull PsiField field) {
      if (field.hasModifierProperty(PsiModifier.STATIC)) {
        return;
      }
      if (!field.hasModifierProperty(PsiModifier.FINAL)) {
        return;
      }
      final PsiExpression initializer = field.getInitializer();
      if (initializer == null) {
        return;
      }
      if (SideEffectChecker.mayHaveSideEffects(initializer)) {
        return;
      }
      final PsiType type = field.getType();
      if (!ClassUtils.isImmutable(type) && 
          Mutability.fromDfType(CommonDataflow.getDfType(initializer)) != Mutability.UNMODIFIABLE) {
        return;
      }
      final PsiClass containingClass = field.getContainingClass();
      if (containingClass != null
          && !containingClass.hasModifierProperty(PsiModifier.STATIC)
          && containingClass.getContainingClass() != null
          && !PsiUtil.isAvailable(JavaFeature.INNER_STATICS, containingClass)
          && !PsiUtil.isCompileTimeConstant(field)) {
        // inner class cannot have static declarations in earlier Java versions
        return;
      }
      if (UnusedSymbolUtil.isImplicitWrite(field)) {
        return;
      }
      if (containingClass instanceof PsiAnonymousClass &&
          !PsiUtil.isAvailable(JavaFeature.INNER_STATICS, containingClass) &&
          !PsiUtil.isCompileTimeConstant(field)) {
        return;
      }
      if (!canBeStatic(initializer)) {
        return;
      }
      if (isIdentitySensitive(field)) {
        return;
      }
      registerFieldError(field);
    }

    private static boolean isIdentitySensitive(@NotNull PsiField field) {
      if (field.getType() instanceof PsiPrimitiveType) return false;
      List<PsiReferenceExpression> refs = VariableAccessUtils.getVariableReferences(field);
      for (PsiReferenceExpression ref : refs) {
        PsiElement parent = PsiUtil.skipParenthesizedExprUp(ref.getParent());
        if (parent instanceof PsiSynchronizedStatement) return true;
        if (parent instanceof PsiBinaryExpression binOp &&
            (binOp.getOperationTokenType() == JavaTokenType.EQEQ || binOp.getOperationTokenType() == JavaTokenType.NE) &&
            ExpressionUtils.getValueComparedWithNull(binOp) == null) {
          return true;
        }
      }
      return false;
    }

    private static boolean canBeStatic(PsiExpression initializer) {
      final CanBeStaticVisitor canBeStaticVisitor = new CanBeStaticVisitor();
      initializer.accept(canBeStaticVisitor);
      return canBeStaticVisitor.canBeStatic();
    }
  }
}