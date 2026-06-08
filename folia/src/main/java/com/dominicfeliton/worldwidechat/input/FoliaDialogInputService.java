package com.dominicfeliton.worldwidechat.input;

/*
 * Keeps the Paper Dialog backend reachable to the shaded Folia artifact.
 * FoliaInputService is the runtime entry point so older Folia can still fall
 * back cleanly when Paper Dialog classes are unavailable.
 */
class FoliaDialogInputService extends PaperDialogInputService {
}
