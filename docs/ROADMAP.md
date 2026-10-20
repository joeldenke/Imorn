# Imorn — Android Development Roadmap

«En sak i taget. Allt på ett ställe.

Imorn is an Android app for managing life's projects, staying on top of tasks and deadlines, organizing important documents, and preserving personal memories along the way.

The initial release focuses on moving home as the first project template. Users can create a project, follow an editable checklist, receive reminders, attach photos and notes, and revisit the entire process through a chronological timeline.

Initial priorities: reliability, a clear user experience, local-first storage, and minimal friction when capturing information.»

## Development principles

- Build a working Android application incrementally.
- Keep the initial release fully usable without an account or backend.
- Use Android platform APIs for notifications, photos, camera access, and file handling.
- Separate UI, business logic, and persistence.
- Keep project data and media associated with the correct project.
- Avoid unnecessary permissions.
- Add automated tests alongside important functionality.
- Do not introduce cloud synchronization, AI, or subscriptions before the core experience is validated.

## Initial technology stack

Area | Technology
--- | ---
Language | Kotlin
UI | Jetpack Compose
Design system | Own tokens and `Imorn*` components in `:core:designsystem`, Material 3 inside it only
Navigation | Navigation Compose
Architecture | ViewModel, repositories, unidirectional UI state
Async operations | Kotlin Coroutines and Flow
Local database | Room
Preferences | DataStore
Background work | WorkManager
Image selection | Android Photo Picker
Camera | Camera intent initially; CameraX if in-app capture requires it
Dependency injection | Constructor injection initially; Hilt if project complexity justifies it
Testing | JUnit, Compose UI testing, Android instrumented tests
CI | GitHub Actions
Distribution | Google Play internal or closed testing

Use stable, mutually compatible dependency versions and a version catalog in gradle/libs.versions.toml.

## Phase 0 — Repository and Android foundation

Goal: Establish a reproducible Android development environment and a maintainable application structure.

Tasks
- [ ] Inspect the existing repository and Gradle configuration.
- [ ] Confirm the Android application module and package name.
- [ ] Configure Kotlin, Android Gradle Plugin, and Java toolchain compatibility.
- [ ] Configure minimum and target SDK versions.
- [ ] Set up Jetpack Compose and Material 3.
- [ ] Establish the application theme, typography, spacing, and color tokens.
- [ ] Add the initial navigation graph.
- [ ] Create a basic app shell with placeholder screens.
- [ ] Configure debug and release build variants.
- [ ] Add .gitignore entries for local builds, IDE files, and secrets.
- [ ] Add a GitHub Actions workflow for building and testing.
- [ ] Document local setup and build commands in README.md.

Suggested package structure:
app/src/main/java/<package>/ with app/, core/{database,designsystem,navigation,util}/, feature/{home,projects,tasks,reminders,timeline,memories,settings}/, and AndroidManifest.xml; app/src/test/. Adjust to the existing codebase; avoid empty abstractions without a current use case.

Acceptance criteria
- [ ] The app builds successfully from a clean checkout.
- [ ] The app launches on an Android emulator.
- [ ] Navigation between placeholder screens works.
- [ ] Unit tests execute in CI.
- [ ] No credentials or signing secrets are committed.

## Phase 1 — Data model and local persistence

Goal: Make project data reliable before adding complex UI or media handling.

Data entities (starting point; use Room entities, relations, foreign keys, indexes, explicit migrations; store dates consistently, preferably epoch millis or a clearly defined date/time model):

Project: id, title, description, templateId, startDate, targetDate, status, createdAt, updatedAt
Task: id, projectId, title, description, category, dueDate, isCompleted, completedAt, sortOrder, createdAt, updatedAt
TimelineEntry: id, projectId, type, title, description, occurredAt, createdAt, updatedAt
Attachment: id, projectId, timelineEntryId, type, displayName, localPath, mimeType, createdAt
Reminder: id, taskId, scheduledAt, isEnabled, notificationId

Tasks
- [ ] Add Room and configure the database.
- [ ] Implement entities and DAOs.
- [ ] Add foreign keys and indexes.
- [ ] Implement repository interfaces and Room-backed implementations.
- [ ] Expose observable data using Flow.
- [ ] Add database migration infrastructure.
- [ ] Define deletion behavior for projects and related records.
- [ ] Add repository tests.
- [ ] Verify data survives process termination and app restarts.

