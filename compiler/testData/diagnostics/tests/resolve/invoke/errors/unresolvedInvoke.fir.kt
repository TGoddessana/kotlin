// DIAGNOSTICS: -UNUSED_EXPRESSION
fun foo(i: Int) {
    <!UNRESOLVED_REFERENCE!>i<!>()
    <!FUNCTION_EXPECTED!>1<!>()
}
