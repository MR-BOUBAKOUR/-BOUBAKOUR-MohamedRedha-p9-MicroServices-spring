import os
from docling.document_converter import DocumentConverter

source = "input/guidelines_diabetes_type2.pdf"

print("⏳ Début de la conversion du PDF...")
converter = DocumentConverter()
result = converter.convert(source)

markdown = result.document.export_to_markdown()

os.makedirs("output", exist_ok=True)
print("📝 Écriture du fichier markdown dans output/guidelines_diabetes_type2_result_initial.md...")
with open("output/guidelines_diabetes_type2_result_initial.md", "w", encoding="utf-8") as f:
    f.write(markdown)

print("✅ Extraction terminée")
