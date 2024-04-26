package runtime.compiler.parse

import runtime.compiler.Environment
import runtime.compiler.compile.CodeBuffer
import runtime.compiler.compile.CompiledCode
import runtime.compiler.compile.CompiledProgram

data class Ast(val env: Environment, val states: List<State>, val stateIndices: Map<String, Int>) {
    fun compile(): CompiledProgram {
        val compiledStates = mutableListOf<CompiledCode>()
        for (state in states) {
            val buffer = CodeBuffer()
            for (expression in state.code) {
                expression.expand(buffer, stateIndices)
            }
            compiledStates.add(buffer.finish())
        }
        return CompiledProgram(env, compiledStates)
    }
}