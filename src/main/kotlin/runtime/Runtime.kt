package runtime

import runtime.bytecode.*
import runtime.compiler.compile.CompiledProgram
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

@OptIn(ExperimentalUnsignedTypes::class)
class Runtime(private val program: CompiledProgram) {
    private val stack = ULongArray(16) // 1024 stack entries
    private var stackIndex = 1023
    private var state = program.states[0]
    private var programCounter = 0

    init {
        val file = Path.of("out.bin")
        if (Files.exists(file)) {
            Files.delete(file)
        }
        for (programState in program.states) {
            Files.write(
                Path.of("out.bin"),
                programState.code.toByteArray() + byteArrayOf(-1, -1),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            )
        }
    }

    private fun push(value: Boolean) {
        stackIndex--
        val slot = stackIndex shr 6
        val bit = stackIndex and 0x3f
        val unsetMask = 1uL shl bit
        val setMask = (if (value) 1uL else 0uL) shl bit
        stack[slot] = stack[slot] and unsetMask.inv() or setMask
    }

    private fun pop(): Boolean {
        val slot = stackIndex shr 6
        val bit = stackIndex and 0x3f
        val value = (stack[slot] shr bit) and 1uL == 1uL
        stackIndex++
        return value
    }

    private fun fetch(): UByte {
        return state.code[programCounter++]
    }

    fun step() {
        val op = fetch()
        when (op) {
            OP_SWITCH -> {
                state = program.states[fetch().toInt()]
                programCounter = 0
            }

            OP_FORWARD -> println("forward")
            OP_TURN_LEFT -> println("left")
            OP_TURN_RIGHT -> println("right")
            OP_AND -> push(pop() && pop())
            OP_OR -> push(pop() || pop())
            OP_XOR -> push(pop() xor pop())
            OP_NOT -> push(!pop())
            OP_JUMP_IF -> {
                val offset = fetch()
                if (pop()) {
                    programCounter += offset.toInt()
                }
            }

            OP_JUMP_IFN -> {
                val offset = fetch()
                if (!pop()) {
                    programCounter += offset.toInt()
                }
            }

            OP_IS_SOLID -> push(true)
            OP_IS_AIR -> push(true)
        }
        programCounter %= state.code.size
    }
}