package com.example.presentation.ui.note_list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.presentation.R
import com.example.presentation.common.AppSnackbarHost
import com.example.presentation.common.AttachedImageThumbnail
import java.text.DateFormat

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesListScreen(
    vm: NotesListViewModel = hiltViewModel(),
    onOpenNote: (Int?) -> Unit
) {
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDialogForId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        vm.onEvent(NoteListEvent.Load)
        vm.effect.collect { eff ->
            when (eff) {
                is NoteListEffect.NavigateToEditor -> onOpenNote(eff.id)
                is NoteListEffect.ShowToast -> snackbarHostState.showSnackbar(eff.text)
                is NoteListEffect.ShowOptionsDialog -> showDialogForId = eff.id
            }
        }
    }

    if (showDialogForId != null) {
        AlertDialog(
            onDismissRequest = { showDialogForId = null },
            title = { Text(stringResource(R.string.choose)) },
            text = { Text(stringResource(R.string.do_you_want_delete_note)) },
            confirmButton = {
                TextButton(onClick = {
                    showDialogForId?.let {
                        onOpenNote(it)
                    }
                    showDialogForId = null
                }) { Text(stringResource(R.string.edit)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    vm.onEvent(NoteListEvent.ConfirmDelete(showDialogForId!!))
                    showDialogForId = null
                }) { Text(stringResource(R.string.delete)) }
            }
        )
    }

    Scaffold(
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { vm.onEvent(NoteListEvent.AddNew) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add))
            }
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            state.notes.isEmpty() -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { Text(stringResource(R.string.no_notes_found)) }
            }

            else -> {
                LazyColumn(
                    Modifier
                        .padding(padding)
                        .padding(8.dp)
                ) {
                    items(state.notes) { note ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { vm.onEvent(NoteListEvent.Open(note.id)) },
                                    onLongClick = { vm.onEvent(NoteListEvent.LongClick(note.id)) }
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                note.title ?: note.content.take(30),
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (!note.imageUri.isNullOrBlank()) {
                                AttachedImageThumbnail(
                                    uri = note.imageUri!!,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                DateFormat.getDateTimeInstance().format(note.updatedAt),
                                style = MaterialTheme.typography.bodySmall
                            )
                            HorizontalDivider(
                                Modifier.padding(top = 8.dp),
                                DividerDefaults.Thickness,
                                DividerDefaults.color
                            )
                        }
                    }
                }
            }
        }
    }
}
