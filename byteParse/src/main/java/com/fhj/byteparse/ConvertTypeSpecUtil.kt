package com.fhj.byteparse

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.google.flatbuffers.FlatBufferBuilder
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
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
                .addAnnotations(it.annotations.map { it.toPoetAnnotation() }.toList())
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

    /*
        // 获取函数体源码（需从原文件截取）
        val bodyCode = body?.let { body ->
            val fileContent = (containingFile as KSFile).fileContent
            fileContent.substring(body.startOffset, body.endOffset)
                .trim()
                .removeSurrounding("{", "}") // 去除外层大括号
                .trimIndent()
        }

        this.returnType.modifiers
        this.returnType.resolve().declaration.qualifiedName
    */

    return FunSpec.builder(simpleName.asString())
        .addModifiers(modifiers.map { it.toPoetModifier() })
        .addParameters(parameters.map { it.toPoetParameter() })
        .build()
}

fun KSType.toTypeName() {


}

/**
 * 打印class的日志
 */
fun KSClassDeclaration.log(ksplog: KSPLogger) {
    val gson = GsonBuilder()
        .setExclusionStrategies(object : ExclusionStrategy {
            var num = 0
            override fun shouldSkipField(f: FieldAttributes?): Boolean {
                if (f == null) return false
                if (f.declaringClass.name.contains("atomic") || f.name.contains("simpleName") || f.name.contains(
                        "annotations"
                    ) || f.name.contains(
                        "parentDeclaration"
                    )
                ) {
                    return true
                }
                return num++ > 200
            }

            override fun shouldSkipClass(clazz: Class<*>?): Boolean {
                if (clazz == null) return false

                try {
                    clazz.genericSuperclass
                    clazz.genericInterfaces
                } catch (_: Exception) {
                    return true
                }
                return false
            }
        })
        .registerTypeAdapterFactory(object : TypeAdapterFactory {
            override fun <T : Any?> create(
                gson: Gson?,
                type: TypeToken<T?>?
            ): TypeAdapter<T?>? {
                if (KSDeclaration::class.java.isAssignableFrom(type!!.rawType)) {
                    val delegateAdapter = gson!!.getDelegateAdapter(this, type)
                    // 生成默认的 JsonObject 转换器
                    val jsonAdapter: TypeAdapter<JsonObject> =
                        gson.getAdapter(JsonObject::class.java)
                    return object : TypeAdapter<T?>() {
                        override fun write(
                            out: JsonWriter?,
                            value: T?
                        ) {
                            val cv = value as KSDeclaration

                            val jobj = delegateAdapter.toJsonTree(value!!).asJsonObject
                            val wjoj = JsonObject().apply {
                                addProperty("simpleName", cv.simpleName.asString())
                                addProperty("qualifyName", cv.qualifiedName?.asString() ?: "null")
                                addProperty("type", cv.javaClass.name)
                                addProperty(
                                    "泛型信息",
                                    cv.typeParameters.fold(
                                        ""
                                    ) { a, b -> a + "," + b.name.asString() }
                                )
                                addProperty(
                                    "annoations",
                                    cv.annotations.fold("",
                                        { a, b ->
                                            a + "," + b.arguments.fold("",
                                                { a, b -> a + "," + b.value.toString() })
                                        })
                                )
                                add("data",jobj)
                            }

                            jsonAdapter.write(out, wjoj)
                        }

                        override fun read(`in`: JsonReader?): T? {
                            return delegateAdapter.read(`in`)
                        }
                    }
                } else {
                    return null
                }
            }
        })
        .create()
    ksplog.info("我的" + gson.toJson(this))
}

data class ParseProperty(val name: String, val byteLength: Int)

fun KSClassDeclaration.createByteParse() {
    val packagename = this.packageName

    this.typeParameters

    //判断是不是需要解析
    val needParse =
        this.annotations.find { it.annotationType.resolve().declaration.simpleName.asString() == ByteParseTarget::class.simpleName!! }

    if (needParse != null) {
        this.getAllProperties().mapNotNull { it }
    }

    val tn = ClassName.bestGuess(this.qualifiedName?.asString()!!)

    CodeBlock.Builder().apply {

    }
    //由于无法还原文件内容，所以只能生成一个文件，来添加扩展方法
//    FunSpec.builder("parse")
//        .receiver(tn)
//        .addCode(CodeBlock.of())
//        .build()

}

fun createNewFile() {


}