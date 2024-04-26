package runtime.bytecode

/**
 * `(state: UByte): * --`
 */
const val OP_SWITCH: UByte = 0x00u

/**
 * `(): Boolean Boolean -- Boolean`
 */
const val OP_AND: UByte = 0x01u

/**
 * `(): Boolean Boolean -- Boolean`
 */
const val OP_OR: UByte = 0x02u

/**
 * `(): Boolean Boolean -- Boolean`
 */
const val OP_XOR: UByte = 0x03u

/**
 * `(): Boolean -- Boolean`
 */
const val OP_NOT: UByte = 0x04u

/**
 * `(offset: UByte): Boolean --`
 */
const val OP_JUMP_IF: UByte = 0x05u

/**
 * `(offset: UByte): Boolean --`
 */
const val OP_JUMP_IFN: UByte = 0x06u

/**
 * `(index: UByte): --`
 */
const val OP_MACRO: UByte = 0x07u;

/**
 * `(index: UByte): -- Boolean`
 */
const val OP_BOOL_MACRO: UByte = 0x08u
