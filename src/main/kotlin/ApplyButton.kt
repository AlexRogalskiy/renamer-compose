import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import java.io.File

@Composable
fun ApplyButton(
    files: State<List<File>>,
    candidates: State<List<File>>,
    refresh: MutableState<Boolean>,
    modifier: Modifier
) {
    val rename = {
        var renamed = false
        files.value.forEachIndexed { index, file ->
            val candidate = candidates.value[index]
            println("File.....: ${file.name}")
            println("Candidate: ${candidate.name}")
            if (file != candidate) {
                file.renameTo(candidate)
                renamed = true
            }
        }
        if (renamed) {
            val (currentRefresh, setRefresh) = refresh
            setRefresh(currentRefresh.not())
        }
    }
    Button(
        content = { Text("Apply") },
        onClick = rename,
        modifier = modifier,
    )
}