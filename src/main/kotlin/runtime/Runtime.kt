package runtime

import runtime.bytecode.*
import runtime.compiler.compile.CompiledProgram
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

@OptIn(ExperimentalUnsignedTypes::class)
class Runtime(private val program: CompiledProgram) {
    private val env = program.env
    private val stack = ULongArray(16) // 1024 stack entries
    private var stackIndex = stack.size * 8 - 1
    private var state = program.states[0]
    private var programCounter = 0
    private var yielded = true

    init { // todo - remove
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

    private fun step() {
        if (state.code.isEmpty()) {
            yield()
            return
        }
        val op = fetch()
        when (op) {
            OP_SWITCH -> {
                state = program.states[fetch().toInt()]
                programCounter = 0
            }

            OP_AND -> {
                push(pop() && pop())
            }

            OP_OR -> {
                push(pop() || pop())
            }

            OP_XOR -> {
                push(pop() xor pop())
            }

            OP_NOT -> {
                push(!pop())
            }

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

            OP_MACRO -> {
                val index = fetch()
                env.invokeMacro(this, index)
            }

            OP_BOOL_MACRO -> {
                val index = fetch()
                push(env.invokeBoolMacro(this, index))
            }
        }
        if (programCounter >= state.code.size) {
            programCounter = 0
            yield()
        }
    }

    fun run(limit: Int) {
        yielded = false
        for (i in 0 until limit) {
            if (yielded) {
                break
            }
            step()
        }
        yielded = true
    }

    fun yield() {
        yielded = true
    }
}