Acceptance criteria: CRUD works locally and related data cannot become orphaned through normal application operations.

## Phase 2 — Projects and project overview

Goal: Allow users to create and manage real-life projects.

Tasks
- [ ] Build the home screen.
- [ ] Display active projects.
- [ ] Display an appropriate empty state when no projects exist.
- [ ] Build the create-project screen.
- [ ] Add project title and optional description.
- [ ] Support optional start and target dates.
- [ ] Add the initial Moving Home template.
- [ ] Build the project overview screen.
- [ ] Display project progress and remaining tasks.
- [ ] Support project editing.
- [ ] Support project completion and archiving.
- [ ] Confirm destructive actions.
- [ ] Add UI and ViewModel tests.

Acceptance criteria: a user can create a moving project, open it, edit its details, and return to it after restarting the app.

## Phase 3 — Checklists and task management

Goal: Make the app useful for managing the actual work involved in a project.

Tasks
- [ ] Implement task creation and editing.
- [ ] Support task descriptions.
- [ ] Add task categories.
- [ ] Support due dates.
- [ ] Support task completion and reopening.
- [ ] Display completed and incomplete task counts.
- [ ] Sort tasks by due date and custom order.
- [ ] Filter tasks by status or category.
- [ ] Add custom tasks to template-generated checklists.
- [ ] Support task deletion.
- [ ] Update project progress reactively.
- [ ] Add unit tests for task and progress logic.

Initial moving template categories: Planning; Contracts and administration; Packing; Moving day; Cleaning and inspection; Settling into the new home. Template tasks must be editable; the app should not imply a checklist covers every legal or administrative requirement.

Acceptance criteria: users can manage a complete checklist, and changes are immediately reflected in the project overview.

## Phase 4 — Reminders and deadlines

Goal: Help users remember important tasks without overwhelming them with notifications.

Tasks
- [ ] Add reminder scheduling to eligible tasks.
- [ ] Create notification channels.
- [ ] Request notification permission on supported Android versions when needed.
- [ ] Implement a notification receiver or suitable scheduling mechanism.
- [ ] Use WorkManager for appropriate deferrable background work.
- [ ] Use exact alarms only if a genuine exact-time requirement justifies the additional permission and policy considerations.
- [ ] Cancel and reschedule notifications when tasks change.
- [ ] Cancel notifications when tasks are completed or deleted.
- [ ] Handle time zones and date changes.
- [ ] Add notification preference controls.
- [ ] Test permission denial and notification behavior after device restarts.

Acceptance criteria: a user can schedule a reminder, receive it when Android permits, and cancel or update it without leaving stale notifications behind. Document Android background execution limitations in app and developer documentation.

## Phase 5 — Photos, notes, and documents

Goal: Make it easy to preserve both practical evidence and personal memories.

Tasks
- [ ] Add a quick-add action from the project screen.
- [ ] Add text notes.
- [ ] Select existing images through Android Photo Picker.
- [ ] Add photo capture through the system camera intent.
- [ ] Evaluate CameraX if a custom in-app camera experience is needed.
- [ ] Add titles and descriptions to attachments.
- [ ] Support receipt and document attachments.
- [ ] Store media using a documented local storage strategy.
- [ ] Handle URI permissions and inaccessible files.
- [ ] Generate image thumbnails where useful.
- [ ] Add media deletion and cleanup logic.
- [ ] Prevent accidental deletion of files still referenced by records.
- [ ] Test large images, low storage, and process recreation.

Storage requirements
- Keep media references associated with their project and timeline entry.
- Do not rely on temporary content URIs remaining accessible indefinitely.
- Copy files into app-controlled storage when long-term access requires it.
- Use app-private storage for private content unless the user explicitly exports or shares it.
- Avoid broad photo-library permissions when the Android Photo Picker is sufficient.
- Explain what will happen to local media when a project is deleted.

Acceptance criteria: a user can add a photo, note, or document to a project and access it again after restarting the app.

## Phase 6 — Unified project timeline

