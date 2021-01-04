import androidx.compose.desktop.DesktopTheme
import androidx.compose.desktop.SwingPanel
import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import java.io.File
import javax.swing.JScrollPane


@InternalCoroutinesApi
@ExperimentalComposeApi
fun main() = Window("Renamer Composer") {

    DesktopTheme {
        Column {

            val padding = Modifier.padding(10.dp)

            val path = remember { mutableStateOf(System.getProperty("user.home")) }
            val pattern = remember { mutableStateOf("") }
            val replacement = remember { mutableStateOf("") }
            val counter = remember { mutableStateOf(false) }
            val files: State<List<File>> = derivedStateOf {
                counter.value
                File(path.value).listFiles()
                    ?.filter { !it.isHidden && it.isFile }
                    ?.toList()
                    ?.sortedBy { it.name }
                    ?: emptyList()
            }
            val candidates: State<List<File>> = derivedStateOf {
                val regex = pattern.value.toRegex()
                val replace = replacement.value
                files.value.map {
                    if (pattern.value.isBlank()) it
                    else {
                        val newName = regex.replace(it.name, replace)
                        File(it.parent, newName)
                    }
                }
            }

            Row {
                Text("Folder:", padding)
                DirectoryTextField(path, padding.weight(1f, true))
                FolderPickerButton(path, padding)
            }
            Row {
                Text("Pattern:", padding)
                PatternTextField(pattern, padding.weight(0.5f, true))
                Text("Replacement:", padding)
                ReplacementTextField(replacement, padding.weight(0.5f, true))
            }
            Row(padding.fillMaxWidth().fillMaxHeight(0.85f)) {
                // model now accepts the wrapped types, not State<T>.
                // We use the LaunchedEffect below to scope a subscription that pushes updates to it.
                val model = remember { FileTableModel(files.value, candidates.value) }
                // Monitor candidates and notify the model of updates
                LaunchedEffect(model) {
                    // snapshotFlow runs the block and emits its result whenever
                    // any snapshot state read by the block was changed.
                    snapshotFlow { Pair(files.value, candidates.value) }
                        .collect {
                            model.files = it.first
                            model.candidates = it.second
                            model.fireTableDataChanged()
                        }
                }
                // Don't recreate the swing UI elements on every recomposition
                val scrollingTable = remember(model) { JScrollPane(FileTable(model)) }
                SwingPanel(scrollingTable)
            }
            Row(Modifier.align(Alignment.End)) {
                ApplyButton(files, candidates, counter, padding)
            }
        }
    }
}