import os
import glob

java_files = glob.glob("backend/src/main/java/**/*.java", recursive=True)

# We need to add `import com.pharmadesk.backend.model.*;` to all files in sales/model/
# And fix `import com.pharmadesk.backend.pharmacy.repository.*;` to include `import com.pharmadesk.backend.sales.repository.*;` in services

for file in java_files:
    with open(file, 'r') as f:
        content = f.read()
    
    new_content = content
    
    # If file is in sales/model, add imports for BaseEntity, MedicineStock, User, etc.
    if "sales/model" in file:
        if "import com.pharmadesk.backend.model.*" not in new_content:
            new_content = new_content.replace("package com.pharmadesk.backend.sales.model;", 
                                              "package com.pharmadesk.backend.sales.model;\n\nimport com.pharmadesk.backend.model.*;")
                                              
    # Some files might be importing specific things from com.pharmadesk.backend.model that were moved.
    # The previous script replaced them. 
    
    # DashboardService, AnalyticsService etc might use wildcard imports or need the new repository package
    if "import com.pharmadesk.backend.pharmacy.repository.*" in new_content:
        if "import com.pharmadesk.backend.sales.repository.*" not in new_content:
            new_content = new_content.replace("import com.pharmadesk.backend.pharmacy.repository.*;",
                                              "import com.pharmadesk.backend.pharmacy.repository.*;\nimport com.pharmadesk.backend.sales.repository.*;")

    # Replace specific imports for the moved entities that were missed (like if they were wildcard imported before)
    # Actually, any file using PharmacyBill etc needs import com.pharmadesk.backend.sales.model.*;
    if "import com.pharmadesk.backend.model.*" in new_content and "sales/model" not in file:
        if "import com.pharmadesk.backend.sales.model.*" not in new_content:
            new_content = new_content.replace("import com.pharmadesk.backend.model.*;",
                                              "import com.pharmadesk.backend.model.*;\nimport com.pharmadesk.backend.sales.model.*;")

    # Specifically for SaleReturnController and other controllers/services:
    if "package com.pharmadesk.backend.sales.controller;" in new_content:
        if "import com.pharmadesk.backend.sales.model.*;" not in new_content:
             new_content = new_content.replace("package com.pharmadesk.backend.sales.controller;", 
                                              "package com.pharmadesk.backend.sales.controller;\n\nimport com.pharmadesk.backend.model.*;\nimport com.pharmadesk.backend.sales.model.*;")

    if "package com.pharmadesk.backend.sales.service;" in new_content:
        if "import com.pharmadesk.backend.sales.model.*;" not in new_content:
             new_content = new_content.replace("package com.pharmadesk.backend.sales.service;", 
                                              "package com.pharmadesk.backend.sales.service;\n\nimport com.pharmadesk.backend.model.*;\nimport com.pharmadesk.backend.sales.model.*;\nimport com.pharmadesk.backend.sales.repository.*;")

    if new_content != content:
        with open(file, 'w') as f:
            f.write(new_content)

print("Import fix complete.")
