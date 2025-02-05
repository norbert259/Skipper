package com.nksoftware.library.composables

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nksoftware.library.R
import com.nksoftware.library.theme.SkipperTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NkScaffold(
    ctx: ComponentActivity,
    title: String,
    topButtons: @Composable ((String) -> Unit) -> Unit,
    optionContent: @Composable ((String) -> Unit) -> Unit,
    content: @Composable ((String) -> Unit) -> Unit,
    bottomSheeetContent: @Composable ((String) -> Unit) -> Unit,
) {

    val scaffoldState = rememberBottomSheetScaffoldState()
    val snackbarHostState = remember { SnackbarHostState() }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    fun mySnackBar(message: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message, duration = SnackbarDuration.Short
            )
        }
    }

    SkipperTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = false,
                scrimColor = Color.White,
                drawerContent = {
                    ModalDrawerSheet {
                        Column(modifier = Modifier.padding(horizontal = 0.dp)) {
                            NkRowNValues(
                                modifier = Modifier
                                    .padding(5.dp)
                                    .fillMaxWidth(),
                                arrangement = Arrangement.SpaceBetween,
                            ) {
                                NkIconButton(
                                    onClick = { coroutineScope.launch { drawerState.close() } },
                                    icon = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "Exit Menu"
                                )
                                NkText(text = stringResource(R.string.options), size = 16)
                                NkIconButton(
                                    onClick = { coroutineScope.launch { ctx.finish() } },
                                    icon = Icons.AutoMirrored.Outlined.ExitToApp,
                                    contentDescription = "Close App"
                                )
                            }

                            HorizontalDivider()
                            Column(
                                modifier = Modifier.fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                optionContent(::mySnackBar)
                            }
                        }
                    }
                }) {

                BottomSheetScaffold(
                    scaffoldState = scaffoldState,

                    topBar = {
                        TopAppBar(
                            title = { Text(text = title) },
                            navigationIcon = {
                                NkAppBarButton(
                                    onClick = { coroutineScope.launch { drawerState.open() } },
                                    icon = Icons.Filled.Menu,
                                    contentDescription = "Main Menu"
                                )
                            },
                            actions = { topButtons(::mySnackBar) }
                        )
                    },

                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    sheetContent = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            bottomSheeetContent(::mySnackBar)
                        }
                    },

                    content = { padding ->
                        Column(modifier = Modifier.padding(padding)) {
                            content(::mySnackBar)
                        }
                    }
                )
            }
        }
    }
}