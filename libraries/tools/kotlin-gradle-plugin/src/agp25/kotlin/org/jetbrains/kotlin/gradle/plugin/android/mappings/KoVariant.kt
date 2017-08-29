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

package org.jetbrains.kotlin.gradle.plugin.android.mappings

import com.android.builder.model.*

class KoVariant : Variant {
    private var name = ""
    private var displayName = ""
    private var buildTypeName = ""
    private var productFlavorNames = emptyList<String>()
    private var mergedFlavor: ProductFlavor? = null
    private var mainArtifactInfo: AndroidArtifact? = null
    private var extraAndroidArtifacts: Collection<AndroidArtifact>? = null
    private var extraJavaArtifacts: Collection<JavaArtifact>? = null
    private var testedTargetVariants: Collection<TestedTargetVariant>? = null

    override fun getName() = name
    override fun getDisplayName() = displayName
    override fun getBuildType() = buildTypeName
    override fun getProductFlavors() = productFlavorNames
    override fun getMergedFlavor() = mergedFlavor
    override fun getMainArtifact() = mainArtifactInfo
    override fun getExtraAndroidArtifacts() = extraAndroidArtifacts
    override fun getExtraJavaArtifacts() = extraJavaArtifacts
    override fun getTestedTargetVariants() = testedTargetVariants
}
