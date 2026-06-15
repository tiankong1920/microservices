$content = Get-Content 'e:\microservices\project-root\core-services\order-service\src\test\java\com\inventory\orderservice\service\impl\OrderServiceImplTest.java' -Raw
$content = $content -replace '\\"', '"'
[System.IO.File]::WriteAllText('e:\microservices\project-root\core-services\order-service\src\test\java\com\inventory\orderservice\service\impl\OrderServiceImplTest.java', $content)