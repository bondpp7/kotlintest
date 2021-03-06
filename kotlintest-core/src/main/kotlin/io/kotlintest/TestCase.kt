package io.kotlintest

import io.kotlintest.extensions.TestCaseExtension
import java.time.Duration

/**
 * Describes an actual testcase.
 * That is, a unit of code that will be tested.
 *
 * A test case is always associated with a container,
 * called a [TestContainer]. Such a descriptor is used
 * to group together related test cases. This allows
 * hierarchical reporting and output using the rich
 * DSL of the [Spec] classes.
 */
data class TestCase(
    // the description contains the names of all parents, plus this one
    val description: Description,
    // the spec that contains this testcase
    val spec: Spec,
    // a closure of the test itself
    val test: TestContext.() -> Unit,
    // the first line number of the test
    val line: Int,
    // config used when running the test, such as number of
    // invocations, number of threads, etc
    var config: TestCaseConfig) : Scope {

  override fun name(): String = description.name
  override fun description(): Description = description

  fun config(
      invocations: Int? = null,
      enabled: Boolean? = null,
      timeout: Duration? = null,
      threads: Int? = null,
      tags: Set<Tag>? = null,
      extensions: List<TestCaseExtension>? = null) {
    config =
        TestCaseConfig(
            enabled ?: config.enabled,
            invocations ?: config.invocations,
            timeout ?: config.timeout,
            threads ?: config.threads,
            tags ?: config.tags,
            extensions ?: config.extensions)
  }

  fun isActive() = config.enabled && Project.tags().isActive(config.tags)
}

enum class TestStatus {
  // the test was skipped completely
  Ignored,
  // the test was successful
  Success,
  // the test failed because of some exception that was not an assertion error
  Error,
  // the test ran but an assertion failed
  Failure
}

data class TestResult(val status: TestStatus, val error: Throwable?, val reason: String?, val metaData: Map<String, Any?> = emptyMap()) {
  companion object {
    val Success = TestResult(TestStatus.Success, null, null)
    val Ignored = TestResult(TestStatus.Ignored, null, null)
    fun error(t: Throwable) = TestResult(TestStatus.Error, t, null)
    fun ignored(reason: String?) = TestResult(TestStatus.Ignored, null, reason)
  }
}