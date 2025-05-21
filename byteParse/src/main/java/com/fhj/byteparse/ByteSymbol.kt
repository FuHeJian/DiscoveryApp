package com.fhj.byteparse


import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class ByteSymbol: SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        environment.logger.warn("我的")
        return ByteSymbolProcessor(environment)
    }
}

class ByteSymbolProcessor(private val environment:SymbolProcessorEnvironment):SymbolProcessor{
    override fun process(resolver: Resolver): List<KSAnnotated> {
        environment.codeGenerator
        environment.options
        resolver.getSymbolsWithAnnotation(ByteParseTarget::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .forEach {
            it.getAllProperties().forEach {
                it.type.resolve()
                environment.logger.info("我的",it)
            }
        }
        return emptyList()
    }
}