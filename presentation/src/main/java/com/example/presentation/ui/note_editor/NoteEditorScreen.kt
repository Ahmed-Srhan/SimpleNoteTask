package com.example.presentation.ui.note_editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.presentation.R
import com.example.presentation.common.AppSnackbarHost
import com.example.presentation.common.PermissionRequest
import com.example.presentation.common.galleryPermission
import com.example.presentation.utils.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Int?, onBack: () -> Unit, vm: NoteEditorViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    var tempUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var askCamera by remember { mutableStateOf(false) }
    var askGallery by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { vm.onEvent(NoteEditorEvent.AttachImageUri(it)) }
        }

    val takePictureLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            tempUri?.let { uri ->
                if (success) vm.onEvent(NoteEditorEvent.AttachImageUri(uri))
            }

        }

    if (askCamera) {
        PermissionRequest(
            permission = android.Manifest.permission.CAMERA,
            rationale = stringResource(R.string.need_camera_permission_to_take_pictures)
        ) {
            askCamera = false
            tempUri = FileUtils.createImageUri(context)
            tempUri?.let { uri ->
                takePictureLauncher.launch(uri)
            }


        }
    }

    if (askGallery) {
        PermissionRequest(
            permission = galleryPermission(),
            rationale = stringResource(R.string.need_access_to_the_images)
        ) {
            askGallery = false
            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    LaunchedEffect(Unit) {
        vm.effect.collect { eff ->
            when (eff) {
                is NoteEditorEffect.BackToList -> onBack()
                is NoteEditorEffect.ShowToast -> snackbarHostState.showSnackbar(eff.text)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (noteId == null) stringResource(R.string.add_note) else stringResource(R.string.edit_note)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.onEvent(NoteEditorEvent.Save) }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                    if (state.note.id != 0) {
                        IconButton(onClick = { vm.onEvent(NoteEditorEvent.Delete) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                })
        },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = state.note.title ?: "",
                onValueChange = { vm.onEvent(NoteEditorEvent.TitleChanged(it)) },
                label = { Text(stringResource(R.string.title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences),
                value = state.note.content,
                onValueChange = { vm.onEvent(NoteEditorEvent.ContentChanged(it)) },
                label = { Text(stringResource(R.string.content)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    askGallery = true
                }) { Text(stringResource(R.string.chose_from_galary)) }
                Button(onClick = { askCamera = true }) { Text(stringResource(R.string.take_photo)) }
            }
            state.note.imageUri?.let { path ->
                AsyncImage(
                    model = path,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
                Button(onClick = { vm.onEvent(NoteEditorEvent.ClearImage) }) {
                    Text(stringResource(R.string.delete_image))
                }
            }
        }
    }
}
