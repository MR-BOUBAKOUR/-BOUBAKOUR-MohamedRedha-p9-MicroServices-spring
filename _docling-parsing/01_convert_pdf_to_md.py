import os
from docling.document_converter import DocumentConverter

source = "input/guidelines_diabetes_type2.pdf"
output_md_file = "output/01_guidelines_result_initial.md"

print("⏳ Début de la conversion du PDF...")
converter = DocumentConverter()
result = converter.convert(source)

markdown = result.document.export_to_markdown()

os.makedirs("output", exist_ok=True)
print("📝 Écriture du fichier markdown dans output/01_guidelines_result_initial.md...")
with open(output_md_file, "w", encoding="utf-8") as f:
    f.write(markdown)

print("✅ Extraction terminée")
