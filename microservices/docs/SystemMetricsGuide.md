# 系统级监控指标文档

## 文档概述

本文档提供企业级分布式应用系统级监控指标的完整定义，涵盖CPU、内存、磁盘、网络等系统资源的监控指标、采集方法、告警阈值和优化建议。

## 目录

1. [系统级监控概述](#系统级监控概述)
2. [CPU监控指标](#cpu监控指标)
3. [内存监控指标](#内存监控指标)
4. [磁盘监控指标](#磁盘监控指标)
5. [网络监控指标](#网络监控指标)
6. [系统负载监控指标](#系统负载监控指标)
7. [进程监控指标](#进程监控指标)
8. [告警阈值配置](#告警阈值配置)

## 系统级监控概述

### 系统级监控定义

系统级监控是对操作系统层面的资源使用情况进行监控，包括CPU、内存、磁盘、网络等系统资源。

### 系统级监控重要性

- **资源利用率监控**：实时监控系统资源使用情况
- **性能瓶颈识别**：快速定位系统性能瓶颈
- **容量规划**：为系统扩容提供数据支持
- **故障预警**：提前发现系统资源不足

### 系统级监控范围

```yaml
system_monitoring_scope:
  resources:
    - "CPU使用率"
    - "内存使用率"
    - "磁盘使用率"
    - "磁盘I/O"
    - "网络I/O"
    - "系统负载"
    - "进程状态"
  
  metrics:
    - "使用率"
    - "空闲率"
    - "等待时间"
    - "队列长度"
    - "错误率"
    - "吞吐量"
```

## CPU监控指标

### CPU使用率指标

#### 1. CPU总使用率

**指标名称**：`system.cpu.usage`

**指标描述**：CPU总使用率，包括用户态、内核态和IO等待时间

**采集方法**：

```java
@Component
public class CpuMetricsCollector {

    private final OperatingSystemMXBean osBean = 
            ManagementFactory.getOperatingSystemMXBean();

    @Scheduled(fixedRate = 5000)
    public void collectCpuUsage() {
        double cpuUsage = osBean.getSystemLoadAverage() * 100;
        
        Metrics.gauge("system.cpu.usage", cpuUsage)
                .tag("host", getHostname())
                .register(Metrics.globalRegistry);
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
```

**告警阈值**：

```yaml
cpu_usage_alerts:
  warning:
    threshold: 70
    duration: "5m"
    message: "CPU使用率超过70%"
  
  critical:
    threshold: 90
    duration: "2m"
    message: "CPU使用率超过90%"
```

#### 2. CPU用户态使用率

**指标名称**：`system.cpu.user`

**指标描述**：CPU在用户态的执行时间占比

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectCpuUserUsage() {
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
    
    AttributeList list = mbs.getAttributes(name, 
            new String[]{"ProcessCpuLoad"});
    
    for (Attribute attr : list) {
        Double cpuUserLoad = (Double) attr.getValue();
        double cpuUserUsage = cpuUserLoad * 100;
        
        Metrics.gauge("system.cpu.user", cpuUserUsage)
                .tag("host", getHostname())
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
cpu_user_alerts:
  warning:
    threshold: 60
    duration: "5m"
    message: "CPU用户态使用率超过60%"
  
  critical:
    threshold: 80
    duration: "2m"
    message: "CPU用户态使用率超过80%"
```

#### 3. CPU内核态使用率

**指标名称**：`system.cpu.system`

**指标描述**：CPU在内核态的执行时间占比

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectCpuSystemUsage() {
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
    
    AttributeList list = mbs.getAttributes(name, 
            new String[]{"SystemCpuLoad"});
    
    for (Attribute attr : list) {
        Double cpuSystemLoad = (Double) attr.getValue();
        double cpuSystemUsage = cpuSystemLoad * 100;
        
        Metrics.gauge("system.cpu.system", cpuSystemUsage)
                .tag("host", getHostname())
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
cpu_system_alerts:
  warning:
    threshold: 30
    duration: "5m"
    message: "CPU内核态使用率超过30%"
  
  critical:
    threshold: 50
    duration: "2m"
    message: "CPU内核态使用率超过50%"
```

#### 4. CPU IO等待时间

**指标名称**：`system.cpu.iowait`

**指标描述**：CPU等待IO完成的时间占比

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectCpuIowait() {
    try {
        Process process = Runtime.getRuntime().exec("iostat -c 1 1");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("avg-cpu")) {
                String[] parts = line.trim().split("\\s+");
                double iowait = Double.parseDouble(parts[4]);
                
                Metrics.gauge("system.cpu.iowait", iowait)
                        .tag("host", getHostname())
                        .register(Metrics.globalRegistry);
            }
        }
        
        reader.close();
        process.waitFor();
    } catch (Exception e) {
        log.error("Failed to collect CPU iowait", e);
    }
}
```

**告警阈值**：

```yaml
cpu_iowait_alerts:
  warning:
    threshold: 20
    duration: "5m"
    message: "CPU IO等待时间超过20%"
  
  critical:
    threshold: 40
    duration: "2m"
    message: "CPU IO等待时间超过40%"
```

### CPU负载指标

#### 1. 系统负载

**指标名称**：`system.load.1m`, `system.load.5m`, `system.load.15m`

**指标描述**：系统1分钟、5分钟、15分钟平均负载

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectSystemLoad() {
    double[] loadAverage = osBean.getSystemLoadAverage();
    
    if (loadAverage != null && loadAverage.length >= 3) {
        Metrics.gauge("system.load.1m", loadAverage[0])
                .tag("host", getHostname())
                .register(Metrics.globalRegistry);
        
        Metrics.gauge("system.load.5m", loadAverage[1])
                .tag("host", getHostname())
                .register(Metrics.globalRegistry);
        
        Metrics.gauge("system.load.15m", loadAverage[2])
                .tag("host", getHostname())
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
system_load_alerts:
  warning:
    threshold: 4
    duration: "5m"
    message: "系统负载超过4"
  
  critical:
    threshold: 8
    duration: "2m"
    message: "系统负载超过8"
```

#### 2. CPU上下文切换

**指标名称**：`system.cpu.context_switches`

**指标描述**：CPU上下文切换次数

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectContextSwitches() {
    try {
        Process process = Runtime.getRuntime().exec("vmstat 1 1");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.matches("^\\s+\\d+\\s+.*")) {
                String[] parts = line.trim().split("\\s+");
                long contextSwitches = Long.parseLong(parts[11]);
                
                Metrics.counter("system.cpu.context_switches")
                        .tag("host", getHostname())
                        .increment(contextSwitches, Metrics.globalRegistry);
            }
        }
        
        reader.close();
        process.waitFor();
    } catch (Exception e) {
        log.error("Failed to collect context switches", e);
    }
}
```

**告警阈值**：

```yaml
context_switches_alerts:
  warning:
    threshold: 100000
    duration: "1m"
    message: "CPU上下文切换次数超过100000/分钟"
  
  critical:
    threshold: 200000
    duration: "1m"
    message: "CPU上下文切换次数超过200000/分钟"
```

## 内存监控指标

### 内存使用率指标

#### 1. 内存总使用率

**指标名称**：`system.memory.usage`

**指标描述**：系统内存总使用率

**采集方法**：

```java
@Component
public class MemoryMetricsCollector {

    private final OperatingSystemMXBean osBean = 
            ManagementFactory.getOperatingSystemMXBean();

    @Scheduled(fixedRate = 5000)
    public void collectMemoryUsage() {
        com.sun.management.OperatingSystemMXBean sunOsBean = 
                (com.sun.management.OperatingSystemMXBean) osBean;
        
        long totalMemory = sunOsBean.getTotalPhysicalMemorySize();
        long freeMemory = sunOsBean.getFreePhysicalMemorySize();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsage = (double) usedMemory / totalMemory * 100;
        
        Metrics.gauge("system.memory.usage", memoryUsage)
                .tag("host", getHostname())
                .register(Metrics.globalRegistry);
        
        Metrics.gauge("system.memory.used", usedMemory)
                .tag("host", getHostname())
                .register(Metrics.globalRegistry);
        
        Metrics.gauge("system.memory.free", freeMemory)
                .tag("host", getHostname())
                .register(Metrics.globalRegistry);
        
        Metrics.gauge("system.memory.total", totalMemory)
                .tag("host", getHostname())
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
memory_usage_alerts:
  warning:
    threshold: 80
    duration: "5m"
    message: "内存使用率超过80%"
  
  critical:
    threshold: 90
    duration: "2m"
    message: "内存使用率超过90%"
```

#### 2. Swap使用率

**指标名称**：`system.memory.swap.usage`

**指标描述**：Swap空间使用率

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectSwapUsage() {
    try {
        Process process = Runtime.getRuntime().exec("free -m");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("Swap:")) {
                String[] parts = line.trim().split("\\s+");
                long totalSwap = Long.parseLong(parts[1]) * 1024 * 1024;
                long usedSwap = Long.parseLong(parts[2]) * 1024 * 1024;
                double swapUsage = (double) usedSwap / totalSwap * 100;
                
                Metrics.gauge("system.memory.swap.usage", swapUsage)
                        .tag("host", getHostname())
                        .register(Metrics.globalRegistry);
                
                Metrics.gauge("system.memory.swap.used", usedSwap)
                        .tag("host", getHostname())
                        .register(Metrics.globalRegistry);
                
                Metrics.gauge("system.memory.swap.total", totalSwap)
                        .tag("host", getHostname())
                        .register(Metrics.globalRegistry);
            }
        }
        
        reader.close();
        process.waitFor();
    } catch (Exception e) {
        log.error("Failed to collect swap usage", e);
    }
}
```

**告警阈值**：

```yaml
swap_usage_alerts:
  warning:
    threshold: 50
    duration: "5m"
    message: "Swap使用率超过50%"
  
  critical:
    threshold: 80
    duration: "2m"
    message: "Swap使用率超过80%"
```

### 内存缓存指标

#### 1. 缓存使用率

**指标名称**：`system.memory.cache.usage`

**指标描述**：系统缓存使用率

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectCacheUsage() {
    try {
        Process process = Runtime.getRuntime().exec("free -m");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("Mem:")) {
                String[] parts = line.trim().split("\\s+");
                long totalMemory = Long.parseLong(parts[1]) * 1024 * 1024;
                long cacheMemory = Long.parseLong(parts[5]) * 1024 * 1024;
                double cacheUsage = (double) cacheMemory / totalMemory * 100;
                
                Metrics.gauge("system.memory.cache.usage", cacheUsage)
                        .tag("host", getHostname())
                        .register(Metrics.globalRegistry);
                
                Metrics.gauge("system.memory.cache.used", cacheMemory)
                        .tag("host", getHostname())
                        .register(Metrics.globalRegistry);
            }
        }
        
        reader.close();
        process.waitFor();
    } catch (Exception e) {
        log.error("Failed to collect cache usage", e);
    }
}
```

**告警阈值**：

```yaml
cache_usage_alerts:
  warning:
    threshold: 70
    duration: "5m"
    message: "缓存使用率超过70%"
  
  critical:
    threshold: 90
    duration: "2m"
    message: "缓存使用率超过90%"
```

## 磁盘监控指标

### 磁盘使用率指标

#### 1. 磁盘使用率

**指标名称**：`system.disk.usage`

**指标描述**：磁盘空间使用率

**采集方法**：

```java
@Component
public class DiskMetricsCollector {

    @Scheduled(fixedRate = 60000)
    public void collectDiskUsage() {
        File[] roots = File.listRoots();
        
        for (File root : roots) {
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            double diskUsage = (double) usedSpace / totalSpace * 100;
            
            Metrics.gauge("system.disk.usage", diskUsage)
                    .tag("host", getHostname())
                    .tag("mount", root.getAbsolutePath())
                    .register(Metrics.globalRegistry);
            
            Metrics.gauge("system.disk.used", usedSpace)
                    .tag("host", getHostname())
                    .tag("mount", root.getAbsolutePath())
                    .register(Metrics.globalRegistry);
            
            Metrics.gauge("system.disk.free", freeSpace)
                    .tag("host", getHostname())
                    .tag("mount", root.getAbsolutePath())
                    .register(Metrics.globalRegistry);
            
            Metrics.gauge("system.disk.total", totalSpace)
                    .tag("host", getHostname())
                    .tag("mount", root.getAbsolutePath())
                    .register(Metrics.globalRegistry);
        }
    }
}
```

**告警阈值**：

```yaml
disk_usage_alerts:
  warning:
    threshold: 80
    duration: "5m"
    message: "磁盘使用率超过80%"
  
  critical:
    threshold: 90
    duration: "2m"
    message: "磁盘使用率超过90%"
```

#### 2. 磁盘Inode使用率

**指标名称**：`system.disk.inode.usage`

**指标描述**：磁盘Inode使用率

**采集方法**：

```java
@Scheduled(fixedRate = 60000)
public void collectInodeUsage() {
    try {
        Process process = Runtime.getRuntime().exec("df -i");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("Filesystem") && !line.startsWith("Mounted")) {
                String[] parts = line.trim().split("\\s+");
                String mount = parts[5];
                long totalInodes = Long.parseLong(parts[1]);
                long usedInodes = Long.parseLong(parts[2]);
                double inodeUsage = (double) usedInodes / totalInodes * 100;
                
                Metrics.gauge("system.disk.inode.usage", inodeUsage)
                        .tag("host", getHostname())
                        .tag("mount", mount)
                        .register(Metrics.globalRegistry);
            }
        }
        
        reader.close();
        process.waitFor();
    } catch (Exception e) {
        log.error("Failed to collect inode usage", e);
    }
}
```

**告警阈值**：

```yaml
inode_usage_alerts:
  warning:
    threshold: 80
    duration: "5m"
    message: "Inode使用率超过80%"
  
  critical:
    threshold: 90
    duration: "2m"
    message: "Inode使用率超过90%"
```

### 磁盘I/O指标

#### 1. 磁盘I/O使用率

**指标名称**：`system.disk.io.usage`

**指标描述**：磁盘I/O使用率

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectDiskIoUsage() {
    try {
        Process process = Runtime.getRuntime().exec("iostat -x 1 1");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.matches("^\\w+.*")) {
                String[] parts = line.trim().split("\\s+");
                String device = parts[0];
                double ioUsage = Double.parseDouble(parts[11]);
                
                Metrics.gauge("system.disk.io.usage", ioUsage)
                        .tag("host", getHostname())
                        .tag("device", device)
                        .register(Metrics.globalRegistry);
            }
        }
        
        reader.close();
        process.waitFor();
    } catch (Exception e) {
        log.error("Failed to collect disk I/O usage", e);
    }
}
```

**告警阈值**：

```yaml
disk_io_usage_alerts:
  warning:
    threshold: 70
    duration: "5m"
    message: "磁盘I/O使用率超过70%"
  
  critical:
    threshold: 90
    duration: "2m"
    message: "磁盘I/O使用率超过90%"
```

#### 2. 磁盘IOPS

**指标名称**：`system.disk.io.iops`

**指标描述**：磁盘每秒读写次数

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectDiskIops() {
    try {
        Process process = Runtime.getRuntime().exec("iostat -x 1 1");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.matches("^\\w+.*")) {
                String[] parts = line.trim().split("\\s+");
                String device = parts[0];
                double readsPerSec = Double.parseDouble(parts[3]);
                double writesPerSec = Double.parseDouble(parts[4]);
                double totalIops = readsPerSec + writesPerSec;
                
                Metrics.gauge("system.disk.io.iops", totalIops)
                        .tag("host", getHostname())
                        .tag("device", device)
                        .register(Metrics.globalRegistry);
            }
        }
        
        reader.close();
        process.waitFor();
    } catch (Exception e) {
        log.error("Failed to collect disk IOPS", e);
    }
}
```

**告警阈值**：

```yaml
disk_iops_alerts:
  warning:
    threshold: 1000
    duration: "5m"
    message: "磁盘IOPS超过1000"
  
  critical:
    threshold: 2000
    duration: "2m"
    message: "磁盘IOPS超过2000"
```

#### 3. 磁盘吞吐量

**指标名称**：`system.disk.io.throughput`

**指标描述**：磁盘每秒读写字节数

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectDiskThroughput() {
    try {
        Process process = Runtime.getRuntime().exec("iostat -x 1 1");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.matches("^\\w+.*")) {
                String[] parts = line.trim().split("\\s+");
                String device = parts[0];
                double readKbPerSec = Double.parseDouble(parts[5]);
                double writeKbPerSec = Double.parseDouble(parts[6]);
                double totalKbPerSec = readKbPerSec + writeKbPerSec;
                
                Metrics.gauge("system.disk.io.throughput.kb", totalKbPerSec)
                        .tag("host", getHostname())
                        .tag("device", device)
                        .register(Metrics.globalRegistry);
                
                Metrics.gauge("system.disk.io.throughput.mb", totalKbPerSec / 1024)
                        .tag("host", getHostname())
                        .tag("device", device)
                        .register(Metrics.globalRegistry);
            }
        }
        
        reader.close();
        process.waitFor();
    } catch (Exception e) {
        log.error("Failed to collect disk throughput", e);
    }
}
```

**告警阈值**：

```yaml
disk_throughput_alerts:
  warning:
    threshold: 102400
    duration: "5m"
    message: "磁盘吞吐量超过100MB/s"
  
  critical:
    threshold: 204800
    duration: "2m"
    message: "磁盘吞吐量超过200MB/s"
```

## 网络监控指标

### 网络流量指标

#### 1. 网络入站流量

**指标名称**：`system.network.rx.bytes`

**指标描述**：网络入站字节数

**采集方法**：

```java
@Component
public class NetworkMetricsCollector {

    @Scheduled(fixedRate = 5000)
    public void collectNetworkRxBytes() {
        try {
            Process process = Runtime.getRuntime().exec("cat /proc/net/dev");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("Inter-") && !line.startsWith("face")) {
                    String[] parts = line.trim().split("\\s+");
                    String interfaceName = parts[0].replace(":", "");
                    long rxBytes = Long.parseLong(parts[1]);
                    
                    Metrics.counter("system.network.rx.bytes")
                            .tag("host", getHostname())
                            .tag("interface", interfaceName)
                            .increment(rxBytes, Metrics.globalRegistry);
                }
            }
            
            reader.close();
            process.waitFor();
        } catch (Exception e) {
            log.error("Failed to collect network RX bytes", e);
        }
    }
}
```

**告警阈值**：

```yaml
network_rx_alerts:
  warning:
    threshold: 104857600
    duration: "5m"
    message: "网络入站流量超过100MB/s"
  
  critical:
    threshold: 209715200
    duration: "2m"
    message: "网络入站流量超过200MB/s"
```

#### 2. 网络出站流量

**指标名称**：`system.network.tx.bytes`

**指标描述**：网络出站字节数

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectNetworkTxBytes() {
    try {
        Process process = Runtime.getRuntime().exec("cat /proc/net/dev");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("Inter-") && !line.startsWith("face")) {
                String[] parts = line.trim().split("\\s+");
                String interfaceName = parts[0].replace(":", "");
                long txBytes = Long.parseLong(parts[9]);
                
                Metrics.counter("system.network.tx.bytes")
                        .tag("host", getHostname())
                        .tag("interface", interfaceName)
                        .increment(txBytes, Metrics.globalRegistry);
            }
        }
        
        reader.close();
        process.waitFor();
    } catch (Exception e) {
        log.error("Failed to collect network TX bytes", e);
    }
}
```

**告警阈值**：

```yaml
network_tx_alerts:
  warning:
    threshold: 104857600
    duration: "5m"
    message: "网络出站流量超过100MB/s"
  
  critical:
    threshold: 209715200
    duration: "2m"
    message: "网络出站流量超过200MB/s"
