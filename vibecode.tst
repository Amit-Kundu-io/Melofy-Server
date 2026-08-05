VIBECODE PROJECT INSTRUCTIONS
Last updated: 2026-08-06 01:51:01 +05:30 (Asia/Kolkata)

This file is the quick instruction reference for future code changes.

1. Add a short KDoc comment above every named Kotlin function describing its purpose.
2. Keep one responsibility per file where practical: request, response, service,
   repository, route, and database mapping should remain easy to find.
3. Use the standard ApiResponse envelope for every API success and error response.
4. Record each material change, with its date, in vibecoding.txt.
5. Do not edit an applied Flyway migration. Add a new migration version instead.
6. Compile the project with .\gradlew.bat compileKotlin before finishing work.
