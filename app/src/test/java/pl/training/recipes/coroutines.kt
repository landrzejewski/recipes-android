package pl.training.recipes

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/*
    Coroutines are components that can be suspended and resumed. Unlike the use of threads, there is no blocking here.
    When a thread is blocked, it still consumes resources and needs to be managed by the operating system.
    When a coroutine is suspended, the only thing that remains is an object that keeps references to local variables
    and the place where this coroutine was suspended. Coroutines are lightweight abstractions that run on top of threads,
    managed by the coroutine library.

    Suspending functions (functions marked with suspend modifier) are functions that can suspend a coroutine. and must be called
    by other suspending functions or by coroutine builders that start coroutines. Suspending functions are not coroutines,
    but they require coroutines.

    The continuation is an object that stores the state of the coroutine. It must also
    store the local variables and the place where the coroutine was suspended.
 */

/*
    Suspending functions are like state machines, with a possible state at the beginning of the function and after each suspending function call.

    Both the number identifying the state and the local data are kept in the continuation object passed as a last argument of a function.

    Continuation of a function decorates a continuation of its caller function; as a result, all these continuations
    represent a call stack that is used when we resume or a resumed function completes.

    So function suspend fun myFunction(): User? becomes fun myFunction(continuation: Continuation<*>): Any? // can return COROUTINE_SUSPENDED or User?

    The next thing is that this function needs its own continuation in order to remember its state.
    Continuations serve as a call stack. Each continuation keeps the state where we suspended (as a label)
    the function’s local variables and parameters (as fields), and the reference to the continuation of the
    function that called this function. One continuation references another, which references another, etc.
    As a result, our continuation is like a huge onion: it keeps everything that is generally kept on the call stack.

    suspend fun myFunction() {
        println("Before")
        delay(1000) // suspending
        println("After")
    }

    A simplified picture of how myFunction looks under the hood. To identify the current state, we use a field called label.
    At the start, it is 0, therefore the function will start from the beginning. However, it is set to the next state
    before each suspension point so that we start from just after the suspension point after a resume.
    When delay is suspended, it returns COROUTINE_SUSPENDED, then myFunction returns COROUTINE_SUSPENDED;
    the same is done by the function that called it, and the function that called this function, and all
    other functions until the top of the call stack. This is how a suspension ends all these functions and leaves the
    thread available for other runnables (including coroutines) to be used (not returning COROUTINE_SUSPENDED would
    cause the execution of next state).

    fun myFunction(continuation: Continuation<Unit>): Any {
        val continuation = continuation as? MyFunctionContinuation ?: MyFunctionContinuation(continuation) // We only need to wrap continuation during first call
        if (continuation.label == 0) {
            println("Before")
            continuation.label = 1
            if (delay(1000, continuation) == COROUTINE_SUSPENDED) {
                return COROUTINE_SUSPENDED
            }
        }
        if (continuation.label == 1) {
            println("After")
            return Unit
        }
        error("Impossible")
    }

    class MyFunctionContinuation(val completion: Continuation<Unit>) : Continuation<Unit> {
        override val context: CoroutineContext
            get() = completion.context
        var label = 0
        var result: Result<Any>? = null
        override fun resumeWith(result: Result<Unit>) {
            this.result = result
            val res = try {
                val r = myFunction(this)
                if (r == COROUTINE_SUSPENDED) return
                Result.success(r as Unit)
            } catch (e: Throwable) {
                Result.failure(e)
            }
            completion.resumeWith(res)
        }
    }

    If function has a state (local variables or parameters) that needs to be restored after suspension.
    Here counter is needed in two states (for a label equal to 0 and 1), so it needs to be kept in the continuation.
    I twill be stored right before suspension. Restoring these kinds of properties happens at the beginning of the function.

    suspend fun myFunction() {
        println("Before")
        var counter = 0
        delay(1000)
        counter++
        println("Counter: $counter")
        println("After")
    }

    fun myFunction(continuation: Continuation<Unit>): Any {
        val continuation = continuation as? MyFunctionContinuation ?: MyFunctionContinuation(continuation)
        var counter = continuation.counter
        if (continuation.label == 0) {
            println("Before")
            counter = 0
            continuation.counter = counter
            continuation.label = 1
            if (delay(1000, continuation) == COROUTINE_SUSPENDED) {
                return COROUTINE_SUSPENDED
            }
        }
        if (continuation.label == 1) {
            counter = (counter as Int) + 1
            println("Counter: $counter")
            println("After")
            return Unit
        }
        error("Impossible")
    }

    class MyFunctionContinuation(val completion: Continuation<Unit>) : Continuation<Unit> {
        override val context: CoroutineContext
            get() = completion.context
        var result: Result<Unit>? = null
        var label = 0
        var counter = 0
        override fun resumeWith(result: Result<Unit>) {
            this.result = result
            val res = try {
                val r = myFunction(this)
                if (r == COROUTINE_SUSPENDED) return
                    Result.success(r as Unit)
            } catch (e: Throwable) {
                Result.failure(e)
            }
            completion.resumeWith(res)
        }
    }

    In case function resumed with value

    suspend fun printUser(token: String) {
        println("Before")
        val userId = getUserId(token) // suspending
        println("Got userId: $userId")
        val userName = getUserName(userId, token) // suspending
        println(User(userId, userName))
        println("After")
    }

    fun printUser(token: String, continuation: Continuation<*>): Any {
        val continuation = continuation as? PrintUserContinuation ?: PrintUserContinuation(
            continuation as Continuation<Unit>,
            token
        )
        var result: Result<Any>? = continuation.result
        var userId: String? = continuation.userId

        val userName: String
        if (continuation.label == 0) {
            println("Before")
            continuation.label = 1
            val res = getUserId(token, continuation)
            if (res == COROUTINE_SUSPENDED) {
                return COROUTINE_SUSPENDED
            }
            result = Result.success(res)
        }
        if (continuation.label == 1) {
            userId = result!!.getOrThrow() as String
            println("Got userId: $userId")
            continuation.label = 2
            continuation.userId = userId
            val res = getUserName(userId, continuation)
            if (res == COROUTINE_SUSPENDED) {
                return COROUTINE_SUSPENDED
            }
            result = Result.success(res)
        }
        if (continuation.label == 2) {
            userName = result!!.getOrThrow() as String
            println(User(userId as String, userName))
            println("After")
            return Unit
        }
        error("Impossible")
    }

    class PrintUserContinuation(val completion: Continuation<Unit>, val token: String) : Continuation<String> {
        override val context: CoroutineContext
            get() = completion.context
        var label = 0
        var result: Result<Any>? = null
        var userId: String? = null
        override fun resumeWith(result: Result<String>) {
            this.result = result
            val res = try {
                val r = printUser(token, this)
                if (r == COROUTINE_SUSPENDED) return
                Result.success(r as Unit)
            } catch (e: Throwable) {
                Result.failure(e)
            }
            completion.resumeWith(res)
        }
    }

    Suspending functions are like state machines, with a possible state at the beginning of the function and after
    each suspending function call.
    Both the label identifying the state and the local data are kept in the continuation object.
    Continuation of one function decorates a continuation of its caller function; as a result, all these continuations
    represent a call stack that is used where resume or a resumed function completes.
 */

