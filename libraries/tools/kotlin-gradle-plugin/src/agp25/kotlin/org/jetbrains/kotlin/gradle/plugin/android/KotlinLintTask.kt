package org.jetbrains.kotlin.gradle.plugin.android

import com.android.build.gradle.internal.dsl.LintOptions
import com.android.builder.model.LintOptions as ILintOptions
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.tasks.BaseTask
import com.android.build.gradle.tasks.Lint
import com.android.builder.core.AndroidBuilder
import com.android.builder.model.*
import com.android.ide.common.repository.GradleCoordinate
import com.android.manifmerger.ManifestMerger2
import com.android.repository.Revision
import com.android.resources.ResourceType
import com.android.sdklib.BuildToolInfo
import com.android.tools.lint.Reporter
import com.android.tools.lint.checks.ApiParser
import com.intellij.openapi.project.Project as IdeaProject
import com.android.tools.lint.client.api.LintRequest
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.serializers.JavaSerializer
import com.esotericsoftware.reflectasm.ConstructorAccess
import com.google.common.collect.Sets
import com.google.gson.*
import com.intellij.util.PathUtil
import de.javakaffee.kryoserializers.guava.ImmutableListSerializer
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.ParallelizableTask
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry
import org.jetbrains.kotlin.gradle.plugin.Android25ProjectHandler.Companion.LINT_WITH_KOTLIN_CONFIGURATION_NAME
import org.jetbrains.uast.UastContext
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.analysis.AnalyzerException
import org.objenesis.strategy.BaseInstantiatorStrategy
import org.objenesis.strategy.StdInstantiatorStrategy
import org.xmlpull.v1.XmlPullParserException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.reflect.Field
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import lombok.ast.Node as LombokNode

@ParallelizableTask
open class KotlinLintTask : AbstractTask() {
    lateinit var javaLintTask: Lint

    @TaskAction
    fun lint() {
        val androidBuilder = javaLintTask.field<BaseTask>("androidBuilder").get<AndroidBuilder>()

        val project = javaLintTask.project
        val variantName = javaLintTask.variantName
        val _buildTools = androidBuilder.targetInfo.buildTools
        val _lintOptions = javaLintTask.field("lintOptions").getOrNull<LintOptions>()
        val sdkHome = javaLintTask.field("sdkHome").getOrNull<File>()
        val fatalOnly = javaLintTask.field("fatalOnly").getOrNull<Boolean>() ?: false
        val _toolingRegistry = javaLintTask.field("toolingRegistry").get<ToolingModelBuilderRegistry>()
        val reportsDir = javaLintTask.field("reportsDir").getOrNull<File>()
        val manifestReportFile = javaLintTask.field("manifestReportFile").getOrNull<File>()
        val outputsDir = javaLintTask.field("outputsDir").get<File>()
        val manifestsForVariant = javaLintTask.field("manifestsForVariant").getOrNull<FileCollection>()
        val _variantScope = javaLintTask.field("variantScope").getOrNull<VariantScope>()

        val topmostClassLoader = findClassLoaderForLint(javaClass.classLoader)
        val classLoaderForLint = URLClassLoader(collectLibrariesForClasspath(project), topmostClassLoader)

        val mapper = LintObjectsMapper(classLoaderForLint)

        val buildTools = mapper.mapNotNull(_buildTools)
        val lintOptions = mapper.map(_lintOptions)
        val androidProject = mapper.mapNotNull(createAndroidProject(project, _toolingRegistry))
        val variantScope = mapper.map(_variantScope)

        val executorClass = Class.forName(KotlinLintExecutor::class.java.name, true, classLoaderForLint)
        val executor = executorClass.constructors.single().newInstance(
                project, variantName, buildTools, lintOptions, sdkHome, fatalOnly, androidProject,
                reportsDir, manifestReportFile, outputsDir, manifestsForVariant, variantScope)

        executorClass.getDeclaredMethod("lint").invoke(executor)
    }

    private fun findClassLoaderForLint(classLoader: ClassLoader): ClassLoader {
        if (classLoader.defines<Project>()
                && classLoader.defines<FileCollection>()
                && !classLoader.defines<KotlinLintExecutor>()
                && !classLoader.defines<IdeaProject>()
                && !classLoader.defines<UastContext>()
                && !classLoader.defines<LintRequest>()
                && !classLoader.defines<BuildToolInfo>()
        ) {
            return classLoader
        }

        val parent = classLoader.parent ?: error("Can't find a classloader for Lint with Kotlin")
        return findClassLoaderForLint(parent)
    }

