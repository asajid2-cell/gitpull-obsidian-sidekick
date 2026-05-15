package dev.gitpull.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.gitpull.app.core.AppConfig
import dev.gitpull.app.core.GitHubArchiveClient
import dev.gitpull.app.core.GitHubOAuthClient
import dev.gitpull.app.core.GitHubRepository
import dev.gitpull.app.core.GitHubRepositoryClient
import dev.gitpull.app.core.PdfItem
import dev.gitpull.app.core.PullConfig
import dev.gitpull.app.core.PullService
import dev.gitpull.app.core.RepoRef
import dev.gitpull.app.core.RepoUrlParser
import dev.gitpull.app.core.SnapshotManifestComparator
import dev.gitpull.app.core.FileSnapshotWriter
import dev.gitpull.app.data.AndroidPdfIndexer
import dev.gitpull.app.data.AndroidSnapshotManifestBuilder
import dev.gitpull.app.data.AndroidSettingsRepository
import dev.gitpull.app.data.AndroidSnapshotTools
import dev.gitpull.app.data.ContentResolverSnapshotWriter
import dev.gitpull.app.data.SecureTokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GitpullTheme {
                GitpullApp(activity = this)
            }
        }
    }
}

private enum class AppTab { Pull, PDFs, Settings }
private enum class PullStatus { Idle, Pulling, Success, Error, MissingPermission, Offline }

private data class PendingGitHubSignIn(
    val deviceCode: String,
    val userCode: String,
    val verificationUri: String,
    val expiresAtMillis: Long,
    val intervalSeconds: Int
)

private object Tokens {
    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF334155)
    val TextMuted = Color(0xFF64748B)
    val Background = Color(0xFFF8FAFC)
    val Surface = Color.White
    val SurfaceSubtle = Color(0xFFF1F5F9)
    val Line = Color(0xFFE2E8F0)
    val Accent = Color(0xFF7C3AED)
    val AccentSoft = Color(0xFFF3E8FF)
    val Success = Color(0xFF16A34A)
    val SuccessSoft = Color(0xFFDCFCE7)
    val Warning = Color(0xFFD97706)
    val WarningSoft = Color(0xFFFEF3C7)
    val Danger = Color(0xFFDC2626)
    val DangerSoft = Color(0xFFFEE2E2)
}

@Composable
private fun GitpullTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Tokens.TextPrimary,
            secondary = Tokens.Accent,
            background = Tokens.Background,
            surface = Tokens.Surface,
            error = Tokens.Danger
        ),
        typography = MaterialTheme.typography.copy(
            titleLarge = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
            titleMedium = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
            labelSmall = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
            labelLarge = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold)
        ),
        content = content
    )
}

