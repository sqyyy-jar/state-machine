package ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import runtime.Runtime
import runtime.compiler.Environment
import runtime.compiler.MacroInfo

class TestEnv : Environment {
    companion object {
        private val macros = mapOf(
            // Void macros
            "startTimeout" to MacroInfo(0u, false),
            "resetDisplay" to MacroInfo(1u, false),
            "updateDisplay" to MacroInfo(2u, false),
            "printTicket" to MacroInfo(3u, false),
            "dispenseMoney" to MacroInfo(4u, false),
            "yield" to MacroInfo(255u, false),
            // Bool macros
            "confirmed?" to MacroInfo(0u, true),
            "cancelled?" to MacroInfo(1u, true),
            "timedOut?" to MacroInfo(2u, true),
            "moneyInserted?" to MacroInfo(3u, true),
        )
    }

    var timeout: Long = 0
    var money by mutableStateOf(0.0)
    var dispensedMoney by mutableStateOf(0.0)
    var display by mutableStateOf("")
    var confirmPressed by mutableLongStateOf(0)
    var cancelPressed by mutableLongStateOf(0)
    var moneyInserted by mutableStateOf(false)

    override fun getMacro(name: String): MacroInfo? {
        return macros[name]
    }

    override fun invokeMacro(runtime: Runtime, index: UByte) {
        when (index.toUInt()) {
//            0u -> timeout = System.currentTimeMillis() + 1000 * 60 * 5 // 5 minutes
            0u -> timeout = System.currentTimeMillis() + 1000 * 15 // todo - for testing
            1u -> {
                money = 0.0 // todo - consider saving money
                display = "%.2f€".format(money)
            }
            2u -> {
                display = "%.2f€".format(money)
            }
            3u -> {
                println("todo: print ticket (%.2f€)".format(money))
            }
            4u -> {
                dispensedMoney += money
                money = 0.0
            }
            255u -> runtime.yield()
            else -> TODO("not implemented")
        }
    }

    override fun invokeBoolMacro(runtime: Runtime, index: UByte): Boolean {
        return when (index.toUInt()) {
            0u -> System.currentTimeMillis() - confirmPressed <= 250
            1u -> System.currentTimeMillis() - cancelPressed <= 250
            2u -> {
                if (timeout == 0L) {
                    false
                } else if (System.currentTimeMillis() >= timeout) {
                    timeout = 0
                    true
                } else {
                    false
                }
            }
            3u -> {
                val value = moneyInserted
                moneyInserted = false
                value
            }
            else -> TODO("not implemented")
        }
    }

    fun addMoney(money: Double) {
        this.money += money
        moneyInserted = true
    }

    fun confirm() {
        confirmPressed = System.currentTimeMillis()
    }

    fun cancel() {
        cancelPressed = System.currentTimeMillis()
    }
}