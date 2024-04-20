package runtime.compiler

import runtime.compiler.parse.Location

class CompileException(message: String, location: Location? = null) : RuntimeException(
    if (location != null) "Error during compilation @ $location - $message"
    else "Error during compilation - $message"
)