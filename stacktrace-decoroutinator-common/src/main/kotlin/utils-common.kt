package dev.reformator.stacktracedecoroutinator.common

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.function.BiFunction

//not getting by reflection because it has not to lead to loading the class
const val BASE_CONTINUATION_CLASS_NAME = "kotlin.coroutines.jvm.internal.BaseContinuationImpl"
val DECOROUTINATOR_MARKER_CLASS_NAME = DecoroutinatorMarker::class.java.name

@Target(AnnotationTarget.CLASS)
@Retention
//has to be internal, but must be visible in 'stacktrace-decoroutinator-stdlib'
annotation class DecoroutinatorMarker

interface JavaUtils {
    fun retrieveResultValue(result: Result<*>): Any?
    fun retrieveResultThrowable(result: Result<*>): Throwable
}

val lookup: MethodHandles.Lookup = MethodHandles.publicLookup()

val invokeStacktraceMethodType: MethodType = MethodType.methodType(
    Object::class.java,
    Array<MethodHandle>::class.java, //stacktrace handles
    IntArray::class.java, //line numbers
    Int::class.javaPrimitiveType, //next step index
    BiFunction::class.java, //invoke coroutine function
    Object::class.java, //coroutine result
    Object::class.java //COROUTINE_SUSPENDED
)

val unknownStacktraceMethodHandle: MethodHandle = lookup.findStatic(
    Class.forName("UnknownKt"),
    "unknown",
    invokeStacktraceMethodType
)

inline fun callStacktraceHandles(
    stacktraceHandles: Array<MethodHandle>,
    lineNumbers: IntArray,
    nextStepIndex: Int,
    invokeCoroutineFunction: BiFunction<Int, Any?, Any?>,
    result: Any?,
    coroutineSuspend: Any
): Any? {
    val updatedResult = if (nextStepIndex < stacktraceHandles.size) {
        val updatedResult = stacktraceHandles[nextStepIndex].invokeExact(
            stacktraceHandles,
            lineNumbers,
            nextStepIndex + 1,
            invokeCoroutineFunction,
            result,
            coroutineSuspend
        )
        if (updatedResult === coroutineSuspend) {
            return coroutineSuspend
        }
        updatedResult
    } else {
        result
    }
    return invokeCoroutineFunction.apply(nextStepIndex, updatedResult)
}

inline val Any.classLoader: ClassLoader?
    get() = javaClass.classLoader

inline val Class<*>.isDecoroutinatorBaseContinuation: Boolean
    get() = isAnnotationPresent(DecoroutinatorMarker::class.java)

fun getFileClass(func: () -> Unit): Class<*> =
    func.javaClass.enclosingClass
