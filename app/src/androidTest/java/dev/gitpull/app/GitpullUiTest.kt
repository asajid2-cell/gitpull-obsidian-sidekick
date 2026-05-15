package dev.gitpull.app

import android.content.Context
import android.net.Uri
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import dev.gitpull.app.core.PendingGitHubSignIn
import dev.gitpull.app.data.GitHubDeviceSignInStore
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class GitpullUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetAppState() {
        composeRule.activity
            .getSharedPreferences("gitpull_settings", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        composeRule.activity
            .getSharedPreferences("gitpull_github_device_sign_in", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
    }

    @Test
    fun freshInstallShowsSetupGate() {
        composeRule.onNodeWithText("gitpull").assertIsDisplayed()
        composeRule.onNodeWithTag("repo-field").assertIsDisplayed()
        composeRule.onNodeWithTag("branch-field").assertTextContains("main", substring = true)
        composeRule.onNodeWithText("Destination folder").assertIsDisplayed()
        composeRule.onNodeWithTag("github-sign-in").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("token-field").assertIsDisplayed()
        composeRule.onNodeWithTag("load-repositories").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("GitHub is source of truth").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("save-setup").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun repositoryBrowserRequiresGitHubSignIn() {
        composeRule.onNodeWithTag("load-repositories").performScrollTo().performClick()

        composeRule.onNodeWithText("Sign in with GitHub before loading repositories.")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun githubBrowserSignInMatchesBuildConfiguration() {
        if (BuildConfig.GITHUB_OAUTH_CLIENT_ID.isBlank()) {
            composeRule.onNodeWithText("Browser sign-in needs a GitHub OAuth client ID in this build.")
                .performScrollTo()
                .assertIsDisplayed()
            composeRule.onNodeWithTag("github-sign-in")
                .performScrollTo()
                .assertIsDisplayed()
                .assertIsNotEnabled()
        } else {
            composeRule.onNodeWithText("Use browser sign-in to browse and pull repositories without pasting a token.")
                .performScrollTo()
                .assertIsDisplayed()
            composeRule.onNodeWithTag("github-sign-in")
                .performScrollTo()
                .assertIsDisplayed()
                .assertIsEnabled()
        }
    }

    @Test
    fun pendingGithubSignInSurvivesActivityRecreate() {
        GitHubDeviceSignInStore(composeRule.activity).save(
            PendingGitHubSignIn(
                deviceCode = "device-test",
                userCode = "ABCD-1234",
                verificationUri = "https://github.com/login/device",
                expiresAtMillis = System.currentTimeMillis() + 900_000L,
                intervalSeconds = 30
            )
        )

        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("github-device-code-card")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("github-device-code")
            .assertTextContains("ABCD-1234")
            .assertIsDisplayed()
        composeRule.onNodeWithTag("copy-github-code")
            .assertIsDisplayed()
            .assertIsEnabled()
        composeRule.onNodeWithTag("check-github-sign-in")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun configuredAppShowsPullPdfAndSettingsTabs() {
        val destinationUri = seedConfiguredApp()

        composeRule.onNodeWithText("octocat/Hello-World").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Pull latest vault snapshot").assertIsDisplayed()
        composeRule.onNodeWithTag("pull-button").assertIsDisplayed()
        composeRule.onNodeWithTag("status-card").assertIsDisplayed()
        composeRule.onNodeWithTag("open-obsidian").assertIsDisplayed()
        if (!isObsidianLaunchAvailable()) {
            composeRule.onNodeWithTag("open-obsidian").performClick()
            composeRule.onNodeWithText(
                "Obsidian is not installed or cannot be launched directly. Open Obsidian Mobile and select this folder:\n$destinationUri"
            ).assertIsDisplayed()
            composeRule.onNodeWithText("OK").performClick()
        }

        composeRule.onNodeWithTag("nav-pdfs").performClick()
        composeRule.onNodeWithText("1 documents").assertIsDisplayed()
        composeRule.onNodeWithTag("pdf-search").assertIsDisplayed()
        composeRule.onNodeWithTag("pdf-search").performTextInput("fixture")
        composeRule.onNodeWithText("Gitpull-fixture.pdf").assertIsDisplayed()
        composeRule.onNodeWithText("Papers/Gitpull-fixture.pdf").assertIsDisplayed()

        composeRule.onNodeWithTag("nav-settings").performClick()
        composeRule.onNodeWithText("Repository and safety").assertIsDisplayed()
        composeRule.onNodeWithText("Repository").assertIsDisplayed()
        composeRule.onNodeWithText("GitHub sign-in").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Token fallback").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("clear-snapshot").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun clearSnapshotRequiresConfirmation() {
        seedConfiguredApp()

        composeRule.onNodeWithTag("nav-settings").performClick()
        composeRule.onNodeWithTag("clear-snapshot").performScrollTo().performClick()

        composeRule.onNodeWithText("This removes files from the selected local snapshot folder. GitHub remains unchanged.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Cancel").assertIsDisplayed()
        composeRule.onNodeWithText("Clear").assertIsDisplayed()
    }

    @Test
    fun primaryTouchTargetsAreAtLeast48dpHigh() {
        composeRule.onNodeWithTag("repo-field").assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithTag("branch-field").assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithTag("token-field").assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithTag("load-repositories").performScrollTo().assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithTag("github-sign-in").performScrollTo().assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithTag("save-setup").performScrollTo().assertHeightIsAtLeast(48.dp)

        seedConfiguredApp()
        composeRule.onNodeWithTag("nav-pull").assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithTag("nav-pdfs").assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithTag("nav-settings").assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithTag("pull-button").assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithTag("open-obsidian").assertHeightIsAtLeast(48.dp)

        composeRule.onNodeWithTag("nav-pdfs").performClick()
        composeRule.onNodeWithTag("pdf-search").assertHeightIsAtLeast(48.dp)
        composeRule.onNodeWithTag("pdf-row").assertHeightIsAtLeast(48.dp)

        composeRule.onNodeWithTag("nav-settings").performClick()
        composeRule.onNodeWithTag("clear-snapshot").performScrollTo().assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun safFolderPickerPersistsFolderUriAcrossRecreate() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val folderName = "GitpullPicker"
        device.executeShellCommand("mkdir -p /sdcard/$folderName")

        composeRule.onNodeWithTag("choose-folder").performClick()
        device.wait(Until.hasObject(By.textContains("FOLDER")), 10_000)

        device.wait(Until.hasObject(By.text(folderName)), 10_000)
        device.findObject(By.text(folderName))?.click()
        device.wait(Until.hasObject(By.textContains("FOLDER")), 10_000)
        val useFolder = device.findObject(By.text("USE THIS FOLDER"))
            ?: device.findObject(By.text("Use this folder"))
            ?: device.findObject(By.textContains("FOLDER"))
        assertTrue("DocumentsUI should expose an enabled Use this folder action", useFolder != null && useFolder.isEnabled)
        useFolder.click()
        device.wait(Until.hasObject(By.textContains("llow")), 10_000)
        (device.findObject(By.text("ALLOW")) ?: device.findObject(By.text("Allow")))?.click()
        device.wait(Until.hasObject(By.pkg("dev.gitpull.app")), 10_000)

        composeRule.waitUntil(10_000) {
            persistedDestination().startsWith("content://")
        }
        val selected = persistedDestination()
        assertTrue(selected, selected.contains(folderName))

        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()

        assertTrue(persistedDestination().startsWith("content://"))
        composeRule.onNodeWithText("content://com.an", substring = true).assertIsDisplayed()
    }

    private fun seedConfiguredApp(): String {
        val destination = File(composeRule.activity.filesDir, "gitpull-test").apply {
            deleteRecursively()
            resolve("Papers").mkdirs()
            resolve("Papers/Gitpull-fixture.pdf").writeText("%PDF-1.4\n%%EOF\n")
        }
        val destinationUri = Uri.fromFile(destination).toString()
        composeRule.activity
            .getSharedPreferences("gitpull_settings", Context.MODE_PRIVATE)
            .edit()
            .putString("repo_url", "https://github.com/octocat/Hello-World")
            .putString("branch", "master")
            .putString("destination_uri", destinationUri)
            .putLong("last_pulled", 0L)
            .putString("last_status", "Ready to refresh")
            .putBoolean("token_configured", false)
            .commit()
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        return destinationUri
    }

    private fun persistedDestination(): String {
        return composeRule.activity
            .getSharedPreferences("gitpull_settings", Context.MODE_PRIVATE)
            .getString("destination_uri", "")
            .orEmpty()
    }

    private fun isObsidianLaunchAvailable(): Boolean {
        return composeRule.activity.packageManager.getLaunchIntentForPackage("md.obsidian") != null
    }
}