@Composable
private fun GitpullApp(activity: ComponentActivity) {
    val settingsRepository = remember { AndroidSettingsRepository(activity) }
    val tokenStore = remember { SecureTokenStore(activity) }
    val oauthClient = remember { GitHubOAuthClient(BuildConfig.GITHUB_OAUTH_CLIENT_ID) }
    val pdfIndexer = remember { AndroidPdfIndexer(activity) }
    val manifestBuilder = remember { AndroidSnapshotManifestBuilder(activity) }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var config by remember { mutableStateOf(settingsRepository.load()) }
    var tab by remember { mutableStateOf(AppTab.Pull) }
    var pullStatus by remember { mutableStateOf(PullStatus.Idle) }
    var statusMessage by remember { mutableStateOf(config.lastStatus) }
    var pdfs by remember { mutableStateOf(emptyList<PdfItem>()) }
    var setupError by remember { mutableStateOf<String?>(null) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }
    var confirmClear by remember { mutableStateOf(false) }
    var confirmLocalOverwrite by remember { mutableStateOf(false) }
    var localChangeMessage by remember { mutableStateOf("") }
    var repoBrowserLoading by remember { mutableStateOf(false) }
    var repoBrowserMessage by remember { mutableStateOf<String?>(null) }
    var repoBrowserItems by remember { mutableStateOf(emptyList<GitHubRepository>()) }
    var repoBrowserNextPage by remember { mutableStateOf<Int?>(null) }
    var oauthLoading by remember { mutableStateOf(false) }
    var pendingSignIn by remember { mutableStateOf<PendingGitHubSignIn?>(null) }
    var signInPollRequest by remember { mutableStateOf(0) }

    val loadRepositoryPage: (String, Int, Boolean) -> Unit = { token, page, append ->
        scope.launch {
            repoBrowserLoading = true
            repoBrowserMessage = if (append) "Loading more repositories..." else "Loading repositories..."
            val result = withContext(Dispatchers.IO) {
                runCatching { GitHubRepositoryClient().listRepositoriesPage(token, page) }
            }
            result.onSuccess { repoPage ->
                val nextItems = if (append) repoBrowserItems + repoPage.repositories else repoPage.repositories
                repoBrowserItems = nextItems
                repoBrowserNextPage = repoPage.nextPage
                repoBrowserMessage = if (nextItems.isEmpty()) {
                    "No repositories were returned for this GitHub account."
                } else {
                    "Loaded ${nextItems.size} repositories."
                }
            }.onFailure { error ->
                repoBrowserMessage = "Could not load repositories. ${error.message.orEmpty()}"
            }
            repoBrowserLoading = false
        }
    }

    LaunchedEffect(config.destinationTreeUri) {
        pdfs = pdfIndexer.indexTree(config.destinationTreeUri)
    }

    DisposableEffect(lifecycleOwner, pendingSignIn?.deviceCode) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && pendingSignIn != null) {
                signInPollRequest += 1
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(pendingSignIn?.deviceCode, signInPollRequest) {
        val pending = pendingSignIn ?: return@LaunchedEffect
        if (System.currentTimeMillis() >= pending.expiresAtMillis) {
            pendingSignIn = null
            oauthLoading = false
            dialogMessage = "GitHub sign-in expired. Try again."
            return@LaunchedEffect
        }

        var intervalSeconds = pending.intervalSeconds.coerceAtLeast(5)
        if (signInPollRequest == 0) {
            delay(intervalSeconds * 1000L)
        }

        while (pendingSignIn?.deviceCode == pending.deviceCode && System.currentTimeMillis() < pending.expiresAtMillis) {
            oauthLoading = true
            val pollResult = withContext(Dispatchers.IO) {
                runCatching { oauthClient.pollDeviceCode(pending.deviceCode) }
            }
            oauthLoading = false

            pollResult.onSuccess { token ->
                tokenStore.save(token)
                val next = config.copy(tokenConfigured = true)
                config = next
                settingsRepository.save(next)
                pendingSignIn = null
                repoBrowserMessage = "Signed in with GitHub."
                loadRepositoryPage(token, 1, false)
                return@LaunchedEffect
            }

            val error = pollResult.exceptionOrNull()
            if (error is GitHubOAuthClient.AuthorizationPendingException) {
                if (error.message == "slow_down") intervalSeconds += 5
                repoBrowserMessage = "Waiting for GitHub approval. Return here after approving access."
            } else {
                pendingSignIn = null
                dialogMessage = error?.message ?: "GitHub sign-in failed."
                return@LaunchedEffect
            }

            delay(intervalSeconds * 1000L)
        }

        if (pendingSignIn?.deviceCode == pending.deviceCode) {
            pendingSignIn = null
            oauthLoading = false
            dialogMessage = "GitHub sign-in expired. Try again."
        }
    }

    val startGitHubSignIn: () -> Unit = {
        if (!oauthClient.isConfigured) {
            dialogMessage = "GitHub sign-in needs a GitHub OAuth client ID in this build."
        } else {
            scope.launch {
                oauthLoading = true
                repoBrowserMessage = "Starting GitHub sign-in..."
                val deviceResult = withContext(Dispatchers.IO) {
                    runCatching { oauthClient.requestDeviceCode() }
                }
                deviceResult.onSuccess { device ->
                    pendingSignIn = PendingGitHubSignIn(
                        deviceCode = device.deviceCode,
                        userCode = device.userCode,
                        verificationUri = device.verificationUri,
                        expiresAtMillis = System.currentTimeMillis() + device.expiresInSeconds * 1000L,
                        intervalSeconds = device.intervalSeconds
                    )
                    signInPollRequest += 1
                    repoBrowserMessage = "Enter ${device.userCode} in GitHub, then return here."
                    try {
                        activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(device.verificationUri)))
                    } catch (_: ActivityNotFoundException) {
                        dialogMessage = "Open ${device.verificationUri} and enter ${device.userCode}."
                    }
                }.onFailure { error ->
                    dialogMessage = error.message ?: "GitHub sign-in failed."
                }
                oauthLoading = false
            }
        }
    }

    val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            activity.contentResolver.takePersistableUriPermission(uri, flags)
            val next = config.copy(destinationTreeUri = uri.toString())
            config = next
            settingsRepository.save(next)
            setupError = null
        }
    }

    val loadRepositories: (String) -> Unit = { tokenOverride ->
        val token = tokenOverride.ifBlank { tokenStore.load().orEmpty() }.trim()
        if (token.isBlank()) {
            repoBrowserMessage = "Sign in with GitHub before loading repositories."
        } else {
            loadRepositoryPage(token, 1, false)
        }
    }

    val loadMoreRepositories: () -> Unit = {
        val nextPage = repoBrowserNextPage
        val token = tokenStore.load().orEmpty().trim()
        if (token.isBlank()) {
            repoBrowserMessage = "Sign in with GitHub before loading more repositories."
        } else if (nextPage == null) {
            repoBrowserMessage = "All loaded repositories are already shown."
        } else {
            loadRepositoryPage(token, nextPage, true)
        }
    }

    if (!config.isReady) {
        SetupScreen(
            initialConfig = config,
            error = setupError,
            onChooseFolder = { folderLauncher.launch(null) },
            repoBrowserLoading = repoBrowserLoading,
            repoBrowserMessage = repoBrowserMessage,
            repoBrowserItems = repoBrowserItems,
            repoBrowserCanLoadMore = repoBrowserNextPage != null,
            tokenConfigured = config.tokenConfigured,
            oauthConfigured = oauthClient.isConfigured,
            oauthLoading = oauthLoading,
            pendingSignIn = pendingSignIn,
            onStartGitHubSignIn = startGitHubSignIn,
            onCheckGitHubSignIn = { signInPollRequest += 1 },
            onCopyGitHubCode = { repoBrowserMessage = "Copied GitHub code ${pendingSignIn?.userCode.orEmpty()}." },
            onSignOut = {
                tokenStore.clear()
                pendingSignIn = null
                val next = config.copy(tokenConfigured = false)
                config = next
                settingsRepository.save(next)
                repoBrowserItems = emptyList()
                repoBrowserNextPage = null
                repoBrowserMessage = "Signed out of GitHub."
            },
            onLoadRepositories = loadRepositories,
            onLoadMoreRepositories = loadMoreRepositories,
            onSave = { repoUrl, branch, token ->
                val parsed = RepoUrlParser.parse(repoUrl, branch)
                if (parsed.isFailure) {
                    setupError = parsed.exceptionOrNull()?.message ?: "Invalid GitHub repo"
                    return@SetupScreen
                }
                if (config.destinationTreeUri.isBlank()) {
                    setupError = "Choose a destination folder first"
                    return@SetupScreen
                }
                val credentialConfigured = if (token.isNotBlank()) {
                    tokenStore.save(token)
                    true
                } else {
                    config.tokenConfigured
                }
                val next = config.copy(
                    repoUrl = repoUrl.trim(),
                    branch = branch.ifBlank { RepoRef.DEFAULT_BRANCH },
                    tokenConfigured = credentialConfigured
                )
                settingsRepository.save(next)
                config = next
                setupError = null
            }
        )
        return
    }

    val runPull: () -> Unit = {
        scope.launch {
            pullStatus = PullStatus.Pulling
            statusMessage = "Downloading latest snapshot"
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val repo = RepoUrlParser.parse(config.repoUrl, config.branch).getOrThrow()
                    val client = GitHubArchiveClient()
                    val service = PullService(archiveDownloader = { pullConfig, output ->
                        client.downloadArchive(pullConfig.repo, pullConfig.token, output)
                    })
                    val destinationUri = Uri.parse(config.destinationTreeUri)
                    val writer = if (destinationUri.scheme == "file") {
                        FileSnapshotWriter(java.io.File(requireNotNull(destinationUri.path)))
                    } else {
                        ContentResolverSnapshotWriter(
                            activity,
                            activity.contentResolver,
                            destinationUri
                        )
                    }
                    service.pull(PullConfig(repo, tokenStore.load(), activity.cacheDir.resolve("pull")), writer)
                }
            }
            result.onSuccess { report ->
                pdfs = pdfIndexer.indexTree(config.destinationTreeUri)
                settingsRepository.saveLastManifest(report.manifest)
                pullStatus = PullStatus.Success
                statusMessage = "Snapshot ready. Obsidian can open the selected folder."
                val next = config.copy(
                    lastPulledAtMillis = System.currentTimeMillis(),
                    lastStatus = statusMessage
                )
                config = next
                settingsRepository.save(next)
            }.onFailure { error ->
                pullStatus = PullStatus.Error
                statusMessage = "Pull failed. Your previous snapshot was kept. ${error.message.orEmpty()}"
            }
        }
    }

    val requestPull: () -> Unit = {
        val lastManifest = settingsRepository.loadLastManifest()
        if (lastManifest.files.isNotEmpty()) {
            val currentManifest = manifestBuilder.build(config.destinationTreeUri)
            val changes = SnapshotManifestComparator.localChanges(lastManifest, currentManifest)
            if (changes.hasChanges) {
                pullStatus = PullStatus.MissingPermission
                localChangeMessage = changes.summary
                statusMessage = "Local edits may be overwritten: ${changes.summary}"
                confirmLocalOverwrite = true
            } else {
                runPull()
            }
        } else {
            runPull()
        }
    }

    Scaffold(
        containerColor = Tokens.Background,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .navigationBarsPadding()
                    .background(Tokens.Surface),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationItem(AppTab.Pull, tab, Icons.Default.Download, "Pull", Modifier.weight(1f)) { tab = AppTab.Pull }
                NavigationItem(AppTab.PDFs, tab, Icons.Default.Article, "PDFs", Modifier.weight(1f)) {
                    pdfs = pdfIndexer.indexTree(config.destinationTreeUri)
                    tab = AppTab.PDFs
                }
                NavigationItem(AppTab.Settings, tab, Icons.Default.Settings, "Settings", Modifier.weight(1f)) { tab = AppTab.Settings }
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            when (tab) {
                AppTab.Pull -> PullScreen(
                    config = config,
                    status = pullStatus,
                    message = statusMessage,
                    onPull = requestPull,
                    onOpenObsidian = { dialogMessage = activity.openObsidianOrMessage(config.destinationTreeUri) }
                )
                AppTab.PDFs -> PdfScreen(
                    pdfs = pdfs,
                    onOpenPdf = { pdf -> dialogMessage = activity.openPdfOrMessage(pdf) }
                )
                AppTab.Settings -> SettingsScreen(
                    config = config,
                    onChooseFolder = { folderLauncher.launch(null) },
                    repoBrowserLoading = repoBrowserLoading,
                    repoBrowserMessage = repoBrowserMessage,
                    repoBrowserItems = repoBrowserItems,
                    repoBrowserCanLoadMore = repoBrowserNextPage != null,
                    tokenConfigured = config.tokenConfigured,
                    oauthConfigured = oauthClient.isConfigured,
                    oauthLoading = oauthLoading,
                    pendingSignIn = pendingSignIn,
                    onStartGitHubSignIn = startGitHubSignIn,
                    onCheckGitHubSignIn = { signInPollRequest += 1 },
                    onCopyGitHubCode = { repoBrowserMessage = "Copied GitHub code ${pendingSignIn?.userCode.orEmpty()}." },
                    onLoadRepositories = { loadRepositories("") },
                    onLoadMoreRepositories = loadMoreRepositories,
                    onSaveRepo = { repoUrl, branch ->
                        val parsed = RepoUrlParser.parse(repoUrl, branch)
                        if (parsed.isFailure) {
                            dialogMessage = parsed.exceptionOrNull()?.message
                        } else {
                            val next = config.copy(repoUrl = repoUrl.trim(), branch = branch.ifBlank { RepoRef.DEFAULT_BRANCH })
                            config = next
                            settingsRepository.save(next)
                        }
                    },
                    onSaveToken = { token ->
                        if (token.isBlank()) {
                            tokenStore.clear()
                            val next = config.copy(tokenConfigured = false)
                            config = next
                            settingsRepository.save(next)
                        } else {
                            tokenStore.save(token)
                            val next = config.copy(tokenConfigured = true)
                            config = next
                            settingsRepository.save(next)
                        }
                    },
                    onSignOut = {
                        tokenStore.clear()
                        pendingSignIn = null
                        val next = config.copy(tokenConfigured = false)
                        config = next
                        settingsRepository.save(next)
                        repoBrowserItems = emptyList()
                        repoBrowserNextPage = null
                        repoBrowserMessage = "Signed out of GitHub."
                    },
                    onClearSnapshot = { confirmClear = true }
                )
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear local snapshot") },
            text = { Text("This removes files from the selected local snapshot folder. GitHub remains unchanged.") },
            confirmButton = {
                TextButton(onClick = {
                    val deleted = AndroidSnapshotTools.clearTree(activity, config.destinationTreeUri)
                    settingsRepository.clearSnapshotMetadata()
                    config = config.copy(lastPulledAtMillis = 0L, lastStatus = "Ready to refresh")
                    pdfs = emptyList()
                    statusMessage = "Cleared $deleted local items"
                    confirmClear = false
                }) { Text("Clear", color = Tokens.Danger) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            }
        )
    }

    if (confirmLocalOverwrite) {
        AlertDialog(
            onDismissRequest = { confirmLocalOverwrite = false },
            title = { Text("Local edits may be overwritten") },
            text = { Text("gitpull detected local changes since the last pull: $localChangeMessage. Continue only if GitHub should replace this local snapshot.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmLocalOverwrite = false
                    runPull()
                }) { Text("Pull anyway", color = Tokens.Warning) }
            },
            dismissButton = {
                TextButton(onClick = { confirmLocalOverwrite = false }) { Text("Cancel") }
            }
        )
    }

    dialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { dialogMessage = null },
            title = { Text("gitpull") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { dialogMessage = null }) { Text("OK") } }
        )
    }
}

