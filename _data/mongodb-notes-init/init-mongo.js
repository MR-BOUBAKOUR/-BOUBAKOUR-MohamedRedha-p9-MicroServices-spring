db = db.getSiblingDB('medilabosolutions');
db.notes.insertMany(require('/docker-entrypoint-initdb.d/notes.json'));