package runtime.compiler.parse

import runtime.bytecode.*
import runtime.compiler.CompileException
import runtime.compiler.compile.CodeBuffer

sealed interface Expression {
    val output: Boolean

    fun expand(code: CodeBuffer, stateIndices: Map<String, Int>)
}

data class Conditional(val condition: Expression, val expr: Expression) : Expression {
    override val output = false

    override fun expand(code: CodeBuffer, stateIndices: Map<String, Int>) {
        condition.expand(code, stateIndices)
        code.writeByte(OP_JUMP_IFN)
        val offset = code.offset()
        code.writeByte(0xffu)
        expr.expand(code, stateIndices)
        code.writeByte((code.offset() - offset - 1).toUByte(), offset)
    }
}

data class Macro(val index: UByte, override val output: Boolean) : Expression {
    override fun expand(code: CodeBuffer, stateIndices: Map<String, Int>) {
        if (output) {
            code.writeByte(OP_BOOL_MACRO)
        } else {
            code.writeByte(OP_MACRO)
        }
        code.writeByte(index)
    }
}

data class Switch(val name: String, val location: Location) : Expression {
    override val output = false

    override fun expand(code: CodeBuffer, stateIndices: Map<String, Int>) {
        code.writeByte(OP_SWITCH)
        val index = stateIndices[name] ?: throw CompileException("The state does not exist", location)
        code.writeByte(index.toUByte())
    }
}

data class BinaryOperation(val kind: OperationKind, val left: Expression, val right: Expression) : Expression {
    override val output = true

    override fun expand(code: CodeBuffer, stateIndices: Map<String, Int>) {
        left.expand(code, stateIndices)
        right.expand(code, stateIndices)
        when (kind) {
            OperationKind.And -> code.writeByte(OP_AND)
            OperationKind.Or -> code.writeByte(OP_OR)
            OperationKind.Xor -> code.writeByte(OP_XOR)
            else -> TODO("Unreachable")
        }
    }
}

data class UnaryOperation(val kind: OperationKind, val value: Expression) : Expression {
    override val output = true

    override fun expand(code: CodeBuffer, stateIndices: Map<String, Int>) {
        value.expand(code, stateIndices)
        when (kind) {
            OperationKind.Not -> code.writeByte(OP_NOT)
            else -> TODO("Unreachable")
        }
    }
}

enum class OperationKind {
    And, Or, Xor, Not,
}