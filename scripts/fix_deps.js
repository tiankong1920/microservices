const fs = require('fs');
const path = 'e:/101/microservices/project-root/build.gradle.kts';
let c = fs.readFileSync(path, 'utf8');

const oldCode = `preferProjectModules()
    }`;

const newCode = `preferProjectModules()
        // 强制覆盖Spring BOM中的传递依赖版本
        eachDependency {
            when (requested.module.toString()) {
                "org.apache.kafka:kafka-clients" -> useVersion("3.9.1")
                "org.lz4:lz4-java" -> useVersion("1.8.1")
                "org.mozilla:rhino" -> useVersion("1.7.14.1")
            }
            if (requested.module.toString().startsWith("io.netty:")) {
                useVersion("4.1.129.Final")
            }
        }
    }`;

c = c.replace(oldCode, newCode);
fs.writeFileSync(path, c);
console.log('Done');