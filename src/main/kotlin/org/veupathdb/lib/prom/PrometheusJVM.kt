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
  private val GCCount = Summary.build()
    .name("gc_count")
    .help("Number of garbage collections.")
    .register()

  @JvmStatic
  private val GCTime = Summary.build()
    .name("gc_time")
    .help("Total time used by the garbage collector.")
    .register()

  /**
   * Enable JVM stats.
   *
   * @param resolution Frequency at which the JVM stats will be measured.  Value
   * is in seconds.
   */
  @JvmStatic
  @JvmOverloads
  fun enable(resolution: Int = 5) {

    Thread {
      while (true) {
        Thread.sleep(resolution * 1000L)
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