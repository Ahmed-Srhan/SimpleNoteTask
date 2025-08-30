# Simple Notes App with Image Attachments
A simple Android note-taking app with image attachments. Users can create, view, edit, and delete notes. Each note can have one image stored locally.

## Features
### Notes List
- Empty state message when no notes exist
- Show all notes with title/content preview, timestamp, and image indicator.
- FAB to add a new note.
- Tap to edit.
- Long click → dialog: Edit or Delete note.
### Add/Edit Note
- Title (optional) & multi-line content.
- Attach an image (camera or gallery).
- Replace or remove image.
- Save or Delete note.
- Image URI saved in app private storage.
### Persistence
- Room database stores all note data including image URI.
- Data persists across app restarts.

## Technology Used

- Clean Architecture
- Modularization
- Jetpack Compose
- Dagger-hilt to handle dependency injection.
- Co-routines to deal with threads.
- Flow & StateFlow to give view the data and notify it when a change occurs.
- Room Database.
- MockK & Turbine for testing.
- Coil for image loading