```

### 网络连接指标

#### 1. TCP连接数

**指标名称**：`system.network.tcp.connections`

**指标描述**：TCP连接数

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectTcpConnections() {
    try {
        Process process = Runtime.getRuntime().exec("ss -s");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("TCP:")) {
                String[] parts = line.trim().split("\\s+");
                int totalConnections = Integer.parseInt(parts[1]);
                int establishedConnections = Integer.parseInt(parts[3]);
                int timeWaitConnections = Integer.parseInt(parts[4]);
                
                Metrics.gauge("system.network.tcp.connections", totalConnections)
                        .tag("host", getHostname())
                        .tag("state", "total")
                        .register(Metrics.globalRegistry);
                
                Metrics.gauge("system.network.tcp.connections", establishedConnections)
                        .tag("host", getHostname())
                        .tag("state", "established")
                        .register(Metrics.globalRegistry);
                
                Metrics.gauge("system.network.tcp.connections", timeWaitConnections)
                        .tag("host", getHostname())
                        .tag("state", "time_wait")
                        .register(Metrics.globalRegistry);
            }
        }
        
        reader.close();
        process.waitFor();
    } catch (Exception e) {
        log.error("Failed to collect TCP connections", e);
    }
}
```

**告警阈值**：

```yaml
tcp_connections_alerts:
  warning:
    threshold: 10000
    duration: "5m"
    message: "TCP连接数超过10000"
  
  critical:
    threshold: 20000
    duration: "2m"
    message: "TCP连接数超过20000"
```

