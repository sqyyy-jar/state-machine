package runtime.compiler

import runtime.compiler.compile.CompiledProgram
import runtime.compiler.parse.*

class Compiler(private val source: String, private val env: Environment) {
    private val lexer = Lexer(source)

    private fun assertOutput(expression: Expression, output: Boolean, location: Location): Expression {
        if (expression.output != output) {
            throw CompileException("Invalid operand for operator", location)
        }
        return expression
    }

    private fun parseGroup(): Expression {
        lexer.expectToken(Token.Kind.LeftParent)
        val expr = parseBinary()
        lexer.expectToken(Token.Kind.RightParen)
        return expr
    }

    private fun parseInvoke(): Expression {
        val name = lexer.expectToken(Token.Kind.Identifier).location
        val nameString = source.slice(name.start..<name.end)
        val macro = env.getMacro(nameString)
        if (macro != null) {
            return Macro(macro.index, macro.output)
        }
        return Switch(nameString, name)
    }

    private fun parseUnary(): Expression {
        val token = lexer.peekToken() ?: throw CompileException("Unexpected end of file in expression")
        return when (token.kind) {
            Token.Kind.Not -> {
                lexer.nextToken()
                val right = assertOutput(parseUnary(), true, token.location)
                UnaryOperation(OperationKind.Not, right)
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
                assertOutput(left, true, op.location)
                val right = assertOutput(parseUnary(), true, op.location)
                parseBinary(BinaryOperation(OperationKind.And, left, right)) // left-associative
            }

            Token.Kind.Or -> {
                lexer.nextToken()
                assertOutput(left, true, op.location)
                val right = assertOutput(parseUnary(), true, op.location)
                parseBinary(BinaryOperation(OperationKind.Or, left, right)) // left-associative
            }

            Token.Kind.Xor -> {
                lexer.nextToken()
                assertOutput(left, true, op.location)
                val right = assertOutput(parseUnary(), true, op.location)
                parseBinary(BinaryOperation(OperationKind.Xor, left, right)) // left-associative
            }

            Token.Kind.Arrow -> {
                lexer.nextToken()
                assertOutput(left, true, op.location)
                val right = assertOutput(parseBinary(), false, op.location)
                Conditional(left, right) // right-associative
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
                    break
                }
                val statement = assertOutput(parseBinary(), false, leftParen) // todo - not an operator
                code.add(statement)
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
        return Ast(env, states, stateIndices)
    }

    fun compile(): CompiledProgram {
        return parse().compile()
    }
}