/*
    Starting coroutines is possible using:
        Asynchronous coroutine builders (launch and async), which start an asynchronous coroutine
        Blocking coroutine builders (runBlocking and runTest), which start a coroutine on the current thread and block it until the coroutine is done
        Coroutine scope functions, which create a synchronous coroutine (suspend the current coroutine until the new one is completed)
    If we call await on an already completed Deferred, then there is no suspending
*/

suspend fun printMessage() {
    delay(1_000)
    println("Message ${Thread.currentThread().name}")
}

/*
suspend fun main() {
    println("Before ${Thread.currentThread().name}")
    printMessage()
    try {
        val result = suspendCoroutine<Int> { continuation ->
            println("Before continuation ${Thread.currentThread().name}")
            continuation.resumeWith(Result.success(10))
        }
        println("Result: $result ${Thread.currentThread().name}")
    } catch (e: Exception) {
        println(e)
    }
}*/

val dispatcher = Executors.newFixedThreadPool(20)
    .asCoroutineDispatcher()

/*suspend fun main() = coroutineScope {

    val result = async {
        println("Running")
        2
    }
    println(result.await())

    *//*repeat(1_000_000) {
        launch(dispatcher) {
            val threadName = Thread.currentThread().name
            println("Running on thread: $threadName")
        }
    }*//*
}*/


