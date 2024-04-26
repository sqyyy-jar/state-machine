package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
    val env by remember { mutableStateOf(TestEnv()) }
    val runtime = remember {
        val compiler = Compiler(
            """
            displayZero (
                resetDisplay
                moneyInserted? -> displayUpdate
            )
            displayUpdate (
                updateDisplay
                moneyInserted? -> displayUpdate
                timedOut? | cancelled? -> dispense
                confirmed? -> print
            )
            dispense (
                dispenseMoney
                displayZero
            )
            print (
                printTicket
                displayZero
            )
            % a ( % this is a comment
            %   isSolid -> b
            %   forward
            % )
            % b (
            %   isAir -> a
            %   turnRight
            % )
            """.trimIndent(), env
        )
        Runtime(compiler.compile())
    }

    runtime.run(100)

    MaterialTheme {
        Row(modifier = Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(env.display)
                Row {
                    Button(onClick = { env.addMoney(0.01) }) { Text("1ct") }
                    Button(onClick = { env.addMoney(0.02) }) { Text("2ct") }
                    Button(onClick = { env.addMoney(0.05) }) { Text("5ct") }
                    Button(onClick = { env.addMoney(0.10) }) { Text("10ct") }
                    Button(onClick = { env.addMoney(0.50) }) { Text("50ct") }
                }
                Row {
                    Button(onClick = { env.addMoney(1.0) }) { Text("1€") }
                    Button(onClick = { env.addMoney(2.0) }) { Text("2€") }
                    Button(onClick = { env.addMoney(5.0) }) { Text("5€") }
                    Button(onClick = { env.addMoney(10.0) }) { Text("10€") }
                    Button(onClick = { env.addMoney(50.0) }) { Text("50€") }
                }
                Row {
                    Button(onClick = { env.confirm() }) {
                        Text("Confirm")
                    }
                    Button(onClick = { env.cancel() }) {
                        Text("Cancel")
                    }
                }
                Text("(Dispensed: %.2f€)".format(env.dispensedMoney))
                Text("(Money: %.2f€)".format(env.money))
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
