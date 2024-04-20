package runtime.compiler

import runtime.bytecode.*
import runtime.compiler.compile.CompiledProgram
import runtime.compiler.parse.*

private val macros = mapOf(
    "forward" to MacroInfo(false, OP_FORWARD),
    "turnLeft" to MacroInfo(false, OP_TURN_LEFT),
    "turnRight" to MacroInfo(false, OP_TURN_RIGHT),
    "isSolid" to MacroInfo(true, OP_IS_SOLID),
    "isAir" to MacroInfo(true, OP_IS_AIR),
)

class Compiler(private val source: String) {
    private val lexer = Lexer(source)

    private fun parseGroup(): Expression {
        lexer.expectToken(Token.Kind.LeftParent)
        val expr = parseBinary()
        lexer.expectToken(Token.Kind.RightParen)
        return expr
    }

    private fun parseInvoke(): Expression {
        val name = lexer.expectToken(Token.Kind.Identifier).location
        val nameString = source.slice(name.start..<name.end)
        val macro = macros[nameString]
        if (macro != null) {
            return RawOp(macro.op, macro.output)
        }
        return Switch(nameString, name)
    }

    private fun parseUnary(): Expression {
        val token = lexer.peekToken() ?: throw CompileException("Unexpected end of file in expression")
        return when (token.kind) {
            Token.Kind.Not -> {
                lexer.nextToken()
                UnaryOperation(OperationKind.Not, parseUnary())
            }

            Token.Kind.LeftParent -> parseGroup()
            Token.Kind.Identifier -> parseInvoke()
            else -> throw CompileException("Unexpected token in expression", token.location)
        }
    }

    private fun parseBinary(left: Expression = parseUnary()): Expression {
        val op = lexer.peekToken() ?: return left
        return when (op.kind) {
            Token.Kind.And -> {
                lexer.nextToken()
                parseBinary(BinaryOperation(OperationKind.And, left, parseUnary())) // left-associative
            }

            Token.Kind.Or -> {
                lexer.nextToken()
                parseBinary(BinaryOperation(OperationKind.Or, left, parseUnary())) // left-associative
            }

            Token.Kind.Xor -> {
                lexer.nextToken()
                parseBinary(BinaryOperation(OperationKind.Xor, left, parseUnary())) // left-associative
            }

            Token.Kind.Arrow -> {
                lexer.nextToken()
                Conditional(left, parseBinary()) // right-associative
            }

            else -> left
        }
    }

    private fun parse(): Ast {
        val states = mutableListOf<State>()
        val stateIndices = mutableMapOf<String, Int>()
        while (lexer.peekToken() != null) {
            val name = lexer.expectToken(Token.Kind.Identifier).location
            val leftParen = lexer.expectToken(Token.Kind.LeftParent).location
            val code = mutableListOf<Expression>()
            while (true) {
                val token = lexer.peekToken() ?: throw CompileException("Unclosed scope", leftParen)
                if (token.kind == Token.Kind.RightParen) {
                    lexer.nextToken()
                    break;
                }
                code.add(parseBinary())
            }
            stateIndices[source.slice(name.start..<name.end)] = states.size
            states.add(State(code))
        }
        if (states.isEmpty()) {
            throw CompileException("At least one state is required")
        }
        if (states.size > 255) {
            throw CompileException("Too many states")
        }
        return Ast(states, stateIndices)
    }

    fun compile(): CompiledProgram {
        return parse().compile()
    }
}