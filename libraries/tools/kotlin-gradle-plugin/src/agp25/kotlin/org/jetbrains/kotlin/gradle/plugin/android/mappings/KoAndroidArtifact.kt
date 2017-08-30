package org.jetbrains.kotlin.gradle.plugin.android.mappings

import com.android.builder.model.*
import com.android.builder.model.level2.DependencyGraphs
import java.io.File

class KoAndroidArtifact : AndroidArtifact {
    private var assembleTaskName: String = ""
    private var name: String = ""
    private var ideSetupTaskNames: Set<String>? = null
    private var classesFolder: File? = null
    private var dependencyGraphs: DependencyGraphs? = null
    private var instantRun: InstantRun? = null
    private var applicationId: String = ""
    private var multiFlavorSourceProvider: SourceProvider? = null
    private var sourceGenTakName: String = ""
    private var signingConfigName: String = ""
    private var buildConfigFields: Map<String, ClassField>? = null
    private var compileTaskName: String = ""
    private var javaResourcesFolder: File? = null
    @JvmField var outputs: Collection<AndroidArtifactOutput>? = null
    private var nativeLibraries: Collection<NativeLibrary>? = null
    private var isSigned: Boolean = false
    private var resValues: Map<String, ClassField>? = null
    private var compileDependencies: Dependencies? = null
    private var variantSourceProvider: SourceProvider? = null
    private var generatedSourceFolders: Collection<File>? = null
    private var abiFilters: Set<String>? = null
    private var generatedResourceFolders: Collection<File>? = null

    override fun getAssembleTaskName() = assembleTaskName
    override fun getName() = name
    override fun getIdeSetupTaskNames() = ideSetupTaskNames
    override fun getClassesFolder() = classesFolder
    override fun getDependencyGraphs() = dependencyGraphs
    override fun getInstantRun() = instantRun
    override fun getApplicationId() = applicationId
    override fun getMultiFlavorSourceProvider() = multiFlavorSourceProvider
    override fun getSourceGenTaskName() = sourceGenTakName
    override fun getSigningConfigName() = signingConfigName
    override fun getBuildConfigFields() = buildConfigFields
    override fun getCompileTaskName() = compileTaskName
    override fun getJavaResourcesFolder() = javaResourcesFolder
    override fun getOutputs() = outputs
    override fun getNativeLibraries() = nativeLibraries
    override fun isSigned() = isSigned
    override fun getResValues() = resValues
    override fun getCompileDependencies() = compileDependencies
    override fun getDependencies() = compileDependencies
    override fun getVariantSourceProvider() = variantSourceProvider
    override fun getGeneratedSourceFolders() = generatedSourceFolders
    override fun getAbiFilters() = abiFilters
    override fun getGeneratedResourceFolders() = generatedResourceFolders
}