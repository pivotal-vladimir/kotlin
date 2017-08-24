/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.uast.kotlin

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiType
import org.jetbrains.kotlin.psi.ValueArgument
import org.jetbrains.uast.*

class KotlinUNamedExpression private constructor(
        override val name: String?,
        override val uastParent: UElement?,
        expressionProducer: (UElement) -> UExpression
) : UNamedExpression {
    override val expression: UExpression by lz { expressionProducer(this) }

    override val annotations: List<UAnnotation> = emptyList()

    override val psi: PsiElement? = null

    companion object {
        internal fun create(name: String?, valueArgument: ValueArgument, uastParent: UElement?): UNamedExpression {
            val expression = valueArgument.getArgumentExpression()
            return KotlinUNamedExpression(name, uastParent) { expressionParent ->
                expression?.let { expressionParent.getLanguagePlugin().convert<UExpression>(it, expressionParent) } ?: UastEmptyExpression
            }
        }

        internal fun create(name: String?, valueArguments: List<ValueArgument>, uastParent: UElement?): UNamedExpression {
            return KotlinUNamedExpression(name, uastParent) { expressionParent ->
                object : KotlinAbstractUExpression(), UCallExpression {
                    override val uastParent: UElement? = expressionParent

                    override val kind: UastCallKind = UastCallKind.NESTED_ARRAY_INITIALIZER

                    override val valueArguments: List<UExpression> by lz {
                        valueArguments.map {
                            it.getArgumentExpression()?.let { argumentExpression ->
                                getLanguagePlugin().convert<UExpression>(argumentExpression, this)
                            } ?: UastEmptyExpression
                        }
                    }

                    override val valueArgumentCount: Int = valueArguments.size

                    override val psi: PsiElement? = null
                    override val methodIdentifier: UIdentifier? = null
                    override val classReference: UReferenceExpression? = null
                    override val methodName: String? = null
                    override val typeArgumentCount: Int = 0
                    override val typeArguments: List<PsiType> = emptyList()
                    override val returnType: PsiType? = null
                    override fun resolve() = null
                    override val receiver: UExpression? = null
                    override val receiverType: PsiType? = null
                }
            }
        }
    }
}