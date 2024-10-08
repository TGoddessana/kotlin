// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6

// TODO: Design Unit materialization and Unit.asDynamic() in JS IR BE

var log = ""

fun foo() {
    log += "foo"
}

fun Unit.bar() = jsTypeOf(this.asDynamic()) == "undefined"

fun Any.baz() = jsTypeOf(this.asDynamic()) == "object"

fun box(): String {
    if (!foo().bar()) return "fail1"
    if (!foo().baz()) return "fail2"

    return "OK"
}