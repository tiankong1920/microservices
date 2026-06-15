import os
path = r'microservices\\project-root\\core-services\\order-service\\src\\test\\java\\com\\inventory\\orderservice\\service\\impl\\OrderServiceImplTest.java'
os.makedirs(os.path.dirname(path), exist_ok=True)
content = open('gen_content.txt', 'r', encoding='utf-8').read()
open(path, 'w', encoding='utf-8').write(content)
print('done')
