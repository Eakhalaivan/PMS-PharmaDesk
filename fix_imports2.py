import os
import glob

java_files = glob.glob("backend/src/main/java/**/*.java", recursive=True)

for file in java_files:
    with open(file, 'r') as f:
        content = f.read()
    
    new_content = content
    
    # Fix InsuranceClaim.java missing import
    if "InsuranceClaim.java" in file:
        if "import com.pharmadesk.backend.sales.model.*;" not in new_content:
            new_content = new_content.replace("package com.pharmadesk.backend.model;",
                                              "package com.pharmadesk.backend.model;\n\nimport com.pharmadesk.backend.sales.model.*;")
                                              
    # Fix SaleReturnController explicit fully qualified names
    if "SaleReturnController.java" in file:
        new_content = new_content.replace("com.pharmadesk.backend.model.MedicineReturn", "com.pharmadesk.backend.sales.model.MedicineReturn")

    if new_content != content:
        with open(file, 'w') as f:
            f.write(new_content)
