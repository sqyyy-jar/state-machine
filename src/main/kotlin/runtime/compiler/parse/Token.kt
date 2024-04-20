package runtime.compiler.parse

data class Token(val kind: Kind, val location: Location) {
    enum class Kind {
        And,
        Or,
        Xor,
        Not,
        LeftParent,
        RightParen,
        Arrow,
        Identifier,
    }
}
