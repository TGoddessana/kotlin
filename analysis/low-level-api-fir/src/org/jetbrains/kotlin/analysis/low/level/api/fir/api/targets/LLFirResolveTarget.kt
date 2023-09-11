/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.api.targets

import org.jetbrains.kotlin.analysis.low.level.api.fir.util.errorWithFirSpecificEntries
import org.jetbrains.kotlin.fir.FirElementWithResolveState
import org.jetbrains.kotlin.fir.FirFileAnnotationsContainer
import org.jetbrains.kotlin.fir.declarations.FirAnonymousInitializer
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration
import org.jetbrains.kotlin.fir.declarations.FirConstructor
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.FirScript

/**
 * [target] element and optionally its subgraph to be lazily resolved by LL FIR lazy resolver.
 *
 * Specifies the path to resolve targets and resolve targets themselves.
 * Those targets are going to be resolved by [LLFirModuleLazyDeclarationResolver][org.jetbrains.kotlin.analysis.low.level.api.fir.lazy.resolve.LLFirModuleLazyDeclarationResolver].
 */
sealed class LLFirResolveTarget(
    /**
     * [FirFile] where the targets are located
     */
    val firFile: FirFile,

    /**
     * The list of [FirRegularClass] which are the required to go from file to target declarations in the top-down order.
     */
    classPath: List<FirRegularClass>,

    /**
     * A dedicated main element.
     */
    val target: FirElementWithResolveState,
) {
    /**
     * The list of [FirScript] and [FirRegularClass] which are the required to go from file to target declarations in the top-down order.
     *
     * If resolve target is [FirRegularClass] or [FirScript] itself, it's not included into the [path].
     */
    val path: List<FirDeclaration> = pathWithScript(firFile, classPath, target)

    /**
     * Executions the [action] for each target that this [LLFirResolveTarget] represents.
     */
    abstract fun forEachTarget(action: (FirElementWithResolveState) -> Unit)

    override fun toString(): String = buildString {
        append(this@LLFirResolveTarget::class.simpleName)
        append("(")
        buildList {
            add(firFile.name)
            path.mapTo(this) {
                when (it) {
                    is FirRegularClass -> it.name
                    is FirScript -> it.name
                    else -> errorWithFirSpecificEntries("Unsupported path declaration: ${it::class.simpleName}", fir = it)
                }
            }

            add(toStringForTarget())
            toStringAdditionalSuffix()?.let(this::add)
        }.joinTo(this, separator = " -> ")
        append(")")
    }

    protected open fun toStringAdditionalSuffix(): String? = null

    private fun toStringForTarget(): String = when (target) {
        is FirConstructor -> "constructor"
        is FirClassLikeDeclaration -> target.symbol.name.asString()
        is FirCallableDeclaration -> target.symbol.name.asString()
        is FirAnonymousInitializer -> ("<init-block>")
        is FirFileAnnotationsContainer -> "<file annotations>"
        is FirScript -> target.name.asString()
        else -> "???"
    }
}

private fun pathWithScript(firFile: FirFile, path: List<FirRegularClass>, target: FirElementWithResolveState): List<FirDeclaration> {
    if (target is FirFile || target is FirFileAnnotationsContainer || target is FirScript) return path
    val firScript = firFile.declarations.singleOrNull() as? FirScript ?: return path
    return listOf(firScript) + path
}