/*
    Structured Concurrency
    When a coroutine is started on a scope, it becomes a child of this scope.
    The parent-child relationship has a couple of important consequences:
        Children inherit context from their parent (but they can also overwrite it)
        A parent cannot complete until all its children have completed
        When the parent is cancelled, its child coroutines are cancelled too
        When a child completes with an exception, this exception is passed to the parent

    GlobalScope is literally an empty scope that configures nothing and builds no relationship with coroutines started
    on it, therefore it is considered bad practice to use it because it can easily break our relationships and
    it cannot be used to control coroutines started on it

    Coroutine scope functions are suspending functions that start a synchronous coroutine - a new coroutine but suspend the current
    one until the new one is completed. They behave a lot like runBlocking, but instead of blocking the thread they suspend the coroutine

    Coroutine scope functions are suspending functions that do not require a scope, while coroutine builders are
    regular functions that do require a scope

    In practice, coroutine scope functions are used to create a scope for asynchronous coroutines

    coroutineScope is the most basic coroutine scope function
    withContext behaves just like coroutineScope but can change the context of the coroutine
    supervisorScope behaves just like coroutineScope but ignores its children’s exceptions
    withTimeout behaves just like coroutineScope but cancels the coroutine after a timeout
*/

/*
fun main() = runBlocking {
    try {
        val parentJob = launch {
            println("Parent job started")

            val childJob1 = launch {
                repeat(10) { i ->
                    delay(200)
                    println("Child job 1 - iter $i")
                }
            }

            val childJob2 = launch {
                repeat(10) { i ->
                    delay(2_000)
                    println("Child job 2- iter $i")
                    if (i == 0) {
                        throw RuntimeException()
                    }
                }
            }

            joinAll(childJob1, childJob2)
        }
        println(parentJob.children)
        delay(5_000)
        println("Canceling parent job")
        parentJob.cancel()
    } catch (e : RuntimeException) {
        println("Exception")
    }

}*/


/*
A Mutex (mutual exclusion) ensures that only one coroutine can access a critical section at a time.
It is coroutine-friendly and avoids thread blocking.
*/

/*
val mutex = Mutex()
var counter = 0

fun main() = runBlocking(Dispatchers.IO) {
    val jobs = List(1_000_000) {
        launch {
            // Only one coroutine can access this block at a time
            mutex.withLock {
                counter++
            }
        }
    }
    jobs.forEach { it.join() }
    println("Counter = $counter") // Ensures counter is updated correctly
}*/


/*
val atomicCounter = AtomicInteger(0)

fun main() = runBlocking(Dispatchers.IO) {
    val jobs = List(1_000_000) {
        launch {
            atomicCounter.incrementAndGet() // Atomic operation
        }
    }
    jobs.forEach { it.join() }
    println("Atomic Counter = ${atomicCounter.get()}")
}*/


/*
fun main() = runBlocking {
    val channel = Channel<Int>()
    val producer = launch {
        for (i in 1..100) {
            channel.send(i)
        }
        channel.close()
    }

    val consumer = launch {
        var sum = 0
        for (value in channel) {
            sum += value
        }
        println("Sum = $sum")
    }

    producer.join()
    consumer.join()
}*/


/*
    A flow represents a lazy process that emits values. The Flow interface itself only
    allows the flowing elements to be collected, which means handling each element
    as it reaches the end of the flow (collect for Flow is like forEach for collections).
*/

suspend fun getUserName(): String {
    delay(1000)
    return "UserName"
}

/*suspend fun main() {
    flowOf(1, 2, 3, 4, 5)
        .filter { it > 2 }
        .map { it * 2 }
        .collect { print(it) } // 12345

    emptyFlow<Int>()
        .collect { print(it) } // (nothing)

    listOf(1, 2, 3, 4, 5)
        // or setOf(1, 2, 3, 4, 5)
        // or sequenceOf(1, 2, 3, 4, 5)
        .asFlow()
        .collect { print(it) } // 12345

    val function = suspend {
        // this is suspending lambda expression
        delay(1000)
        "UserName"
    }
    function.asFlow()
        .collect { println(it) }

    ::getUserName.asFlow()
        .collect { println(it) }
}*/

data class User(val name: String)

interface UserApi {
    suspend fun takePage(pageNumber: Int): List<User>
}

class FakeUserApi : UserApi {
    private val users = List(20) { User("User$it") }
    private val pageSize: Int = 3
    override suspend fun takePage(
        pageNumber: Int
    ): List<User> {
        delay(1000) // suspending
        return users
            .drop(pageSize * pageNumber)
            .take(pageSize)
    }
}

fun allUsersFlow(api: UserApi): Flow<User> = flow {
    var page = 0
    do {
        println("Fetching page $page")
        val users = api.takePage(page++) // suspending
        emitAll(users.asFlow())
    } while (users.isNotEmpty())
}

suspend fun main() {
    val api = FakeUserApi()
    val users = allUsersFlow(api)
    val user = users
        .first {
            println("Checking $it")
            delay(1000) // suspending
            it.name == "User3"
        }
    println(user)
}
