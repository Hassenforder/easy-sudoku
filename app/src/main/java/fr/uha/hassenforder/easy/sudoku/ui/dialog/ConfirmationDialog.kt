package fr.uha.hassenforder.easy.sudoku.ui.dialog

import androidx.compose.runtime.Composable
import fr.uha.hassenforder.easy.android.ui.dialog.ConfirmationDialog
import fr.uha.hassenforder.easy.sudoku.R

@Composable
fun ConfirmationEditDialog (onSelect: (Int?) -> Unit) = ConfirmationDialog(
    messageId = R.string.warning_edit_message,
    optionsId = R.string.warning_edit_options,
    actions = listOf(
        R.string.warning_edit_yes,
        R.string.warning_edit_clone,
        R.string.warning_edit_no
    ),
    onSelect = onSelect
)

@Composable
fun ConfirmationDeleteDialog (onSelect: (Int?) -> Unit) = ConfirmationDialog(
    messageId = R.string.warning_delete_message,
    optionsId = R.string.warning_delete_options,
    actions = listOf(
        R.string.warning_delete_yes,
        R.string.warning_delete_no
    ),
    onSelect = onSelect
)
