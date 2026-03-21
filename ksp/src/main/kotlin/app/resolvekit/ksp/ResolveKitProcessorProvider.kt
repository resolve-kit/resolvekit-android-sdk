package app.resolvekit.ksp

import com.google.devtools.ksp.processing.*

class ResolveKitProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        ResolveKitProcessor(environment.codeGenerator, environment.logger)
}
