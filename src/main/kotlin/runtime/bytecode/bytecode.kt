package runtime.bytecode

/**
 * `(state: UByte): * --`
 */
const val OP_SWITCH: UByte = 0x00u

/**
 * `(): --`
 */
const val OP_FORWARD: UByte = 0x01u

/**
 * `(): --`
 */
const val OP_TURN_LEFT: UByte = 0x02u

/**
 * `(): --`
 */
const val OP_TURN_RIGHT: UByte = 0x03u

/**
 * `(): Boolean Boolean -- Boolean`
 */
const val OP_AND: UByte = 0x10u

/**
 * `(): Boolean Boolean -- Boolean`
 */
const val OP_OR: UByte = 0x11u

/**
 * `(): Boolean Boolean -- Boolean`
 */
const val OP_XOR: UByte = 0x12u

/**
 * `(): Boolean -- Boolean`
 */
const val OP_NOT: UByte = 0x13u

/**
 * `(offset: UByte): Boolean --`
 */
const val OP_JUMP_IF: UByte = 0x14u

/**
 * `(offset: UByte): Boolean --`
 */
const val OP_JUMP_IFN: UByte = 0x15u

/**
 * `(): -- Boolean`
 */
const val OP_IS_SOLID: UByte = 0x20u

/**
 * `(): -- Boolean`
 */
const val OP_IS_AIR: UByte = 0x21u
