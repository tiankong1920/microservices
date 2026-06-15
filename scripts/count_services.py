import os

base = r'E:\101\microservices\project-root\support-services'
services = ['admin-service', 'auth-service', 'config-service', 'finance-service', 
            'gateway-service', 'registry-service', 'report-service']

for svc in services:
    java_path = os.path.join(base, svc, 'src', 'main', 'java')
    if os.path.exists(java_path):
        files = []
        for root, dirs, fnames in os.walk(java_path):
            for f in fnames:
                if f.endswith('.java'):
                    files.append(os.path.join(root, f))
        total_lines = 0
        for f in files:
            with open(f, 'r', encoding='utf-8', errors='ignore') as fh:
                total_lines += len(fh.readlines())
        print(f'{svc}: {len(files)} files, {total_lines} lines')
    else:
        print(f'{svc}: NO JAVA SOURCE')
