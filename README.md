# Database Migrator

Reads all data from the existing SQL database and writes key information into a NoSQL GoogleFireStore database.

Run daily and existing records in the NoSQL database are updates with any changes. Additional data / attributes added to
the NoSQL databse are persisted during a migration.