Goal: Bring practical progress and personal memories together into one chronological view.

Tasks
- [ ] Build the timeline screen.
- [ ] Combine timeline entries, photos, notes, and completed tasks.
- [ ] Sort entries chronologically.
- [ ] Support date and time formatting using the user's locale.
- [ ] Display photo thumbnails.
- [ ] Open full-size photos.
- [ ] Display document and note entries.
- [ ] Support editing and deleting entries.
- [ ] Add empty and loading states.
- [ ] Optimize image loading and list performance.
- [ ] Test projects with many timeline entries.

Example timeline: March 2 First visit to the new home, Photo: The empty living room; March 5 Moving company booked; March 8 Started packing, Photo: First boxes; March 10 Receipt: Moving supplies; March 15 Moving day; March 16 First breakfast in the new home.

Acceptance criteria: the timeline presents a coherent history of the project, combining tasks and memories without duplicating or losing records.

## Phase 7 — Polish, accessibility, and MVP testing

Goal: Make the first release reliable enough for real users.

Tasks
- [ ] Test the complete moving workflow.
- [ ] Add UI tests for critical journeys.
- [ ] Test database migrations.
- [ ] Test task scheduling and notifications.
- [ ] Test media access and deletion.
- [ ] Handle offline usage and app restarts.
- [ ] Review accessibility labels and touch target sizes.
- [ ] Verify light and dark themes if both are supported.
- [ ] Review Swedish text, formatting, and localization.
- [ ] Add crash reporting only with appropriate privacy disclosures.
- [ ] Check performance on lower-end Android devices.
- [ ] Review privacy and data deletion behavior.
- [ ] Prepare internal or closed beta distribution.

Critical user journeys: 1. Create a moving project. 2. Complete and reschedule tasks. 3. Set and receive a reminder. 4. Attach a photo and a receipt. 5. Reopen the project and inspect the timeline. 6. Delete a project and verify the documented deletion behavior.

Acceptance criteria: all critical journeys pass, there are no known critical data-loss bugs, and beta users can use the app without developer assistance.

## Phase 8 — Post-MVP development

Prioritize these features only after evaluating feedback and usage.

Backup and synchronization
- [ ] Design account and identity requirements.
- [ ] Define a backend and synchronization model.
- [ ] Implement secure authentication if needed.
- [ ] Synchronize project metadata.
- [ ] Synchronize media with explicit storage and privacy rules.
- [ ] Handle conflicts and offline changes.
- [ ] Implement backup and restore.
- [ ] Add account and data deletion flows.

Collaboration
- [ ] Share a project with another person.
- [ ] Define viewer and editor permissions.
- [ ] Assign tasks to collaborators.
- [ ] Synchronize task and timeline changes.
- [ ] Handle revoked access and shared-media permissions.

Advanced features
- [ ] Add more project templates.
- [ ] Add search across tasks and attachments.
- [ ] Export a project timeline.
- [ ] Generate a visual project recap.
- [ ] Evaluate AI-assisted task suggestions.
- [ ] Evaluate premium storage and advanced project features.

## Engineering Definition of Done
- [ ] Acceptance criteria are met.
- [ ] Relevant unit and UI tests pass.
- [ ] UI state is handled correctly across lifecycle events.
- [ ] Loading, empty, and error states are implemented.
- [ ] Data is persisted and validated where appropriate.
- [ ] Permissions and privacy implications are reviewed.
- [ ] Accessibility has been considered.
- [ ] No secrets are committed.
- [ ] Documentation is updated where needed.
- [ ] CI passes.

## MVP Release Criteria
- [ ] A user can create and manage a project.
- [ ] Project checklists and progress work reliably.
- [ ] Task reminders can be scheduled and managed.
- [ ] Users can attach photos, notes, and documents.
- [ ] The timeline combines practical and personal entries.
- [ ] Data persists locally across app restarts.
- [ ] Critical user journeys pass testing.
- [ ] Data deletion and media storage behavior are documented.
- [ ] Beta feedback has been reviewed and critical issues resolved.

Product north star: Imorn helps people stay in control of life's important projects while preserving the memories that make those experiences meaningful. Build the smallest reliable Android experience first. Add complexity only when users demonstrate a real need for it.