#### 2. 网络错误数

**指标名称**：`system.network.errors`

**指标描述**：网络错误数

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectNetworkErrors() {
    try {
        Process process = Runtime.getRuntime().exec("cat /proc/net/dev");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("Inter-") && !line.startsWith("face")) {
                String[] parts = line.trim().split("\\s+");
                String interfaceName = parts[0].replace(":", "");
                long rxErrors = Long.parseLong(parts[2]);
                long txErrors = Long.parseLong(parts[10]);
                long totalErrors = rxErrors + txErrors;
                
                Metrics.counter("system.network.errors")
                        .tag("host", getHostname())
                        .tag("interface", interfaceName)
                        .tag("direction", "rx")
                        .increment(rxErrors, Metrics.globalRegistry);
                
                Metrics.counter("system.network.errors")
                        .tag("host", getHostname())
                        .tag("interface", interfaceName)
                        .tag("direction", "tx")
                        .increment(txErrors, Metrics.globalRegistry);
            }
        }
        
        reader.close();
        process.waitFor();
    } catch (Exception e) {
        log.error("Failed to collect network errors", e);
    }
}
```

**告警阈值**：

```yaml
network_errors_alerts:
  warning:
    threshold: 100
    duration: "5m"
    message: "网络错误数超过100/分钟"
  
  critical:
    threshold: 500
    duration: "2m"
    message: "网络错误数超过500/分钟"