@Composable
private fun NavigationItem(
    tab: AppTab,
    selected: AppTab,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val active = selected == tab
    Column(
        modifier = modifier
            .height(72.dp)
            .testTag("nav-${label.lowercase()}")
            .clickable(onClick = onClick)
            .semantics { contentDescription = label },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = if (active) Tokens.Accent else Tokens.TextMuted)
        Text(label, color = if (active) Tokens.Accent else Tokens.TextMuted, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SetupScreen(
    initialConfig: AppConfig,
    error: String?,
    onChooseFolder: () -> Unit,
    repoBrowserLoading: Boolean,
    repoBrowserMessage: String?,
    repoBrowserItems: List<GitHubRepository>,
    repoBrowserCanLoadMore: Boolean,
    tokenConfigured: Boolean,
    oauthConfigured: Boolean,
    oauthLoading: Boolean,
    pendingSignIn: PendingGitHubSignIn?,
    onStartGitHubSignIn: () -> Unit,
    onCheckGitHubSignIn: () -> Unit,
    onCopyGitHubCode: () -> Unit,
    onSignOut: () -> Unit,
    onLoadRepositories: (String) -> Unit,
    onLoadMoreRepositories: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var repoUrl by remember { mutableStateOf(initialConfig.repoUrl) }
    var branch by remember { mutableStateOf(initialConfig.branch.ifBlank { RepoRef.DEFAULT_BRANCH }) }
    var token by remember { mutableStateOf("") }

    Surface(color = Tokens.Background, modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppHeader("gitpull", "Pull vault snapshots")
            OutlinedTextField(
                value = repoUrl,
                onValueChange = { repoUrl = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("repo-field"),
                label = { Text("GitHub repo") },
                singleLine = true
            )
            OutlinedTextField(
                value = branch,
                onValueChange = { branch = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("branch-field"),
                label = { Text("Branch") },
                singleLine = true
            )
            FolderSelectorCard(
                destination = initialConfig.destinationTreeUri,
                onChooseFolder = onChooseFolder
            )
            GitHubAuthCard(
                tokenConfigured = tokenConfigured,
                oauthConfigured = oauthConfigured,
                oauthLoading = oauthLoading,
                pendingSignIn = pendingSignIn,
                onStartGitHubSignIn = onStartGitHubSignIn,
                onCheckGitHubSignIn = onCheckGitHubSignIn,
                onCopyGitHubCode = onCopyGitHubCode,
                onSignOut = onSignOut
            )
            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("token-field"),
                label = { Text("Personal access token fallback") },
                singleLine = true
            )
            RepositoryBrowserSection(
                loading = repoBrowserLoading,
                message = repoBrowserMessage,
                repositories = repoBrowserItems,
                canLoadMore = repoBrowserCanLoadMore,
                onLoad = { onLoadRepositories(token) },
                onLoadMore = onLoadMoreRepositories,
                onSelect = { repo ->
                    repoUrl = repo.htmlUrl
                    branch = repo.defaultBranch
                }
            )
            WarningBanner()
            if (error != null) StatusCard("Setup error", error, PullStatus.Error)
            Button(
                onClick = { onSave(repoUrl, branch, token) },
                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("save-setup")
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun PullScreen(
    config: AppConfig,
    status: PullStatus,
    message: String,
    onPull: () -> Unit,
    onOpenObsidian: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).testTag("pull-list"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { AppHeader("gitpull", "Pull from GitHub") }
        item { RepoSummaryCard(config) }
        item {
            Button(
                onClick = onPull,
                enabled = status != PullStatus.Pulling,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Tokens.TextPrimary,
                    contentColor = Tokens.Surface,
                    disabledContainerColor = Color(0xFFCBD5E1)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .semantics { contentDescription = "Pull latest vault snapshot" }
                    .testTag("pull-button")
            ) {
                if (status == PullStatus.Pulling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Tokens.Surface,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Pulling...", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text(if (status == PullStatus.Error) "Retry Pull" else "Pull", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        item { StatusCard(statusTitle(status), message, status) }
        item {
            OutlinedButton(
                onClick = onOpenObsidian,
                modifier = Modifier.fillMaxWidth().height(56.dp).testTag("open-obsidian")
            ) {
                Icon(Icons.Default.Smartphone, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Open Obsidian")
            }
        }
    }
}

@Composable
private fun PdfScreen(pdfs: List<PdfItem>, onOpenPdf: (PdfItem) -> Unit) {
    var query by remember { mutableStateOf("") }
    val filtered = pdfs.filter {
        it.filename.contains(query, ignoreCase = true) || it.relativePath.contains(query, ignoreCase = true)
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { AppHeader("PDFs", "${filtered.size} documents") }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                label = { Text("Search PDFs") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("pdf-search")
            )
        }
        if (filtered.isEmpty()) {
            item { EmptyCard("No PDFs found", "No PDFs found in this vault snapshot.") }
        } else {
            items(filtered) { pdf ->
                PdfRow(pdf = pdf, onOpenPdf = onOpenPdf)
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    config: AppConfig,
    onChooseFolder: () -> Unit,
    repoBrowserLoading: Boolean,
    repoBrowserMessage: String?,
    repoBrowserItems: List<GitHubRepository>,
    repoBrowserCanLoadMore: Boolean,
    tokenConfigured: Boolean,
    oauthConfigured: Boolean,
    oauthLoading: Boolean,
    pendingSignIn: PendingGitHubSignIn?,
    onStartGitHubSignIn: () -> Unit,
    onCheckGitHubSignIn: () -> Unit,
    onCopyGitHubCode: () -> Unit,
    onLoadRepositories: () -> Unit,
    onLoadMoreRepositories: () -> Unit,
    onSaveRepo: (String, String) -> Unit,
    onSaveToken: (String) -> Unit,
    onSignOut: () -> Unit,
    onClearSnapshot: () -> Unit
) {
    var repoUrl by remember(config.repoUrl) { mutableStateOf(config.repoUrl) }
    var branch by remember(config.branch) { mutableStateOf(config.branch) }
    var token by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .testTag("settings-list"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppHeader("Settings", "Repository and safety")
        ComponentCard {
            Text("Repository", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = repoUrl,
                onValueChange = { repoUrl = it },
                label = { Text("GitHub repo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = branch,
                onValueChange = { branch = it },
                label = { Text("Branch") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = { onSaveRepo(repoUrl, branch) }, modifier = Modifier.height(48.dp)) { Text("Save repository") }
        }
        RepositoryBrowserSection(
            loading = repoBrowserLoading,
            message = repoBrowserMessage,
            repositories = repoBrowserItems,
            canLoadMore = repoBrowserCanLoadMore,
            onLoad = onLoadRepositories,
            onLoadMore = onLoadMoreRepositories,
            onSelect = { repo ->
                repoUrl = repo.htmlUrl
                branch = repo.defaultBranch
            }
        )
        FolderSelectorCard(config.destinationTreeUri, onChooseFolder)
        GitHubAuthCard(
            tokenConfigured = tokenConfigured,
            oauthConfigured = oauthConfigured,
            oauthLoading = oauthLoading,
            pendingSignIn = pendingSignIn,
            onStartGitHubSignIn = onStartGitHubSignIn,
            onCheckGitHubSignIn = onCheckGitHubSignIn,
            onCopyGitHubCode = onCopyGitHubCode,
            onSignOut = onSignOut
        )
        ComponentCard {
            Text("Token fallback", style = MaterialTheme.typography.titleMedium)
            Text(
                if (config.tokenConfigured) "A GitHub credential is saved" else "Paste a token only if browser sign-in is not available",
                color = Tokens.TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text("Personal access token fallback") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onSaveToken(token) }, modifier = Modifier.height(48.dp)) { Text("Save token") }
                OutlinedButton(onClick = { token = ""; onSaveToken("") }, modifier = Modifier.height(48.dp)) { Text("Remove") }
            }
        }
        WarningBanner()
        OutlinedButton(
            onClick = onClearSnapshot,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Tokens.Danger),
            border = BorderStroke(1.dp, Tokens.Danger),
            modifier = Modifier.fillMaxWidth().height(56.dp).testTag("clear-snapshot")
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Clear local snapshot")
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun GitHubAuthCard(
    tokenConfigured: Boolean,
    oauthConfigured: Boolean,
    oauthLoading: Boolean,
    pendingSignIn: PendingGitHubSignIn?,
    onStartGitHubSignIn: () -> Unit,
    onCheckGitHubSignIn: () -> Unit,
    onCopyGitHubCode: () -> Unit,
    onSignOut: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    ComponentCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Tokens.Accent)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("GitHub sign-in", style = MaterialTheme.typography.titleMedium)
                Text(
                    when {
                        tokenConfigured -> "Signed in. Repository browsing and private pulls can use your GitHub account."
                        oauthConfigured -> "Use browser sign-in to browse and pull repositories without pasting a token."
                        else -> "Browser sign-in needs a GitHub OAuth client ID in this build."
                    },
                    color = Tokens.TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onStartGitHubSignIn,
                enabled = !oauthLoading && oauthConfigured,
                modifier = Modifier.height(48.dp).testTag("github-sign-in")
            ) {
                if (oauthLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        when {
                            tokenConfigured -> "Sign in again"
                            oauthConfigured -> "Sign in with GitHub"
                            else -> "OAuth client ID needed"
                        }
                    )
                }
            }
            if (tokenConfigured) {
                OutlinedButton(onClick = onSignOut, modifier = Modifier.height(48.dp).testTag("github-sign-out")) {
                    Text("Sign out")
                }
            }
        }
        if (pendingSignIn != null) {
            Spacer(Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Tokens.AccentSoft),
                border = BorderStroke(1.dp, Tokens.Line),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("github-device-code-card")
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("GitHub code", style = MaterialTheme.typography.labelSmall, color = Tokens.TextMuted)
                    Text(
                        pendingSignIn.userCode,
                        color = Tokens.TextPrimary,
                        fontSize = 24.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("github-device-code")
                    )
                    Text(
                        pendingSignIn.verificationUri,
                        color = Tokens.TextMuted,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(pendingSignIn.userCode))
                                onCopyGitHubCode()
                            },
                            modifier = Modifier.height(48.dp).testTag("copy-github-code")
                        ) {
                            Text("Copy code")
                        }
                        Button(
                            onClick = onCheckGitHubSignIn,
                            enabled = !oauthLoading,
                            modifier = Modifier.height(48.dp).testTag("check-github-sign-in")
                        ) {
                            if (oauthLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Check sign-in")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RepositoryBrowserSection(
    loading: Boolean,
    message: String?,
    repositories: List<GitHubRepository>,
    canLoadMore: Boolean,
    onLoad: () -> Unit,
    onLoadMore: () -> Unit,
    onSelect: (GitHubRepository) -> Unit
) {
    ComponentCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("GitHub repositories", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Load repos available to your GitHub account, then choose the vault repo. More pages load only when you ask.",
                    color = Tokens.TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(
                onClick = onLoad,
                enabled = !loading,
                modifier = Modifier.height(48.dp).testTag("load-repositories")
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Load")
                }
            }
        }
        if (message != null) {
            Spacer(Modifier.height(8.dp))
            Text(message, color = Tokens.TextMuted, style = MaterialTheme.typography.bodyMedium)
        }
        repositories.forEach { repo ->
            Spacer(Modifier.height(10.dp))
            RepositoryRow(repo = repo, onSelect = { onSelect(repo) })
        }
        if (canLoadMore) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onLoadMore,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("load-more-repositories")
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Load more")
                }
            }
        }
    }
}

@Composable
private fun RepositoryRow(repo: GitHubRepository, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .testTag("repo-browser-row"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (repo.privateRepo) Icons.Default.Lock else Icons.Default.Public,
            contentDescription = null,
            tint = Tokens.Accent
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(repo.fullName, color = Tokens.TextPrimary, fontWeight = FontWeight.SemiBold)
            Text(repo.defaultBranch, color = Tokens.TextMuted, style = MaterialTheme.typography.bodyMedium)
        }
        OutlinedButton(onClick = onSelect, modifier = Modifier.height(48.dp)) {
            Text("Use")
        }
    }
}

@Composable
private fun AppHeader(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = Tokens.TextPrimary)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Tokens.TextMuted)
    }
}

@Composable
private fun RepoSummaryCard(config: AppConfig) {
    val repo = RepoUrlParser.parse(config.repoUrl, config.branch).getOrNull()
    ComponentCard {
        Text("Repository", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(repo?.slug ?: config.repoUrl, color = Tokens.TextPrimary, fontWeight = FontWeight.SemiBold)
        Text(config.branch.ifBlank { RepoRef.DEFAULT_BRANCH }, color = Tokens.TextMuted)
        Text(config.destinationTreeUri, color = Tokens.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("Last pulled ${formatTimestamp(config.lastPulledAtMillis)}", color = Tokens.TextMuted)
    }
}

@Composable
private fun FolderSelectorCard(destination: String, onChooseFolder: () -> Unit) {
    ComponentCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Folder, contentDescription = null, tint = Tokens.Accent)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Destination folder", style = MaterialTheme.typography.titleMedium)
                Text(
                    destination.ifBlank { "Choose folder" },
                    color = Tokens.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            OutlinedButton(onClick = onChooseFolder, modifier = Modifier.height(48.dp).testTag("choose-folder")) {
                Text("Choose")
            }
        }
    }
}

@Composable
private fun StatusCard(title: String, body: String, status: PullStatus) {
    val (icon, tint, bg) = when (status) {
        PullStatus.Success -> Triple(Icons.Default.CheckCircle, Tokens.Success, Tokens.SuccessSoft)
        PullStatus.Error -> Triple(Icons.Default.Error, Tokens.Danger, Tokens.DangerSoft)
        PullStatus.MissingPermission, PullStatus.Offline -> Triple(Icons.Default.Warning, Tokens.Warning, Tokens.WarningSoft)
        else -> Triple(Icons.Default.Download, Tokens.TextMuted, Tokens.Surface)
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = bg),
        border = BorderStroke(1.dp, Tokens.Line),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().testTag("status-card")
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = title, tint = tint)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, color = Tokens.TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(body, color = Tokens.TextSecondary)
            }
        }
    }
}

