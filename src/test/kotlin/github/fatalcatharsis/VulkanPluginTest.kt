package github.fatalcatharsis

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.FileOutputStream
import java.io.PrintWriter
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class VulkanPluginTest {
    @get:Rule
    val projectDir = TemporaryFolder()

    @Test
    fun `general functionality test`() {
        // given
        writeFile(Paths.get("build.gradle.kts"), """
            import java.nio.file.Paths

            plugins {
                `cpp-application`
                id("github.fatalcatharsis.vulkan") version("1.0-SNAPSHOT")
            }

            val user32Path = "C:\\Program Files (x86)\\Windows Kits\\10\\Lib\\10.0.17763.0\\um\\x64\\user32.lib"
            project.afterEvaluate {
                tasks.withType<LinkExecutable>().forEach {
                    if (!it.libs.contains(user32Path)) {
                        it.libs.from(user32Path)
                    }
                }
            }
        """.trimIndent())

        writeFile(Paths.get("src/main/cpp/main.cpp"), """
            #include <iostream>
            #ifdef _WIN32
                #define VK_USE_PLATFORM_WIN32_KHR
            #endif
            #include <vulkan/vulkan.h>
            #include <windows.h>
            #include <stdexcept>

            using namespace std;

            LRESULT wndProc(HWND hwnd, UINT msg, WPARAM wparam, LPARAM lparam) {
                switch (msg) {
                    case WM_CLOSE:
                        DestroyWindow(hwnd);
                        break;
                    case WM_DESTROY:
                        PostQuitMessage(0);
                        break;
                    default:
                        return DefWindowProc(hwnd, msg, wparam, lparam);
                }

                return 0;
            }

            int main() {
                WNDCLASSEX wc;

                wc.cbSize = sizeof(WNDCLASSEX);
                wc.style = 0;
                wc.lpfnWndProc = wndProc;
                wc.cbClsExtra = 0;
                wc.cbWndExtra = 0;
                wc.hInstance = 0;
                wc.hIcon = LoadIcon(NULL, IDI_APPLICATION);
                wc.hCursor = LoadCursor(NULL, IDC_ARROW);
                wc.hbrBackground = (HBRUSH)(COLOR_WINDOW + 1);
                wc.lpszMenuName = NULL;
                wc.lpszClassName = "wyatt_test";
                wc.hIconSm = LoadIcon(NULL, IDI_APPLICATION);

                if (!RegisterClassEx(&wc)) {
                    throw new exception("Failed to register base window class");
                }

                HWND hwnd = CreateWindowEx(
                    WS_EX_CLIENTEDGE,
                    "wyatt_test",
                    "wyatt_test",
                    WS_OVERLAPPEDWINDOW,
                    CW_USEDEFAULT,
                    CW_USEDEFAULT,
                    800,
                    600,
                    NULL,
                    NULL,
                    GetModuleHandle(NULL),
                    NULL
                );

                if (hwnd == NULL) {
                    throw new exception("Error occurred when creating a new window " + GetLastError());
                }

                VkApplicationInfo appInfo = {};
                appInfo.sType = VK_STRUCTURE_TYPE_APPLICATION_INFO;
                appInfo.pApplicationName = "worlds_game";
                appInfo.applicationVersion = VK_MAKE_VERSION(1, 0, 0);
                appInfo.pEngineName = "No Engine";
                appInfo.engineVersion = VK_MAKE_VERSION(1, 0, 0);
                appInfo.apiVersion = VK_API_VERSION_1_1;

                VkInstanceCreateInfo createInfo = {};
                createInfo.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
                createInfo.pApplicationInfo = &appInfo;

                // create vulkan instance
                VkInstance vkInstance = VK_NULL_HANDLE;
                if (vkCreateInstance(&createInfo, nullptr, &vkInstance) != VK_SUCCESS) {
                    throw runtime_error("failed to create instance!");
                }
            }
        """.trimIndent())

        // when
        val result = GradleRunner.create()
            .withPluginClasspath()
            .withArguments("build")
            .withProjectDir(projectDir.root)
            .build()

        // then
        println(result.output)
        assertEquals("Expected success from the gradle execution", TaskOutcome.SUCCESS, result.task(":build")!!.outcome)
    }

    private fun writeFile(filePath: Path, content: String) {
        if (filePath.parent != null) {
            val parentPath = projectDir.root.toPath().resolve(filePath.parent).normalize()
            Files.createDirectories(parentPath)
        } else {
            projectDir.root.toPath()
        }

        val outputStream = FileOutputStream(projectDir.root.resolve(filePath.toFile()))
        PrintWriter(outputStream).use {
            it.write(content)
        }
    }
}
