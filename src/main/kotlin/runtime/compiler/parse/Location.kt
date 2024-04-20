package runtime.compiler.parse

data class Location(val start: Int, val end: Int, val line: Int, val column: Int) {
    override fun toString(): String {
        return "$line:$column"
    }
}
