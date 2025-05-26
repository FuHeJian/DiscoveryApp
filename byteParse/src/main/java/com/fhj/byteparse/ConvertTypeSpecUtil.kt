package com.fhj.byteparse

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec

/**
 * 获取原始class的代码对应的TypeSpec，方便修改源文件，修改完之后需要writeTo
 */
fun KSClassDeclaration.getRawTypeSpec(): TypeSpec {

    val rawFile = FileSpec.builder(this.packageName.asString(), this.simpleName.asString())
        .addImport(this.packageName.asString(), this.simpleName.asString())

    val cons = this.primaryConstructor?.run {
        val ps = this.parameters.map {
            val cl = it.type.resolve().declaration.run {
                ClassName(packageName.asString(), simpleName.asString())
            }
            it.annotations
            ParameterSpec.builder(it.name!!.asString(), cl)
                .addAnnotation()
                .build()
        }
        FunSpec.constructorBuilder()
            .addParameters(ps)
            .build()
    }
    val can = this.annotations
        .map {
            AnnotationSpec
                .builder(ClassName.bestGuess(it.annotationType.resolve().declaration.qualifiedName?.asString()!!))
                .addMember("length = %L", it.arguments[0].value!!)
                .build()
        }.toList()
    val cm = this.modifiers.map { it.toPoetModifier() }

    val ps = this.getDeclaredProperties().map {
        //获取属性初始化的值
        PropertySpec.builder(it.simpleName.asString(), it.type.resolve().declaration.run {
            ClassName.bestGuess(it.qualifiedName!!.asString())
        }, it.modifiers.map { it.toPoetModifier() })
            .build()
    }.toList()

    val fs = this.getDeclaredFunctions().map {
        it.toPoetFunctionSpec()
    }.toList()

    return TypeSpec.classBuilder(this.simpleName.asString())
        .primaryConstructor(cons)
        .addAnnotations(can)
        .addModifiers(cm)
        .addProperties(ps)
        .addFunctions(fs)
        .addSuperinterfaces(this.superTypes.map {
            ClassName.bestGuess(it.resolve().declaration.qualifiedName!!.asString())
        }.toList())
        .build()

}

fun Modifier.toPoetModifier(): KModifier {
    when (this) {
        Modifier.PUBLIC -> return KModifier.PUBLIC
        Modifier.PRIVATE -> return KModifier.PRIVATE
        Modifier.PROTECTED -> return KModifier.PROTECTED
        Modifier.INTERNAL -> return KModifier.INTERNAL
        Modifier.ABSTRACT -> return KModifier.ABSTRACT
        Modifier.SEALED -> return KModifier.SEALED
        Modifier.FINAL -> return KModifier.FINAL
        Modifier.OPEN -> return KModifier.OPEN
        Modifier.INNER -> return KModifier.INNER
        Modifier.DATA -> return KModifier.DATA
        Modifier.ENUM -> return KModifier.ENUM
        Modifier.ANNOTATION -> return KModifier.ANNOTATION
        Modifier.INFIX -> return KModifier.INFIX
        Modifier.INLINE -> return KModifier.INLINE
        Modifier.LATEINIT -> return KModifier.LATEINIT
        Modifier.NOINLINE -> return KModifier.NOINLINE
        Modifier.OVERRIDE -> return KModifier.OVERRIDE
        Modifier.OUT -> return KModifier.OUT
        Modifier.OPERATOR -> return KModifier.OPERATOR
        Modifier.SUSPEND -> return KModifier.SUSPEND
        Modifier.TAILREC -> return KModifier.TAILREC
        Modifier.VARARG -> return KModifier.VARARG
        Modifier.VALUE -> return KModifier.VALUE
        Modifier.CROSSINLINE -> return KModifier.CROSSINLINE
        Modifier.EXPECT -> return KModifier.EXPECT
        Modifier.IN -> return KModifier.IN
        Modifier.FUN -> return KModifier.FUN
        Modifier.EXTERNAL -> return KModifier.EXTERNAL
        Modifier.CONST -> return KModifier.CONST
        Modifier.REIFIED -> return KModifier.REIFIED
        Modifier.ACTUAL -> return KModifier.ACTUAL
        else -> return KModifier.PUBLIC
    }
}

fun KSValueParameter.toPoetParameter(): ParameterSpec {
    val cl = type.resolve().declaration.run {
        ClassName(packageName.asString(), simpleName.asString())
    }
    return ParameterSpec.builder(name!!.asString(), cl)
        .addAnnotations(annotations.map { it.toPoetAnnotation() }.toList())
        .build()
}

fun KSAnnotation.toPoetAnnotation(): AnnotationSpec {

    val spec = AnnotationSpec
        .builder(ClassName.bestGuess(annotationType.resolve().declaration.qualifiedName?.asString()!!))

    if (arguments.isNotEmpty()) {
        for (argument in arguments) {
            spec.addMember("%L = %L", argument.name!!.asString(), argument.value!!)
        }
    }

    return spec.build()
}

fun KSFunctionDeclaration.toPoetFunctionSpec(): FunSpec {

    // 获取函数体源码（需从原文件截取）
    val bodyCode = body?.let { body ->
        val fileContent = (containingFile as KSFile).fileContent
        fileContent.substring(body.startOffset, body.endOffset)
            .trim()
            .removeSurrounding("{", "}") // 去除外层大括号
            .trimIndent()
    }


    this.returnType.resolve().declaration.qualifiedName


    return FunSpec.builder(simpleName.asString())
        .addModifiers(modifiers.map { it.toPoetModifier() })
        .addParameters(parameters.map { it.toPoetParameter() })
        .returns()
        .build()
}

fun KSTypeReference.toTypeName(){

    this.resolve().isFunctionType


}