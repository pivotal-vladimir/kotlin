package org.jetbrains.kotlin.gradle.plugin.android.mappings

import com.android.build.FilterData
import com.android.build.OutputFile
import com.android.builder.model.AndroidArtifactOutput
import java.io.File

class KoAndroidArtifactOutput : AndroidArtifactOutput {
    private var outputFile: File? = null
    private var filterTypes: Collection<String>? = null
    private var assembleTaskName: String = ""
    private var filters: Collection<FilterData>? = null
    private var generatedManifest: File? = null
    private var outputs: Collection<OutputFile>? = null
    private var outputType: String = ""
    private var versionCode: Int = 0
    private var mainOutputFile: OutputFile? = null

    override fun getOutputFile() = outputFile
    override fun getFilterTypes() = filterTypes
    override fun getAssembleTaskName() = assembleTaskName
    override fun getFilters() = filters
    override fun getGeneratedManifest() = generatedManifest
    override fun getOutputs() = outputs
    override fun getOutputType() = outputType
    override fun getVersionCode() = versionCode
    override fun getMainOutputFile() = mainOutputFile
}