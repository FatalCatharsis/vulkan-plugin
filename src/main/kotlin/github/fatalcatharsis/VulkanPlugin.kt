package github.fatalcatharsis

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.DefaultNamedDomainObjectCollection
import org.gradle.kotlin.dsl.*
import org.gradle.language.cpp.CppApplication
import org.gradle.language.cpp.CppExecutable
import org.gradle.language.cpp.plugins.CppApplicationPlugin
import org.gradle.language.cpp.plugins.CppBasePlugin
import org.gradle.language.cpp.tasks.CppCompile
import org.gradle.nativeplatform.tasks.LinkExecutable
import org.gradle.nativeplatform.toolchain.Clang
import org.gradle.nativeplatform.toolchain.NativeToolChain
import org.gradle.nativeplatform.toolchain.internal.DefaultNativeToolChainRegistry
import org.gradle.nativeplatform.toolchain.internal.msvcpp.VisualCppToolChain
import org.gradle.tooling.model.cpp.CppBinary
import sun.plugin.dom.exception.PluginNotSupportedException
import java.nio.file.Paths

class VulkanPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.afterEvaluate {
            if (!project.plugins.hasPlugin(CppApplicationPlugin::class)) {
                throw PluginNotSupportedException("This requires that the \"cpp-application\" plugin be applied to this project.")
            }

            val sdkPath = System.getenv("VULKAN_SDK")
                ?: throw PluginNotSupportedException("This plugin requires that the vulkan sdk be installed on this machine.")

            val vulkanHeaderPath = Paths.get(sdkPath).resolve("./Include").normalize()
            val vulkanLibPath = Paths.get(sdkPath).resolve("./Lib/vulkan-1.lib").normalize()

            val application = project.properties["application"] as CppApplication

            if (!application.privateHeaderDirs.contains(vulkanHeaderPath)) {
                application.privateHeaders { from(vulkanHeaderPath) }
            }

            val linkTasks = project.tasks.withType<LinkExecutable>()
            linkTasks.forEach {
                if (!it.libs.contains(vulkanLibPath)) {
                    it.libs.from(vulkanLibPath)
                }
            }
        }
    }
}
