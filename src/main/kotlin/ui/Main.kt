package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import runtime.Runtime
import runtime.compiler.Compiler

@Composable
@Preview
fun App() {
    val runtime = remember {
        val compiler = Compiler(
            """
            a ( % this is a comment
              isSolid -> b
              forward
            )
            b (
              isAir -> a
              turnRight
            )
            """.trimIndent()
        )
        Runtime(compiler.compile())
    }

    MaterialTheme {
        Column {
            Button(onClick = {
                runtime.step()
            }) {
                Text("Step")
            }
            Canvas(modifier = Modifier.size(200.dp)) {
                drawRect(color = Color(0xffff0000), size = Size(200.0F, 200.0F))
                drawCircle(color = Color(0xff00ffff), radius = 50.0F)
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
