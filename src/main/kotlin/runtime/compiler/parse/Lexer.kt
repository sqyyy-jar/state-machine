package runtime.compiler.parse

import runtime.compiler.CompileException

class Lexer(private val source: String) {
    private var start = 0
    private var startLine = 1
    private var startColumn = 1
    private var current = 0
    private var currentLine = 1
    private var currentColumn = 1
    private var peekBuffer: Token? = null

    private fun peek(): Char? {
        if (current >= source.length) {
            return null
        }
        return source[current]
    }

    private fun next() {
        val c = peek() ?: return
        current += 1
        currentColumn += 1
        if (c == '\n') {
            currentLine += 1
            currentColumn = 1
        }
    }

    private fun clearLocation() {
        start = current
        startLine = currentLine
        startColumn = currentColumn
    }

    private fun makeLocation(): Location {
        val location = Location(start, current, startLine, startColumn)
        clearLocation()
        return location
    }

    private fun makeToken(kind: Token.Kind): Token {
        val location = makeLocation()
        return Token(kind, location)
    }

    private fun skipComment() {
        while (true) {
            val c = peek() ?: break
            next()
            if (c == '\n') {
                break
            }
        }
    }

    private fun parseIdentifier(): Token {
        while (true) {
            val c = peek() ?: break
            if (!c.isLetterOrDigit() && c != '_' && c != '?') {
                break
            }
            next()
        }
        return makeToken(Token.Kind.Identifier)
    }

    private fun parseToken() {
        peekBuffer = null
        while (peekBuffer == null) {
            clearLocation()
            val c = peek() ?: return
            next()
            when (c) {
                '%' -> skipComment()
                '&', '*' -> peekBuffer = makeToken(Token.Kind.And)
                '|', '+' -> peekBuffer = makeToken(Token.Kind.Or)
                '^' -> peekBuffer = makeToken(Token.Kind.Xor)
                '!', '~' -> peekBuffer = makeToken(Token.Kind.Not)
                '(' -> peekBuffer = makeToken(Token.Kind.LeftParent)
                ')' -> peekBuffer = makeToken(Token.Kind.RightParen)
                '-' -> {
                    if (peek() != '>') {
                        throw CompileException("Expected arrow got '-$c'", makeLocation())
                    }
                    next()
                    peekBuffer = makeToken(Token.Kind.Arrow)
                }

                '_' -> peekBuffer = parseIdentifier()
                else -> {
                    if (c.isWhitespace()) {
                        continue
                    }
                    if (c.isLetter()) {
                        peekBuffer = parseIdentifier()
                        break
                    }
                    throw CompileException("Unexpected character: '$c'", makeLocation())
                }
            }
        }
    }

    fun peekToken(): Token? {
        if (peekBuffer == null) {
            parseToken()
        }
        return peekBuffer
    }

    fun nextToken() {
        if (peekBuffer == null) {
            parseToken()
        } else {
            peekBuffer = null
        }
    }

    fun expectToken(kind: Token.Kind): Token {
        val token = peekToken() ?: throw CompileException("Unexpected end of file. Expected $kind", makeLocation())
        nextToken()
        if (token.kind != kind) {
            throw CompileException("Unexpected token. Expected ${kind}, but got $kind", makeLocation())
        }
        return token
    }
}