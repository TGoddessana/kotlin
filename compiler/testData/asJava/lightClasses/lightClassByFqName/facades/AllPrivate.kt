// p.AllPrivate
// WITH_STDLIB
// LIBRARY_PLATFORMS: JVM

@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("AllPrivate")

package p

private fun f(): Int = 3

private fun g(p: String): String = "p"
