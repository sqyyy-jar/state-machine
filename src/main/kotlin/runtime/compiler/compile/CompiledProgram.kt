package runtime.compiler.compile

import runtime.compiler.Environment

data class CompiledProgram(val env: Environment, val states: List<CompiledCode>)