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
    .create()

  @JvmStatic
  private val FreeMemory = Gauge.build()
    .name("process_free_memory")
    .help("Unused allocated memory.")
    .create()

  @JvmStatic
  private val UsedMemory = Gauge.build()
    .name("process_active_memory")
    .help("Allocated memory currently in use.")
    .create()

  @JvmStatic
  private val GCCount = Summary.build()
    .name("gc_count")
    .help("Number of garbage collections.")
    .create()

  @JvmStatic
  private val GCTime = Summary.build()
    .name("gc_time")
    .help("Total time used by the garbage collector.")
    .create()

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

    with(Runtime.getRuntime()) {
      total = totalMemory()
      free  = freeMemory()
      diff  = total - free
    }

    TotalMemory.set(total.toDouble())
    FreeMemory.set(free.toDouble())
    UsedMemory.set(diff.toDouble())

    var totalGC = 0L
    var gcTime = 0L

    ManagementFactory.getGarbageCollectorMXBeans().forEach {
      totalGC += it.collectionCount
      gcTime  += it.collectionTime
    }

    GCCount.observe(totalGC.toDouble())
    GCTime.observe(gcTime.toDouble())
  }
}