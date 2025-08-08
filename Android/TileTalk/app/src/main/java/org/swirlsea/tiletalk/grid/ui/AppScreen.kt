
package org.swirlsea.tiletalk.grid.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.swirlsea.tiletalk.AuthViewModel
import org.swirlsea.tiletalk.MainViewModel
import org.swirlsea.tiletalk.TileTalkApp
import org.swirlsea.tiletalk.auth.ui.AuthDialog
import org.swirlsea.tiletalk.contacts.ui.ContactsDialog
import org.swirlsea.tiletalk.contacts.ui.ContactsViewModel
import org.swirlsea.tiletalk.data.User
import org.swirlsea.tiletalk.ui.DialogState
import org.swirlsea.tiletalk.ui.GridUiState
import org.swirlsea.tiletalk.ui.MainScreenEvent
import org.swirlsea.tiletalk.ui.MainUiState

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val app = application as TileTalkApp
            val container = app.container
            val authViewModel = AuthViewModel(application, container.loginRegisterUseCase, container.repository, container.sessionManager)
            val contactsViewModel = ContactsViewModel(container.contactsUseCase)

            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, container.repository, authViewModel, contactsViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showAboutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAdvancedSettingsDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            uiState.loggedInUser?.let { user ->
                mainViewModel.onEvent(MainScreenEvent.ExportKeyFile(user, fileUri))
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            uiState.loggedInUser?.let { user ->
                mainViewModel.onEvent(MainScreenEvent.ImportKeyFile(user, fileUri))
            }
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            mainViewModel.onEvent(MainScreenEvent.ClearSnackbar)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(250.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top
                ) {
                    IconButton(onClick = {
                        scope.launch {
                            drawerState.close()
                            delay(50)
                            showAdvancedSettingsDialog = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    if (uiState.loggedInUser == null) {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    delay(50)
                                    mainViewModel.onEvent(MainScreenEvent.ShowAuthDialog)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Login / Register",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                    } else {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    mainViewModel.onEvent(MainScreenEvent.Logout)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Logout ${uiState.loggedInUser?.username}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                delay(50)
                                mainViewModel.onEvent(MainScreenEvent.ShowContactsDialog)
                            }
                        },
                        enabled = uiState.loggedInUser != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Manage Contacts",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Divider()
                    Spacer(Modifier.height(16.dp))

                    TextButton(
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                delay(50)
                                showAboutDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "About",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                delay(50)
                                showHelpDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Help",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                delay(50)
                                showPrivacyDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Privacy",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("TileTalk") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            val configuration = LocalConfiguration.current
            val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.TopCenter
            ) {
                if (isLandscape) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            UserGridSection(uiState = uiState, mainViewModel = mainViewModel)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ContactGridSection(uiState = uiState, mainViewModel = mainViewModel)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .width(maxWidth * 0.8f), // Constrain width to 80% of the screen
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        UserGridSection(uiState = uiState, mainViewModel = mainViewModel)
                        Spacer(Modifier.height(16.dp))
                        ContactGridSection(uiState = uiState, mainViewModel = mainViewModel)
                    }
                }
            }

            // Dialogs are now correctly placed within the Scaffold's content lambda
            if (showAboutDialog) {
                AboutDialog(onDismiss = { showAboutDialog = false })
            }
            if (showHelpDialog) {
                HelpDialog(onDismiss = { showHelpDialog = false })
            }
            if (showPrivacyDialog) {
                PrivacyDialog(onDismiss = { showPrivacyDialog = false })
            }
            if (showAdvancedSettingsDialog) {
                AdvancedSettingsDialog(
                    onDismiss = { showAdvancedSettingsDialog = false },
                    onGenerateNewKey = {
                        uiState.loggedInUser?.let {
                            mainViewModel.onEvent(MainScreenEvent.GenerateNewKeyPair(it))
                        }
                        showAdvancedSettingsDialog = false
                    },
                    onExportKeyfile = {
                        uiState.loggedInUser?.let { user ->
                            exportLauncher.launch("${user.username}_keypair.json")
                        }
                    },
                    onImportKeyfile = {
                        importLauncher.launch(arrayOf("application/json"))
                        showAdvancedSettingsDialog = false
                    },
                    loggedInUser = uiState.loggedInUser,
                    encryptionKeyPair = uiState.encryptionKeyPair,
                )
            }

            when (val dialog = uiState.currentDialog) {
                is DialogState.Auth -> AuthDialog(
                    onDismiss = { mainViewModel.onEvent(MainScreenEvent.DismissDialog) },
                    onLogin = { user, pass -> mainViewModel.onEvent(MainScreenEvent.Login(user, pass)) },
                    onRegister = { user, pass -> mainViewModel.onEvent(MainScreenEvent.Register(user, pass)) }
                )
                is DialogState.EditingTile -> EditTileDialog(
                    dialogState = dialog,
                    onDismiss = { mainViewModel.onEvent(MainScreenEvent.DismissDialog) },
                    onSave = { symbol, animationType, flip, callout, title ->
                        mainViewModel.onEvent(
                            MainScreenEvent.SaveTileChanges(
                                dialog.tileOwnerId,
                                dialog.tileId,
                                dialog.x,
                                dialog.y,
                                symbol,
                                animationType,
                                flip,
                                callout,
                                title
                            )
                        )
                    },
                    onDelete = { tileId ->
                        mainViewModel.onEvent(MainScreenEvent.DeleteTile(tileId))
                    }
                )
                is DialogState.Contacts -> {
                    ContactsDialog(
                        contacts = dialog.contactList,
                        resolvedContacts = dialog.resolvedContacts,
                        resolvedIncoming = dialog.resolvedIncoming,
                        resolvedPending = dialog.resolvedPending,
                        onDismiss = { mainViewModel.onEvent(MainScreenEvent.DismissDialog) },
                        onSelectContact = { contactId ->
                            mainViewModel.onEvent(MainScreenEvent.SelectContact(contactId))
                            mainViewModel.onEvent(MainScreenEvent.DismissDialog)
                        },
                        onRequestContact = { username ->
                            mainViewModel.onEvent(MainScreenEvent.RequestContact(username))
                        },
                        onAcceptContact = { contactId ->
                            mainViewModel.onEvent(MainScreenEvent.AcceptContact(contactId))
                        },
                        onRemoveContact = { contactId ->
                            mainViewModel.onEvent(MainScreenEvent.RemoveContact(contactId))
                        },
                        onRefresh = {
                            mainViewModel.onEvent(MainScreenEvent.RefreshContacts)
                        }
                    )
                }
                is DialogState.ShowingMessages -> {
                    MessagesDialog(
                        dialogState = dialog,
                        onDismiss = { mainViewModel.onEvent(MainScreenEvent.DismissDialog) },
                        onDeleteMessage = { ownerId, x, y -> mainViewModel.onEvent(MainScreenEvent.DeleteMessage(ownerId, x, y)) },
                        onAddComment = { message ->
                            mainViewModel.onEvent(MainScreenEvent.AddComment(dialog.tileOwnerId, dialog.x, dialog.y, message))
                        },
                        onSaveEditedMessage = { message -> mainViewModel.onEvent(MainScreenEvent.SaveEditedMessage(message)) },
                        onStartEditing = { messageId, currentText -> mainViewModel.onEvent(MainScreenEvent.StartEditingMessage(messageId, currentText)) },
                        onCancelEditing = { mainViewModel.onEvent(MainScreenEvent.CancelEditingMessage) }
                    )
                }
                is DialogState.ConfirmKeyGeneration -> {
                    ConfirmKeyGenerationDialog(
                        dialogState = dialog,
                        onDismiss = { mainViewModel.onEvent(MainScreenEvent.DismissDialog) },
                        onConfirm = { mainViewModel.onEvent(MainScreenEvent.ConfirmGenerateNewKeyPair(dialog.user)) }
                    )
                }
                is DialogState.ConfirmRekey -> {
                    ConfirmRekeyDialog(
                        dialogState = dialog,
                        onDismiss = { mainViewModel.onEvent(MainScreenEvent.DismissDialog) },
                        onConfirm = { mainViewModel.onEvent(MainScreenEvent.ConfirmRekey(dialog.user)) }
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun UserGridSection(uiState: MainUiState, mainViewModel: MainViewModel) {
    val loggedInUser = uiState.loggedInUser
    Column {
        Text(
            text = if (loggedInUser != null) "Your Grid (${loggedInUser.username})" else "Your Grid (Offline)",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            textAlign = TextAlign.Start
        )
        val userGridState = uiState.userGrid ?: GridUiState(owner = User(id = -1, username = "Offline"))
        TileGridView(
            gridState = userGridState,
            onTileTap = { x, y ->
                loggedInUser?.let {
                    mainViewModel.onEvent(MainScreenEvent.TileTapped(it.id, x, y))
                }
            },
            onTileLongPress = { x, y ->
                loggedInUser?.let {
                    mainViewModel.onEvent(MainScreenEvent.TileLongPressed(it.id, x, y))
                }
            },
            selectedTile = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )
    }
}

@Composable
private fun ContactGridSection(uiState: MainUiState, mainViewModel: MainViewModel) {
    val loggedInUser = uiState.loggedInUser
    val selectedContact = uiState.selectedContact
    Column {
        ControlRow(
            loggedInUser = loggedInUser,
            currentPeerUser = selectedContact,
            onSelectPeerClick = {
                if (loggedInUser != null) {
                    mainViewModel.onEvent(MainScreenEvent.ShowContactsDialog)
                }
            }
        )
        val contactGridState = uiState.contactGrid ?: GridUiState(owner = User(id = -1, username = "No Contact Selected"))
        TileGridView(
            gridState = contactGridState,
            onTileTap = if (selectedContact != null) {
                { x, y ->
                    mainViewModel.onEvent(MainScreenEvent.TileTapped(contactGridState.owner.id, x, y))
                }
            } else {
                { _, _ -> }
            },
            onTileLongPress = if (selectedContact != null) {
                { x, y ->
                    mainViewModel.onEvent(MainScreenEvent.TileLongPressed(contactGridState.owner.id, x, y))
                }
            } else {
                { _, _ -> }
            },
            selectedTile = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )
    }
}
