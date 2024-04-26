package runtime.compiler

import runtime.Runtime

interface Environment {
    fun getMacro(name: String): MacroInfo?

    fun invokeMacro(runtime: Runtime, index: UByte)

    fun invokeBoolMacro(runtime: Runtime, index: UByte): Boolean
}