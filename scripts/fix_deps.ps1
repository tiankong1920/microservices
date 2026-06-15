$filePath = "e:\microservices\project-root\build.gradle.kts"
$content = Get-Content $filePath -Raw

$old = "        preferProjectModules()
    }"

$new = @"        preferProjectModules()
        // 强制覆盖Spring BOM中的旧版本依赖
        force(
            "org.apache.kafka:kafka-clients:3.9.1",
            "org.lz4:lz4-java:1.8.1",
            "org.mozilla:rhino:1.7.14.1",
            "io.netty:netty-codec-http:4.1.129.Final",
            "io.netty:netty-codec-http2:4.1.129.Final",
            "io.netty:netty-common:4.1.129.Final",
            "io.netty:netty-buffer:4.1.129.Final",
            "io.netty:netty-transport:4.1.129.Final",
            "io.netty:netty-handler:4.1.129.Final",
            "io.netty:netty-resolver:4.1.129.Final",
            "io.netty:netty-codec:4.1.129.Final"
        )
    }"@

$content = $content -replace [regex]::Escape($old), $new
$content | Set-Content $filePath -NoNewline

Write-Host "Done"