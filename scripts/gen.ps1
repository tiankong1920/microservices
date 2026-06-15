$p = 'E:\microservices\project-root\core-services\order-service\src\test\java\com\inventory\orderservice\service\impl\OrderServiceImplTest.java'
$d = [System.IO.Path]::GetDirectoryName($p)
if (!(Test-Path $d)) { New-Item -ItemType Directory -Path $d -Force | Out-Null }