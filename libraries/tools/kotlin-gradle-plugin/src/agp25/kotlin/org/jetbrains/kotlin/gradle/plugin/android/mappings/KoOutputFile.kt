package org.jetbrains.kotlin.gradle.plugin.android.mappings

import com.android.build.FilterData
import com.android.build.OutputFile
import java.io.File

class KoOutputFile : OutputFile {
    private var outputFile: File? = null
    private var filterTypes: Collection<String>? = null
    private var outputType: String = ""
    private var filters: Collection<FilterData>? = null
    private var versionCode: Int = 0
    private var outputs: Collection<OutputFile>? = null
    private var mainOutputFile: OutputFile? = null

    override fun getOutputFile() = outputFile
    override fun getFilterTypes() = filterTypes
    override fun getOutputType() = outputType
    override fun getFilters() = filters
    override fun getVersionCode() = versionCode
    override fun getOutputs() = outputs
    override fun getMainOutputFile() = mainOutputFile
}