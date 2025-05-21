package com.fhj.byteparse


import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class ByteSymbol : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        environment.logger.warn("我的")
        return ByteSymbolProcessor(environment)
    }
}

class ByteSymbolProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        environment.codeGenerator
        environment.options
        resolver.getSymbolsWithAnnotation(ByteParseTarget::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .forEach {
                it.getAllProperties().forEach {
                    environment.logger.info("${it.simpleName.asString()} type ${it.type.resolve().arguments}")
                    environment.logger.info("${it.simpleName.asString()} ext: ${it.extensionReceiver.toString()}")
                    environment.logger.info("${it.simpleName.asString()} getter ${it.getter.toString()}")
                    environment.logger.info("${it.simpleName.asString()} setter ${it.setter.toString()}")
                    environment.logger.info("${it.simpleName.asString()} file ${it.containingFile?.fileName}")
                    environment.logger.info("${it.simpleName.asString()} override ${it.findOverridee().toString()}")
                    environment.logger.info("${it.simpleName.asString()} pd ${it.parentDeclaration?.simpleName?.asString()}" )
                    environment.logger.info("${it.annotations.fold(""){a,b->a+b.annotationType + "--" +b.arguments + "---" + b}} ")
                }
            }
        return emptyList()
    }
}