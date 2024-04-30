package org.veupathdb.lib.prom

import io.prometheus.client.Gauge
import io.prometheus.client.Summary
import java.lang.management.ManagementFactory

/**
 * Prometheus JVM Stats
 *
 * Access point to enable JVM stats for a service.
 */
object PrometheusJVM {

  @JvmStatic
  private val TotalMemory = Gauge.build()
    .name("process_total_memory")
    .help("Total memory allocated by this java process.")
    .register()

  @JvmStatic
  private val FreeMemory = Gauge.build()
    .name("process_free_memory")
    .help("Unused allocated memory.")
    .register()

  @JvmStatic
  private val UsedMemory = Gauge.build()
    .name("process_active_memory")
    .help("Allocated memory currently in use.")
    .register()

  @JvmStatic
  private val UsedMemoryAfterGC = Gauge.build()
    .name("process_active_memory_after_gc")
    .help("Allocated memory currently in use after garbage collection. This can be indicative of memory accounted for by long-lived objects.")
    .register()

  @JvmStatic
  private val GCCount = Summary.build()
    .name("gc_count")
    .help("Number of garbage collections.")
    .register()

  @JvmStatic
  private val GCTime = Summary.build()
    .name("gc_time")
    .help("Total time used by the garbage collector.")
    .register()

  @JvmStatic
  private val Threads = Gauge.build()
    .name("thread_count")
    .help("Number of active threads.")
    .register()

  /**
   * Enable JVM stats.
   */
  @JvmStatic
  fun enable() {

    Thread {
      while (true) {
        Thread.sleep(500)
        calculate()
      }
    }.start()

  }

  @JvmStatic
  private fun calculate() {
    val total: Long
    val free: Long
    val diff: Long
    val memAfterGC: Long?

    with(Runtime.getRuntime()) {
      total = totalMemory()
      free = freeMemory()
      diff = total - free
      memAfterGC = calculateMemoryAfterGC()
    }

    TotalMemory.set(total.toDouble())
    FreeMemory.set(free.toDouble())
    UsedMemory.set(diff.toDouble())
    memAfterGC?.let { UsedMemoryAfterGC.set(it.toDouble()) }

    var totalGC = 0L
    var gcTime = 0L

    ManagementFactory.getGarbageCollectorMXBeans().forEach {
      totalGC += it.collectionCount
      gcTime += it.collectionTime
    }

    Threads.set(ManagementFactory.getThreadMXBean().threadCount.toDouble())

    GCCount.observe(totalGC.toDouble())
    GCTime.observe(gcTime.toDouble())
  }

  /**
   * Returns memory after last garbage collection. Returns null if no GC has happened.
   */
  private fun calculateMemoryAfterGC(): Long? {
    val mxbeans: List<com.sun.management.GarbageCollectorMXBean> =
      ManagementFactory.getPlatformMXBeans(com.sun.management.GarbageCollectorMXBean::class.java)

    val memUsedByPool = mxbeans
      .filter { it.name.contains("G1 Young Generation") }
      .filter { it.lastGcInfo != null }
      .flatMap { it.lastGcInfo.memoryUsageAfterGc.entries }
      .filter { it.key.contains("G1") }
      .map { it.value.used }

    return if (memUsedByPool.isNotEmpty()) {
      memUsedByPool.reduce { totalUsed: Long, used: Long -> totalUsed + used }
    } else {
      null
    }
  }
}