import os
import glob

# Files moved
models = ["PharmacyBill", "PharmacyBillItem", "CreditBill", "PaymentTransaction", "MedicineReturn", "MedicineReturnItem"]
repos = ["PharmacyBillRepository", "PharmacyBillItemRepository", "CreditBillRepository", "PaymentTransactionRepository", "MedicineReturnRepository"]
services = ["SaleService", "PaymentService", "ReturnService"]
controllers = ["SaleController", "SaleReturnController", "CreditBillController", "BillingController"]
dtos = ["SaleRequestDTO", "SaleItemDTO"]

# Package mappings (old -> new)
package_map = {}
for m in models:
    package_map[f"com.pharmadesk.backend.model.{m}"] = f"com.pharmadesk.backend.sales.model.{m}"
for r in repos:
    package_map[f"com.pharmadesk.backend.pharmacy.repository.{r}"] = f"com.pharmadesk.backend.sales.repository.{r}"
for s in services:
    package_map[f"com.pharmadesk.backend.pharmacy.service.{s}"] = f"com.pharmadesk.backend.sales.service.{s}"
for c in controllers:
    if c == "BillingController":
        package_map[f"com.pharmadesk.backend.controller.{c}"] = f"com.pharmadesk.backend.sales.controller.{c}"
    else:
        package_map[f"com.pharmadesk.backend.pharmacy.controller.{c}"] = f"com.pharmadesk.backend.sales.controller.{c}"
for d in dtos:
    package_map[f"com.pharmadesk.backend.pharmacy.dto.{d}"] = f"com.pharmadesk.backend.sales.dto.{d}"

java_files = glob.glob("backend/src/main/java/**/*.java", recursive=True)

for file in java_files:
    with open(file, 'r') as f:
        content = f.read()
    
    new_content = content
    
    # Check if this file is one of the moved files, and update its package declaration
    filename = os.path.basename(file)
    classname = filename.replace(".java", "")
    
    if classname in models:
        new_content = new_content.replace("package com.pharmadesk.backend.model;", "package com.pharmadesk.backend.sales.model;")
    elif classname in repos:
        new_content = new_content.replace("package com.pharmadesk.backend.pharmacy.repository;", "package com.pharmadesk.backend.sales.repository;")
    elif classname in services:
        new_content = new_content.replace("package com.pharmadesk.backend.pharmacy.service;", "package com.pharmadesk.backend.sales.service;")
    elif classname in controllers:
        new_content = new_content.replace("package com.pharmadesk.backend.pharmacy.controller;", "package com.pharmadesk.backend.sales.controller;")
        new_content = new_content.replace("package com.pharmadesk.backend.controller;", "package com.pharmadesk.backend.sales.controller;")
    elif classname in dtos:
        new_content = new_content.replace("package com.pharmadesk.backend.pharmacy.dto;", "package com.pharmadesk.backend.sales.dto;")
        
    # Update imports in the file
    for old_import, new_import in package_map.items():
        new_content = new_content.replace(f"import {old_import};", f"import {new_import};")
        
    if new_content != content:
        with open(file, 'w') as f:
            f.write(new_content)

print("Refactoring complete.")