    private inline fun <reified T : Any> ClassLoader.defines(): Boolean {
        if (T::class.java.enclosingClass != null) {
            throw IllegalArgumentException("Only top level classes are supported, got ${T::class.java.name}")
        }

        return try {
            Class.forName(T::class.java.name, false, this) != null
        } catch (e: Exception) {
            false
        }
    }

    private fun collectLibrariesForClasspath(project: Project): Array<URL> {
        val kotlinGradlePlugin = findLibraryFor<KotlinLintTask>()
        val kotlinCompilerAndUast = project.configurations
                .getByName(LINT_WITH_KOTLIN_CONFIGURATION_NAME)
                .resolve()
                .map { it.toURI().toURL() }
                .toTypedArray()

        //TODO uast kotlin
        //TODO uast java

        // Android libraries
        val builder = findLibraryFor<AndroidBuilder>()
        val gradleCore = findLibraryFor<LintOptions>()
        val sdklib = findLibraryFor<BuildToolInfo>()
        val sdkCommon = findLibraryFor<GradleCoordinate>()
        val repository = findLibraryFor<Revision>()
        val builderModel = findLibraryFor<com.android.builder.model.LintOptions>()
        val common = findLibraryFor<com.android.utils.Pair<*, *>>()
        val manifestMerger = findLibraryFor<ManifestMerger2>()
        val layoutLibApi = findLibraryFor<ResourceType>()
        val lombokAst = findLibraryFor<LombokNode>()

        // Third-parties
        val asm = findLibraryFor<ClassVisitor>()
        val asmTree = findLibraryFor<AbstractInsnNode>()
        val asmAnalysis = findLibraryFor<AnalyzerException>()
        val guava = findLibraryFor<Sets>()
        val gson = findLibraryFor<Gson>()
        val kxml = findLibraryFor<XmlPullParserException>()

        val kryo = findLibraryFor<Kryo>()
        val objenesis = findLibraryFor<BaseInstantiatorStrategy>()
        val kryoSerializers = findLibraryFor<ImmutableListSerializer>()
        val reflectAsm = findLibraryFor<ConstructorAccess<*>>()
        val minlog = findLibraryFor<com.esotericsoftware.minlog.Log>()

        // Lint
        val lint = findLibraryFor<Reporter>()
        val lintApi = findLibraryFor<LintRequest>()
        val lintChecks = findLibraryFor<ApiParser>()
        val uastJava = findLibraryFor<UastContext>() // TODO remove

        return arrayOf(guava, kotlinGradlePlugin, *kotlinCompilerAndUast,
                builder, gradleCore, sdklib, sdkCommon, repository, builderModel, common, manifestMerger,
                layoutLibApi, lombokAst,
                asm, asmTree, asmAnalysis,
                gson, kxml,
                kryo, objenesis, kryoSerializers, reflectAsm, minlog,
                lint, lintApi, lintChecks).apply {
            project.logger.warn("Lint with Kotlin classpath: ${this.joinToString { it.path }}")
        }
    }

    private inline fun <reified T : Any> findLibraryFor(): URL {
        if (T::class.java.enclosingClass != null) {
            throw IllegalArgumentException("Only top level classes are supported, got ${T::class.java.name}")
        }

        return File(PathUtil.getJarPathForClass(T::class.java)).toURI().toURL()
    }

    private fun createAndroidProject(
            gradleProject: Project,
            toolingRegistry: ToolingModelBuilderRegistry
    ): AndroidProject {
        val modelName = AndroidProject::class.java.name
        val modelBuilder = toolingRegistry.getBuilder(modelName)

        // setup the level 3 sync.
        val ext = gradleProject.extensions.extraProperties
        ext.set(
                AndroidProject.PROPERTY_BUILD_MODEL_ONLY_VERSIONED,
                Integer.toString(AndroidProject.MODEL_LEVEL_3_VARIANT_OUTPUT_POST_BUILD))

        try {
            return modelBuilder.buildAll(modelName, gradleProject) as AndroidProject
        } finally {
            ext.set(AndroidProject.PROPERTY_BUILD_MODEL_ONLY_VERSIONED, null)
        }
    }
}

private inline fun <reified T: Any> T.field(name: String) = FieldWrapper(this, T::class.java.getDeclaredField(name))

private class FieldWrapper(val obj: Any, val field: Field) {
    private fun Field.obtain(): Any? {
        val oldIsAccessible = isAccessible
        try {
            isAccessible = true
            return get(obj)
        } finally {
            isAccessible = oldIsAccessible
        }
    }

    inline fun <reified T> getOrNull(): T? = field.obtain() as? T
    inline fun <reified T> get(): T = field.obtain() as? T ?: error("Unable to get ${field.name} from $obj")
}