```

## 系统负载监控指标

### 系统负载指标

#### 1. 1分钟平均负载

**指标名称**：`system.load.1m`

**指标描述**：系统1分钟平均负载

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectLoad1m() {
    double[] loadAverage = osBean.getSystemLoadAverage();
    
    if (loadAverage != null && loadAverage.length >= 1) {
        Metrics.gauge("system.load.1m", loadAverage[0])
                .tag("host", getHostname())
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
load_1m_alerts:
  warning:
    threshold: 4
    duration: "5m"
    message: "1分钟平均负载超过4"
  
  critical:
    threshold: 8
    duration: "2m"
    message: "1分钟平均负载超过8"
```

#### 2. 5分钟平均负载

**指标名称**：`system.load.5m`

**指标描述**：系统5分钟平均负载

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectLoad5m() {
    double[] loadAverage = osBean.getSystemLoadAverage();
    
    if (loadAverage != null && loadAverage.length >= 2) {
        Metrics.gauge("system.load.5m", loadAverage[1])
                .tag("host", getHostname())
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
load_5m_alerts:
  warning:
    threshold: 4
    duration: "5m"
    message: "5分钟平均负载超过4"
  
  critical:
    threshold: 8
    duration: "2m"
    message: "5分钟平均负载超过8"
```

#### 3. 15分钟平均负载

**指标名称**：`system.load.15m`

**指标描述**：系统15分钟平均负载

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectLoad15m() {
    double[] loadAverage = osBean.getSystemLoadAverage();
    
    if (loadAverage != null && loadAverage.length >= 3) {
        Metrics.gauge("system.load.15m", loadAverage[2])
                .tag("host", getHostname())
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
load_15m_alerts:
  warning:
    threshold: 4
    duration: "5m"
    message: "15分钟平均负载超过4"
  
  critical:
    threshold: 8
    duration: "2m"
    message: "15分钟平均负载超过8"
```

## 进程监控指标

### 进程状态指标

#### 1. 进程数

**指标名称**：`system.process.count`

**指标描述**：系统进程总数

**采集方法**：

```java
@Component
public class ProcessMetricsCollector {

    @Scheduled(fixedRate = 5000)
    public void collectProcessCount() {
        try {
            Process process = Runtime.getRuntime().exec("ps aux | wc -l");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            
            String line = reader.readLine();
            int processCount = Integer.parseInt(line.trim());
            
            Metrics.gauge("system.process.count", processCount)
                    .tag("host", getHostname())
                    .register(Metrics.globalRegistry);
            
            reader.close();
            process.waitFor();
        } catch (Exception e) {
            log.error("Failed to collect process count", e);
        }
    }
}
```

**告警阈值**：

```yaml
process_count_alerts:
  warning:
    threshold: 1000
    duration: "5m"
    message: "进程数超过1000"
  
  critical:
    threshold: 2000
    duration: "2m"
    message: "进程数超过2000"
```

#### 2. 僵尸进程数

**指标名称**：`system.process.zombie`

**指标描述**：僵尸进程数

**采集方法**：

```java
@Scheduled(fixedRate = 5000)
public void collectZombieProcesses() {
    try {
        Process process = Runtime.getRuntime().exec("ps aux | grep Z | wc -l");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        
        String line = reader.readLine();
        int zombieCount = Integer.parseInt(line.trim());
        
        Metrics.gauge("system.process.zombie", zombieCount)
                .tag("host", getHostname())
                .register(Metrics.globalRegistry);
        
        reader.close();
        process.waitFor();
    } catch (Exception e) {
        log.error("Failed to collect zombie processes", e);
    }
}
```

**告警阈值**：

```yaml
zombie_process_alerts:
  warning:
    threshold: 10
    duration: "5m"
    message: "僵尸进程数超过10"
  
  critical:
    threshold: 50
    duration: "2m"
    message: "僵尸进程数超过50"
```

## 告警阈值配置

### 告警阈值配置文件

```yaml
system_alerts:
  cpu:
    usage:
      warning:
        threshold: 70
        duration: "5m"
        message: "CPU使用率超过70%"
      critical:
        threshold: 90
        duration: "2m"
        message: "CPU使用率超过90%"
    user:
      warning:
        threshold: 60
        duration: "5m"
        message: "CPU用户态使用率超过60%"
      critical:
        threshold: 80
        duration: "2m"
        message: "CPU用户态使用率超过80%"
    system:
      warning:
        threshold: 30
        duration: "5m"
        message: "CPU内核态使用率超过30%"
      critical:
        threshold: 50
        duration: "2m"
        message: "CPU内核态使用率超过50%"
    iowait:
      warning:
        threshold: 20
        duration: "5m"
        message: "CPU IO等待时间超过20%"
      critical:
        threshold: 40
        duration: "2m"
        message: "CPU IO等待时间超过40%"
  
  memory:
    usage:
      warning:
        threshold: 80
        duration: "5m"
        message: "内存使用率超过80%"
      critical:
        threshold: 90
        duration: "2m"
        message: "内存使用率超过90%"
    swap:
      warning:
        threshold: 50
        duration: "5m"
        message: "Swap使用率超过50%"
      critical:
        threshold: 80
        duration: "2m"
        message: "Swap使用率超过80%"
    cache:
      warning:
        threshold: 70
        duration: "5m"
        message: "缓存使用率超过70%"
      critical:
        threshold: 90
        duration: "2m"
        message: "缓存使用率超过90%"
  
  disk:
    usage:
      warning:
        threshold: 80
        duration: "5m"
        message: "磁盘使用率超过80%"
      critical:
        threshold: 90
        duration: "2m"
        message: "磁盘使用率超过90%"
    inode:
      warning:
        threshold: 80
        duration: "5m"
        message: "Inode使用率超过80%"
      critical:
        threshold: 90
        duration: "2m"
        message: "Inode使用率超过90%"
    io:
      warning:
        threshold: 70
        duration: "5m"
        message: "磁盘I/O使用率超过70%"
      critical:
        threshold: 90
        duration: "2m"
        message: "磁盘I/O使用率超过90%"
    iops:
      warning:
        threshold: 1000
        duration: "5m"
        message: "磁盘IOPS超过1000"
      critical:
        threshold: 2000
        duration: "2m"
        message: "磁盘IOPS超过2000"
    throughput:
      warning:
        threshold: 102400
        duration: "5m"
        message: "磁盘吞吐量超过100MB/s"
      critical:
        threshold: 204800
        duration: "2m"
        message: "磁盘吞吐量超过200MB/s"
  
  network:
    rx:
      warning:
        threshold: 104857600
        duration: "5m"
        message: "网络入站流量超过100MB/s"
      critical:
        threshold: 209715200
        duration: "2m"
        message: "网络入站流量超过200MB/s"
    tx:
      warning:
        threshold: 104857600
        duration: "5m"
        message: "网络出站流量超过100MB/s"
      critical:
        threshold: 209715200
        duration: "2m"
        message: "网络出站流量超过200MB/s"
    tcp:
      warning:
        threshold: 10000
        duration: "5m"
        message: "TCP连接数超过10000"
      critical:
        threshold: 20000
        duration: "2m"
        message: "TCP连接数超过20000"
    errors:
      warning:
        threshold: 100
        duration: "5m"
        message: "网络错误数超过100/分钟"
      critical:
        threshold: 500
        duration: "2m"
        message: "网络错误数超过500/分钟"
  
  load:
    m1:
      warning:
        threshold: 4
        duration: "5m"
        message: "1分钟平均负载超过4"
      critical:
        threshold: 8
        duration: "2m"
        message: "1分钟平均负载超过8"
    m5:
      warning:
        threshold: 4
        duration: "5m"
        message: "5分钟平均负载超过4"
      critical:
        threshold: 8
        duration: "2m"
        message: "5分钟平均负载超过8"
    m15:
      warning:
        threshold: 4
        duration: "5m"
        message: "15分钟平均负载超过4"
      critical:
        threshold: 8
        duration: "2m"
        message: "15分钟平均负载超过8"
  
  process:
    count:
      warning:
        threshold: 1000
        duration: "5m"
        message: "进程数超过1000"
      critical:
        threshold: 2000
        duration: "2m"
        message: "进程数超过2000"
    zombie:
      warning:
        threshold: 10
        duration: "5m"
        message: "僵尸进程数超过10"
      critical:
        threshold: 50
        duration: "2m"
        message: "僵尸进程数超过50"
```

## 相关文档

- [应用级监控指标文档](ApplicationMetricsGuide.md)
- [业务级监控指标文档](BusinessMetricsGuide.md)
- [数据库监控指标文档](DatabaseMetricsGuide.md)
- [缓存监控指标文档](CacheMetricsGuide.md)

## 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|---------|
| 1.0.0 | 2026-01-19 | System | 初始版本 |