@Composable
private fun WarningBanner() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Tokens.WarningSoft),
        border = BorderStroke(1.dp, Tokens.Line),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().testTag("source-warning")
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Tokens.Warning)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("GitHub is source of truth", color = Tokens.TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("Pulling replaces the selected local snapshot with the repository version.", color = Tokens.TextSecondary)
            }
        }
    }
}

@Composable
private fun PdfRow(pdf: PdfItem, onOpenPdf: (PdfItem) -> Unit) {
    ComponentCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable { onOpenPdf(pdf) }
            .testTag("pdf-row")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Article, contentDescription = null, tint = Tokens.Accent)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(pdf.filename, color = Tokens.TextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(pdf.relativePath, color = Tokens.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = { onOpenPdf(pdf) }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.OpenInNew, contentDescription = "Open with")
            }
        }
    }
}

@Composable
private fun EmptyCard(title: String, body: String) {
    ComponentCard {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(body, color = Tokens.TextMuted)
    }
}

@Composable
private fun ComponentCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Tokens.Surface),
        border = BorderStroke(1.dp, Tokens.Line),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

private fun statusTitle(status: PullStatus): String {
    return when (status) {
        PullStatus.Idle -> "Ready to refresh"
        PullStatus.Pulling -> "Downloading"
        PullStatus.Success -> "Snapshot ready"
        PullStatus.Error -> "Pull failed"
        PullStatus.MissingPermission -> "Permission needed"
        PullStatus.Offline -> "Offline"
    }
}

private fun formatTimestamp(millis: Long): String {
    if (millis <= 0L) return "never"
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(millis))
}

private fun ComponentActivity.openPdfOrMessage(pdf: PdfItem): String? {
    val uri = Uri.parse(pdf.uriString)
    val viewUri = if (uri.scheme == "file") {
        FileProvider.getUriForFile(this, "$packageName.fileprovider", File(requireNotNull(uri.path)))
    } else {
        uri
    }
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(viewUri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return try {
        startActivity(Intent.createChooser(intent, "Open with"))
        null
    } catch (_: ActivityNotFoundException) {
        "No PDF app is installed. Install Samsung Notes or another PDF reader, then try again."
    } catch (_: IllegalArgumentException) {
        "This PDF could not be opened from the current snapshot."
    } catch (_: SecurityException) {
        "gitpull could not grant read access for this PDF."
    }
}

private fun ComponentActivity.openObsidianOrMessage(destination: String): String? {
    val intent = packageManager.getLaunchIntentForPackage("md.obsidian")
    return if (intent != null) {
        startActivity(intent)
        null
    } else {
        "Obsidian is not installed or cannot be launched directly. Open Obsidian Mobile and select this folder:\n$destination"
    }
}
