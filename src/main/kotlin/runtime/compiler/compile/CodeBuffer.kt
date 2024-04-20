package runtime.compiler.compile

class CodeBuffer {
    private val code = mutableListOf<UByte>()

    fun offset(): Int = code.size

    fun writeByte(byte: UByte) {
        code.add(byte);
    }

    fun writeByte(byte: UByte, offset: Int) {
        code[offset] = byte
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun finish(): CompiledCode {
        return CompiledCode(code.toUByteArray())
    }
}